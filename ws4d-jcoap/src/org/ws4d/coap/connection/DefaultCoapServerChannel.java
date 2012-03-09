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

import java.net.InetAddress;

import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapServer;
import org.ws4d.coap.interfaces.CoapServerChannel;
import org.ws4d.coap.interfaces.CoapSocketHandler;
import org.ws4d.coap.messages.CoapRequest;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */


public class DefaultCoapServerChannel extends DefaultCoapChannel implements CoapServerChannel{
	CoapServer server = null;
	public DefaultCoapServerChannel(CoapSocketHandler socketHandler,
			CoapServer server, InetAddress remoteAddress,
			int remotePort) {
		super(socketHandler, remoteAddress, remotePort);
		this.server = server;
	}
	
	
	@Override
	public void newIncommingMessage(CoapMessage message) {
		/* message MUST be a request */
		if (!message.isRequest()){
			throw new IllegalStateException("Incomming server message is not a request");
		}
		CoapRequest request= (CoapRequest) message;
		server.handleRequest(request.getCoapChannel(), request);
	}
	
    /*TODO: implement */
	public void lostConnection(boolean notReachable, boolean resetByServer){
		// this could never happen for a server???
	}
}
