
package org.ws4d.coap.interfaces;

import java.net.InetAddress;
import java.nio.channels.DatagramChannel;

import org.ws4d.coap.messages.CoapMessageCode;

public interface CoapChannel {
    public void sendMessage(CoapMessage msg);

    public void close();
    
    public InetAddress getRemoteAddress();

    public int getRemotePort();

    public CoapMessage createRequest(boolean reliable, CoapMessageCode.MessageCode messageCode);

    public CoapMessage createResponse(CoapMessage request, CoapMessageCode.MessageCode messageCode);
    
    public void newIncommingMessage(CoapMessage message);
    
}
