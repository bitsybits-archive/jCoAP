
package org.ws4d.coap.interfaces;

public interface CoapServerListener extends CoapChannelListener {
    public boolean onAccept(CoapChannel newChannel);
}
