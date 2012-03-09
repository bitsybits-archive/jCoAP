/* Copyright [2011] [University of Rostock]
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

/* WS4D Java CoAP Implementation
 * (c) 2011 WS4D.org
 * 
 * written by Sebastian Unger 
 */

package org.ws4d.coap.messages;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapMessage;

public abstract class AbstractCoapMessage implements CoapMessage {
	protected static final int HEADER_LENGTH = 4;
	
	/* Header */
	protected int version;
	protected CoapPacketType packetType;
	protected int messageCodeValue;
	protected int optionCount; 
	protected int messageId;
    
    /* Options */
    protected CoapHeaderOptions options;

    /* Payload */
    protected byte[] payload = null;
    protected int payloadLength = 0;

    /* corresponding channel */
    CoapChannel channel = null;
    
    /* Retransmission State */
    int timeout = 0;
    int retransmissionCounter = 0;

    protected void serialize(byte[] bytes, int length, int offset){
    	/* check length to avoid buffer overflow exceptions */
    	this.version = 1; 
        this.packetType = (CoapPacketType.getPacketType((bytes[offset + 0] & 0x30) >> 4)); 
        this.optionCount = bytes[offset + 0] & 0x0F;
        this.messageCodeValue = (bytes[offset + 1] & 0xFF);
        this.messageId = ((bytes[offset + 2] << 8) & 0xFF00) + (bytes[offset + 3] & 0xFF);		
		
        /* serialize options */
        this.options = new CoapHeaderOptions(bytes, offset + HEADER_LENGTH, optionCount);
		
        /* get and check payload length */
        payloadLength = length - HEADER_LENGTH - options.getDeserializedLength();
		if (payloadLength < 0){
			throw new IllegalStateException("Invaldid CoAP Message (payload length negative)");
		}
		
		/* copy payload */
		int payloadOffset = offset + HEADER_LENGTH + options.getDeserializedLength();
		payload = new byte[payloadLength];
		for (int i = 0; i < payloadLength; i++){
			payload[i] = bytes[i + payloadOffset];
		}
    }
    
    
    public static CoapMessage parseMessage(byte[] bytes, int length){
    	return parseMessage(bytes, length, 0);
    }
    
    public static CoapMessage parseMessage(byte[] bytes, int length, int offset){
    	/* we "peek" the header to determine the kind of message 
    	 * TODO: duplicate Code */
    	int messageCodeValue = (bytes[offset + 1] & 0xFF);
    	
    	if (messageCodeValue == 0){
    		return new CoapEmptyMessage(bytes, length, offset);
    	} else if (messageCodeValue >= 0 && messageCodeValue <= 31 ){
    		return new CoapRequest(bytes, length, offset);
    	} else if (messageCodeValue >= 64 && messageCodeValue <= 191){
    		return new CoapResponse(bytes, length, offset);
    	} else {
    		throw new IllegalArgumentException("unknown CoAP message");
    	}
    }
    
    
    @Override
	public boolean isEmptyMessage() {
		if (messageCodeValue == 0){
			return true;
		} else {
			return false;
		}
	}

    public int getVersion() {
		return version;
	}
    
	@Override
	public int getMessageCodeValue() {
		return messageCodeValue;
	}

