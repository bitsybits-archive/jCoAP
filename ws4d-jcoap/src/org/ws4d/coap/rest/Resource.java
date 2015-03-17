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

import java.util.Vector;

/**
 * A resource known from the REST architecture style. A resource has a type,
 * name and data associated with it.
 * 
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * 
 */
public interface Resource {
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

    public byte[] getValue();
    
    public byte[] getValue(Vector<String> query);
    
    //TODO: bad api: no return value
    public void post(byte[] data);
    
    public String getResourceType();
    
	public void registerServerListener(ResourceServer server);
	
	public void unregisterServerListener(ResourceServer server);
    
}
