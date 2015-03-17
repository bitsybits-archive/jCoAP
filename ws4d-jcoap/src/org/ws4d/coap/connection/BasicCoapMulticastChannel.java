/* Copyright 2015 University of Rostock
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.messages.CoapBlockOption.CoapBlockSize;

public class BasicCoapMulticastChannel implements CoapChannel {
	private MulticastSocket multicastSocket = null;
	private int port = 0;
	private InetAddress multicastAddress = null;

	public BasicCoapMulticastChannel(String multicastAddress, int port)
			throws UnknownHostException {
		this.multicastAddress = InetAddress.getByName(multicastAddress);
		this.port = port;
	}

	public void init() {
		try {
			this.multicastSocket = new MulticastSocket(this.port);
			this.multicastSocket.setReuseAddress(true);
			this.multicastSocket.setSoTimeout(15000);
			this.multicastSocket.joinGroup(multicastAddress);
		} catch (Exception e) {
			System.out.println("Error occured: " + e.getMessage());
			return;
		}
	}

	@Override
	public void sendMessage(CoapMessage msg) {
		try {
			DatagramPacket p = new DatagramPacket(msg.serialize(),
					msg.serialize().length);
			this.multicastSocket.send(p);
		} catch (IOException e) {
			System.out.println("Error occured: " + e.getMessage());
			return;
		}
	}

	@Override
	public void close() {
		this.multicastSocket.close();
	}

	@Override
	public InetAddress getRemoteAddress() {
		return this.multicastAddress;
	}

	@Override
	public int getRemotePort() {
		return this.port;
	}

	@Override
	public void handleMessage(CoapMessage message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lostConnection(boolean notReachable, boolean resetByServer) {
		// TODO Auto-generated method stub

	}

	@Override
	public CoapBlockSize getMaxReceiveBlocksize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaxReceiveBlocksize(CoapBlockSize maxReceiveBlocksize) {
		// TODO Auto-generated method stub

	}

	@Override
	public CoapBlockSize getMaxSendBlocksize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaxSendBlocksize(CoapBlockSize maxSendBlocksize) {
		// TODO Auto-generated method stub

	}

}
