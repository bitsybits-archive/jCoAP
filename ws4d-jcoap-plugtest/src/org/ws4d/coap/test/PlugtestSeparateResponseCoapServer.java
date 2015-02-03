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

package org.ws4d.coap.test;

import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.interfaces.CoapServer;
import org.ws4d.coap.interfaces.CoapServerChannel;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapResponseCode;

public class PlugtestSeparateResponseCoapServer implements CoapServer {
    private static final int PORT = 5683;
    static int counter = 0;
    CoapResponse response = null;
    CoapServerChannel channel = null;
    int separateResponseTimeMs = 4000;
    
    public void start(int separateResponseTimeMs){
    	CoapChannelManager channelManager = BasicCoapChannelManager.getInstance();
    	channelManager.createServerListener(this, PORT);
    	this.separateResponseTimeMs = separateResponseTimeMs;
    }

	@Override
	public CoapServer onAccept(CoapRequest request) {
		System.out.println("Accept connection...");
		return this;
	}

	@Override
	public void onRequest(CoapServerChannel channel, CoapRequest request) {
		System.out.println("Received message: " + request.toString());

		this.channel = channel;
		response = channel.createSeparateResponse(request, CoapResponseCode.Content_205);
		(new Thread( new SendDelayedResponse())).start();
	}
	
	public class SendDelayedResponse implements Runnable
	  {
	    public void run()
	    {
	    	response.setContentType(CoapMediaType.text_plain);
	    	response.setPayload("payload...".getBytes());
	    	try {
	    		Thread.sleep(separateResponseTimeMs);
	    	} catch (InterruptedException e) {
	    		e.printStackTrace();
	    	}
	    	channel.sendSeparateResponse(response);
			System.out.println("Send separate Response: " + response.toString());
	    }
	}

	@Override
	public void onSeparateResponseFailed(CoapServerChannel channel) {
		System.out.println("Separate Response failed");
	}
	
}
