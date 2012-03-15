
package org.ws4d.coap.interfaces;


public interface CoapClient extends CoapChannelListener {
    public void onResponse(CoapClientChannel channel, CoapResponse response);
    public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer);
}
