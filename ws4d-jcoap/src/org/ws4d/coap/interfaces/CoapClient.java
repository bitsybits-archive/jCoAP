
package org.ws4d.coap.interfaces;

import org.ws4d.coap.messages.CoapEmptyMessage;


public interface CoapClient extends CoapChannelListener {
    public void onResponse(CoapClientChannel channel, CoapResponse response);
    public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer);
	public void onSeparateResponseAck(CoapClientChannel channel, CoapEmptyMessage message);
	public void onSeparateResponse(CoapClientChannel channel, CoapResponse response);
	
}
