
package org.ws4d.coap.interfaces;

public interface CoapServerHandler extends CoapChannelHandler {
    /* TODO: add the possibility to reject */
    public boolean onAccept(CoapChannel newChannel);
}
