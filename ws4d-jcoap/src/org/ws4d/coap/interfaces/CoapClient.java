
package org.ws4d.coap.interfaces;

import org.ws4d.coap.messages.CoapResponse;

public interface CoapClient extends CoapChannelListener {
    public void onResponse(CoapChannel channel, CoapResponse response);
    public void onConnectionFailed(CoapChannel channel, boolean notReachable, boolean resetByServer);
}
