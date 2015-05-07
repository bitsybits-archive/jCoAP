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

package org.ws4d.coap.interfaces;

import org.ws4d.coap.messages.AbstractCoapMessage.CoapHeaderOptionType;
import org.ws4d.coap.messages.CoapBlockOption;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapPacketType;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface CoapMessage {

	/**
	 * The number of milliseconds before a timeout for an ACK is indicated <br>
	 * See rfc7252 - 4.8. "Transmission Parameters" for further details.
	 */
	public static final int RESPONSE_TIMEOUT_MS = 2000;

	/**
	 * For a new confirmable message, the initial timeout is set to a random
	 * duration (often not an integral number of seconds) between ACK_TIMEOUT
	 * and (ACK_TIMEOUT * ACK_RANDOM_FACTOR)
	 * See rfc7252 - 4.8. "Transmission Parameters" for further details.
	 */
	public static final double RESPONSE_RANDOM_FACTOR = 1.5;

	/**
	 * The maximum number of retransmits
	 * See rfc7252 - 4.8. "Transmission Parameters" for further details.
	 */
	public static final int MAX_RETRANSMIT = 4;

	/* TODO: what is the right value? */
	//TODO: Documentation
	/**
	 * 
	 */
	public static final int ACK_RST_RETRANS_TIMEOUT_MS = 120000;

	/**
	 * @return Value of the internal message code.<br>
	 *         -1, in case of an error.
	 */
	public int getMessageCodeValue();

	/**
	 * @return The ID of the message.
	 */
	public int getMessageID();

	/**
	 * 
	 * @param msgID
	 *            - The ID of the Message to be set.
	 */
	public void setMessageID(int msgID);

	/**
	 * Convert the CoAP message into its serialized form for transmission.<br>
	 * See rfc7252 - 3.  "Message Format" for further details.
	 * @return The serialized CoAP message.
	 */
	public byte[] serialize();

	//TODO: Documentation
	/**
	 * 
	 */
	public void incRetransCounterAndTimeout();

	/**
	 * @return The packet type of the message (CON, NON, ACK, RST).
	 */
	public CoapPacketType getPacketType();

	/**
	 * Get the payload of the message for further use on application level.
	 * @return The payload of the message
	 */
	public byte[] getPayload();

	/**
	 * Set the payload of the message to be sent.
	 * @param payload the payload of the message to be sent.
	 */
	public void setPayload(byte[] payload);

	/**
	 * Set the payload of the message to be sent.
	 * @param payload the payload of the message to be sent.
	 */
	public void setPayload(char[] payload);

	/**
	 * Set the payload of the message to be sent.
	 * @param payload the payload of the message to be sent.
	 */
	public void setPayload(String payload);

	/**
	 * @return The size of the message payload in byte. 
	 */
	public int getPayloadLength();

	/**
	 * Change the media type of the message.
	 * @param mediaType The new media type.
	 */
	public void setContentType(CoapMediaType mediaType);

	/**
	 * @return The media type of the message.
	 */
	public CoapMediaType getContentType();

	/**
	 * Set the token of the message.
	 * The token value is used to correlate requests and responses.
	 * @param token The token to be set
	 */
	public void setToken(byte[] token);

	/**
	 * The token value is used to correlate requests and responses.
	 * @return The token of the message.
	 */
	public byte[] getToken();

	// public URI getRequestUri();
	// public void setRequestUri(URI uri);
	// TODO:allow this method only for Clients, Define Token Type

	/**
	 * @return The block option for get requests.
	 */
	CoapBlockOption getBlock1();

	/**
	 * @param blockOption The block option for get requests.
	 */
	void setBlock1(CoapBlockOption blockOption);

	/**
	 * @return The block option for POST & PUT requests.
	 */
	CoapBlockOption getBlock2();

	/**
	 * @param blockOption The block option for POST & PUT requests.
	 */
	void setBlock2(CoapBlockOption blockOption);

	/**
	 * 
	 * @return
	 */
	public Integer getObserveOption();

	/**
	 * 
	 * @param sequenceNumber
	 */
	public void setObserveOption(int sequenceNumber);

	// TODO: could this compromise the internal state?
	/**
	 * 
	 * @param optionType
	 */
	public void removeOption(CoapHeaderOptionType optionType);

	@Override
	public String toString();

	/**
	 * 
	 * @return
	 */
	public CoapChannel getChannel();

	/**
	 * 
	 * @param channel
	 */
	public void setChannel(CoapChannel channel);

	/**
	 * 
	 * @return
	 */
	public int getTimeout();

	/**
	 * 
	 * @return
	 */
	public boolean maxRetransReached();

	/**
	 * 
	 * @return
	 */
	public boolean isReliable();

	/**
	 * 
	 * @return
	 */
	public boolean isRequest();

	/**
	 * 
	 * @return
	 */
	public boolean isResponse();

	/**
	 * 
	 * @return
	 */
	public boolean isEmpty();

	/* unique by remote address, remote port, local port and message id */
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

}
