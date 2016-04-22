package org.ws4d.coap.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public class UDPRelay {
	public static final int SERVER_PORT = 6000;
	public static final int CLIENT_PORT = 8000;
	public static final int UDP_BUFFER_SIZE = 66000; // max UDP size = 65535 
	

	public static void main(String[] args) {
		if (args.length < 2){
			System.out.println("expected parameter: server host and port, e.g. 192.168.1.1 1234");
			System.exit(-1);
		}

		UDPRelay relay = new UDPRelay();
		relay.run(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
	}
	
	private DatagramChannel serverChannel = null;
	private DatagramChannel clientChannel = null;
	ByteBuffer serverBuffer = ByteBuffer.allocate(UDP_BUFFER_SIZE);
	ByteBuffer clientBuffer = ByteBuffer.allocate(UDP_BUFFER_SIZE);
	Selector selector = null;
	InetSocketAddress clientAddr = null;

	public void run(InetSocketAddress serverAddr) {
		try {
			this.serverChannel = DatagramChannel.open();
			this.serverChannel.socket().bind(new InetSocketAddress(SERVER_PORT));  
			this.serverChannel.configureBlocking(false);
			this.serverChannel.connect(serverAddr);
			
			this.clientChannel = DatagramChannel.open();
			this.clientChannel.socket().bind(new InetSocketAddress(CLIENT_PORT));  
			this.clientChannel.configureBlocking(false);

		    try {
				this.selector = Selector.open();
				this.serverChannel.register(this.selector, SelectionKey.OP_READ);
				this.clientChannel.register(this.selector, SelectionKey.OP_READ);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Initialization failed, Shut down");
			System.exit(-1);
		}
		System.out.println("Start UDP Realy on Server Port " + SERVER_PORT + " and Client Port " + CLIENT_PORT);

		int serverLen = 0;
		while (true) {
			/* Receive Packets */
			InetSocketAddress tempClientAddr = null;
			try {
				this.clientBuffer.clear();
				tempClientAddr = (InetSocketAddress) this.clientChannel.receive(this.clientBuffer);
				this.clientBuffer.flip();
				
				this.serverBuffer.clear();
				serverLen = this.serverChannel.read(this.serverBuffer);
				this.serverBuffer.flip();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Read failed");
			}
			
			/* forward/send packets client -> server*/
			if (tempClientAddr != null) {
				/* the client address is obtained automatically by the first request of the client 
				 * clientAddr is the last known valid address of the client */
				this.clientAddr = tempClientAddr;
				try {
					this.serverChannel.write(this.clientBuffer);
					System.out.println("Forwarded Message client ("+this.clientAddr.getHostName()+" "+this.clientAddr.getPort()
							+ ") -> server (" + serverAddr.getHostName()+" " + serverAddr.getPort() + "): " 
							+ this.clientBuffer.limit() + " bytes");
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Send failed");
				}
			}

			/* forward/send packets server -> client*/
			if (serverLen > 0) {
				try {
					this.clientChannel.send(this.serverBuffer, this.clientAddr);
					System.out.println("Forwarded Message server ("+serverAddr.getHostName()+" "+serverAddr.getPort()
							+ ") -> client (" + this.clientAddr.getHostName()+" " + this.clientAddr.getPort() + "): " 
							+ this.serverBuffer.limit() + " bytes");
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Send failed");
				}
			}
			
			/* Select */
			try {
				this.selector.select(2000);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("select failed");
			}
		}
	}
}
