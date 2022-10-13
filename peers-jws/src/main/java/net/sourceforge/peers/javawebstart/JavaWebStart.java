/*
    This file is part of Peers, a java SIP softphone.

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
    
    Copyright 2011 Yohann Martineau 
*/

package net.sourceforge.peers.javawebstart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static net.sourceforge.peers.sip.Utils.DEFAULT_PEERS_HOME;

public class JavaWebStart {
    
    public static void main(final String[] args) {

        String peersDir = ".peers";
        String home = System.getProperty("user.home", DEFAULT_PEERS_HOME);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String peersHome = home + File.separator + peersDir + File.separator
            + format.format(new Date());
        createDirectory(peersHome + File.separator + "conf");
        createDirectory(peersHome + File.separator + "logs");
        createDirectory(peersHome + File.separator + "media");
        copyFile("conf/peers.xml", peersHome + File.separator + "conf"
                + File.separator + "peers.xml");
        copyFile("conf/peers.xsd", peersHome + File.separator + "conf"
                + File.separator + "peers.xsd");
        String peersPath = new File(peersHome).getAbsolutePath();
        final String[] args2 = {peersPath};
        new RegisterSIPClient(args2);
    }
    
    private static void createDirectory(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    private static void copyFile(String source, String dest) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(source))) {
            try (FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[256];
                int readBytes;
                while ((readBytes = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, readBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public JavaWebStart(String[] args) {
    }
    
}
