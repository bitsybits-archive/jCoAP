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

import org.ws4d.coap.core.CoapServer;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapServerChannel;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapResponseCode;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;

public class PlugtestSeparateResponseCoapServer implements CoapServer {
	private static final int PORT = 5683;
	CoapResponse response = null;
	CoapServerChannel ch = null;
	int responseTimeMs = 4000;

	public void start(int separateResponseTimeMs) {
		CoapChannelManager channelManager = BasicCoapChannelManager.getInstance();
		channelManager.createServerListener(this, PORT);
		this.responseTimeMs = separateResponseTimeMs;
	}

	@Override
	public CoapServer onAccept(CoapRequest request) {
		System.out.println("Accept connection...");
		return this;
	}

	@Override
	public void onRequest(CoapServerChannel channel, CoapRequest request) {
		System.out.println("Received message: " + request.toString());

		this.ch = channel;
		this.response = channel.createSeparateResponse(request, CoapResponseCode.Content_205);
		(new Thread(new SendDelayedResponse())).start();
	}

	public class SendDelayedResponse implements Runnable {

		public void run() {
			PlugtestSeparateResponseCoapServer.this.response.setContentType(CoapMediaType.text_plain);
			PlugtestSeparateResponseCoapServer.this.response.setPayload("payload...".getBytes());
			try {
				Thread.sleep(PlugtestSeparateResponseCoapServer.this.responseTimeMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			PlugtestSeparateResponseCoapServer.this.ch
					.sendSeparateResponse(PlugtestSeparateResponseCoapServer.this.response);
			System.out
					.println("Send separate Response: " + PlugtestSeparateResponseCoapServer.this.response.toString());
		}
	}

	@Override
	public void onSeparateResponseFailed(CoapServerChannel channel) {
		System.out.println("Separate Response failed");
	}

	@Override
	public void onReset(CoapRequest lastRequest) {
		// empty
	}
}
