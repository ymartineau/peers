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

package net.sourceforge.peers.sip.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;


public abstract class Transaction {

    public static final char ID_SEPARATOR = '|';
    
    protected String branchId;
    protected String method;
    
    protected SipRequest request;
    protected List<SipResponse> responses;

    protected Transaction(String branchId, String method) {
        super();
        this.branchId = branchId;
        this.method = method;
        responses = Collections.synchronizedList(new ArrayList<SipResponse>());
    }

    protected String getId() {
        StringBuffer buf = new StringBuffer();
        buf.append(branchId).append(ID_SEPARATOR);
        buf.append(method);
        return buf.toString();
    }

    protected SipResponse getLastResponse() {
        return responses.get(responses.size() - 1);
    }

    public SipRequest getRequest() {
        return request;
    }
    
}
