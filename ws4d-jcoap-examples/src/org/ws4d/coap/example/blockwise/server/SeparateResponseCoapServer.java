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

package org.ws4d.coap.example.blockwise.server;

import org.ws4d.coap.core.CoapServer;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapServerChannel;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapResponseCode;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public class SeparateResponseCoapServer implements CoapServer {
    private static final int PORT = CoapConstants.COAP_DEFAULT_PORT;
    private CoapResponse response;
    private CoapServerChannel serverChannel;

    public static void main(String[] args) {
        System.out.println("Start CoAP Server on port " + PORT);
        SeparateResponseCoapServer server = new SeparateResponseCoapServer();

        CoapChannelManager channelManager = BasicCoapChannelManager.getInstance();
        channelManager.createServerListener(server, PORT);
    }

	@Override
	public CoapServer onAccept(CoapRequest request) {
		System.out.println("Accept connection...");
		return this;
	}

	@Override
	public void onRequest(CoapServerChannel channel, CoapRequest request) {
		System.out.println("Received message: " + request.toString());
		
		
		this.serverChannel = channel;
		this.response = channel.createSeparateResponse(request, CoapResponseCode.Content_205);
		Thread t =   new Thread( new DelayedResponder(this.response, this.serverChannel) );
	    t.start();
	}
	
	@Override
	public void onSeparateResponseFailed(CoapServerChannel channel) {
		System.out.println("Separate Response failed");
		
	}

	@Override
	public void onReset(CoapRequest lastRequest) {
		System.out.println("Received RST message");
	}
	
	private class DelayedResponder implements Runnable{
		
		private CoapResponse preparedResponse;
		private CoapServerChannel preparedChannel;
		
		public DelayedResponder(CoapResponse response, CoapServerChannel serverChannel){
			this.preparedResponse = response;
			this.preparedChannel = serverChannel;
		}
		
	    public void run()
	    {
	    	this.preparedResponse.setContentType(CoapMediaType.text_plain);
	    	this.preparedResponse.setPayload("payload...".getBytes());
	    	try {
	    		Thread.sleep(4000);
	    	} catch (InterruptedException e) {
	    		e.printStackTrace();
	    	}
	    	this.preparedChannel.sendSeparateResponse(this.preparedResponse);
	    }
	}
}
