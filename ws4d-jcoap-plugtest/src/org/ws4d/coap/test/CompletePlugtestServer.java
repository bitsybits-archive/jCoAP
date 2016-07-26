/**
 * Server Application for Plugtest 2012, Paris, France
 * 
 * Execute with argument Identifier (e.g., TD_COAP_CORE_01)
 */
package org.ws4d.coap.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.BasicCoapSocketHandler;
import org.ws4d.coap.core.rest.CoapResourceServer;
import org.ws4d.coap.test.resources.LongPathResource;
import org.ws4d.coap.test.resources.QueryResource;
import org.ws4d.coap.test.resources.TestResource;

/**
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * 
 */
public class CompletePlugtestServer {

	private static CompletePlugtestServer plugtestServer;
	private CoapResourceServer resourceServer;
	private static Logger logger = Logger
			.getLogger(BasicCoapSocketHandler.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		logger.setLevel(Level.WARNING);
		plugtestServer = new CompletePlugtestServer();
		plugtestServer.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("PlugtestServer is now stopping.");
				System.out.println("===END===");
			}
		});
	}

	public void start() {
		System.out.println("===Run Test Server ===");
		init();

		this.resourceServer.createResource(new TestResource());
		this.resourceServer.createResource(new LongPathResource());
		this.resourceServer.createResource(new QueryResource());
		run();
	}

	private void init() {
		BasicCoapChannelManager.getInstance().setMessageId(2000);
		if (this.resourceServer != null)
			this.resourceServer.stop();
		this.resourceServer = new CoapResourceServer();

	}

	private void run() {
		try {
			this.resourceServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
