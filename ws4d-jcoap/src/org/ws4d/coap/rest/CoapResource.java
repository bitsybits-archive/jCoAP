
package org.ws4d.coap.rest;

import org.ws4d.coap.messages.CoapMediaType;

/**
 * @author Nico Laum <nico.laum@uni-rostock.de>
 */
public interface CoapResource extends Resource {
    public String getResourceType();
    public CoapMediaType getCoapMediaType();
}
