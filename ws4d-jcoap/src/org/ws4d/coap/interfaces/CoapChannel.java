
package org.ws4d.coap.interfaces;

import java.net.InetAddress;

public interface CoapChannel {

	public void sendMessage(CoapMessage msg);

//	public void sendMessage(CoapMessage msg, CoapMessage request);

	/*TODO: close when finished, & abort()*/
    public void close();
    
    public InetAddress getRemoteAddress();

    public int getRemotePort();
    
    public void newIncommingMessage(CoapMessage message);
    /*TODO: implement Error Type*/
	public void lostConnection(boolean notReachable, boolean resetByServer);

}
