
package org.ws4d.coap.interfaces;

public interface CoapChannelHandler {
    public CoapMessage onReceivedMessage(CoapMessage msg);
    public void onLostConnection();
}
