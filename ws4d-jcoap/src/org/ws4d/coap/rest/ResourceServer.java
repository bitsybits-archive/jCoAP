
package org.ws4d.coap.rest;

import java.net.URI;

/**
 * A ResourceServer provides network access to resources via a network protocol such as HTTP or CoAP.
 * @author Nico Laum <nico.laum@uni-rostock.de>
 */
public interface ResourceServer {
    /**
     * Register a resource to be handled by this ResourceServer.
     * 
     * @param resource The resource to be handled.
     */
    public boolean createResource(CoapResource resource);
    public Resource readResource(String path);
    public boolean updateResource(CoapResource resource);
    public boolean deleteResource(String path);

//    /*creates a resource, if resource exits the resource will be updated*/
//    public void createUpdateResource(CoapResource resource);

    
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
     * Returns the Host Uri
     */    
    public URI getHostUri();
}
