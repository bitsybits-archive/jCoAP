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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ws4d.coap.core.CoapConstants;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */

public class Proxy {
	private static final Logger logger = LogManager.getLogger();
	private static int defaultCachingTime = CoapConstants.COAP_DEFAULT_MAX_AGE_S;

	public static void main(String[] args) {

		CommandLineParser cmdParser = new DefaultParser();

		/* Add command line options */
		Options options = new Options();
		options.addOption("c", "default-cache-time", true, "Default caching time in seconds");
		CommandLine cmd = null;
		try {
			cmd = cmdParser.parse(options, args);
			if (cmd.hasOption("c")) {
				defaultCachingTime = Integer.parseInt(cmd.getOptionValue("c"));
				if (defaultCachingTime == 0) {
					ProxyMapper.getInstance().setCacheEnabled(false);
				}
				logger.info("Set caching time to " + cmd.getOptionValue("c") + " seconds (0 disables the cache)");
			}
		} catch (ParseException e) {
			logger.warn("Unexpected exception:" + e.getLocalizedMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("jCoAP-Proxy", options);
			System.exit(-1);
		}

		HttpServerNIO httpserver = new HttpServerNIO();
		HttpClientNIO httpclient = new HttpClientNIO();
		CoapClientProxy coapclient = new CoapClientProxy();
		CoapServerProxy coapserver = new CoapServerProxy();

		ProxyMapper.getInstance().setHttpServer(httpserver);
		ProxyMapper.getInstance().setHttpClient(httpclient);
		ProxyMapper.getInstance().setCoapClient(coapclient);
		ProxyMapper.getInstance().setCoapServer(coapserver);

		httpserver.start();
		httpclient.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("===END===");
			}
		});

		ProxyRestInterface restInterface = new ProxyRestInterface();
		restInterface.start();
	}
}
