/* Copyright 2011 University of Rostock
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

package org.ws4d.coap;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public final class Constants {
	
	/**
	 * The minimal possible ID of a message. 
	 * A CoAP Message ID must always be higher or equal.
	 */
	public final static int MESSAGE_ID_MIN = 0;
	
	/**
	 * The maximal possible id of a message. 
	 * A CoAP Message ID must always be lower or equal.
	 */
	public final static int MESSAGE_ID_MAX = 65535;
	
	/**
	 * The maximal size of a CoAP Message in ?Bytes?
	 */
	// TODO: which unit?
	public final static int COAP_MESSAGE_SIZE_MAX = 1152;
	
	/**
	 * The default port used by CoAP connections.
	 */
	public final static int COAP_DEFAULT_PORT = 5683;
	
	/**
	 * 
	 */
	public final static int COAP_DEFAULT_MAX_AGE_S = 60;
	
	/**
	 * 
	 */
	public final static int COAP_DEFAULT_MAX_AGE_MS = COAP_DEFAULT_MAX_AGE_S * 1000;
}
