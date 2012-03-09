
package org.ws4d.coap.interfaces;

import java.net.InetAddress;

import org.ws4d.coap.messages.CoapRequest;
import org.ws4d.coap.messages.CoapRequest.CoapRequestCode;
import org.ws4d.coap.messages.CoapResponse;
import org.ws4d.coap.messages.CoapResponse.CoapResponseCode;

public interface CoapChannel {

	public void sendMessage(CoapMessage msg);

//	public void sendMessage(CoapMessage msg, CoapMessage request);

    public void close();
    
    public InetAddress getRemoteAddress();

    public int getRemotePort();

    public CoapRequest createRequest(boolean reliable, CoapRequestCode requestCode);

    public CoapResponse createResponse(CoapMessage request, CoapResponseCode responseCode);

    public void newIncommingMessage(CoapMessage message);
    /*TODO: implement Error Type*/
	public void lostConnection(boolean notReachable, boolean resetByServer);

}
