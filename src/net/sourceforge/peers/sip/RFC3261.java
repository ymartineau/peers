/*
    This file is part of Peers.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    Copyright 2007, 2008 Yohann Martineau 
*/

package net.sourceforge.peers.sip;

public final class RFC3261 {
    
    //SYNTAX ENCODING
    
      //HEADERS
    
        //Methods
    
    public final static String METHOD_INVITE   = "INVITE";
    public final static String METHOD_ACK      = "ACK";
    public final static String METHOD_REGISTER = "REGISTER";
    public final static String METHOD_BYE      = "BYE";
    public final static String METHOD_OPTIONS  = "OPTIONS";
    public final static String METHOD_CANCEL   = "CANCEL";
    
        //Normal form
    
    public final static String HDR_CALLID           = "Call-ID";
    public final static String HDR_CONTACT          = "Contact";
    public final static String HDR_CONTENT_ENCODING = "Content-Encoding";
    public final static String HDR_CONTENT_LENGTH   = "Content-Length";
    public final static String HDR_CONTENT_TYPE     = "Content-Type";
    public final static String HDR_CSEQ             = "CSeq";
    public final static String HDR_FROM             = "From";
    public final static String HDR_MAXFORWARDS      = "Max-Forwards";
    public final static String HDR_RECORD_ROUTE     = "Record-Route";
    public final static String HDR_ROUTE            = "Route";
    public final static String HDR_SUBJECT          = "Subject";
    public final static String HDR_SUPPORTED        = "Supported";
    public final static String HDR_TO               = "To";
    public final static String HDR_VIA              = "Via";
    
        //Compact form
    
    public final static char COMPACT_HDR_CALLID           = 'i';
    public final static char COMPACT_HDR_CONTACT          = 'm';
    public final static char COMPACT_HDR_CONTENT_ENCODING = 'e';
    public final static char COMPACT_HDR_CONTENT_LENGTH   = 'l';
    public final static char COMPACT_HDR_CONTENT_TYPE     = 'c';
    public final static char COMPACT_HDR_FROM             = 'f';
    public final static char COMPACT_HDR_SUBJECT          = 's';
    public final static char COMPACT_HDR_SUPPORTED        = 'k';
    public final static char COMPACT_HDR_TO               = 't';
    public final static char COMPACT_HDR_VIA              = 'v';
    
        //Parameters
    
    public final static String PARAM_SEPARATOR  = ";";
    public final static String PARAM_ASSIGNMENT = "=";
    public final static String PARAM_MADDR      = "maddr";
    public final static String PARAM_TTL        = "ttl";
    public final static String PARAM_SENTBY     = "sent-by";
    public final static String PARAM_BRANCH   = "branch";
    public final static String PARAM_TAG        = "tag";
    public final static String PARAM_TRANSPORT  = "transport";
    public final static String PARAM_RECEIVED   = "received";
    
        //Miscellaneous
    public final static char   FIELD_NAME_SEPARATOR = ':';
    public final static String DEFAULT_SIP_VERSION  = "SIP/2.0";
    public final static String CRLF                 = "\r\n";
    public final static String IPV4_TTL             = "1";
    public final static char   AT                   = '@';
    public final static String LOOSE_ROUTING        = "lr";
    public final static char   LEFT_ANGLE_BRACKET   = '<';
    public final static char   RIGHT_ANGLE_BRACKET  = '>';
    
      //STATUS CODES
    public final static int CODE_MIN_PROV          = 100;
    public final static int CODE_MIN_SUCCESS       = 200;
    public final static int CODE_MIN_REDIR         = 300;
    public final static int CODE_MAX               = 699;

    public final static int CODE_180_RINGING       = 180;
    public final static int CODE_200_OK            = 200;
    public final static int CODE_486_BUSYHERE      = 486;
    
      //REASON PHRASES
    public final static String REASON_180_RINGING  = "Ringing";
    public final static String REASON_200_OK       = "OK";
    public final static String REASON_486_BUSYHERE = "Busy Here";
    
    //TRANSPORT
    
    public final static String TRANSPORT_UDP                = "UDP";
    public final static String TRANSPORT_TCP                = "TCP";
    public final static String TRANSPORT_SCTP               = "SCTP";
    public final static String TRANSPORT_TLS                = "TLS";
    public final static int    TRANSPORT_UDP_USUAL_MAX_SIZE = 1300;
    public final static int    TRANSPORT_UDP_MAX_SIZE       = 65535;
    public final static char   TRANSPORT_VIA_SEP            = '/';
    public final static char   TRANSPORT_VIA_SEP2           = ' ';
    public final static int    TRANSPORT_DEFAULT_PORT       = 5060;
    public final static int    TRANSPORT_TLS_PORT           = 5061;
    public final static char   TRANSPORT_PORT_SEP           = ':';
    
    
    //TRANSACTION
    
    
    //TRANSACTION USER
    
    public final static int    DEFAULT_MAXFORWARDS   = 70;
    public final static String BRANCHID_MAGIC_COOKIE = "z9hG4bK";
    public final static String SIP_SCHEME            = "sip";
    public final static char   SCHEME_SEPARATOR      = ':';
    
    //TIMERS (in milliseconds)
    
    public final static int TIMER_T1 = 500;
    public final static int TIMER_T2 = 4000;
    public final static int TIMER_T4 = 5000;
    public final static int TIMER_INVITE_CLIENT_TRANSACTION = 32000;
 
    
    //TRANSACTION USER
    
    
    //CORE
    
    public final static String CONTENT_TYPE_SDP = "application/sdp";
    
}
