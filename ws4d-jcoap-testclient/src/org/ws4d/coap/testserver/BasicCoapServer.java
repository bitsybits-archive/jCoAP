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

package org.ws4d.coap.testserver;

import org.ws4d.coap.connection.DefaultCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelListener;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapServerListener;
import org.ws4d.coap.messages.CoapMessageCode.MessageCode;

public class BasicCoapServer implements CoapServerListener, CoapChannelListener {
    private static final int PORT = 61616;
    static int counter = 0;

    public static void main(String[] args) {
        System.out.println("Start CoAP Server on port " + PORT);
        BasicCoapServer server = new BasicCoapServer();

        CoapChannelManager channelManager = DefaultCoapChannelManager.getInstance();
        channelManager.createServerListener(server, PORT);
    }

    @Override
    public void onLostConnection() {
        System.out.println("Connection lost...");
    }

    @Override
    public boolean onAccept(CoapChannel newChannel) {
        System.out.println("Accept connection...");
        newChannel.setCoapChannelHandler(this);
        return true;
    }

    @Override
    public void onReceivedMessage(CoapMessage msg) {
        System.out.println("Received message: " + msg.getHeader().toStringShort());
        CoapChannel channel = msg.getCoapChannel();
        CoapMessage response = channel.createResponse(msg,
                MessageCode.Bad_Request_400);
        channel.sendMessage(response);
    }
}
