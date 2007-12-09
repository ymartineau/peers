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
    
    Copyright 2007 Yohann Martineau 
*/

package net.sourceforge.peers.sdp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;

public class SdpParser {

	public SessionDescription parse(byte[] body) throws IOException {
		if (body == null || body.length == 0) {
			return null;
		}
		ByteArrayInputStream in = new ByteArrayInputStream(body);
		InputStreamReader inputStreamReader = new InputStreamReader(in);
		BufferedReader reader = new BufferedReader(inputStreamReader);
		SessionDescription sessionDescription = new SessionDescription();
		
		//version
		
		String line = reader.readLine();
		if (line.length() < 3) {
			return null;
		}
		if (line.charAt(0) != RFC4566.TYPE_VERSION
				|| line.charAt(1) != RFC4566.SEPARATOR
				|| line.charAt(2) != RFC4566.VERSION) {
			return null;
		}

		//origin
		
		line = reader.readLine();
		if (line.length() < 3) {
			return null;
		}
		if (line.charAt(0) != RFC4566.TYPE_ORIGIN
				|| line.charAt(1) != RFC4566.SEPARATOR) {
			return null;
		}
		line = line.substring(2);
		String[] originArr = line.split(" ");
		if (originArr == null || originArr.length != 6) {
			return null;
		}
		sessionDescription.setUsername(originArr[0]);
		sessionDescription.setId(Long.parseLong(originArr[1]));
		sessionDescription.setVersion(Long.parseLong(originArr[2]));
		sessionDescription.setIpAddress(InetAddress.getByName(originArr[5]));

		//name
		
		line = reader.readLine();
		if (line.length() < 3) {
			return null;
		}
		if (line.charAt(0) != RFC4566.TYPE_SUBJECT
				|| line.charAt(1) != RFC4566.SEPARATOR) {
			return null;
		}
		sessionDescription.setName(line.substring(2));
		
		//session connection and attributes
        Hashtable<String, String> sessionAttributes = new Hashtable<String, String>();
        sessionDescription.setAttributes(sessionAttributes);
		
		while ((line = reader.readLine()) != null
				&& line.charAt(0) != RFC4566.TYPE_MEDIA) {
			if (line.length() > 3
					&& line.charAt(0) == RFC4566.TYPE_CONNECTION
					&& line.charAt(1) == RFC4566.SEPARATOR) {
				String connection = parseConnection(line.substring(2));
				if (connection == null) {
					continue;
				}
				sessionDescription.setIpAddress(InetAddress.getByName(connection));
			} else if (line.length() > 3
                    && line.charAt(0) == RFC4566.TYPE_ATTRIBUTE
                    && line.charAt(1) == RFC4566.SEPARATOR) {
                parseAttribute(line.substring(2), sessionAttributes);
            }
		}
		if (line == null) {
			return null;
		}
		//we are at the first media line
        
        ArrayList<SdpLine> mediaLines = new ArrayList<SdpLine>();
        do {
            if (line.length() < 3) {
                return null;
            }
            if (line.charAt(1) != RFC4566.SEPARATOR) {
                return null;
            }
            SdpLine mediaLine = new SdpLine();
            mediaLine.setType(line.charAt(0));
            mediaLine.setValue(line.substring(2));
            mediaLines.add(mediaLine);
        }
        while ((line = reader.readLine()) != null);
        
        ArrayList<MediaDescription> mediaDescriptions = new ArrayList<MediaDescription>();
        sessionDescription.setMedias(mediaDescriptions);
        
        for (SdpLine mediaLine : mediaLines) {
            MediaDescription mediaDescription;
            if (mediaLine.getType() == RFC4566.TYPE_MEDIA) {
                String[] mediaArr = mediaLine.getValue().split(" ");
                if (mediaArr == null || mediaArr.length < 4) {
                    return null;
                }
                mediaDescription = new MediaDescription();
                //TODO manage port range
                mediaDescription.setPort(Integer.parseInt(mediaArr[1]));
                mediaDescriptions.add(mediaDescription);
                mediaDescription.setAttributes(new Hashtable<String, String>());
            } else {
                mediaDescription = mediaDescriptions.get(mediaDescriptions.size() - 1);
                String mediaValue = mediaLine.getValue();
                if (mediaLine.getType() == RFC4566.TYPE_CONNECTION) {
                    String ipAddress = parseConnection(mediaValue);
                    mediaDescription.setIpAddress(InetAddress.getByName(ipAddress));
                }
                if (mediaLine.getType() == RFC4566.TYPE_ATTRIBUTE) {
                    Hashtable<String, String> attributes = mediaDescription.getAttributes();
                    parseAttribute(mediaValue, attributes);
                }
            }
        }
        sessionDescription.setMedias(mediaDescriptions);
        
//		ArrayList<MediaDescription> mediaDescriptions = new ArrayList<MediaDescription>();
//		sessionDescription.setMedias(mediaDescriptions);
//		do {
//            
//			if (line.length() < 3) {
//				return null;
//			}
//            
//			if (line.charAt(0) != RFC4566.TYPE_MEDIA
//					|| line.charAt(1) != RFC4566.SEPARATOR) {
//				return null;
//			}
//			String[] mediaArr = line.split(" ");
//			if (mediaArr == null || mediaArr.length < 4) {
//				return null;
//			}
//			MediaDescription mediaDescription = new MediaDescription();
//			//TODO manage port range
//			mediaDescription.setPort(Integer.parseInt(mediaArr[1]));
//			mediaDescriptions.add(mediaDescription);
//			Hashtable<String, String> attributes = new Hashtable<String, String>();
//			mediaDescription.setAttributes(attributes);
//            
//            //FIXME:
//            //m=
//            //m=
//            //a=
//            
//			while ((line = reader.readLine()) != null
//					&& line.charAt(0) != RFC4566.TYPE_MEDIA) {
//				if (line.length() < 3) {
//                    return null;
//                }
//                if (line.charAt(0) == RFC4566.TYPE_ATTRIBUTE
//						&& line.charAt(1) == RFC4566.SEPARATOR) {
//					line = line.substring(2);
//					int columnPos = line.indexOf(RFC4566.ATTR_SEPARATOR);
//					if (columnPos > 0 && line.length() > columnPos + 1) {
//                        attributes.put(line.substring(0, columnPos),
//                                line.substring(columnPos + 1));
//					} else {
//						attributes.put(line, "");
//					}
//				} else if (line.charAt(0) == RFC4566.TYPE_CONNECTION
//                        && line.charAt(1) == RFC4566.SEPARATOR) {
//                    String connection = parseConnection(line.substring(2));
//                    if (connection != null) {
//                        mediaDescription.setIpAddress(InetAddress.getByName(connection));
//                    }
//                }
//			}
//		} while ((line = reader.readLine()) != null);
        for (MediaDescription description : mediaDescriptions) {
            if (description.getIpAddress() == null) {
                InetAddress sessionAddress = sessionDescription.getIpAddress();
                if (sessionAddress == null) {
                    return null;
                }
                description.setIpAddress(sessionAddress);
            }
        }
		return sessionDescription;
	}
	
	private String parseConnection(String line) {
		String[] connectionArr = line.split(" ");
		if (connectionArr == null || connectionArr.length != 3) {
			return null;
		}
		return connectionArr[2];
	}
	
    private void parseAttribute(String value, Hashtable<String, String> attributes) {
        int colonPos = value.indexOf(RFC4566.ATTR_SEPARATOR);
        if (colonPos > -1) {
            //warning:
            //m=
            //a=test:     (colon in last position in line)
            attributes.put(value.substring(0, colonPos),
                    value.substring(colonPos + 1));
        } else {
            attributes.put(value, "");
        }
    }
}
