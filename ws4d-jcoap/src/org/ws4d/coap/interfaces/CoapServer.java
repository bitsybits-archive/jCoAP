
package org.ws4d.coap.interfaces;


public interface CoapServer extends CoapChannelListener {
    public CoapServer onAccept(CoapRequest request);
    public void onRequest(CoapServerChannel channel, CoapRequest request);
	public void onSeparateResponseFailed(CoapServerChannel channel);
}
