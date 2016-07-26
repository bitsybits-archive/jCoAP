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

package org.ws4d.coap.core.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ws4d.coap.core.connection.api.CoapChannel;
import org.ws4d.coap.core.connection.api.CoapServerChannel;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapResponseCode;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;
import org.ws4d.coap.core.rest.api.CoapResource;
import org.ws4d.coap.core.rest.api.ResourceServer;
import org.ws4d.coap.core.tools.Encoder;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Björn Butzin <bjoern.butzin@uni-rostock.de>
 */
public class BasicCoapResource implements CoapResource {
	private static final Logger logger = LogManager.getLogger();
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

	public BasicCoapResource(String path, byte[] value, CoapMediaType mediaType) {
		init(path, value, mediaType);
	}

	private void init(String initPath, byte[] initValue, CoapMediaType initMediaType) throws IllegalArgumentException {
		String[] segments = initPath.trim().split("/");
		for (String segment : segments) {
			if (segment.getBytes().length > 255) {
				IllegalArgumentException e = new IllegalArgumentException("Uri-Path too long");
				logger.info("Uri-Path too long: " + initPath, e);
				throw e;
			}
		}
		this.path = initPath;
		this.value = initValue;
		this.mediaType = initMediaType;
	}

	public synchronized CoapResource setCoapMediaType(CoapMediaType mediaType) {
		this.mediaType = mediaType;
		return this;
	}

	public synchronized String getPath() {
		return this.path;
	}

	public synchronized String getShortName() {
		String[] segments = getPath().split("/");
		return segments[segments.length - 1];
	}

	/**
	 * Sets the value of the resource. Be aware to take care about the right
	 * encoding!
	 * 
	 * @param value
	 *            the value to be set
	 * @return true if and only if the value was changed
	 */
	public synchronized boolean setValue(String value) {
		this.value = Encoder.StringToByte(value);
		this.changed();
		return true;
	}

	public synchronized boolean setValue(byte[] value) {
		this.value = value;
		this.changed();
		return true;
	}

	/**
	 * @param reliableNotification
	 *            NULL = let the client decide
	 */
	public synchronized CoapResource setReliableNotification(Boolean reliableNotification) {
		this.reliableNotification = reliableNotification;
		return this;
	}

	public synchronized Boolean getReliableNotification() {
		return this.reliableNotification;
	}

	public synchronized void changed() {
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
			CoapData content = this.get(obsRequest.getAccept());
			response.setPayload(content.getPayload());
			response.setContentType(content.getMediaType());
			channel.sendNotification(response);
		}
	}

	public synchronized void registerServerListener(ResourceServer server) {
		this.serverListener = server;
	}

	public synchronized void unregisterServerListener(ResourceServer server) {
		this.serverListener = null;
	}

	public synchronized boolean addObserver(CoapRequest request) {
		this.observer.put(request.getChannel(), request);
		return true;
	}

	public synchronized void removeObserver(CoapChannel channel) {
		this.observer.remove(channel);
	}

	public synchronized int getObserveSequenceNumber() {
		return this.observeSequenceNumber;
	}

	public synchronized BasicCoapResource setExpires(long expires) {
		this.expires = expires;
		return this;
	}

	public synchronized long expires() {
		return this.expires;
	}

	public synchronized boolean isExpired() {
		if (this.expires == -1 || this.expires > System.currentTimeMillis()) {
			return false;
		}
		return true;
	}

	public synchronized BasicCoapResource setReadable(boolean readable) {
		this.readable = readable;
		return this;
	}

	public synchronized boolean isReadable() {
		return this.readable;
	}

	public synchronized BasicCoapResource setPostable(boolean postable) {
		this.postable = postable;
		return this;
	}

	public synchronized BasicCoapResource setPutable(boolean putable) {
		this.putable = putable;
		return this;
	}

	public synchronized BasicCoapResource setObservable(boolean observable) {
		this.observable = observable;
		return this;
	}

	public synchronized boolean isObservable() {
		return this.observable;
	}

	public synchronized BasicCoapResource setDeletable(boolean deletable) {
		this.deletable = deletable;
		return this;
	}

	public synchronized boolean isDeletable() {
		return this.deletable;
	}

	public synchronized BasicCoapResource setResourceType(String resourceType) {
		this.resourceType = resourceType;
		return this;
	}

	public synchronized String getResourceType() {
		return this.resourceType;
	}

	public synchronized BasicCoapResource setInterfaceDescription(String interfaceDescription) {
		this.interfaceDescription = interfaceDescription;
		return this;
	}

	public synchronized String getInterfaceDescription() {
		return this.interfaceDescription;
	}

	public synchronized int getSizeEstimate() {
		return this.value.length;
	}

	public synchronized boolean isPostable() {
		return this.postable;
	}

	public synchronized boolean isPutable() {
		return this.putable;
	}

	public synchronized CoapData get(List<CoapMediaType> mediaTypesAccepted) {
		return new CoapData(this.value, this.mediaType);
	}

	public synchronized CoapData get(List<String> query, List<CoapMediaType> mediaTypesAccepted) {
		return new CoapData(this.value, this.mediaType);
	}

	public synchronized boolean post(byte[] data, CoapMediaType type) {
		if (type == this.mediaType) {
			byte[] c = new byte[this.value.length + data.length];
			System.arraycopy(this.value, 0, c, 0, this.value.length);
			System.arraycopy(data, 0, c, this.value.length, data.length);
			this.value = c;
			return true;
		}
		return false;
	}

	public synchronized boolean put(byte[] data, CoapMediaType type) {
		if (this.mediaType == type) {
			return this.setValue(data);
		}
		return false;
	}

	public synchronized Set<CoapMediaType> getAvailableMediaTypes() {
		Set<CoapMediaType> mediatypes = new HashSet<CoapMediaType>();
		mediatypes.add(this.mediaType);
		return mediatypes;
	}

	public synchronized void delete() {
		// nothing has to be done to delete this resource.
	}
}