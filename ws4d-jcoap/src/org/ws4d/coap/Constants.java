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
 * Defines some CoAP related constants.
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public final class Constants {

	/**
	 * The minimal possible ID of a CoAP message. <br>
	 * A CoAP Message ID must always be higher or equal this value. <br>
	 * The Message ID is a 16-bit unsigned integer. <br>
	 * See rfc7252 - 3. "Message Format" for further details.
	 */
	public final static int MESSAGE_ID_MIN = 0;

	/**
	 * The maximal possible id of a CoAP message. <br>
	 * A CoAP Message ID must always be lower or equal. <br>
	 * The Message ID is a 16-bit unsigned integer. <br>
	 * See rfc7252 - 3. "Message Format" for further details.
	 */
	public final static int MESSAGE_ID_MAX = 65535;

	/**
	 * The maximal size of a CoAP message in bytes <br>
	 * See rfc7252 - 4.6. "Message Size" for further details.
	 */
	public final static int COAP_MESSAGE_SIZE_MAX = 1152;

	/**
	 * The maximal size of a CoAP message payload in bytes <br>
	 * See rfc7252 - 4.6. "Message Size" for further details.
	 */
	public final static int COAP_PAYLOAD_SIZE_MAX = 1024;

	/**
	 * The default port used by CoAP connections. <br>
	 * See rfc7252 - 6.1. "coap URI Scheme" for further details.
	 */
	public final static int COAP_DEFAULT_PORT = 5683;
	
	/**
	 * The all CoAP nodes IPv4 multicast address.
	 */
	public final static String COAP_ALL_NODES_IPV4_MC_ADDR = "224.0.1.187";
	
	/**
	 * The all CoAP nodes link-local IPv6 multicast address.
	 */
	public final static String COAP_ALL_NODES_IPV6_LL_MC_ADDR = "ff02::fd";
	
	/**
	 * The all CoAP nodes site-local IPv6 multicast address.
	 */
	public final static String COAP_ALL_NODES_IPV6_SL_MC_ADDR = "ff05::fd";

	/**
	 * This is the default value (in seconds) for the Max-Age Option. <br>
	 * The Max-Age Option indicates the maximum time (in seconds) a response may
	 * be cached before it is considered not fresh. <br>
	 * See rfc7252 - 5.10.5. "Max-Age" for further details.
	 */
	public final static int COAP_DEFAULT_MAX_AGE_S = 60;

	/**
	 * This is the default value (in milliseconds) for the Max-Age Option. <br>
	 * The Max-Age Option indicates the maximum time (in milliseconds) a
	 * response may be cached before it is considered not fresh. <br>
	 * See rfc7252 - 5.10.5. "Max-Age" for further details.
	 */
	public final static int COAP_DEFAULT_MAX_AGE_MS = COAP_DEFAULT_MAX_AGE_S * 1000;
}
