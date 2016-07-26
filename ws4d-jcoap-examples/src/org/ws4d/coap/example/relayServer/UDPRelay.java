package org.ws4d.coap.example.relayServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public class UDPRelay {
	private static final int SERVER_PORT = 6000;
	private static final int CLIENT_PORT = 8000;
	// max UDP size = 65535
	private static final int UDP_BUFFER_SIZE = 66000;
	private static final Logger logger = LogManager.getLogger();

	private DatagramChannel serverChannel = null;
	private DatagramChannel clientChannel = null;
	private ByteBuffer serverBuffer = ByteBuffer.allocate(UDP_BUFFER_SIZE);
	private ByteBuffer clientBuffer = ByteBuffer.allocate(UDP_BUFFER_SIZE);
	private Selector selector = null;
	private InetSocketAddress clientAddr = null;

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Expected parameter: server host and port, e.g. 192.168.1.1 1234");
			System.exit(-1);
		}
		UDPRelay relay = new UDPRelay();
		relay.run(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
	}

	public void run(InetSocketAddress serverAddr) {

		try {
			this.serverChannel = DatagramChannel.open();
			this.serverChannel.socket().bind(new InetSocketAddress(SERVER_PORT));
			this.serverChannel.configureBlocking(false);
			this.serverChannel.connect(serverAddr);

			this.clientChannel = DatagramChannel.open();
			this.clientChannel.socket().bind(new InetSocketAddress(CLIENT_PORT));
			this.clientChannel.configureBlocking(false);

			this.selector = Selector.open();
			this.serverChannel.register(this.selector, SelectionKey.OP_READ);
			this.clientChannel.register(this.selector, SelectionKey.OP_READ);
			logger.info("Start UDP Relay on server port " + SERVER_PORT + " and client port " + CLIENT_PORT);
		} catch (IOException e) {
			logger.error("Initialization failed, shutting down: " + e.getLocalizedMessage());
			System.exit(-1);
		}

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
			} catch (IOException e) {
				logger.warn("Read failed: " + e.getLocalizedMessage());
			}

			/* forward/send packets client -> server */
			if (tempClientAddr != null) {
				/*
				 * the client address is obtained automatically by the first
				 * request of the client clientAddr is the last known valid
				 * address of the client
				 */
				this.clientAddr = tempClientAddr;
				try {
					this.serverChannel.write(this.clientBuffer);
					logger.info("Forwarded message: " + "client (" + this.clientAddr.getHostName() + ":"
							+ this.clientAddr.getPort() + ") -> " + "server (" + serverAddr.getHostName() + ":"
							+ serverAddr.getPort() + ") " + "[" + this.clientBuffer.limit() + " bytes]");
				} catch (IOException e) {
					logger.warn("Send failed: " + e.getLocalizedMessage());
				}
			}

			/* forward/send packets server -> client */
			if (serverLen > 0) {
				try {
					this.clientChannel.send(this.serverBuffer, this.clientAddr);
					logger.info("Forwarded Message: " + "server (" + serverAddr.getHostName() + ":"
							+ serverAddr.getPort() + ") -> " + "client (" + this.clientAddr.getHostName() + ":"
							+ this.clientAddr.getPort() + ") " + "[" + this.serverBuffer.limit() + " bytes]");
				} catch (IOException e) {
					logger.warn("Send failed: " + e.getLocalizedMessage());
				}
			}

			/* Select */
			try {
				this.selector.select(2000);
			} catch (IOException e) {
				logger.warn("Select failed: " + e.getLocalizedMessage());
			}
		}
	}
}
