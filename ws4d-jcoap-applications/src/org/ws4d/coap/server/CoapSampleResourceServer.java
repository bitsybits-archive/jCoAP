package org.ws4d.coap.server;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.rest.BasicCoapResource;
import org.ws4d.coap.rest.CoapResourceServer;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * 
 */
public class CoapSampleResourceServer {

	private static CoapSampleResourceServer sampleServer;
	private CoapResourceServer resourceServer;
	private static Logger logger = Logger
			.getLogger(CoapSampleResourceServer.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
		logger.setLevel(Level.INFO);
		logger.info("Start Sample Resource Server");
		sampleServer = new CoapSampleResourceServer();
		sampleServer.run();
	}

	private void run() {
		if (resourceServer != null)
			resourceServer.stop();
		resourceServer = new CoapResourceServer();
		
		/* add resources */
		resourceServer.createResource(new BasicCoapResource("/test/light", "Content".getBytes(), CoapMediaType.text_plain));
		
		
		
		try {
			resourceServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
