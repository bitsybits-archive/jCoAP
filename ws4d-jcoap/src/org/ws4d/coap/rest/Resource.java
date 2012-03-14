
package org.ws4d.coap.rest;

import java.util.Vector;

/**
 * A resource known from the REST architecture style. A resource has a type,
 * name and data associated with it.
 * 
 * @author Nico Laum <nico.laum@uni-rostock.de>
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
    
}
