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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ws4d.coap.Constants;
import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelHandler;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapServerHandler;
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
    protected DatagramSocket socket;
    protected CoapReceiveThread receiveThread = null;
    protected CoapSendThread sendThread = null;
    protected List<CoapChannel> channels = new LinkedList<CoapChannel>();
    private CoapChannelManager channelManager = null;

    byte[] sendBuffer = new byte[Constants.COAP_MESSAGE_SIZE_MAX];

    public DefaultCoapSocketListener(CoapChannelManager channelManager, DatagramSocket socket) {
        this.channelManager = channelManager;
        this.socket = socket;
        receiveThread = new CoapReceiveThread(socket);
        receiveThread.start();
        sendThread = new CoapSendThread(socket);
        sendThread.start();
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

    protected class CoapReceiveThread extends Thread {
        DatagramSocket socket;
        DatagramPacket dgramPacket;
        /*
         * contains all global duplicate messageIDs for all ACK and RST Messages
         * second Integer is the timestamp when the messageID expires
         */
        TimeoutHashMap<Integer, Integer> dupRstAck = new TimeoutHashMap<Integer, Integer>(
                CoapMessage.ACK_RST_RETRANS_TIMEOUT_MS);

        public CoapReceiveThread(DatagramSocket socket) {
            this.socket = socket;
            byte[] dgramBuffer = new byte[1500];
            dgramPacket = new DatagramPacket(dgramBuffer, Constants.COAP_MESSAGE_SIZE_MAX);

        }

        @Override
        public void run() {
            logger.log(Level.INFO, "Receive Thread started.");
            while (!socket.isClosed()) {
                try {
                    socket.receive(dgramPacket);
                    CoapMessage msg = new DefaultCoapMessage(dgramPacket.getData(),
                            dgramPacket.getLength());
                    CoapPacketType packetType = msg.getPacketType();
                    int msgID = msg.getMessageID();
                    /* TODO drop invalid messages (invalid version, type etc.) */
                    CoapChannel channel = getChannel(dgramPacket.getAddress(),
                            dgramPacket.getPort());
                    if (channel != null) {
                        /* channel already established */
                        msg.setChannel(channel);
                        if ((packetType == CoapPacketType.CON)
                                || (packetType == CoapPacketType.NON)) {
                            ChannelBuffers buf = (ChannelBuffers) channel.getHookObject();
                            Object duplicate = buf.recvdConNon.get(msgID);
                            if (duplicate == null) {
                                /* Received CON or NON */
                                channel.newIncommingMessage(msg);
                                /* put something in there, use it as HashSet */
                                buf.recvdConNon.put(msgID, new Integer(1));
                            } else {
                                /*
                                 * duplicate NONs were ignored (dropped)
                                 * duplicate CONs were dropped but corresponding
                                 * ACKs and RSTs were retransmitted
                                 */
                                if (packetType == CoapPacketType.CON) {
                                    /* retransmit ACK or RST */
                                    sendThread.sendMessage(buf.sentAckRst.get(msgID));
                                    logger.log(Level.INFO,
                                            "CON duplicate msg (" + msg.getMessageID() + ")");
                                } else
                                    logger.log(Level.INFO, "NON duplicate msg");
                            }
                        }

                        if ((packetType == CoapPacketType.ACK)
                                || (packetType == CoapPacketType.RST)) {
                            if (!dupRstAck.containsKey(msgID)) {
                                sendThread.confirmMessage(msgID);
                                /*
                                 * in case of a RST the listener is responsible
                                 * for closing the channel
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
                             * no channel found -> ask server for acceptance of
                             * a new channel
                             */
                            /*
                             * ignore ACKs and RSTs if there is no corresponding
                             * channel
                             */
                                /*
                                 * TODO: check if the sever wants to create a
                                 * channel and create a new one
                                 */
                            CoapChannel newChannel = channelManager.createServerChannel(DefaultCoapSocketListener.this, dgramPacket.getAddress(), dgramPacket.getPort());
                            if (newChannel != null) {
                                newChannel.setHookObject(new ChannelBuffers());
                                addChannel(newChannel);
                                ((ChannelBuffers) newChannel.getHookObject()).recvdConNon.put(
                                        msgID, 1);
                                logger.log(Level.INFO, "Created new server channel...");
                                msg.setChannel(newChannel);
                                newChannel.newIncommingMessage(msg);
                            } else {
                            	/* TODO: send a RST */
                            }
                        }
                    }

                } catch (SocketException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            socket = null;
        }
    }

    /**
     * @author Christian Lerche <christian.lerche@uni-rostock.de>
     * @author Nico Laum <nico.laum@uni-rostock.de>
     */
    protected class CoapSendThread extends Thread {
        /* Contains all messages that will be send at a given time */
        private PriorityBlockingQueue<CoapMessage> sendQueue;
        /* Contains all received confirmations (ACK and RST) */
        private LinkedBlockingQueue<Integer> conQueue;
        // /* Contains buffered messages which expire after a while (sent ACK
        // and RST)*/
        // private PriorityBlockingQueue<CoapMessage> timeoutQueue;

        /* Contains all sent messages sorted by message ID */
        private HashMap<Integer, CoapMessage> msgMap = new HashMap<Integer, CoapMessage>();

        long startTime;
        static final int MAX_WAIT = 10000;

        private DatagramSocket socket = null;

        public CoapSendThread(DatagramSocket socket) {
            this.socket = socket;
            sendQueue = new PriorityBlockingQueue<CoapMessage>(10,
                    new Comparator<CoapMessage>() {
                        public int compare(CoapMessage a, CoapMessage b) {
                            if (a.getSendTimestamp() < b.getSendTimestamp()) {
                                return -1;
                            } else
                                return 1;
                        }
                    });

            conQueue = new LinkedBlockingQueue<Integer>();
            startTime = System.currentTimeMillis();
        }

        public synchronized void sendMessage(CoapMessage msg) {
            if (msg != null) {
                try {
                    msg.setSendTimestamp(System.currentTimeMillis());
                    sendQueue.add(msg);
                    msgMap.put(msg.getMessageID(), msg);
                    /* notify sendthread to send message immediately */
                    synchronized (sendThread) {
                        sendThread.notify();
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void confirmMessage(int messageID) {
            try {
                conQueue.add(messageID);
                sendThread.notify();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            logger.log(Level.INFO, "Send Thread started.");
            while (!socket.isClosed()) {
                try {
                    /*
                     * check for received RSTs and ACKs to remove the
                     * corresponding CONs from the retransmission queue
                     */
                    do {
                        Integer msgID = conQueue.poll();
                        if (msgID != null) {
                            CoapMessage msg = msgMap.get(msgID);
                            if (msg != null) {
                                msg.confirmMessage();
                                logger.log(Level.INFO, "Confirm Message with ID " + msgID);
                            }
                            continue;
                        }
                    } while (false);

                    /*
                     * send all messages from the queue with an expired
                     * timestamp
                     */
                    long waitFor = MAX_WAIT; // Maximum Wait
                    do {
                        CoapMessage msg = sendQueue.peek();
                        if (msg != null) {
                            waitFor = msg.getSendTimestamp() - System.currentTimeMillis();
                            if (waitFor <= 0) {
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
                                         * Failed to send Packet (MAX RETRANS
                                         * REACHED)
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
                            }
                        }
                    } while (false);

                    /*
                     * This wait() needs to be interrupted by a notify() when
                     * something in the queues changes
                     */
                    synchronized (sendThread) {
                        if (waitFor > MAX_WAIT) {
                            waitFor = MAX_WAIT;
                        }
                        if (waitFor < 0) {
                            logger.log(Level.WARNING, "Wait for is negative");
                        } else if (waitFor > 0) {
                            /* wait(0) means infinite */
                            sendThread.wait(waitFor);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            socket = null;
        }

        private void sendMsg(CoapMessage msg) {
            DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, msg
                    .getCoapChannel().getRemoteAddress(), msg.getCoapChannel().getRemotePort());
            packet.setData(msg.serialize());
            try {
                socket.send(packet);
                logger.log(Level.INFO, "Send Msg with ID: " + msg.getMessageID());
            } catch (IOException e) {
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
    }

    private synchronized CoapChannel getChannel(InetAddress addr, int port) {
        for (CoapChannel channel : channels) {
            if (channel.getRemoteAddress().equals(addr) && channel.getRemotePort() == port) {
                /* Found the corresponding channel */
                return channel;
            }
        }
        return null;
    }

    private synchronized void addChannel(CoapChannel channel) {
        channels.add(channel);
    }

    @Override
    public synchronized void removeChannel(CoapChannel channel) {
        channels.remove(channel);
    }

    @Override
    public synchronized void close() {
        if (channels != null)
            channels.clear();
        if (socket != null)
            socket.close(); // will throw SocketException in ReceiverThread
        socket = null;
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
        if (sendThread != null) {
            sendThread.sendMessage(message);
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
