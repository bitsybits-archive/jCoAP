/* Copyright 2015 University of Rostock
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

package org.ws4d.coap.messages;

/**
 * This ENUM defines a subset of Internet media types to be used in CoAP. <br>
 * See rfc7252 - 12.3. "CoAP Content-Formats Registry" for further details.
 * 
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public enum CoapMediaType {
	text_plain(0), 		// text/plain; charset=utf-8
	link_format(40), 	// application/link-format
	xml(41), 			// application/xml
	octet_stream(42), 	// application/octet-stream
	exi(47), 			// application/exi
	json(50), 			// application/json
	UNKNOWN(-1);

	int mediaType;

	private CoapMediaType(int mediaType) {
		this.mediaType = mediaType;
	}

	/**
	 * @param mediaType
	 *            The media type code of the media type.
	 * @return The ENUM element matching the media type code. <br>
	 *         UNKNOWN, if the media type code is not known.
	 */
	public static CoapMediaType parse(int mediaType) {
		switch (mediaType) {
		case 0:
			return text_plain;
		case 40:
			return link_format;
		case 41:
			return xml;
		case 42:
			return octet_stream;
		case 47:
			return exi;
		case 50:
			return json;
		default:
			return UNKNOWN;
		}
	}

	/**
	 * @return The media type code of the ENUM element.
	 */
	public int getValue() {
		return mediaType;
	}
}