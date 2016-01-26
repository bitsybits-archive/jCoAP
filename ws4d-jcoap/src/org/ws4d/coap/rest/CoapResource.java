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

package org.ws4d.coap.rest;

import java.util.List;

import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.messages.CoapMediaType;

/**
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface CoapResource{

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
	 */
	public void removeObserver(CoapChannel channel);

	/**
	 * @return The last sequence number used for notification. Will not be
	 *         greater than 0xFFFF (2 byte integer)
	 */
	public int getObserveSequenceNumber();

	/**
	 * @return The Unix time (in milliseconds), when the resource expires. -1,
	 *         when the resource never expires.
	 */
	public long expires();

	/**
	 * @return true if and only if the resource is expired
	 */
	public boolean isExpired();

	/* ------------------------------------------------------------------*/
    /**
     * Get the MIME Type of the resource (e.g., "application/xml")
     * @return The MIME Type of this resource as String.
     */
    public String getMimeType();

    /**
     * Get the unique name of this resource
     * @return The unique name of the resource.
     */
    public String getPath();

    public String getShortName();
    
    public boolean setValue(byte[] value);

    public byte[] getValue();
    
	public byte[] getValue(List<String> query);
    
    //TODO: bad api: no return value
    public boolean post(byte[] data);
    
	public void registerServerListener(ResourceServer server);
	
	public void unregisterServerListener(ResourceServer server);
	/**
	 * @return True, if and only if the resource is readable.
	 */
	public boolean isReadable();

	/**
	 * @return True, if and only if the resource is writable.
	 */
	public boolean isWriteable();

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
	
	
    public String getResourceType();
    
    public String getInterfaceDescription();
    
    public int getSizeEstimate();
}
