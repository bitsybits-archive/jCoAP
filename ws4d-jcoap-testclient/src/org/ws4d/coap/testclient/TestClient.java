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

package org.ws4d.coap.testclient;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.ws4d.coap.connection.DefaultCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapServerHandler;
import org.ws4d.coap.interfaces.CoapSocketListener;
import org.ws4d.coap.messages.CoapHeaderOption;
import org.ws4d.coap.messages.CoapHeaderOptions.HeaderOptionNumber;
import org.ws4d.coap.messages.CoapMessageCode.MessageCode;

public class TestClient implements CoapServerHandler {

    // private static final String UNICAST_HOST = "[aaaa::212:7400:117b:6dd4]";
    private static final String UNICAST_HOST = "localhost";
    private static final String MULTICAST_HOST = "[ff02::1]";
    private static final String PORT = "61616";
    private static final int LOCAL_PORT = 12345;
    URI uri;

    public static void main(String[] args) {
        TestClient client = new TestClient();
        try {
            client.testUnicastRequest();
        } catch (UnknownHostException e) {
            System.err.println("Unicast test failed...");
            e.printStackTrace();
        }
        // client.testMulticastRequest(coapMessage);
    }

    private void testUnicastRequest() throws UnknownHostException {
        // setup channel
        try {
            uri = new URI("coap://" + UNICAST_HOST + ":" + PORT + "/helloworld");
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
            return;
        }

        CoapChannelManager connectionManager = DefaultCoapChannelManager.getInstance();
        CoapSocketListener socketListener = connectionManager.createSocketListener(LOCAL_PORT);

        /* To be a Server and accept channels */
        CoapChannel channel = socketListener.connect(this, InetAddress.getByName(uri.getHost()),
                uri.getPort());
        socketListener.setCoapServerHandler(this);

        /* create a CoAP Message */
        CoapMessage coapMessage = channel.createRequest(true, MessageCode.GET);
        CoapMessage coapMessage2 = channel.createRequest(true, MessageCode.GET);

        String path = uri.getPath();
        String[] inputSegments = path.split("/");
        // Message 1
        for (int i = 0; i < inputSegments.length; i++) {
            String location = inputSegments[i];
            if (location.length() > 0)
                coapMessage.getHeader().addOption(
                        new CoapHeaderOption(HeaderOptionNumber.Uri_Path, location.getBytes()));
        }
        channel.sendMessage(coapMessage);
        System.out.println("Send message: " + coapMessage.getHeader().toStringShort());

        // Message 2
        coapMessage2.setPayload("Test");
        inputSegments = path.split("/");
        for (int i = 0; i < inputSegments.length; i++) {
            String location = inputSegments[i];
            if (location.length() > 0)
                coapMessage2.getHeader().addOption(
                        new CoapHeaderOption(HeaderOptionNumber.Uri_Path, location.getBytes()));
        }
        channel.sendMessage(coapMessage2);
        System.out.println("Send message: " + coapMessage2.getHeader().toStringShort());

        // close channel
        try {
            synchronized (this) {
                this.wait(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CoapMessage onReceivedMessage(CoapMessage msg) {
        System.out.println("Received message: " + msg.toString());
        return null;
    }

    @Override
    public void onLostConnection() {
        System.out.println("Lost connection...");

    }

    @Override
    public boolean onAccept(CoapChannel newChannel) {
        System.out.println("Accept connection...");
        newChannel.setCoapChannelHandler(this);
        return true;
    }

    // private void testMulticastRequest() {
    // /* create a CoAP Message */
    // DefaultCoapMessage coapMessage = new DefaultCoapMessage();
    // try {
    // coapMessage.getHeader().addOption(new
    // CoapHeaderOption(HeaderOptionNumber.Uri_Path, "helloworld".getBytes()));
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // System.out.println("Send message: " + coapMessage.toString());
    //
    // try {
    // uri = new URI("coap://" + TestClient.MULTICAST_HOST + ":" + PORT +
    // "/helloworld");
    // CoapChannelManager connectionManager =
    // DefaultCoapChannelManager.getInstance();
    // CoapSocketListener socketListener =
    // connectionManager.createSocketListener(61616);
    // CoapChannel channel = null;
    // try {
    // channel = socketListener.connect(InetAddress.getByName(uri.getHost()),
    // uri.getPort());
    // } catch (UnknownHostException e) {
    // System.out.println("connect failed");
    // e.printStackTrace();
    // }
    // channel.sendMessage(coapMessage);
    // } catch (URISyntaxException e) {
    // e.printStackTrace();
    // }
    // }

}
