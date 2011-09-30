
package org.ws4d.coap.interfaces;

public interface CoapServer extends CoapChannelListener {
    public CoapServer onAccept(CoapMessage request);
    public void handleRequest(CoapChannel channel, CoapMessage request);
}
