/*
 * Copyright 2012 University of Rostock, Institute of Applied Microelectronics and Computer Engineering
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
 * 
 * This work has been sponsored by Siemens Corporate Technology. 
 *
 */
package org.ws4d.coap.proxy;



import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.ws4d.coap.connection.BasicCoapSocketHandler;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */

public class Proxy {
	static Logger logger = Logger.getLogger(BasicCoapSocketHandler.class);

	public static void main(String[] args) {
		if (args.length > 0){
			int cacheTime = Integer.parseInt(args[0]);
			Mapper.getInstance().setCacheTime(cacheTime);
		}
		
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
        // ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
        logger.setLevel(Level.INFO);
	
		HttpServerNIO httpserver = new HttpServerNIO();
		HttpClientNIO httpclient = new HttpClientNIO();
		CoapClientProxy coapclient = new CoapClientProxy();
		CoapServerProxy coapserver = new CoapServerProxy();	

		
		Mapper.getInstance().setHttpServer(httpserver);
		Mapper.getInstance().setHttpClient(httpclient);
		Mapper.getInstance().setCoapClient(coapclient);
		Mapper.getInstance().setCoapServer(coapserver);
	
		httpserver.start();
		httpclient.start();
	}
}
