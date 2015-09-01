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

import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.messages.CoapMediaType;

/**
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface CoapResource extends Resource {
	/**
	 * @return the CoAP Media Type
	 */
	public CoapMediaType getCoapMediaType();

	/**
	 * called by the application, when the resource state changed -> used for
	 * observation
	 */
	public void changed();

	/**
	 * called by the server to register a new observer
	 * @param request
	 * @return false if resource is not observable
	 */
	public boolean addObserver(CoapRequest request);

	/**
	 * removes an observer from the list
	 * @param channel
	 */
	public void removeObserver(CoapChannel channel);

	/**
	 * @return true, if the resource is observable.
	 */
	public boolean isObservable();
	
	/**
	 * @return true, if the resource is deletable.
	 */
	public boolean isDeletable();

	/**
	 * if the resource is observable
	 * @return
	 */
	public int getObserveSequenceNumber();

	/**
	 * @return The Unix time (in milliseconds), when resource expires. <br>
	 * -1, when the resource never expires.
	 */
	public long expires();

	/**
	 * @return true, when the resource is expired
	 */
	public boolean isExpired();
}
