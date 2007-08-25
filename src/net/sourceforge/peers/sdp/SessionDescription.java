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

package net.sourceforge.peers.sdp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;

public class SessionDescription {

	private long id;
	private long version;
	private String name;
	private String username;
	private InetAddress ipAddress;
	private ArrayList<MediaDescription> medias;
    private Hashtable<String, String> attributes;

    public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public InetAddress getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	public ArrayList<MediaDescription> getMedias() {
		return medias;
	}
	public void setMedias(ArrayList<MediaDescription> medias) {
		this.medias = medias;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
    public Hashtable<String, String> getAttributes() {
        return attributes;
    }
    public void setAttributes(Hashtable<String, String> attributes) {
        this.attributes = attributes;
    }
	
}
