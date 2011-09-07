============================================================
                   Peers: java sip softphone
                 http://peers.sourceforge.net/
============================================================


LICENSE


This software is released under GPL License version 3 or
any later version. Please read this license in gpl.txt if
not already done.


SPECIFICATION


Peers is a software phone (softphone) compatible with the
following specifications:
 - RFC 3261 (SIP),
 - RFC 4566 (SDP),
 - RFC 3550 (RTP),
 - RFC 3551 (RTP Audio/Video profile),
 - RFC 2617 (Digest Authentication),
 - RFC 4733 (DTMF),
 - ITU-T G.711 (PCMU, PCMA)
You should easilly retrieve those specifications on internet with
your favorite search engine.


PREREQUISITES


This software has been developed using Sun Java Development Kit
version 6. You should install the latest Java Runtime Environment
on your computer if you just want to run the application. If you
want to compile the sources yourself, you should use the JDK. In
both cases, you can download the installation files here:
  http://www.oracle.com/technetwork/java/javase/downloads/index.html


CONFIGURATION


Your SIP account credentials can be configured in conf/peers.xml.
Please read comments in this file for configuration details.
This configuration file is ruled by a grammar file: peers.xsd.
Thus to modify this file, you can use jEdit with XML and Error list
plugins. You can download jEdit here:
  http://www.jedit.org/index.php?page=download
This provides xml completion and grammar checking which can be
very useful to avoid simple configuration errors.
You can also configure your SIP account using graphical user
interface.


RUNNING


If you are a Windows user you can use the .bat batch script in root
directory, if you use any Unix compatible sytem, you can use the
.sh script. You can also double click .jar file. You can then call
any IP address using SIP protocol, if the remote host does not listen
on the default SIP port (5060), you can use the following example URI:
  sip:192.168.1.2:6060
For some softphones, it is necessary to add a userpart to the called
sip uri, for example:
  sip:alice@192.168.1.2:6060

If you configured a sip account in configuration file or using gui,
you can also place calls with usual sip uris:
  sip:bob@biloxi.com

Advanced users can run several peers instances on the same computer.
In this case a folder should be created in peers root directory for
each peers instance. This folder should contain three directories:
conf, logs and media. conf should contain peers.xml and peers.xsd for this
instance. peers.xml will need to be updated with this instance
parameters, peers.xsd can be copied from root conf directory. You will
need to do this for each instance. <media> parameter in configuration file
should be activated for at most one peers instance, this avoids comfusion
in microphone capture and sound playback. You should also check that
SIP and RTP ports are not the same in each configuration file.

Here is an example configuration:

  peers/
    user1/
      conf/
        peers.xml
        peers.xsd
      logs/
      media/
    user2/
      conf/
        peers.xml
        peers.xsd
      logs/
      media/

Once all those files have been created and updated, each instance can
be run providing a java system property giving peers home directory:

  java -classpath build/classes -Dpeers.home=user1 net.sourceforge.peers.gui.MainFrame

As a Main-Class has been defined in jar manifest, you can also use the following
command line:

  java -jar build/peers.jar -Dpeers.home=user1 net.sourceforge.peers.gui.MainFrame


HISTORY


2007-11-25 0.1 First release


minimalist UAC and UAS.


2007-12-09 0.1.1 First release update


moved startup scripts in root directory


2008-03-29 0.2 Second release


New features:
 - provisional responses (UAC and UAS),
 - CANCEL management (UAC and UAS), updated GUI with provisional stuff
 - new Logger which enables network traffic tracing and classical log4j-like
   logging in two separate files
Bugs fixed:
 - 1900810 MTU too small management


2008-06-08 0.3 Third release


New features:
 - register management (initial register, register refresh, unregister)
 - authentication using message digest (RFC2617)
Improved features:
 - media capture/sending using pipes and three threads
 - using TestNG for tests
 - no singleton is used anymore
 - xxxRequestManagers and xxxMethodHandlers are instanciated only once for
   uas and uac
 - provisional responses can create or update dialog info (remote target, etc.)
Bugs fixed:
 - 1994625 provisional responses with to-tag


2009-09-23 0.3.1 Peers resurrection


New features:
 - Running:
   - peers.home system property to run peers in several environments
 - Configuration:
   - an outbound proxy can now be configured in configuration file
   - media can now be activated or deactivated in configuration file
 - SIP:
   - support "sent-by" and "received" Via parameters
   - support 407 Proxy-Authenticate on REGISTER
   - support 401 and 407 on INVITE
   - support re-INVITEs (refresh target)
   - manage challenges on INVITEs
 - RTP:
   - support remote party update (ip address and port)
Improved features:
 - transport log file now contains real remote ip address and port
 - fixed media sending issue (replaced encoder with mobicents media server
   g711 encoder)


2010-12-13 0.4 Peers GUI


New features:
 - GUI:
   - update sip account settings in account frame with no modification in files
 - SIP:
   - 3102136 keep-alives sent and answered
   - 3109472 added rport management based on RFC 3581
   - 3031364 added OPTIONS method management
 - RTP:
   - 2934223 support RFC 4733 outgoing telephone-events (DTMF)
   - 3107231 added PCMA (G711 A-law) codec
Improved features:
 - gui complete refactoring, now using clean events between sip core and gui
Bugs fixed:
   - 3032080 no media on pickup
   - 2864885 multiple challenged calls
   - 3098214 start CSeq at 1 instead of 0


2011-05-19 0.4.1 Bug fixes


Bugs fixed:
 - GUI:
   - 3155571 tuijldert: update dialog state before gui notification
 - SIP:
   - 3137803 tuijldert: client transaction synchronization on responses reception
   - 3285107 added authentication on bye request


2011-07-04 0.4.2 Bug fixes


Bugs fixed:
 - SIP:
   - 3325451 cannot hangup with asterisk 1.8.4.2
   - 3324115 ACK does not contain Authorization header


2011-09-06 0.4.3 Various improvements


New features:
 - SIP:
   - manage opaque parameter in challenge
   - use same tag in From header between original and authenticated INVITE
   - support incoming INVITEs and reINVITEs with empty body


Bugs fixed:
 - SIP:
   - 3392342 bad CANCEL sip request when other party doesn't picking up


AUTHOR

Yohann Martineau yohann.martineau@gmail.com
