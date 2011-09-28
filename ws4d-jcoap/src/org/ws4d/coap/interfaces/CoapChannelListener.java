
package org.ws4d.coap.interfaces;

public interface CoapChannelListener {
    public void onReceivedMessage(CoapMessage msg);
    public void onLostConnection();
}
