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
    
    Copyright 2007, 2008, 2009, 2010 Yohann Martineau 
*/

package net.sourceforge.peers.sip.transport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Debugger {

    public static void showObject(Object o) {
        Method[] methods = o.getClass().getMethods();
        StringBuffer buf = new StringBuffer();
        buf.append(o.getClass().getName());
        buf.append("\n");
        for (Method m : methods) {
            Class<?>[] classes = m.getParameterTypes();
            String name = m.getName();
            if (name.startsWith("get") && classes.length == 0) {
                buf.append("\t.");
                buf.append(name);
                buf.append("(): ");
                try {
                    buf.append(m.invoke(o, new Object[0]));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                buf.append("\n");
            }
        }
        System.out.println(buf);
    }
    
}
