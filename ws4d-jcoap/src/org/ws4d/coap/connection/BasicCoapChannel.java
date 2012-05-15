
package org.ws4d.coap.connection;

import java.net.InetAddress;

import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapSocketHandler;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */


public abstract class BasicCoapChannel implements CoapChannel {
	protected CoapSocketHandler socketHandler = null;
    protected CoapChannelManager channelManager = null;
    protected InetAddress remoteAddress;
    protected int remotePort;
    protected int localPort;

    public BasicCoapChannel(CoapSocketHandler socketHandler, InetAddress remoteAddress, int remotePort) {
        this.socketHandler = socketHandler;
        channelManager = socketHandler.getChannelManager();
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.localPort = socketHandler.getLocalPort(); //FIXME:can be 0 when socketHandler is not yet ready
    }

//    @Override
//    public void sendMessage(CoapMessage msg, CoapMessage request) {
//    	/* TODO: remove this method, packet type should be determined by the channel */
//        if (request.getPacketType() == CoapPacketType.CON) {
//            msg.setPacketType(CoapPacketType.ACK);
//        }
//
//        if (request.getPacketType() == CoapPacketType.NON) {
//            msg.setPacketType(CoapPacketType.NON);
//        }
//        msg.setChannel(this);
//        msg.setMessageID(request.getMessageID());
//        socketHandler.sendMessage(msg);
//    }
    
    @Override
    public void sendMessage(CoapMessage msg) {
        msg.setChannel(this);
        socketHandler.sendMessage(msg);
    }

    @Override
    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    /*A channel is identified (and therefore unique) by its remote address, remote port and the local port */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + localPort;
		result = prime * result
				+ ((remoteAddress == null) ? 0 : remoteAddress.hashCode());
		result = prime * result + remotePort;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicCoapChannel other = (BasicCoapChannel) obj;
		if (localPort != other.localPort)
			return false;
		if (remoteAddress == null) {
			if (other.remoteAddress != null)
				return false;
		} else if (!remoteAddress.equals(other.remoteAddress))
			return false;
		if (remotePort != other.remotePort)
			return false;
		return true;
	}
    
}
