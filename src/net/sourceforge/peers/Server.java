/*
    This file is part of Peers.

    Foobar is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    
    Copyright 2007 Yohann Martineau 
*/

package net.sourceforge.peers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class Server {

    public static final String SERVER_HOST = "peers.sourceforge.net";
    public static final String PREFIX = "/peers";
    //public static final int SOCKET_TIMEOUT = 30000;//millis
    
    private InetAddress localAddress;
    private int localPort;
    private InetAddress remoteAddress;
    private int remotePort;
    
    private Socket socket;
    
    //TODO constructor without parameters
    public Server(InetAddress localAddress, int localPort) throws IOException {
        super();
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.remoteAddress = InetAddress.getByName(SERVER_HOST);
        this.remotePort = 80;
        socket = new Socket(remoteAddress, remotePort, localAddress, localPort);
        //socket.setSoTimeout(SOCKET_TIMEOUT);
    }

    /**
     * This method will update public address on the web server.
     * @param email user identifier
     */
    public void update(String email) {
        String encodedEmail;
        try {
            encodedEmail = URLEncoder.encode(email, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        StringBuffer urlEnd = new StringBuffer();
        urlEnd.append("update2.php?email=");
        urlEnd.append(encodedEmail);
        get(urlEnd.toString());
        close();
    }
    
    public Document getPeers(String email) {
        String encodedEmail;
        try {
            encodedEmail = URLEncoder.encode(email, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuffer urlBuf = new StringBuffer();
        urlBuf.append("http://");
        urlBuf.append(SERVER_HOST);
        urlBuf.append(PREFIX);
        urlBuf.append("/getassocasxml.php?email=");
        urlBuf.append(encodedEmail);
        URL url;
        try {
            url = new URL(urlBuf.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        System.out.println("retrieved peers");
        SAXReader saxReader = new SAXReader();
        Document doc;
        try {
            doc = saxReader.read(url);
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
        return doc;
    }
    
    private String get(String urlEnd) {
        StringBuffer get = new StringBuffer();
        get.append("GET ");
        get.append(PREFIX);
        get.append('/');
        get.append(urlEnd);
        get.append(" HTTP/1.1\r\n");
        get.append("Host: ");
        get.append(SERVER_HOST);
        get.append("\r\n");
        get.append("\r\n");
        
        try {
            socket.getOutputStream().write(get.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        System.out.println("> sent:\n" + get.toString());
        
        StringBuffer result = new StringBuffer();
        try {
            byte[] buf = new byte[256];
            int read = 0;
            while ((read = socket.getInputStream().read(buf)) > -1) {
                byte[] exactBuf = new byte[read];
                System.arraycopy(buf, 0, exactBuf, 0, read);
                result.append(new String(exactBuf));
            }
        } catch (SocketTimeoutException e) {
            System.out.println("socket timeout");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        System.out.println("< received:\n" + result.toString());
        return result.toString();
    }
    
    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
