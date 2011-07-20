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

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Random;

import org.ws4d.coap.Constants;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapSocketListener;

public class DefaultCoapChannelManager implements CoapChannelManager {
    // global message id
    private int messageId;
    private static DefaultCoapChannelManager instance;
    private HashMap<Integer, DatagramSocket> socketMap = new HashMap<Integer, DatagramSocket>();

    private DefaultCoapChannelManager() {
        reset();
    }

    public synchronized static CoapChannelManager getInstance() {
        if (instance == null) {
            instance = new DefaultCoapChannelManager();
        }
        return instance;
    }

    // /**
    // * Create a CoapConnection to a remote Socket, choosing a random local
    // port
    // */
    // @Override
    // public synchronized CoapClientChannel getClientChannel(String remoteHost,
    // int remotePort) {
    // CoapClientChannel connection = new DefaultCoapClientChannel(this);
    // connection.connect(remoteHost, remotePort);
    // return connection;
    // }
    //
    // /**
    // * Create a CoapConnection to a remote Socket, choosing a random local
    // port
    // */
    // @Override
    // public synchronized CoapServerChannel getServerChannel(int port) {
    // CoapServerChannel serverChannel = new DefaultCoapServerChannel(this);
    // serverChannel.listen(port);
    // return serverChannel;
    // }

    /**
     * Creates a new, global message id for a new COAP message
     */
    @Override
    public synchronized int getNewMessageID() {
        if (messageId < Constants.MESSAGE_ID_MAX) {
            ++messageId;
        } else
            messageId = Constants.MESSAGE_ID_MIN;
        return messageId;
    }

    @Override
    public synchronized void reset() {
        // generate random 16 bit messageId
        Random random = new Random();
        messageId = random.nextInt(Constants.MESSAGE_ID_MAX + 1);
    }

    @Override
    public CoapSocketListener createSocketListener(int localPort) {
        DatagramSocket socket = null;
        if (!socketMap.containsKey(localPort)) {
            try {
                socket = new DatagramSocket(localPort);
                socketMap.put(localPort, socket);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        } else {
            socket = socketMap.get(localPort);
        }

        return (socket != null) ? new DefaultCoapSocketListener(this, socket) : null;
    }
}
