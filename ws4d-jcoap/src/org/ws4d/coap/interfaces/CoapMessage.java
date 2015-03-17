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
	 * 
	 */
	public static final int RESPONSE_TIMEOUT_MS = 2000;
	
	/**
	 * 
	 */
	public static final double RESPONSE_RANDOM_FACTOR = 1.5;
	
	/**
	 * 
	 */
	public static final int MAX_RETRANSMIT = 4;

	/* TODO: what is the right value? */
	/**
	 * 
	 */
	public static final int ACK_RST_RETRANS_TIMEOUT_MS = 120000;

	/**
	 * returns the value of the internal message code in case of an error this
	 * function returns -1
	 */
	public int getMessageCodeValue();

	/**
	 * 
	 * @return
	 */
	public int getMessageID();

	/**
	 * 
	 * @param msgID
	 */
	public void setMessageID(int msgID);

	/**
	 * 
	 * @return
	 */
	public byte[] serialize();

	/**
	 * 
	 */
	public void incRetransCounterAndTimeout();

	/**
	 * 
	 * @return
	 */
	public CoapPacketType getPacketType();

	/**
	 * 
	 * @return
	 */
	public byte[] getPayload();

	/**
	 * 
	 * @param payload
	 */
	public void setPayload(byte[] payload);

	/**
	 * 
	 * @param payload
	 */
	public void setPayload(char[] payload);

	/**
	 * 
	 * @param payload
	 */
	public void setPayload(String payload);

	/**
	 * 
	 * @return
	 */
	public int getPayloadLength();

	/**
	 * 
	 * @param mediaType
	 */
	public void setContentType(CoapMediaType mediaType);

	/**
	 * 
	 * @return
	 */
	public CoapMediaType getContentType();

	/**
	 * 
	 * @param token
	 */
	public void setToken(byte[] token);

	/**
	 * 
	 * @return
	 */
	public byte[] getToken();

	// public URI getRequestUri();
	// public void setRequestUri(URI uri); 
	//TODO:allow this method only for Clients, Define Token Type

	/**
	 * 
	 * @return
	 */
	CoapBlockOption getBlock1();

	/**
	 * 
	 * @param blockOption
	 */
	void setBlock1(CoapBlockOption blockOption);

	/**
	 * 
	 * @return
	 */
	CoapBlockOption getBlock2();

	/**
	 * 
	 * @param blockOption
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

	/**
	 * 
	 * @return
	 */
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
	/**
	 * 
	 * @return
	 */
	public int hashCode();

	/**
	 * 
	 * @param obj
	 * @return
	 */
	public boolean equals(Object obj);

}
