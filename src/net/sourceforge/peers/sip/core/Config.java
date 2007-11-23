/*
    This file is part of Peers.

    Peers is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Peers is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    
    Copyright 2007 Yohann Martineau 
*/

package net.sourceforge.peers.sip.core;

import java.net.URL;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.jaxen.SimpleNamespaceContext;
import org.xml.sax.SAXException;

public class Config {

//    private URL url;
    private Document document;

    public Config(URL url) throws DocumentException {
        super();
//        this.url = url;
        SAXReader reader = new SAXReader(true);
        try {
            reader.setFeature(
                    "http://apache.org/xml/features/validation/schema",
                    true);
        } catch (SAXException e) {
            e.printStackTrace();
            return;
        }
        document = reader.read(url);
    }
    
    public Node selectSingleNode(String xpath) {
        DefaultXPath xpathObj = new DefaultXPath(xpath);
        SimpleNamespaceContext snc = new SimpleNamespaceContext();
        snc.addNamespace("peers", "http://peers.sourceforge.net");
        xpathObj.setNamespaceContext(snc);
        return (Node)xpathObj.selectSingleNode(document);
//        try {
//            return (Node)getNSXpath(xpath).selectSingleNode(
//                    document.getRootElement());
//        } catch (JaxenException e) {
//            e.printStackTrace();
//            return null;
//        }
//        return (Node)document.selectSingleNode(xpath);
    }
    
    public List selectNodes(String xpath) {
        DefaultXPath xpathObj = new DefaultXPath(xpath);
        SimpleNamespaceContext snc = new SimpleNamespaceContext();
        snc.addNamespace("peers", "http://peers.sourceforge.net");
        xpathObj.setNamespaceContext(snc);
        return xpathObj.selectNodes(document);
//        try {
//            return getNSXpath(xpath).selectNodes(document);
//        } catch (JaxenException e) {
//            e.printStackTrace();
//            return null;
//        }
//        return document.selectNodes(xpath);
    }
    
//    private XPath getNSXpath(String xpathWithNS) {
//        XPath xpath = null;
//        try {
//            xpath = new DOMXPath(xpathWithNS);
//            xpath.addNamespace("ym", "http://yohann.martineau.free.fr");
//        } catch (JaxenException e) {
//            e.printStackTrace();
//        }
//        return xpath;
//    }
}
