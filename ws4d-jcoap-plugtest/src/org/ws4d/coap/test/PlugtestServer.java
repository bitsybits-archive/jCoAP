package org.ws4d.coap.test;

import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.rest.CoapResourceServer;
import org.ws4d.coap.test.resources.LongPathResource;
import org.ws4d.coap.test.resources.QueryResource;
import org.ws4d.coap.test.resources.TestResource;

/**
 * Server Application for Plugtest 2012, Paris, France Execute with argument
 * Identifier (e.g., TD_COAP_CORE_01)
 * 
 * @author Nico Laum
 * @author Christian Lerche
 */
public class PlugtestServer {

	private static final int SEPARATE_RESPONSE_TIME_MS = 1000;
	private static PlugtestServer plugtestServer;
	private CoapResourceServer resourceServer;

	public static void main(String[] args) {
		if (args.length > 1 || args.length < 1) {
			System.err.println("illegal number of arguments");
			System.exit(1);
		}
		plugtestServer = new PlugtestServer();
		plugtestServer.start(args[0]);
	}

	public void start(String testId) {
		BasicCoapChannelManager.getInstance().setMessageId(2000);
		if (this.resourceServer != null)
			this.resourceServer.stop();
		this.resourceServer = new CoapResourceServer();

		switch (testId) {
		case "TD_COAP_CORE_01":
		case "TD_COAP_CORE_03":
		case "TD_COAP_CORE_04":
		case "TD_COAP_CORE_05":
		case "TD_COAP_CORE_07":
		case "TD_COAP_CORE_08":
		case "TD_COAP_CORE_10":
		case "TD_COAP_CORE_11":
		case "TD_COAP_CORE_14":
			this.resourceServer.createResource(new TestResource());
			run();
			break;
		case "TD_COAP_CORE_02":
		case "TD_COAP_CORE_06":
			/* Nothing to setup, POST creates new resource */run();
			break;
		case "TD_COAP_CORE_09":
		case "TD_COAP_CORE_15":
		case "TD_COAP_CORE_16":
			// SPECIAL CASE: Separate Response: for these tests we cannot use
			// the resource server
			PlugtestSeparateResponseCoapServer server = new PlugtestSeparateResponseCoapServer();
			server.start(SEPARATE_RESPONSE_TIME_MS);
			break;
		case "TD_COAP_CORE_12":
			this.resourceServer.createResource(new LongPathResource());
			run();
			break;
		case "TD_COAP_CORE_13":
			this.resourceServer.createResource(new QueryResource());
			run();
			break;
		case "TD_COAP_LINK_01":
			this.resourceServer.createResource(new LongPathResource());
			this.resourceServer.createResource(new TestResource());
			run();
			break;
		case "TD_COAP_LINK_02":
			this.resourceServer.createResource(new LongPathResource());
			this.resourceServer.createResource(new TestResource());
			run();
			break;
		default:
			System.err.println("unknown test case");
			System.exit(-1);
		}
	}

	private void run() {
		try {
			this.resourceServer.start();
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
	}
}
