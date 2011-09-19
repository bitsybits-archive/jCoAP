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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ws4d.coap.Constants;
import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelHandler;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapSocketListener;
import org.ws4d.coap.messages.CoapPacketType;
import org.ws4d.coap.messages.DefaultCoapMessage;
import org.ws4d.coap.tools.TimeoutHashMap;

public class DefaultCoapSocketListener implements CoapSocketListener {
    /**
     * @author Christian Lerche <christian.lerche@uni-rostock.de>
     * @author Nico Laum <nico.laum@uni-rostock.de>
     */
    private static Logger logger = Logger.getLogger(DefaultCoapSocketListener.class.getName());
    protected WorkerThread workerThread = null;
    protected List<CoapChannel> channels = new LinkedList<CoapChannel>();
    private CoapChannelManager channelManager = null;
    private DatagramChannel dgramChannel = null;

    byte[] sendBuffer = new byte[Constants.COAP_MESSAGE_SIZE_MAX];

    public DefaultCoapSocketListener(CoapChannelManager channelManager, int port) throws IOException {
        this.channelManager = channelManager;
        dgramChannel = DatagramChannel.open();
        dgramChannel.socket().bind(new InetSocketAddress(port));
        dgramChannel.configureBlocking(false);
//        ByteBuffer dst = ByteBuffer.allocate(1000);
//        dgramChannel.receive(dst);
        
        
        workerThread = new WorkerThread();
        workerThread.start();
        logger.setLevel(Level.ALL);
    }

    public class ChannelBuffers {
        /* TODO: make them ThreadSafe */
        /* this channel buffers are hooked with each CoapChannel */
        /* Contains all sent ACKs and RSTs for retransmission */
        private TimeoutHashMap<Integer, CoapMessage> sentAckRst = new TimeoutHashMap<Integer, CoapMessage>(
                CoapMessage.ACK_RST_RETRANS_TIMEOUT_MS);
        /* use it as a HashSet */
        private TimeoutHashMap<Integer, Integer> recvdConNon = new TimeoutHashMap<Integer, Integer>(
                CoapMessage.ACK_RST_RETRANS_TIMEOUT_MS);
    }

    protected class WorkerThread extends Thread {
        Selector selector = null;
        /*
         * contains all global duplicate messageIDs for all ACK and RST Messages
         * second Integer is the timestamp when the messageID expires
         */
        TimeoutHashMap<Integer, Integer> dupRstAck = new TimeoutHashMap<Integer, Integer>(
                CoapMessage.ACK_RST_RETRANS_TIMEOUT_MS);

		/* Contains all messages that will be send at a given time */
		private PriorityBlockingQueue<CoapMessage> sendQueue;
		/* Contains all received confirmations (ACK and RST) */
//		private LinkedBlockingQueue<Integer> conQueue;
		// /* Contains buffered messages which expire after a while (sent ACK
		// and RST)*/
		// private PriorityBlockingQueue<CoapMessage> timeoutQueue;
		
		/* Contains all sent messages sorted by message ID */
		private HashMap<Integer, CoapMessage> msgMap = new HashMap<Integer, CoapMessage>();
		long startTime;
		static final int POLLING_INTERVALL = 10000;

		ByteBuffer dgramBuffer;
//		dgramPacket = new DatagramPacket(dgramBuffer, Constants.COAP_MESSAGE_SIZE_MAX);

		public WorkerThread() {
			dgramBuffer = ByteBuffer.allocate(1500);
		    sendQueue = new PriorityBlockingQueue<CoapMessage>(10,
		            new Comparator<CoapMessage>() {
		                public int compare(CoapMessage a, CoapMessage b) {
		                    if (a.getSendTimestamp() < b.getSendTimestamp()) {
		                        return -1;
		                    } else
		                        return 1;
		                }
		            });
		
//		    conQueue = new LinkedBlockingQueue<Integer>();
		    startTime = System.currentTimeMillis();
		}

		public synchronized void putMessageToSendMessageBuffer(CoapMessage msg) {
			if (msg != null) {
				msg.setSendTimestamp(System.currentTimeMillis());
				sendQueue.add(msg);
				/* notify sendthread to send message immediately */
				msgMap.put(msg.getMessageID(), msg);
				selector.wakeup();
			}
		}
		
