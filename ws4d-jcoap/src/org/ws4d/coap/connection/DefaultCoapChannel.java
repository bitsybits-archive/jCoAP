
package org.ws4d.coap.connection;

import java.net.InetAddress;
import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelListener;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapSocketHandler;
import org.ws4d.coap.messages.CoapMessageCode;
import org.ws4d.coap.messages.CoapPacketType;
import org.ws4d.coap.messages.DefaultCoapMessage;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */


public abstract class DefaultCoapChannel implements CoapChannel {
    private CoapSocketHandler socketHandler = null;
    private CoapChannelManager channelManager = null;
    private InetAddress remoteAddress;
    private int remotePort;
    private int localPort;

    public DefaultCoapChannel(CoapSocketHandler socketHandler, InetAddress remoteAddress, int remotePort) {
        this.socketHandler = socketHandler;
        channelManager = socketHandler.getChannelManager();
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.localPort = socketHandler.getLocalPort(); //FIXME:can be 0 when socketHandler is not yet ready
    }

    @Override
    public void close() {
        socketHandler.removeChannel(this);
    }

    @Override
    public void sendMessage(CoapMessage msg, CoapMessage request) {
        if (request.getPacketType() == CoapPacketType.CON) {
            msg.setPacketType(CoapPacketType.ACK);
        }

        if (request.getPacketType() == CoapPacketType.NON) {
            msg.setPacketType(CoapPacketType.NON);
        }
        msg.setChannel(this);
        msg.setMessageID(request.getMessageID());
        socketHandler.sendMessage(msg);
    }
    
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

    @Override
    public CoapMessage createRequest(boolean reliable, CoapMessageCode.MessageCode messageCode) {
        CoapMessage msg = new DefaultCoapMessage(
                reliable ? CoapPacketType.CON : CoapPacketType.NON, messageCode,
                channelManager.getNewMessageID());
        msg.setChannel(this);
        return msg;
    }

    @Override
    public CoapMessage createResponse(CoapMessage request, CoapMessageCode.MessageCode messageCode) {
        if (request.getPacketType() == CoapPacketType.CON) {
            CoapMessage msg = new DefaultCoapMessage(CoapPacketType.ACK, messageCode,
                    request.getMessageID());
            msg.setChannel(this);
            return msg;
        }

        if (request.getPacketType() == CoapPacketType.NON) {
            CoapMessage msg = new DefaultCoapMessage(CoapPacketType.NON, messageCode,
                    channelManager.getNewMessageID());
            msg.setChannel(this);
            return msg;
        }

        return null;
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
		DefaultCoapChannel other = (DefaultCoapChannel) obj;
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
