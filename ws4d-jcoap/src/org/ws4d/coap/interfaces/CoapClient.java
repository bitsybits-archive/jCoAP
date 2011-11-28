
package org.ws4d.coap.interfaces;

public interface CoapClient extends CoapChannelListener {
    public void onResponse(CoapChannel channel, CoapMessage response);
    public void onConnectionFailed(CoapChannel channel, boolean notReachable, boolean resetByServer);
}