		public synchronized void close() {
	        if (channels != null)
	            channels.clear();
	        try {
				dgramChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        /* TODO: wake up thread and kill it*/
		}

		@Override
		public void run() {
		    logger.log(Level.INFO, "Receive Thread started.");
		    
		    try {
				selector = Selector.open();
				dgramChannel.register(selector, SelectionKey.OP_READ);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			long waitFor = POLLING_INTERVALL;
			InetSocketAddress addr = null;
			
			while (dgramChannel != null) {
				
				try {
					selector.select(waitFor);
					dgramBuffer.clear();
					addr = (InetSocketAddress) dgramChannel.receive(dgramBuffer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				/* handle incoming packets */
				if (addr != null){
					logger.log(Level.INFO, "handle incomming msg");
					handleIncommingMessage(dgramBuffer, addr);
				}
				
				/* send messages */
				waitFor = handleSendMessage();
			}
		}

		private void confirmMessage(int messageID) {
			CoapMessage msg = msgMap.get(messageID);
			if (msg != null) {
				msg.confirmMessage();
				logger.log(Level.INFO, "Confirm Message with ID " + messageID);
			}
		}

		private void handleIncommingMessage(ByteBuffer buffer, InetSocketAddress addr) {
			CoapMessage msg = new DefaultCoapMessage(buffer.array(), buffer.array().length);
			CoapPacketType packetType = msg.getPacketType();
			int msgID = msg.getMessageID();
			/* TODO drop invalid messages (invalid version, type etc.) */
			CoapChannel channel = getChannel(addr.getAddress(),	addr.getPort());
			if (channel != null) {
				/* channel already established */
				msg.setChannel(channel);
				if ((packetType == CoapPacketType.CON)
						|| (packetType == CoapPacketType.NON)) {
					ChannelBuffers buf = (ChannelBuffers) channel
							.getHookObject();
					Object duplicate = buf.recvdConNon.get(msgID);
					if (duplicate == null) {
						/* Received CON or NON */
						channel.newIncommingMessage(msg);
						/* put something in there, use it as HashSet */
						buf.recvdConNon.put(msgID, new Integer(1));
					} else {
						/*
						 * duplicate NONs were ignored (dropped) duplicate CONs
						 * were dropped but corresponding ACKs and RSTs were
						 * retransmitted
						 */
						if (packetType == CoapPacketType.CON) {
							/* retransmit ACK or RST */
							putMessageToSendMessageBuffer(buf.sentAckRst.get(msgID));
							logger.log(Level.INFO,
									"CON duplicate msg (" + msg.getMessageID()
											+ ")");
						} else
							logger.log(Level.INFO, "NON duplicate msg");
					}
				}

				if ((packetType == CoapPacketType.ACK)
						|| (packetType == CoapPacketType.RST)) {
					if (!dupRstAck.containsKey(msgID)) {
						confirmMessage(msgID);
						/*
						 * in case of a RST the listener is responsible for
						 * closing the channel
						 */
						channel.newIncommingMessage(msg);
					} else
						logger.log(Level.INFO, "ACK or RST duplicate");
				}
			} else {
				/* no channel found */
				if ((packetType == CoapPacketType.CON)
						|| (packetType == CoapPacketType.NON)) {
					/*
					 * no channel found -> ask server for acceptance of a new
					 * channel
					 */
					/*
					 * ignore ACKs and RSTs if there is no corresponding channel
					 */
					/*
					 * TODO: check if the sever wants to create a channel and
					 * create a new one
					 */
					CoapChannel newChannel = channelManager
							.createServerChannel(
									DefaultCoapSocketListener.this,
									addr.getAddress(),
									addr.getPort());
					if (newChannel != null) {
						newChannel.setHookObject(new ChannelBuffers());
						addChannel(newChannel);
						((ChannelBuffers) newChannel.getHookObject()).recvdConNon
								.put(msgID, 1);
						logger.log(Level.INFO, "Created new server channel...");
						msg.setChannel(newChannel);
						newChannel.newIncommingMessage(msg);
					} else {
						/* TODO: send a RST */
					}
				}
			}
		}
		
		private long handleSendMessage() {
			/*
			 * send all messages from the queue with an expired timestamp
			 */
			long waitFor;
			while (true) {
				waitFor = POLLING_INTERVALL; // Maximum Wait
				CoapMessage msg = sendQueue.peek();
				if (msg == null){
					break;
				}

				waitFor = msg.getSendTimestamp() - System.currentTimeMillis();
				
				if (waitFor > 0){
					/* go to sleep */
					break;
				}

				msg = sendQueue.poll();
				CoapPacketType packetType = msg.getPacketType();

				if (packetType == CoapPacketType.NON) {
					sendMsg(msg);
					continue;
				}
				
				if ((packetType == CoapPacketType.CON) && !msg.isConfirmed()) {
					/* Retransmit only CON packets */
					/* Confirmed messages were just dropped */
					if (retransMsgCheck(msg, sendQueue)) {
						sendMsg(msg);
					} else {
						/*
						 * Failed to send Packet (MAX RETRANS REACHED)
						 */
						msg.getCoapChannel().getCoapChannelHandler()
								.onLostConnection();
						logger.log(Level.INFO, "Connection Lost...");
					}
					continue;
				}
				/* save ACK and RST for possible retransmission */
				if ((packetType == CoapPacketType.ACK)
						|| (packetType == CoapPacketType.RST)) {
					ChannelBuffers buf = (ChannelBuffers) msg.getCoapChannel()
							.getHookObject();
					buf.sentAckRst.put(msg.getMessageID(), msg);
					sendMsg(msg);
					continue;
				}
				logger.log(Level.INFO,
						"drop confirmend msg with ID " + msg.getMessageID());
				continue;

			};

			/*
			 * This wait() needs to be interrupted by a notify() when something
			 * in the queues changes
			 */
			if (waitFor > POLLING_INTERVALL) {
				waitFor = POLLING_INTERVALL;
			}
			if (waitFor < 0) {
				logger.log(Level.WARNING, "Wait for is negative");
				waitFor = POLLING_INTERVALL;
			}
			if (waitFor == 0) {
				logger.log(Level.WARNING, "Wait for is zero");
				waitFor = POLLING_INTERVALL;
			}
			return waitFor;
		}

		private void sendMsg(CoapMessage msg) {
			ByteBuffer buf = ByteBuffer.wrap(msg.serialize());
		    try {
		    	dgramChannel.send(buf, new InetSocketAddress(msg.getCoapChannel().getRemoteAddress(), msg.getCoapChannel().getRemotePort()));
		        logger.log(Level.INFO, "Send Msg with ID: " + msg.getMessageID());
		    } catch (IOException e) {
		    	/* TODO: handle error*/
		    	e.printStackTrace();
		    }
		}

		private boolean retransMsgCheck(CoapMessage msg, PriorityBlockingQueue<CoapMessage> msgQueue) {
		    if (msg.getHeader().getType() == CoapPacketType.CON) {
		        try {
		            if (msg.maxRetransReached()) {
		                /* Connection Failed */
		                return false;
		            }
		            msg.incRetransCounterAndTimeout();
		            msg.setSendTimestamp(System.currentTimeMillis() + msg.getTimeout());
		            msgQueue.add(msg);
		        } catch (IllegalStateException e) {
		            e.printStackTrace();
		        }
		    }
		    /* nothing happened, the msg is dropped from the queue */
		    return true;
		}

		private synchronized CoapChannel getChannel(InetAddress addr, int port) {
			for (CoapChannel channel : channels) {
				if (channel.getRemoteAddress().equals(addr)
						&& channel.getRemotePort() == port) {
					/* Found the corresponding channel */
					return channel;
				}
			}
			return null;
		}
	}

	private synchronized void addChannel(CoapChannel channel) {
        channels.add(channel);
    }

    @Override
    public synchronized void removeChannel(CoapChannel channel) {
        channels.remove(channel);
    }

    @Override
    public void close() {
    	workerThread.close();
    }

    // @Override
    // public boolean isOpen() {
    // if (socket!=null && socket.isBound() && socket.isConnected())
    // return true;
    // else
    // return false;
    // }

    /**
     * @param message The message to be sent. This method will give the message
     *            a new message id!
     */
    @Override
    public void sendMessage(CoapMessage message) {
        if (workerThread != null) {
            workerThread.putMessageToSendMessageBuffer(message);
        }
    }

    //
    // @Override
    // public int sendRequest(CoapMessage request) {
    // sendMessage(request);
    // return request.getMessageID();
    // }
    //
    // @Override
    // public void sendResponse(CoapResponse response) {
    // sendMessage(response);
    // }
    //
    // @Override
    // public void establish(DatagramSocket socket) {
    //
    // }
    //
    // @Override
    // public void unregisterResponseListener(CoapResponseListener
    // responseListener) {
    // coapResponseListeners.remove(responseListener);
    // }

    @Override
    public CoapChannel connect(CoapChannelHandler channelListener, InetAddress remoteAddress,
            int remotePort) {
    	if (channelListener == null){
    		return null;
    	}
    	
        CoapChannel channel = new DefaultCoapChannel(this, channelListener, remoteAddress,
                remotePort);
        channel.setHookObject(new ChannelBuffers());
        addChannel(channel);
        return channel;
    }

    @Override
    public CoapChannelManager getChannelManager() {
        return this.channelManager;
    }

}
