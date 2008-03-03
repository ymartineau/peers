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

package net.sourceforge.peers.sip.syntaxencoding;

import java.util.ArrayList;

public class SipHeaderFieldMultiValue extends SipHeaderFieldValue {

    private ArrayList<SipHeaderFieldValue> values;
    
    private static String toString(ArrayList<SipHeaderFieldValue> arr) {
        if (arr == null) {
            return null;
        }
        String arrToString = arr.toString();
        return arrToString.substring(1, arrToString.length() - 1);
    }
    
    public SipHeaderFieldMultiValue(ArrayList<SipHeaderFieldValue> values) {
        super(toString(values));
        this.values = values;
    }

    public ArrayList<SipHeaderFieldValue> getValues() {
        return values;
    }
    
    @Override
    public String toString() {
        return toString(values);
    }
}
