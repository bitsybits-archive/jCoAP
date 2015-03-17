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
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public enum CoapRequestCode {
	GET(1), POST(2), PUT(3), DELETE(4);

	private int code;

	private CoapRequestCode(int code) {
		this.code = code;
	}

	public static CoapRequestCode parseRequestCode(int codeValue) {
		switch (codeValue) {
		case 1:
			return GET;
		case 2:
			return POST;
		case 3:
			return PUT;
		case 4:
			return DELETE;
		default:
			throw new IllegalArgumentException("Invalid Request Code");
		}
	}

	public int getValue() {
		return code;
	}

	@Override
	public String toString() {
		switch (this) {
		case GET:
			return "GET";
		case POST:
			return "POST";
		case PUT:
			return "PUT";
		case DELETE:
			return "DELETE";
		}
		return null;
	}
}
