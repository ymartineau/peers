/*
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * deployJava.js
 *
 * This file is part of the Deployment Toolkit.  It provides functions for web
 * pages to detect the presence of a JRE, install the latest JRE, and easily run
 * applets or Web Start programs.  More Information on usage of the 
 * Deployment Toolkit can be found in the Deployment Guide at:
 * http://java.sun.com/javase/6/docs/technotes/guides/jweb/index.html
 * 
 * The "live" copy of this file may be found at :
 * http://java.com/js/deployJava.js.  
 * For web pages provisioned using https, you may want to access the copy at:
 * https://java.com/js/deployJava.js.
 *
 * You are encouraged to link directly to the live copies. 
 * The above files are stripped of comments and whitespace for performance,
 * You can access this file w/o the whitespace and comments removed at:
 * http://java.com/js/deployJava.txt.
 *
 */

var deployJava = {
    debug: null,

    firefoxJavaVersion: null,

    myInterval: null,
    preInstallJREList: null,
    returnPage: null,
    brand: null,
    locale: null,
    installType: null,
    
    EAInstallEnabled: false,
    EarlyAccessURL: null,
    
    // GetJava page
    getJavaURL: 'http://java.sun.com/webapps/getjava/BrowserRedirect?host=java.com',
    
    // Apple redirect page
    appleRedirectPage: 'http://www.apple.com/support/downloads/',

    // mime-type of the DeployToolkit plugin object
    oldMimeType: 'application/npruntime-scriptable-plugin;DeploymentToolkit',
    mimeType: 'application/java-deployment-toolkit',

    // location of the Java Web Start launch button graphic
    launchButtonPNG: 'http://java.sun.com/products/jfc/tsc/articles/swing2d/webstart.png',

    browserName: null,
    browserName2: null,

    /**
     * Returns an array of currently-installed JRE version strings.  
     * Version strings are of the form #.#[.#[_#]], with the function returning
     * as much version information as it can determine, from just family 
     * versions ("1.4.2", "1.5") through the full version ("1.5.0_06").
     *
     * Detection is done on a best-effort basis.  Under some circumstances 
     * only the highest installed JRE version will be detected, and 
     * JREs older than 1.4.2 will not always be detected.
     */
    getJREs: function() {
        var list = new Array();
        if (deployJava.isPluginInstalled()) {
            var plugin =  deployJava.getPlugin();
            var VMs = plugin.jvms;
            for (var i = 0; i < VMs.getLength(); i++) {
                list[i] = VMs.get(i).version;
            }
        } else {
            var browser = deployJava.getBrowser();
        
            if (browser == 'MSIE') {
                if (deployJava.testUsingActiveX('1.7.0')) {
                    list[0] = '1.7.0';
                } else if (deployJava.testUsingActiveX('1.6.0')) {
                    list[0] = '1.6.0';
                } else if (deployJava.testUsingActiveX('1.5.0')) {
                    list[0] = '1.5.0';
                } else if (deployJava.testUsingActiveX('1.4.2')) {
                    list[0] = '1.4.2';
                } else if (deployJava.testForMSVM()) {
                    list[0] = '1.1';
                }
            } else if (browser == 'Netscape Family') {
                deployJava.getJPIVersionUsingMimeType();
                if (deployJava.firefoxJavaVersion != null) {
                    list[0] = deployJava.firefoxJavaVersion;
                } else if (deployJava.testUsingMimeTypes('1.7')) {
                    list[0] = '1.7.0'; 
                } else if (deployJava.testUsingMimeTypes('1.6')) {
                    list[0] = '1.6.0';
                } else if (deployJava.testUsingMimeTypes('1.5')) {
                    list[0] = '1.5.0';
                } else if (deployJava.testUsingMimeTypes('1.4.2')) {
                    list[0] = '1.4.2';
                } else if (deployJava.browserName2 == 'Safari') {
                    if (deployJava.testUsingPluginsArray('1.7.0')) {
                        list[0] = '1.7.0'; 
                    } else if (deployJava.testUsingPluginsArray('1.6')) {
                        list[0] = '1.6.0';
                    } else if (deployJava.testUsingPluginsArray('1.5')) {
                        list[0] = '1.5.0';
                    } else if (deployJava.testUsingPluginsArray('1.4.2')) {
                        list[0] = '1.4.2';
                    }
                }
            }
        }
            
        if (deployJava.debug) {
            for (var i = 0; i < list.length; ++i) {
                alert('We claim to have detected Java SE ' + list[i]);
            }
        }
    
        return list;
    },
    
    /**
     * Triggers a JRE installation.  The exact effect of triggering an 
     * installation varies based on platform, browser, and if the 
     * Deployment Toolkit plugin is installed.
     *
     * The requestVersion string is of the form #[.#[.#[_#]]][+|*], 
     * which includes strings such as "1.4", "1.5.0*", and "1.6.0_02+".  
     * A star (*) means "any version starting within this family" and 
     * a plus (+) means "any version greater or equal to this".  
     * "1.5.0*" * matches 1.5.0_06 but not 1.6.0_01, whereas 
     * "1.5.0+" matches both.
     *
     * If the Deployment Toolkit plugin is not present, this will just call 
     * deployJava.installLatestJRE(). 
     */
    installJRE: function(requestVersion) {
        var ret = false;
        if (deployJava.isPluginInstalled()) {
            if (deployJava.getPlugin().installJRE(requestVersion)) {
                deployJava.refresh();
                if (deployJava.returnPage != null) {
                    document.location = deployJava.returnPage;
                }
                return true;
            } else {
                return false;
            }
        } else {
            return deployJava.installLatestJRE();
        }
    },


    /**
     * Triggers a JRE installation.  The exact effect of triggering an 
     * installation varies based on platform, browser, and if the 
     * Deployment Toolkit plugin is installed.
     *
     * In the simplest case, the browser window will be redirected to the 
     * java.com JRE installation page, and (if possible) a redirect back to 
     * the current URL upon successful installation.  The return redirect is 
     * not always possible, as the JRE installation may require the browser to 
     * be restarted.
     *
     * In the best case (when the Deployment Toolkit plugin is present), this
     * function will immediately cause a progress dialog to be displayed 
     * as the JRE is downloaded and installed.
     */
    installLatestJRE: function() {
        if (deployJava.isPluginInstalled()) {
            if (deployJava.getPlugin().installLatestJRE()) {
                deployJava.refresh();
                if (deployJava.returnPage != null) {
                    document.location = deployJava.returnPage;
                }
                return true;
            } else {
                return false;
            }
        } else {
            var browser = deployJava.getBrowser();
            var platform = navigator.platform.toLowerCase();
            if ((deployJava.EAInstallEnabled == 'true') && 
                (platform.indexOf('win') != -1) && 
                (deployJava.EarlyAccessURL != null)) {

                deployJava.preInstallJREList = deployJava.getJREs();
                if (deployJava.returnPage != null) {
                    deployJava.myInterval = 
                        setInterval("deployJava.poll()", 3000);
                }

                location.href = deployJava.EarlyAccessURL;

                // we have to return false although there may be an install
                // in progress now, when complete it may go to return page
                return false;
            } else {
                if (browser == 'MSIE') {
                    return deployJava.IEInstall();
                } else if ((browser == 'Netscape Family') && 
                           (platform.indexOf('win32') != -1)) {
                    return deployJava.FFInstall();
                } else {
                    location.href = deployJava.getJavaURL + 
                        ((deployJava.returnPage != null) ?
                        ('&returnPage=' + deployJava.returnPage) : '') + 
                        ((deployJava.locale != null) ?
                        ('&locale=' + deployJava.locale) : '') +
                        ((deployJava.brand != null) ? 
                         ('&brand=' + deployJava.brand) : '');
                }
                // we have to return false although there may be an install
                // in progress now, when complete it may go to return page
                return false;
            }
        }
    },


    /**
     * Ensures that an appropriate JRE is installed and then runs an applet.
     * minimumVersion is of the form #[.#[.#[_#]]], and is the minimum 
     * JRE version necessary to run this applet.  minimumVersion is optional, 
     * defaulting to the value "1.1" (which matches any JRE).  
     * If an equal or greater JRE is detected, runApplet() will call 
     * writeAppletTag(attributes, parameters) to output the applet tag, 
     * otherwise it will call installJRE(minimumVersion + '+').
     *
     * After installJRE() is called, the script will attempt to detect that the 
     * JRE installation has completed and begin running the applet, but there
     * are circumstances (such as when the JRE installation requires a browser
     * restart) when this cannot be fulfilled.
     *
     * As with writeAppletTag(), this function should only be called prior to 
     * the web page being completely rendered.  Note that version wildcards 
     * (star (*) and plus (+)) are not supported, and including them in the 
     * minimumVersion will result in an error message.
     */
    runApplet: function(attributes, parameters, minimumVersion) {
        if (minimumVersion == 'undefined' || minimumVersion == null) {
            minimumVersion = '1.1';
        }

        var regex = "^(\\d+)(?:\\.(\\d+)(?:\\.(\\d+)(?:_(\\d+))?)?)?$";

        var matchData = minimumVersion.match(regex);

        if (deployJava.returnPage == null) {
            // if there is an install, come back here and run the applet
            deployJava.returnPage = document.location;
        }

        if (matchData != null) {
            var browser = deployJava.getBrowser();
            if ((browser != '?') && ('Safari' != deployJava.browserName2)) {
                if (deployJava.versionCheck(minimumVersion + '+')) {
                    deployJava.writeAppletTag(attributes, parameters);
                } else if (deployJava.installJRE(minimumVersion + '+')) {
                    // after successfull install we need to refresh page to pick
                    // pick up new plugin
                    deployJava.refresh();
                    location.href = document.location;
                    deployJava.writeAppletTag(attributes, parameters);
                }
            } else {
                // for unknown or Safari - just try to show applet
                deployJava.writeAppletTag(attributes, parameters);
            }
        } else {
            if (deployJava.debug) {
                alert('Invalid minimumVersion argument to runApplet():' + 
                      minimumVersion);
            }
        }
    },

    
    /**
     * Outputs an applet tag with the specified attributes and parameters, where
     * both attributes and parameters are associative arrays.  Each key/value 
     * pair in attributes becomes an attribute of the applet tag itself, while
     * key/value pairs in parameters become <PARAM> tags.  No version checking 
     * or other special behaviors are performed; the tag is simply written to 
     * the page using document.writeln().
     *
     * As document.writeln() is generally only safe to use while the page is 
     * being rendered, you should never call this function after the page 
     * has been completed.
     */
    writeAppletTag: function(attributes, parameters) {
        var startApplet = '<' + 'applet ';
        var params = '';
        var endApplet = '<' + '/' + 'applet' + '>';
        var addCodeAttribute = true;

        for (var attribute in attributes) {
            startApplet += (' ' +attribute+ '="' +attributes[attribute] + '"');
            if (attribute == 'code' || attribute == 'java_code') {
                addCodeAttribute = false;
            }
        }
    
        if (parameters != 'undefined' && parameters != null) {
            var codebaseParam = false;
            for (var parameter in parameters) {
                if (parameter == 'codebase_lookup') {
                    codebaseParam = true;
                }
                // Originally, parameter 'object' was used for serialized 
                // applets, later, to avoid confusion with object tag in IE
                // the 'java_object' was added.  Plugin supports both.
                if (parameter == 'object' || parameter == 'java_object') {
                    addCodeAttribute = false;
                }
                params += '<param name="' + parameter + '" value="' + 
                    parameters[parameter] + '"/>';
            }
            if (!codebaseParam) {
                params += '<param name="codebase_lookup" value="false"/>';
            }
        }

        if (addCodeAttribute) {
            startApplet += (' code="dummy"');
        }
        startApplet += '>';

        document.write(startApplet + '\n' + params + '\n' + endApplet);
    },
    
    
     /**
      * Returns true if there is a matching JRE version currently installed 
      * (among those detected by getJREs()).  The versionPattern string is 
      * of the form #[.#[.#[_#]]][+|*], which includes strings such as "1.4", 
      * "1.5.0*", and "1.6.0_02+".  
      * A star (*) means "any version within this family" and a plus (+) means 
      * "any version greater or equal to the specified version".  "1.5.0*"
      * matches 1.5.0_06 but not 1.6.0_01, whereas "1.5.0+" matches both.
      *
      * If the versionPattern does not include all four version components 
      * but does not end with a star or plus, it will be treated as if it 
      * ended with a star.  "1.5" is exactly equivalent to "1.5*", and will 
      * match any version number beginning with "1.5".
      *
      * If getJREs() is unable to detect the precise version number, a match 
      * could be ambiguous.  For example if getJREs() detects "1.5", there is 
      * no way to know whether the JRE matches "1.5.0_06+".  versionCheck() 
      * compares only as much of the version information as could be detected, 
      * so versionCheck("1.5.0_06+") would return true in in this case.
      *
      * Invalid versionPattern will result in a JavaScript error alert.  
      * versionPatterns which are valid but do not match any existing JRE 
      * release (e.g. "32.65+") will always return false.
      */
    versionCheck: function(versionPattern)
    {
        var index = 0;
        var regex = "^(\\d+)(?:\\.(\\d+)(?:\\.(\\d+)(?:_(\\d+))?)?)?(\\*|\\+)?$";

        var matchData = versionPattern.match(regex);

        if (matchData != null) {
            var familyMatch = true;

            var patternArray = new Array();

            for (var i = 1; i < matchData.length; ++i) {
                // browser dependency here.
                // Fx sets 'undefined', IE sets '' string for unmatched groups
                if ((typeof matchData[i] == 'string') && (matchData[i] != '')) {
                    patternArray[index] = matchData[i];
                    index++;
                }
            }

            if (patternArray[patternArray.length-1] == '+') {
                familyMatch = false;
                patternArray.length--;
            } else {
                if (patternArray[patternArray.length-1] == '*') {
                    patternArray.length--;
                }
            }

            var list = deployJava.getJREs();       
            for (var i = 0; i < list.length; ++i) {
                if (deployJava.compareVersionToPattern(list[i], patternArray, 
                                                       familyMatch)) {
                    return true;
                }
            }
  
            return false;
        } else {
            alert('Invalid versionPattern passed to versionCheck: ' + 
                  versionPattern);
            return false;
        }
    },


    /**
     * Returns true if an installation of Java Web Start of the specified 
     * minimumVersion can be detected.  minimumVersion is optional, and 
     * if not specified, '1.4.2' will be used. 
     * (Versions earlier than 1.4.2 may not be detected.)
     */
    isWebStartInstalled: function(minimumVersion) {

        var browser = deployJava.getBrowser();
        if ((browser == '?') || ('Safari' == deployJava.browserName2)) {
            // we really don't know - better to try to use it than reinstall
            return true;
        }

        if (minimumVersion == 'undefined' || minimumVersion == null) {
            minimumVersion = '1.4.2';
        }

        var retval = false;
        var regex = "^(\\d+)(?:\\.(\\d+)(?:\\.(\\d+)(?:_(\\d+))?)?)?$";
        var matchData = minimumVersion.match(regex);

        if (matchData != null) {
            retval = deployJava.versionCheck(minimumVersion + '+');
        } else {
            if (deployJava.debug) {
                alert('Invalid minimumVersion argument to isWebStartInstalled(): ' + minimumVersion);
            }
            retval = deployJava.versionCheck('1.4.2+');
        }
        return retval;
    },

    // obtain JPI version using navigator.mimeTypes array
    // if found, set the version to deployJava.firefoxJavaVersion
    getJPIVersionUsingMimeType: function() {
        // Walk through the full list of mime types.
        for (var i = 0; i < navigator.mimeTypes.length; ++i) {
            var s = navigator.mimeTypes[i].type;
            // The jpi-version is the plug-in version.  This is the best
            // version to use.
            var m = s.match(/^application\/x-java-applet;jpi-version=(.*)$/);
            if (m != null) {
                deployJava.firefoxJavaVersion = m[1];
                // Opera puts the latest sun JRE last not first
                if ('Opera' != deployJava.browserName2) {
                    break;
                }
            }
        }
    },
  
   // launch the specified JNLP application using the passed in jnlp file
   // the jnlp file does not need to have a codebase
   // this requires JRE 7 or above to work
   // if machine has no JRE 7 or above, we will try to auto-install and then launch
   // (function will return false if JRE auto-install failed)
   launchWebStartApplication: function(jnlp) {
	var uaString = navigator.userAgent.toLowerCase();

	deployJava.getJPIVersionUsingMimeType();

	// make sure we are JRE 7 or above
        if (deployJava.isWebStartInstalled('1.7.0') == false) {

		// perform latest JRE auto-install
  		if ((deployJava.installJRE('1.7.0+') == false) ||
                         ((deployJava.isWebStartInstalled('1.7.0') == false))) {
                          return false;
                }
	}

        var jnlpDocbase = null;

        // use document.documentURI for docbase
        if (document.documentURI) {
		jnlpDocbase = document.documentURI;
	}

	// fallback to document.URL if documentURI not available
        if (jnlpDocbase == null) {
		jnlpDocbase = document.URL;
	}

        var browser = deployJava.getBrowser();

        var launchTag;

        if (browser == 'MSIE') {

            launchTag = '<' +
                'object classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" ' +
		'width="0" height="0">' +
		'<' + 'PARAM name="launchjnlp" value="' + jnlp + '"' + '>' +
	        '<' + 'PARAM name="docbase" value="' + jnlpDocbase + '"' + '>' +
                '<' + '/' + 'object' + '>';
        } else if (browser == 'Netscape Family') {

            launchTag = '<' +
		'embed type="application/x-java-applet;jpi-version=' +
		deployJava.firefoxJavaVersion + '" ' +
                'width="0" height="0" ' +
                'launchjnlp="' +  jnlp + '"' +
                'docbase="' +  jnlpDocbase + '"' +
                ' />';
        }
 
        if (document.body == 'undefined' || document.body == null) {
           document.write(launchTag);
           // go back to original page, otherwise current page becomes blank
           document.location = jnlpDocbase;
        } else {
           var divTag = document.createElement("div");
           divTag.id = "div1";
           divTag.style.position = "relative";
           divTag.style.left = "-10000px";
           divTag.style.margin = "0px auto";
           divTag.className ="dynamicDiv";
           divTag.innerHTML = launchTag;
           document.body.appendChild(divTag);
        }
   },

   createWebStartLaunchButtonEx: function(jnlp, minimumVersion) {

        if (deployJava.returnPage == null) {
            // if there is an install, come back and run the jnlp file
            deployJava.returnPage = jnlp;
        }

        var url = 'javascript:deployJava.launchWebStartApplication(\'' + jnlp +
			'\');';

        document.write('<' + 'a href="' + url + 
                       '" onMouseOver="window.status=\'\'; ' +
                       'return true;"><' + 'img ' +
                       'src="' + deployJava.launchButtonPNG + '" ' + 
                       'border="0" /><' + '/' + 'a' + '>');
    },


    /**
     * Outputs a launch button for the specified JNLP URL.  When clicked, the 
     * button will ensure that an appropriate JRE is installed and then launch 
     * the JNLP application.  minimumVersion is of the form #[.#[.#[_#]]], and 
     * is the minimum JRE version necessary to run this JNLP application.  
     * minimumVersion is optional, and if it is not specified, '1.4.2' 
     * will be used.
     * If an appropriate JRE or Web Start installation is detected, 
     * the JNLP application will be launched, otherwise installLatestJRE() 
     * will be called.
     *
     * After installLatestJRE() is called, the script will attempt to detect 
     * that the JRE installation has completed and launch the JNLP application,
     * but there are circumstances (such as when the JRE installation 
     * requires a browser restart) when this cannot be fulfilled.
     */
    createWebStartLaunchButton: function(jnlp, minimumVersion) {

        if (deployJava.returnPage == null) {
            // if there is an install, come back and run the jnlp file
            deployJava.returnPage = jnlp;
        }

        var url = 'javascript:' +
                  'if (!deployJava.isWebStartInstalled(&quot;' + 
                      minimumVersion + '&quot;)) {' + 
                      'if (deployJava.installLatestJRE()) {' + 
                        'if (deployJava.launch(&quot;' + jnlp + '&quot;)) {}' +
                      '}' +
                  '} else {' +
                      'if (deployJava.launch(&quot;' + jnlp + '&quot;)) {}' +
                  '}';

        document.write('<' + 'a href="' + url + 
                       '" onMouseOver="window.status=\'\'; ' +
                       'return true;"><' + 'img ' +
                       'src="' + deployJava.launchButtonPNG + '" ' + 
                       'border="0" /><' + '/' + 'a' + '>');
    },


    /**
     * Launch a JNLP application, (using the plugin if available)
     */
    launch: function(jnlp) {
   /*
    * Using the plugin to launch Java Web Start is disabled for the time being
    */
        document.location=jnlp;
        return true;
    },

    
    /*
     * returns true if the ActiveX or XPI plugin is installed
     */
    isPluginInstalled: function() {
        var plugin = deployJava.getPlugin();
        if (plugin && plugin.jvms) {
            return true;
        } else {
            return false;
        }
    },
    
    /* 
     * returns true if the plugin is installed and AutoUpdate is enabled
     */
    isAutoUpdateEnabled: function() {
        if (deployJava.isPluginInstalled()) {
            return deployJava.getPlugin().isAutoUpdateEnabled();
        }
        return false;
    },

    /* 
     * sets AutoUpdate on if plugin is installed
     */
    setAutoUpdateEnabled: function() {
        if (deployJava.isPluginInstalled()) {
            return deployJava.getPlugin().setAutoUpdateEnabled(); 
        }
        return false;
    },

    /*
     * sets the preferred install type : null, online, kernel
     */
    setInstallerType: function(type) {
        deployJava.installType = type;
        if (deployJava.isPluginInstalled()) {
            return deployJava.getPlugin().setInstallerType(type);
        }
        return false;
    },

    /*
     * sets additional package list - to be used by kernel installer
     */
    setAdditionalPackages: function(packageList) {
        if (deployJava.isPluginInstalled()) {
            return deployJava.getPlugin().setAdditionalPackages(
                                                     packageList);
        }
        return false;
    },

    /*
     * sets preference to install Early Access versions if available
     */
    setEarlyAccess: function(enabled) {
        deployJava.EAInstallEnabled = enabled;
    },

    /*
     * Determines if the next generation plugin (Plugin II) is default
     */
    isPlugin2: function() {
        if (deployJava.isPluginInstalled()) {
            if (deployJava.versionCheck('1.6.0_10+')) {
                try {
                    return deployJava.getPlugin().isPlugin2();
                } catch (err) {
                    // older plugin w/o isPlugin2() function - 
                }
            }
        }
        return false;
    },
       
    allowPlugin: function() {
        deployJava.getBrowser();

        // Safari and Opera browsers find the plugin but it 
        // doesn't work, so until we can get it to work - don't use it.
        var ret = ('Safari' != deployJava.browserName2 &&
            'Opera' != deployJava.browserName2);

        return ret;
    },

    getPlugin: function() {
        deployJava.refresh();

        var ret = null;
        if (deployJava.allowPlugin()) {
            ret = document.getElementById('deployJavaPlugin');
        }
        return ret;
    },

    compareVersionToPattern: function(version, patternArray, familyMatch) {
        var regex = "^(\\d+)(?:\\.(\\d+)(?:\\.(\\d+)(?:_(\\d+))?)?)?$";
        var matchData = version.match(regex);  

        if (matchData != null) {
            var index = 0;
            var result = new Array();

            for (var i = 1; i < matchData.length; ++i) {
                if ((typeof matchData[i] == 'string') && (matchData[i] != ''))
                {
                    result[index] = matchData[i];
                    index++;
                }
            }

            var l = Math.min(result.length, patternArray.length);

            if (familyMatch) {
                for (var i = 0; i < l; ++i) {
                    if (result[i] != patternArray[i]) return false;
                }

                return true;
            } else {
                for (var i = 0; i < l; ++i) {
                    if (result[i] < patternArray[i]) {
                        return false;
                    } else if (result[i] > patternArray[i]) {
                        return true;
                    }
                }
                
                return true;
            }
        } else {
            return false;
        }
    },

  
    getBrowser: function() {

        if (deployJava.browserName == null) {
            var browser = navigator.userAgent.toLowerCase();
    
            if (deployJava.debug) {
                alert('userAgent -> ' + browser);
            }

            // order is important here.  Safari userAgent contains mozilla,
            // and Chrome userAgent contains both mozilla and safari.
            if (browser.indexOf('msie') != -1) {
                deployJava.browserName = 'MSIE';
                deployJava.browserName2 = 'MSIE';
            } else if (browser.indexOf('iphone') != -1) {
                // this included both iPhone and iPad
                deployJava.browserName = 'Netscape Family';
                deployJava.browserName2 = 'iPhone';
            } else if (browser.indexOf('firefox') != -1) {
                deployJava.browserName = 'Netscape Family';
                deployJava.browserName2 = 'Firefox';
            } else if (browser.indexOf('chrome') != -1) {
                deployJava.browserName = 'Netscape Family';
                deployJava.browserName2 = 'Chrome';
            } else if (browser.indexOf('safari') != -1) {
                deployJava.browserName = 'Netscape Family';
                deployJava.browserName2 = 'Safari';
            } else if (browser.indexOf('mozilla') != -1) {
                deployJava.browserName = 'Netscape Family';
                deployJava.browserName2 = 'Other';
            } else if (browser.indexOf('opera') != -1) {
                deployJava.browserName = 'Netscape Family';
                deployJava.browserName2 = 'Opera';
            } else {
                deployJava.browserName = '?';
                deployJava.browserName2 = 'unknown';
            }

            if (deployJava.debug) {
                alert ('Detected browser name:'+ deployJava.browserName +
                       ', ' + deployJava.browserName2);
            }
        }
        return deployJava.browserName;
    },
    
    
    testUsingActiveX: function(version) {
        var objectName = 'JavaWebStart.isInstalled.' + version + '.0';
    
        if (!ActiveXObject) {
            if (deployJava.debug) {
              alert ('Browser claims to be IE, but no ActiveXObject object?');
            }
            return false;
        }
    
        try {
            return (new ActiveXObject(objectName) != null);
        } catch (exception) {
            return false;
        }
    },
    

    testForMSVM: function() {
        var clsid = '{08B0E5C0-4FCB-11CF-AAA5-00401C608500}';

        if (typeof oClientCaps != 'undefined') {
            var v = oClientCaps.getComponentVersion(clsid, "ComponentID");
            if ((v == '') || (v == '5,0,5000,0')) {
                return false;
            } else {
                return true;
            } 
        } else {
            return false;
        }
    },

    
    testUsingMimeTypes: function(version) {
        if (!navigator.mimeTypes) {
            if (deployJava.debug) {
                alert ('Browser claims to be Netscape family, but no mimeTypes[] array?');
            }
            return false;
        }
    
        for (var i = 0; i < navigator.mimeTypes.length; ++i) {
            s = navigator.mimeTypes[i].type;
            var m = s.match(/^application\/x-java-applet\x3Bversion=(1\.8|1\.7|1\.6|1\.5|1\.4\.2)$/);
            if (m != null) {
                if (deployJava.compareVersions(m[1], version)) {
                    return true;   
                }
            }
        }
        return false;
    },
    
    testUsingPluginsArray: function(version) {
        if ((!navigator.plugins) || (!navigator.plugins.length)) {
            return false;
        }
        var platform = navigator.platform.toLowerCase();

        for (var i = 0; i < navigator.plugins.length; ++i) {
            s = navigator.plugins[i].description;
            if (s.search(/^Java Switchable Plug-in (Cocoa)/) != -1) {
                // Safari on MAC
                if (deployJava.compareVersions("1.5.0", version)) {
                    return true;
                }
            } else if (s.search(/^Java/) != -1) {
                if (platform.indexOf('win') != -1) {
                    // still can't tell - opera, safari on windows
                    // return true for 1.5.0 and 1.6.0
                    if (deployJava.compareVersions("1.5.0", version) ||
                        deployJava.compareVersions("1.6.0", version)) {
                        return true;
                    }
                }
            }
        }
        // if above dosn't work on Apple or Windows, just allow 1.5.0
        if (deployJava.compareVersions("1.5.0", version)) {
            return true;
        }
        return false;



    },
    
    IEInstall: function() {
    
        location.href = deployJava.getJavaURL + 
            ((deployJava.returnPage != null) ?
            ('&returnPage=' + deployJava.returnPage) : '') +
            ((deployJava.locale != null) ?
            ('&locale=' + deployJava.locale) : '') +
            ((deployJava.brand != null) ? ('&brand=' + deployJava.brand) : '') +
            ((deployJava.installType != null) ? 
             ('&type=' + deployJava.installType) : '');

         // should not actually get here
         return false;
    },
    
    done: function (name, result) {
    },
    
    FFInstall: function() {

        location.href = deployJava.getJavaURL + 
            ((deployJava.returnPage != null) ?
            ('&returnPage=' + deployJava.returnPage) : '') +
            ((deployJava.locale != null) ?
            ('&locale=' + deployJava.locale) : '') +
            ((deployJava.brand != null) ? ('&brand=' + deployJava.brand) : '') +
            ((deployJava.installType != null) ? 
                ('&type=' + deployJava.installType) : '');

         // should not actually get here
         return false;
    },

    // return true if 'installed' (considered as a JRE version string) is
    // greater than or equal to 'required' (again, a JRE version string).
    compareVersions: function(installed, required) {

        var a = installed.split('.');
        var b = required.split('.');
    
        for (var i = 0; i < a.length; ++i) {
            a[i] = Number(a[i]);
        }
        for (var i = 0; i < b.length; ++i) {
            b[i] = Number(b[i]);
        }
        if (a.length == 2) {
            a[2] = 0;      
        }
    
        if (a[0] > b[0]) return true;
        if (a[0] < b[0]) return false;
    
        if (a[1] > b[1]) return true;
        if (a[1] < b[1]) return false;
    
        if (a[2] > b[2]) return true;
        if (a[2] < b[2]) return false;
    
        return true;
    },
    
    
    enableAlerts: function() {
        // reset this so we can show the browser detection
        deployJava.browserName = null;
        deployJava.debug = true;
    },

    poll: function() {

        deployJava.refresh();
        var postInstallJREList = deployJava.getJREs();           

        if ((deployJava.preInstallJREList.length == 0) && 
            (postInstallJREList.length != 0)) {
            clearInterval(deployJava.myInterval);
            if (deployJava.returnPage != null) {
                location.href = deployJava.returnPage;
            };
        }

        if ((deployJava.preInstallJREList.length != 0) && 
            (postInstallJREList.length != 0) &&
            (deployJava.preInstallJREList[0] != postInstallJREList[0])) {
            clearInterval(deployJava.myInterval);
            if (deployJava.returnPage != null) {
                location.href = deployJava.returnPage;
            }
        }

    },
    
    writePluginTag: function() {
        var browser = deployJava.getBrowser();

        if (browser == 'MSIE') {
            document.write('<' + 
                'object classid="clsid:CAFEEFAC-DEC7-0000-0000-ABCDEFFEDCBA" ' +
                'id="deployJavaPlugin" width="0" height="0">' +
                '<' + '/' + 'object' + '>');
        } else if (browser == 'Netscape Family' && deployJava.allowPlugin()) {
            deployJava.writeEmbedTag();
        }
    },

    refresh: function() {
        navigator.plugins.refresh(false);

        var browser = deployJava.getBrowser();
        if (browser == 'Netscape Family' && deployJava.allowPlugin()) {
            var plugin = document.getElementById('deployJavaPlugin');
            // only do this again if no plugin
            if (plugin == null) {
                deployJava.writeEmbedTag();
            }
        }
     },

    writeEmbedTag: function() {
        var written = false;
        if (navigator.mimeTypes != null) {
            for (var i=0; i < navigator.mimeTypes.length; i++) {
                if (navigator.mimeTypes[i].type == deployJava.mimeType) {
                    if (navigator.mimeTypes[i].enabledPlugin) {
                        document.write('<' +
                            'embed id="deployJavaPlugin" type="' +
                            deployJava.mimeType + '" hidden="true" />');
                        written = true;
                    }
                }
            }
            // if we ddn't find new mimeType, look for old mimeType
            if (!written) for (var i=0; i < navigator.mimeTypes.length; i++) {
                if (navigator.mimeTypes[i].type == deployJava.oldMimeType) {
                    if (navigator.mimeTypes[i].enabledPlugin) {
                        document.write('<' +
                            'embed id="deployJavaPlugin" type="' +
                            deployJava.oldMimeType + '" hidden="true" />');
                    }
                }
            }
        }
    },

    do_initialize: function() {
        deployJava.writePluginTag();
        if (deployJava.locale == null) {
            var loc = null;

            if (loc == null) try {
                loc = navigator.userLanguage;
            } catch (err) { }

            if (loc == null) try {
                loc = navigator.systemLanguage;
            } catch (err) { }
    
            if (loc == null) try {
                loc = navigator.language;
            } catch (err) { }
    
            if (loc != null) {
                loc.replace("-","_")
                deployJava.locale = loc;
            }
        }
    }
      
};
deployJava.do_initialize();

