
package org.ws4d.coap.interfaces;

public interface CoapServerHandler extends CoapChannelHandler {
    public boolean onAccept(CoapChannel newChannel);
}
