/* Copyright 2016 University of Rostock
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

package org.ws4d.coap.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.interfaces.CoapServerChannel;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapResponseCode;
import org.ws4d.coap.tools.Encoder;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Björn Butzin <bjoern.butzin@uni-rostock.de>
 */
public class BasicCoapResource implements CoapResource {
	/* use the logger of the resource server */
	private final static Logger logger = Logger.getLogger(CoapResourceServer.class);
	private ResourceServer serverListener = null; // could be a list of listener

	// parameter
	private String resourceType = null;
	private String interfaceDescription = null;
	private CoapMediaType mediaType;
	private String path;
	private byte[] value;
	private long expires = -1; // -1 = never expires

	// permissions
	private boolean readable = true;
	private boolean postable = true;
	private boolean putable = true;
	private boolean observable = true;
	private boolean deletable = true;

	// observe
	private Map<CoapChannel, CoapRequest> observer = new HashMap<CoapChannel, CoapRequest>();
	/** MUST NOT be greater than 0xFFFF (2 byte integer) **/
	private int observeSequenceNumber = 0;
	/** DEFAULT NULL: let the client decide **/
	private Boolean reliableNotification = null;

	public BasicCoapResource(String path, String value, CoapMediaType mediaType) {
		init(path, Encoder.StringToByte(value), mediaType);
	}
	
	public BasicCoapResource(String path, byte[] value, CoapMediaType mediaType){
		init(path, value, mediaType);
	}
	
	private void init(String initPath, byte[] initValue, CoapMediaType initMediaType)throws IllegalArgumentException{
		String[] segments = initPath.trim().split("/");
		for (String segment : segments) {
			if (segment.getBytes().length > 255) {
				IllegalArgumentException e = new IllegalArgumentException("Uri-Path too long");
				logger.info("Uri-Path too long: "+initPath, e);
				throw e;
			}
		}
		this.path = initPath;
		this.value = initValue;
		this.mediaType = initMediaType;
	}

	public BasicCoapResource setCoapMediaType(CoapMediaType mediaType) {
		this.mediaType = mediaType;
		return this;
	}

	public CoapMediaType getCoapMediaType() {
		return this.mediaType;
	}

	public String getMimeType() {
		return this.mediaType.getMimeType();
	}

	public String getPath() {
		return this.path;
	}

	public String getShortName() {
		return getPath();
	}

	/**
	 * Sets the value of the resource. Be aware to take care about the right
	 * encoding!
	 * 
	 * @param value
	 *            the value to be set
	 * @return true if and only if the value was changed
	 */
	public boolean setValue(String value) {
		this.value = Encoder.StringToByte(value);
		this.changed();
		return true;
	}
	
	public boolean setValue(byte[] value) {
		this.value = value;
		this.changed();
		return true;
	}

	public byte[] getValue() {
		return this.value;
	}

	public byte[] getValue(List<String> query) {
		// query string is ignored by intention
		return this.value;
	}
	
	public String getStringValue() {
		return Encoder.ByteToString(this.value);
	}

	public String getStringValue(@SuppressWarnings("unused") List<String> query) {
		// query string is ignored by intention
		return Encoder.ByteToString(this.value);
	}

	/**
	 * @param reliableNotification
	 * NULL = let the client decide
	 */
	public BasicCoapResource setReliableNotification(Boolean reliableNotification) {
		this.reliableNotification = reliableNotification;
		return this;
	}

	public Boolean getReliableNotification() {
		return this.reliableNotification;
	}

	@Override
	public String toString() {
		return getPath()+"\n"+Encoder.ByteToString(getValue()); 
	}

	public boolean post(byte[] data) {
		byte[] c = new byte[this.value.length + data.length];
		System.arraycopy(this.value, 0, c, 0, this.value.length);
		System.arraycopy(data, 0, c, this.value.length, data.length);
		this.value = c;
		return true;
	}

	public void changed() {
		if (this.serverListener != null) {
			this.serverListener.resourceChanged(this);
		}
		this.observeSequenceNumber++;
		if (this.observeSequenceNumber > 0xFFFF) {
			this.observeSequenceNumber = 0;
		}

		// notify all observers
		for (CoapRequest obsRequest : this.observer.values()) {
			CoapServerChannel channel = (CoapServerChannel) obsRequest.getChannel();
			CoapResponse response;
			if (this.reliableNotification == null) {
				response = channel.createNotification(obsRequest, CoapResponseCode.Content_205,
						this.observeSequenceNumber);
			} else {
				response = channel.createNotification(obsRequest, CoapResponseCode.Content_205,
						this.observeSequenceNumber, this.reliableNotification);
			}
			response.setPayload(this.getValue());
			channel.sendNotification(response);
		}
	}

	public void registerServerListener(ResourceServer server) {
		this.serverListener = server;
	}

	public void unregisterServerListener(ResourceServer server) {
		this.serverListener = null;
	}

	public boolean addObserver(CoapRequest request) {
		this.observer.put(request.getChannel(), request);
		return true;
	}

	public void removeObserver(CoapChannel channel) {
		this.observer.remove(channel);
	}

	public int getObserveSequenceNumber() {
		return this.observeSequenceNumber;
	}

	public BasicCoapResource setExpires(long expires) {
		this.expires = expires;
		return this;
	}

	public long expires() {
		return this.expires;
	}

	public boolean isExpired() {
		if (this.expires == -1 || this.expires > System.currentTimeMillis()) {
			return false;
		}
		return true;
	}

	public BasicCoapResource setReadable(boolean readable) {
		this.readable = readable;
		return this;
	}

	public boolean isReadable() {
		return this.readable;
	}

	public BasicCoapResource setPostable(boolean postable) {
		this.postable = postable;
		return this;
	}
	
	public BasicCoapResource setPutable(boolean putable) {
		this.putable = putable;
		return this;
	}

	public BasicCoapResource setObservable(boolean observable) {
		this.observable = observable;
		return this;
	}

	public boolean isObservable() {
		return this.observable;
	}

	public BasicCoapResource setDeletable(boolean deletable) {
		this.deletable = deletable;
		return this;
	}

	public boolean isDeletable() {
		return this.deletable;
	}

	public BasicCoapResource setResourceType(String resourceType) {
		this.resourceType = resourceType;
		return this;
	}

	public String getResourceType() {
		return this.resourceType;
	}

	public BasicCoapResource setInterfaceDescription(String interfaceDescription) {
		this.interfaceDescription = interfaceDescription;
		return this;
	}

	public String getInterfaceDescription() {
		return this.interfaceDescription;
	}

	public int getSizeEstimate() {
		return getValue().length;
	}

	public boolean isPostable() {
		return this.postable;
	}

	public boolean isPutable() {
		return this.putable;
	}
}