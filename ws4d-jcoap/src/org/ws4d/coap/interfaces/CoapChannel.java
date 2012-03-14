
package org.ws4d.coap.interfaces;

import java.net.InetAddress;

import org.ws4d.coap.messages.BasicCoapRequest;
import org.ws4d.coap.messages.BasicCoapRequest.CoapRequestCode;
import org.ws4d.coap.messages.BasicCoapResponse;
import org.ws4d.coap.messages.BasicCoapResponse.CoapResponseCode;
import org.ws4d.coap.messages.CoapMediaType;

public interface CoapChannel {

	public void sendMessage(CoapMessage msg);

//	public void sendMessage(CoapMessage msg, CoapMessage request);

    public void close();
    
    public InetAddress getRemoteAddress();

    public int getRemotePort();

    public BasicCoapRequest createRequest(boolean reliable, CoapRequestCode requestCode);

    public BasicCoapResponse createResponse(CoapMessage request, CoapResponseCode responseCode);

    public BasicCoapResponse createResponse(CoapMessage request, CoapResponseCode responseCode, CoapMediaType contentType);
    
    public void newIncommingMessage(CoapMessage message);
    /*TODO: implement Error Type*/
	public void lostConnection(boolean notReachable, boolean resetByServer);

}
