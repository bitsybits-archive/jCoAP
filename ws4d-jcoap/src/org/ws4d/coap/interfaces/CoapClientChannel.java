/* Copyright 2015 University of Rostock
 
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

import org.ws4d.coap.messages.CoapRequestCode;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface CoapClientChannel extends CoapChannel {
	
	/**
	 * 
	 * @param reliable
	 * @param requestCode
	 * @return
	 */
	public CoapRequest createRequest(boolean reliable,
			CoapRequestCode requestCode);

	/**
	 * 
	 * @param request
	 * @return
	 */
	public CoapRequest addBlockContext(CoapRequest request);

	/**
	 * 
	 * @param o
	 */
	public void setTrigger(Object o);

	/**
	 * 
	 * @return
	 */
	public Object getTrigger();

	/**
	 * 
	 * @return
	 */
	public byte[] getLastToken();
}
