============================================================
                Peers: SIP related experiments
                 http://peers.sourceforge.net
============================================================

LICENSE

This software is released under GPL License version 3 or
any later version. Please read this license in gpl.txt if
not already done.

SPECIFICATION

Peers is a SIP User-Agent compatible with RFC3261, you can
retrieve this specification here:
  http://www.ietf.org/rfc/rfc3261.txt

PREREQUISITES

This software has been developed using Sun Java Development Kit
version 6. You should install the latest Java Runtime Environment
on your computer if you just want to run the application. If you
want to compile the sources yourself, you should use the JDK. In
both cases, you can download the installation files here:
  http://java.sun.com/javase/downloads/index.jsp

CONFIGURATION

You can manually configure the IP address of your computer in file
conf/peers.xml. You can also configure your SIP listening port and
your RTP port. This configuration file is ruled by a grammar file:
peers.xsd. Thus to modify this file, you can use jEdit with the
XML plugin. You can download jEdit here:
  http://www.jedit.org/index.php?page=download
Remember that this configuration step is optional, the default
configuration should be enough.
You can also configure an account with profile element, using
userpart, domain and password elements. The authentication is
done with Message Digest and MD5 algorithm, as specified in RFC2617.

RUNNING

If you are a Windows user you can use the .bat batch script in root
directory, if you use any Unix compatible sytem, you can use the
.sh script. You can then call any IP address using SIP protocol,
if the remote host does not listen on the default SIP port (5060),
you can use the following example URI:
  sip:192.168.1.2:6060
For some softphones, it is necessary to add a userpart to the called
sip uri, for example:
  sip:alice@192.168.1.2:6060

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


AUTHOR

Yohann Martineau yohann.martineau@gmail.com
