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

import java.util.List;

import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.messages.CoapMediaType;

/**
 * A CoapResource takes care of the resources content, its permissions and observers.
 * In order to be served over a network connection it needs to be added to a {@link ResourceServer}
 * 
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Björn Butzin <bjoern.butzin@uni-rostock.de>
 */
public interface CoapResource {

	/**
	 * Can be called to inform the resource about changed content.
	 */
	public void changed();

	/**
	 * Adds an observer to this resource
	 * 
	 * @param request
	 *            - the client request to observe this resource
	 * @return False if and only if the observer can not be added. <br>
	 *         This might have several reasons e.g. that the resource is not
	 *         observable.
	 */
	public boolean addObserver(CoapRequest request);

	/**
	 * Removes an observer from this resource
	 * 
	 * @param channel
	 *            - the channel that should be removed from the observers list.
	 *            In most cases this will be the CoapRequest.getChannel() from
	 *            the clients request
	 */
	public void removeObserver(CoapChannel channel);

	/**
	 * @return The last sequence number used for notification. Will not be
	 *         greater than 0xFFFF (2 byte integer)
	 */
	public int getObserveSequenceNumber();

	/**
	 * @return The Unix time (in milliseconds), when the resource expires. -1,
	 *         if the resource never expires.
	 */
	public long expires();

	/**
	 * @return true if and only if the resource is expired
	 */
	public boolean isExpired();

	/**
	 * Get the MIME Type of the resource (e.g., "application/xml")
	 * 
	 * @return The MIME Type of this resource as String.
	 */
	public String getMimeType();

	/**
	 * Get the unique name of this resource
	 * 
	 * @return The unique name of the resource.
	 */
	public String getPath();

	/**
	 * Get the name of this resource. Might not be unique
	 * 
	 * @return The unique name of the resource.
	 */
	public String getShortName();

	/**
	 * Sets the value of the resource. Be aware to take care about the right
	 * encoding!
	 * 
	 * @param value
	 *            the value to be set
	 * @return true if and only if the value was changed
	 */
	public boolean setValue(byte[] value);

	/**
	 * Get the current value of the resource as byte[].
	 * 
	 * @return the current value
	 * @see {@link #getMimeType()} to get the encoding of the data
	 * @see {@link #getValue(List)} If you want to pass a query string public
	 */
	byte[] getValue();

	/**
	 * If can use this method to get the current value of the resource as byte[]
	 * with respect to query parameters.
	 * 
	 * @return the current value
	 * @see {@link #getMimeType()} to get the encoding of the data
	 */
	public byte[] getValue(List<String> query);

	/**
	 * Use this method to hand posted data to the resource. The behavior
	 * strongly depends on the resource itself.
	 * 
	 * @param data
	 *            - The data posted
	 * @return true if and only if the resource accepted the post and did the
	 *         respective changes
	 */
	public boolean post(byte[] data);

	/**
	 * Give the resource a callback to inform the resource server about changes.
	 * 
	 * @param server
	 *            - The resource server that handles this resource
	 */
	public void registerServerListener(ResourceServer server);

	/**
	 * Remove the callback handle to inform the resource server about changes.
	 * Changes of the resource will not be propagated to this resource server
	 * anymore.
	 * 
	 * @param server
	 *            - The resource server handle to be removed
	 */
	public void unregisterServerListener(ResourceServer server);

	/**
	 * @return True, if and only if the resource is readable.
	 */
	public boolean isReadable();

	/**
	 * @return True, if and only if the resource accepts post requests.
	 */
	public boolean isPostable();

	/**
	 * @return True, if and only if the resource accepts put requests.
	 */
	public boolean isPutable();

	/**
	 * @return True, if and only if the resource is observable.
	 */
	public boolean isObservable();

	/**
	 * @return True, if and only if the resource is delete-able.
	 */
	public boolean isDeletable();

	/**
	 * @return the CoAP Media Type of this resource
	 */
	public CoapMediaType getCoapMediaType();

	/**
	 * This method is used to get the resource type of this resource.
	 * 
	 * @return The string representing the resource type or null.
	 */
	public String getResourceType();

	/**
	 * This method is used to get the interface description of this resource.
	 * 
	 * @return The string representing the interface description or null.
	 */
	public String getInterfaceDescription();

	/**
	 * @return an integer value representing the size of the resources value
	 */
	public int getSizeEstimate();
}
