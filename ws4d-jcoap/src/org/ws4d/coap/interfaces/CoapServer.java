
package org.ws4d.coap.interfaces;


public interface CoapServer extends CoapChannelListener {
    public CoapServer onAccept(CoapRequest request);
    public void handleRequest(CoapChannel channel, CoapRequest request);
}
