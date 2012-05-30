
package org.ws4d.coap.rest;

import java.net.URI;

/**
 * A ResourceServer provides network access to resources via a network protocol such as HTTP or CoAP.
 * @author Nico Laum <nico.laum@uni-rostock.de>
 */
public interface ResourceServer {
    /**
     * 
     * @param resource The resource to be handled.
     */
    public boolean createResource(Resource resource);
    public Resource readResource(String path);
    public boolean updateResource(Resource resource);
    public boolean deleteResource(String path);
    
    
    public Resource get(String path);
    public void post(String path, byte[] data);
    public boolean put(Resource resource);
    public boolean delete(String path);

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
