
package org.ws4d.coap.interfaces;

public interface CoapChannelHandler {
    /* TODO: add the ability to reject or delayed response */
    public CoapMessage onReceivedMessage(CoapMessage msg);

    public void onLostConnection();
}
