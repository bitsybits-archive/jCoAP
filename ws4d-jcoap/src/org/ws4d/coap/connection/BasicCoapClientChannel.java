/* Copyright [2011] [University of Rostock]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

package org.ws4d.coap.connection;

import java.net.InetAddress;

import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapSocketHandler;
import org.ws4d.coap.messages.BasicCoapRequest;
import org.ws4d.coap.messages.BasicCoapResponse;
import org.ws4d.coap.messages.CoapEmptyMessage;
import org.ws4d.coap.messages.CoapPacketType;
import org.ws4d.coap.messages.CoapRequestCode;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */


public class BasicCoapClientChannel extends BasicCoapChannel implements CoapClientChannel {
	CoapClient client = null;
	public BasicCoapClientChannel(CoapSocketHandler socketHandler,
			CoapClient client, InetAddress remoteAddress,
			int remotePort) {
		super(socketHandler, remoteAddress, remotePort);
		this.client = client;
	}
	
    @Override
    public void close() {
        socketHandler.removeClientChannel(this);
    }

	@Override
	public void newIncommingMessage(CoapMessage message) {
		if (message.isEmpty() && message.getPacketType() == CoapPacketType.ACK){
			/* this is the ACK of a separate response */
			client.onSeparateResponseAck(this, (CoapEmptyMessage) message);
		} else 
		if (message.isResponse()){
			if (message.getPacketType() == CoapPacketType.CON){
				/* this is a separate response */
				/* send ACK */
				message.getChannel().sendMessage(new CoapEmptyMessage(CoapPacketType.ACK, message.getMessageID()));
				client.onSeparateResponse(this, (BasicCoapResponse) message);
			} else {
				/* normal response*/
				client.onResponse(this, (BasicCoapResponse) message);
			}
		} else {
			throw new IllegalStateException("Incomming client message is not a valid response");
		}
	}

	@Override
	public void lostConnection(boolean notReachable, boolean resetByServer) {
		client.onConnectionFailed(this, notReachable, resetByServer);		

	}
	
    @Override
    public BasicCoapRequest createRequest(boolean reliable, CoapRequestCode requestCode) {
    	BasicCoapRequest msg = new BasicCoapRequest(
                reliable ? CoapPacketType.CON : CoapPacketType.NON, requestCode,
                channelManager.getNewMessageID());
        msg.setChannel(this);
        return msg;
    }

    // public DefaultCoapClientChannel(CoapChannelManager channelManager) {
    // super(channelManager);
    // }
    //
    // @Override
    // public void connect(String remoteHost, int remotePort) {
    // socket = null;
    // if (remoteHost!=null && remotePort!=-1) {
    // try {
    // socket = new DatagramSocket();
    // } catch (SocketException e) {
    // e.printStackTrace();
    // }
    // }
    //
    // try {
    // InetAddress address = InetAddress.getByName(remoteHost);
    // socket.connect(address, remotePort);
    // super.establish(socket);
    // } catch (UnknownHostException e) {
    // e.printStackTrace();
    // }
    // }

}
