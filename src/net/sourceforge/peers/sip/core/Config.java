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

import java.net.URL;
import java.util.List;

import net.sourceforge.peers.Logger;

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
            Logger.error("SAX error", e);
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
