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

package net.sourceforge.peers.sip.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import net.sourceforge.peers.sip.core.Config;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;

public class ConfigTest extends TestCase {

    public void testConfig() {
        String urlStr =
            "file:" + new File("conf/sipstack.xml").getAbsolutePath();
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            fail();
            return;
        }
        Config config;
        try {
            config = new Config(url);
        } catch (DocumentException e) {
            e.printStackTrace();
            fail();
            return;
        }
        
        List list = config.selectNodes("//peers:profile/@name");
        if (list == null) {
            fail("no node found for xpath: //peers:profile/@name");
        }
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Attribute name = (Attribute)it.next();
            System.out.println(name.getValue());
        }
    }
}