	@Override
    public CoapPacketType getPacketType() {
        return packetType;
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    @Override
    public int getMessageID() {
        return messageId;
    }
    
	@Override
	public void setMessageID(int messageId) {
		this.messageId = messageId;
	}
    

    public byte[] serialize() {
    	/* TODO improve memory allocation */

    	/* serialize header options first to get the length*/
        int optionsLength = 0;
        byte[] optionsArray = null;
        if (options != null) {
            optionsArray = this.options.serialize();
            optionsLength = this.options.getSerializedLength();
        }
        
        /* allocate memory for the complete packet */
        int length = HEADER_LENGTH + optionsLength + payloadLength;
        byte[] serializedPacket = new byte[length];
        
        /* serialize header */
        serializedPacket[0] = (byte) ((this.version & 0x03) << 6);
        serializedPacket[0] |= (byte) ((this.packetType.getValue() & 0x03) << 4);
        serializedPacket[0] |= (byte) (options.getOptionCount() & 0x0F);
        serializedPacket[1] = (byte) (this.getMessageCodeValue() & 0xFF);
        serializedPacket[2] = (byte) ((this.messageId >> 8) & 0xFF);
        serializedPacket[3] = (byte) (this.messageId & 0xFF);

        /* copy serialized options to the final array */
        int offset = HEADER_LENGTH;
        if (options != null) {
            for (int i = 0; i < optionsLength; i++)
                serializedPacket[i + offset] = optionsArray[i];
        }
        
        /* copy payload to the final array */
        offset = HEADER_LENGTH + optionsLength; 
        for (int i = 0; i < this.payloadLength; i++) {
        	serializedPacket[i + offset] = payload[i];
        }

        return serializedPacket;
    }
    
    /* TODO: check payload implementation */
    public void setPayload(byte[] payload) {
        this.payload = payload;
        if (payload!=null)
            this.payloadLength = payload.length;
        else
            this.payloadLength = 0;
    }

    public void setPayload(char[] payload) {
        this.payload = new byte[payload.length];
        for (int i = 0; i < payload.length; i++) {
            this.payload[i] = (byte) payload[i];
        }
        this.payloadLength = payload.length;
    }

    public void setPayload(String payload) {
        setPayload(payload.toCharArray());
    }

    @Override
	public String getUriPath() {
		StringBuilder uriPathBuilder = new StringBuilder();
		for (CoapHeaderOption coapHeaderOption : options) {
			if (coapHeaderOption.getOptionNumber() == CoapHeaderOptionType.Uri_Path.getValue()) {
				String uriPathElement;
				try {
					uriPathElement = new String(coapHeaderOption.getOptionValue(), "UTF-8");
					uriPathBuilder.append("/");
					uriPathBuilder.append(uriPathElement);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return uriPathBuilder.toString();

	}

	@Override
	public CoapChannel getCoapChannel() {
	    return channel;
	}

	@Override
	public void setChannel(CoapChannel channel) {
	    this.channel = channel;
	}

	@Override
    public int getTimeout() {
        if (timeout == 0) {
            Random random = new Random();
            timeout = RESPONSE_TIMEOUT_MS
                    + random.nextInt((int) (RESPONSE_TIMEOUT_MS * RESPONSE_RANDOM_FACTOR)
                            - RESPONSE_TIMEOUT_MS);
        }
        return timeout;
    }

    @Override
    public boolean maxRetransReached() {
        if (retransmissionCounter < MAX_RETRANSMIT) {
            return false;
        }
        return true;
    }

    @Override 
    public void incRetransCounterAndTimeout() { /*TODO: Rename*/
        retransmissionCounter += 1;
        timeout *= 2;
    }

    @Override
	public String toString() {
    	/* TODO implement */
    	return "toString not implemented yet"; 
	}

	@Override
	public boolean isReliable() {
		if (packetType == CoapPacketType.NON){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + getMessageID();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractCoapMessage other = (AbstractCoapMessage) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (getMessageID() != other.getMessageID())
			return false;
		return true;
	}
	
	
	/*TODO use enum*/
	public enum CoapHeaderOptionType {
	    UNKNOWN(-1),
	    Content_Type (1),
	    Max_Age (2),
	    Proxy_Uri(3),
	    Etag (4),
	    Uri_Host (5),
	    Location_Path (6),
	    Uri_Port (7),
	    Location_Query (8),
	    Uri_Path (9),
	    Token (11),
	    Accept (12),
	    If_Match (13),
	    Uri_Query (15),
	    If_None_Match (21);
	    
	    int value; 
	    
	    CoapHeaderOptionType(int optionValue){
	    	value = optionValue;
	    }
	    
	    public int getValue(){
	    	return value;
	    }
	    /* TODO: implement validity checks */
	    /*TODO: implement isCritical(), isElective()*/
	    
	
	}

	protected class CoapHeaderOption implements Comparable<CoapHeaderOption> {
	
	    int optionNumber = CoapHeaderOptionType.UNKNOWN.getValue();
	    byte[] optionValue = null;
	    int shortLength = 0;
	    int longLength = 0;
	
	    public CoapHeaderOption() {
	    }
	    
	    public CoapHeaderOption(int optionNumber, byte[] value) {
	        this.optionNumber = optionNumber;
	        this.optionValue = value;
	        if (value.length < 15) {
	            shortLength = value.length;
	            longLength = 0;
	        } else {
	            shortLength = 15;
	            longLength = value.length - shortLength;	
	        }
	    }
	
	    @Override
	    public int compareTo(CoapHeaderOption option) {
	        if (this.optionNumber != option.optionNumber)
	            return this.optionNumber < option.optionNumber ? -1 : 1;
	        else
	            return 0;
	    }
	
	    public int getLongLength() {
	        return longLength;
	    }
	    
	    public int getShortLength() {
	    	return shortLength;
	    }
	
	    public int getOptionNumber() {
	        return optionNumber;
	    }
	
	    public byte[] getOptionValue() {
	        return optionValue;
	    }
	
	    
	    
	    private void setLongLength(int l) {
	    	this.longLength = l;
	    }
	
	
	    private void setOptionNumber(int optionNumber) {
	        this.optionNumber = optionNumber;
	    }
	
	    private void setOptionValue(byte[] v) {
	        this.optionValue = v;
	    }
	
	    private void setShortLength(int l) {
	        this.shortLength = l;
	    }
	
	    @Override
	    public String toString() {
	        char[] printableOptionValue = new char[optionValue.length];
	        for (int i = 0; i < optionValue.length; i++)
	            printableOptionValue[i] = (char) optionValue[i];
	        return "Option Number: "
	                + " (" + optionNumber + ")"
	                + ", Option Value: " + String.copyValueOf(printableOptionValue);
	    }
	}

	protected class CoapHeaderOptions implements Iterable<CoapHeaderOption>{

		private Vector<CoapHeaderOption> headerOptions = new Vector<CoapHeaderOption>();
		private int deserializedLength = 0;
		private int serializedLength = 0;
		
		public CoapHeaderOptions(byte[] bytes, int option_count){
			this(bytes, option_count, option_count);
		}
		
		public CoapHeaderOptions(byte[] bytes, int offset, int option_count){
			/* note: we only receive deltas and never concrete numbers */
			/* TODO: check integrity (in case of an error raise an exception)*/
			int lastOptionNumber = 0;
			int arrayIndex = offset;
			for (int i = 0; i < option_count; i++) {
				CoapHeaderOption option = new CoapHeaderOption();
				/* Calculate Option Number from Delta */
				option.setOptionNumber(((bytes[arrayIndex] & 0xF0) >> 4) + lastOptionNumber);
				lastOptionNumber = option.getOptionNumber();
				deserializedLength += 1; /* keep track of length */
				
				/* Calculate length fields and real length */
				int tmpLength = 0;
				if ((bytes[arrayIndex] & 0x0F) < 15) {
					option.setShortLength(bytes[arrayIndex++] & 0x0F);
					option.setLongLength(0);
					tmpLength = option.getShortLength();
				} else {
					option.setShortLength(bytes[arrayIndex++] & 0x0F);
					option.setLongLength(bytes[arrayIndex++]);
					tmpLength = option.getLongLength() + 15; 
					deserializedLength += 1; /* additional length byte */
				}
				deserializedLength += tmpLength;
				/* TODO: allocate memory only once and work with lengths*/
				byte[] optionValue = new byte[tmpLength];
				for (int j = 0; j < tmpLength; j++)
					optionValue[j] = bytes[arrayIndex + j];
				option.setOptionValue(optionValue);
				arrayIndex += tmpLength;
				addOption(option);
			}
		}
		
		public CoapHeaderOptions() {
			/* creates empty header options */
		}
		
	    public CoapHeaderOption getOption(int optionNumber) {
			for (CoapHeaderOption headerOption : headerOptions) {
				if (headerOption.getOptionNumber() == optionNumber) {
					return headerOption;
				}
			}
			return null;
		}

		public void addOption(CoapHeaderOption option) {
	        headerOptions.add(option);
	        /*TODO: only sort when options are serialized*/
	        Collections.sort(headerOptions);
	    }

	    public void addOption(int optNumber, byte[] value) throws Exception {
	        headerOptions.add(new CoapHeaderOption(optNumber, value));
	    }
	    
	    public void removeOption(int optNumber){
			CoapHeaderOption headerOption;
			// get elements of Vector
			
			/* note: iterating and changing a vector at the same time is not allowed */
			int i = 0;
			while (i < headerOptions.size()){
				headerOption = headerOptions.get(i);
				if (headerOption.getOptionNumber() == optNumber) {
					headerOptions.remove(i);
				} else {
					/* only increase when no element was removed*/
					i++;
				}
			}
			Collections.sort(headerOptions);
	    }

	    public int getOptionCount() {
	        return headerOptions.size();
	    }

	    public byte[] serialize() {
	        int length = 0;

	        /* find length of options_string. Each option contains ... */
	        for (CoapHeaderOption option : headerOptions) {
	            ++length;
	            // ... sometimes an additional length-byte
	            if (option.getLongLength() > 0) {
	                ++length;
	            }
	            length += option.getOptionValue().length;
	        }

	        // TODO: don't allocate new memory every time serialize() is called
	        byte[] data = new byte[length];
	        int arrayIndex = 0;

	        int lastOptionNumber = 0; /* let's keep track of this */
	        for (CoapHeaderOption headerOption : headerOptions) {
	            int optionDelta = headerOption.getOptionNumber() - lastOptionNumber;
	            lastOptionNumber = headerOption.getOptionNumber();
	            // set length(s)
	            data[arrayIndex++] = (byte) (((optionDelta & 0x0F) << 4) | (headerOption
	                    .getShortLength() & 0x0F));
	            if (headerOption.getLongLength() > 0) {
	                data[arrayIndex++] = (byte) (headerOption.getLongLength() & 0xFF);
	            }
	            // copy option value
	            byte[] value = headerOption.getOptionValue();
	            for (int i = 0; i < value.length; i++) {
	                data[arrayIndex++] = value[i];
	            }
	        }
	        serializedLength = length;
	        return data;
	    }

	    public int getDeserializedLength(){
	    	return deserializedLength;
	    }
		
		public int getSerializedLength() {
			return serializedLength;
		}
		
	    @Override
	    public Iterator<CoapHeaderOption> iterator() {
	        return headerOptions.iterator();
	    }


		@Override
		public String toString() {
			String result = "\tOptions:\n";
			for (CoapHeaderOption option : headerOptions) {
				result += "\t\t" + option.toString() + "\n";
			}
			return result;
		}
	}

	
	
	
}
