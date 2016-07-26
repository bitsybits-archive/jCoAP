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
public class PlugtestServer {

	private static PlugtestServer plugtestServer;
	private CoapResourceServer resourceServer;
	private static Logger logger = Logger
			.getLogger(BasicCoapSocketHandler.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 1 || args.length < 1) {
			System.err.println("illegal number of arguments");
			System.exit(1);
		}
		logger.setLevel(Level.WARNING);
		plugtestServer = new PlugtestServer();
		plugtestServer.start(args[0]);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("PlugtestServer is now stopping.");
				System.out.println("===END===");
			}
		});
	}

	public void start(String testId) {
		System.out.println("===Run Test Server: " + testId + "===");
		init();
		if (testId.equals("TD_COAP_CORE_01")) {
			this.resourceServer.createResource(new TestResource());
			run();
		} else if (testId.equals("TD_COAP_CORE_02")) {
			/* Nothing to setup, POST creates new resource */
			run();
		} else if (testId.equals("TD_COAP_CORE_03")) {
			this.resourceServer.createResource(new TestResource());
			run();
		} else if (testId.equals("TD_COAP_CORE_04")) {
			this.resourceServer.createResource(new TestResource());
			run();
		} else if (testId.equals("TD_COAP_CORE_05")) {
			this.resourceServer.createResource(new TestResource());
			run();
		} else if (testId.equals("TD_COAP_CORE_06")) {
			/* Nothing to setup, POST creates new resource */
			run();
		} else if (testId.equals("TD_COAP_CORE_07")) {
			this.resourceServer.createResource(new TestResource());
			run();
		} else if (testId.equals("TD_COAP_CORE_08")) {
			this.resourceServer.createResource(new TestResource());
			run();
		} else if (testId.equals("TD_COAP_CORE_09")) {
			/*
			 * === SPECIAL CASE: Separate Response: for these tests we cannot
			 * use the resource server
			 */
			PlugtestSeparateResponseCoapServer server = new PlugtestSeparateResponseCoapServer();
			server.start(TestConfiguration.SEPARATE_RESPONSE_TIME_MS);
		} else if (testId.equals("TD_COAP_CORE_10")) {
			this.resourceServer.createResource(new TestResource());
			run();
		} else if (testId.equals("TD_COAP_CORE_11")) {
			this.resourceServer.createResource(new TestResource());
			run();
		} else if (testId.equals("TD_COAP_CORE_12")) {
			this.resourceServer.createResource(new LongPathResource());
			run();
		} else if (testId.equals("TD_COAP_CORE_13")) {
			this.resourceServer.createResource(new QueryResource());
			run();
		} else if (testId.equals("TD_COAP_CORE_14")) {
			this.resourceServer.createResource(new TestResource());
			run();
		} else if (testId.equals("TD_COAP_CORE_15")) {
			/*
			 * === SPECIAL CASE: Separate Response: for these tests we cannot
			 * use the resource server
			 */
			PlugtestSeparateResponseCoapServer server = new PlugtestSeparateResponseCoapServer();
			server.start(TestConfiguration.SEPARATE_RESPONSE_TIME_MS);
		} else if (testId.equals("TD_COAP_CORE_16")) {
			/*
			 * === SPECIAL CASE: Separate Response: for these tests we cannot
			 * use the resource server
			 */
			PlugtestSeparateResponseCoapServer server = new PlugtestSeparateResponseCoapServer();
			server.start(TestConfiguration.SEPARATE_RESPONSE_TIME_MS);
		} else if (testId.equals("TD_COAP_LINK_01")) {
			this.resourceServer.createResource(new LongPathResource());
			this.resourceServer.createResource(new TestResource());
			run();
		} else if (testId.equals("TD_COAP_LINK_02")) {
			this.resourceServer.createResource(new LongPathResource());
			this.resourceServer.createResource(new TestResource());
			run();
		} else if (testId.equals("TD_COAP_BLOCK_01")) {
		} else if (testId.equals("TD_COAP_BLOCK_02")) {
		} else if (testId.equals("TD_COAP_BLOCK_03")) {
		} else if (testId.equals("TD_COAP_BLOCK_04")) {
		} else if (testId.equals("TD_COAP_OBS_01")) {
		} else if (testId.equals("TD_COAP_OBS_02")) {
		} else if (testId.equals("TD_COAP_OBS_03")) {
		} else if (testId.equals("TD_COAP_OBS_04")) {
		} else if (testId.equals("TD_COAP_OBS_05")) {
		} else {
			System.out.println("unknown test case");
			System.exit(-1);
		}

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
