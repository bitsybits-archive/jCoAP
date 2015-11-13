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

import java.net.URI;
import java.util.HashMap;

import org.ws4d.coap.interfaces.CoapRequest;

/**
 * A ResourceServer provides network access to resources via a network protocol
 * such as HTTP or CoAP.
 * 
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface ResourceServer {
	
	/**
	 * Creates a resource. Resource must not exist.
	 * @param resource The resource to be handled
	 * @return false, if resource exists
	 */
	public boolean createResource(Resource resource);

	/**
	 * @param path
	 * @return resource at the given path. <br> null, if no resource exists
	 */
	public Resource readResource(String path);

	/**
	 * Updates a resource. Resource must exist. Resource is NOT created.
	 * @param resource
	 * @param request
	 * @return false, if resource not exists
	 */
	public boolean updateResource(Resource resource, CoapRequest request);

	/**
	 * deletes the resource at path
	 * @param path path of the resource to be deleted
	 * @return false, if resource does not exist
	 */
	public boolean deleteResource(String path);

	/**
	 * Start the ResourceServer. This usually opens network ports and makes the
	 * resources available through a certain network protocol.
	 */
	public void start() throws Exception;

	/**
	 * Stops the ResourceServer.
	 */
	public void stop();

	/**
	 * @return the Host Uri
	 */
	public URI getHostUri();

	public void resourceChanged(Resource resource);
	
	public HashMap<String, Resource> getResources();
}
