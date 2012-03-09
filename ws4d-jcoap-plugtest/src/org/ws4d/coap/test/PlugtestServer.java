/**
 * Server Application for Plugtest 2012, Paris, France
 * 
 * Execute with argument Identifier (e.g., TD_COAP_CORE_01)
 */
package org.ws4d.coap.test;

import org.ws4d.coap.rest.CoapResource;
import org.ws4d.coap.rest.CoapResourceServer;
import org.ws4d.coap.test.resources.DelayedResource;
import org.ws4d.coap.test.resources.LargeResource;
import org.ws4d.coap.test.resources.LongPathResource;
import org.ws4d.coap.test.resources.ObservableResource;
import org.ws4d.coap.test.resources.QueryResource;
import org.ws4d.coap.test.resources.TestResource;

/**
 * @author Nico Laum <nico.laum@uni-rostock.de>
 *
 */
public class PlugtestServer {

    private static PlugtestServer plugtestServer;
    private CoapResourceServer resourceServer;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
	if (args.length > 1 || args.length < 1) {
	    System.err.println("Too many arguments");
	    System.exit(1);
	}
	
	plugtestServer = new PlugtestServer();
	
	String testId = args[0];
	if (testId.equals("TD_COAP_CORE_01")) {
	    plugtestServer.runTestServer(new TestResource());
	} else if (testId.equals("TD_COAP_CORE_02")) {
	    plugtestServer.runTestServer();
	} else if (testId.equals("TD_COAP_CORE_03")) {
	    plugtestServer.runTestServer(new TestResource());
	} else if (testId.equals("TD_COAP_CORE_04")) {
	    plugtestServer.runTestServer(new TestResource());
	} else if (testId.equals("TD_COAP_CORE_05")) {
	    plugtestServer.runTestServer(new TestResource());
	} else if (testId.equals("TD_COAP_CORE_06")) {
	    plugtestServer.runTestServer();
	} else if (testId.equals("TD_COAP_CORE_07")) {
	    plugtestServer.runTestServer(new TestResource());
	} else if (testId.equals("TD_COAP_CORE_08")) {
	    plugtestServer.runTestServer(new TestResource());
	} else if (testId.equals("TD_COAP_CORE_09")) {
	    plugtestServer.runTestServer(new DelayedResource());
	} else if (testId.equals("TD_COAP_CORE_10")) {
	    plugtestServer.runTestServer(new TestResource());
	} else if (testId.equals("TD_COAP_CORE_11")) {
	    plugtestServer.runTestServer(new TestResource());
	} else if (testId.equals("TD_COAP_CORE_12")) {
	    plugtestServer.runTestServer(new LongPathResource());
	} else if (testId.equals("TD_COAP_CORE_13")) {
	    plugtestServer.runTestServer(new QueryResource());
	} else if (testId.equals("TD_COAP_CORE_14")) {
	    plugtestServer.runTestServer(new TestResource());
	} else if (testId.equals("TD_COAP_CORE_15")) {
	    plugtestServer.runTestServer(new DelayedResource());
	} else if (testId.equals("TD_COAP_LINK_01")) {
	    plugtestServer.runTestServer(new TestResource());
	} else if (testId.equals("TD_COAP_LINK_02")) {
	    plugtestServer.setupServer();
	    plugtestServer.attachCoapResource(new LongPathResource());
	    plugtestServer.attachCoapResource(new TestResource());
	    plugtestServer.run();
	} else if (testId.equals("TD_COAP_BLOCK_01")) {
	    plugtestServer.runTestServer(new LargeResource());
	} else if (testId.equals("TD_COAP_BLOCK_02")) {
	    plugtestServer.runTestServer(new LargeResource());
	} else if (testId.equals("TD_COAP_BLOCK_03")) {
	    plugtestServer.runTestServer(new LargeResource());
	} else if (testId.equals("TD_COAP_BLOCK_04")) {
	    plugtestServer.runTestServer();
	} else if (testId.equals("TD_COAP_OBS_01")) {
	    plugtestServer.runTestServer(new ObservableResource());
	} else if (testId.equals("TD_COAP_OBS_02")) {
	    plugtestServer.runTestServer(new ObservableResource());
	} else if (testId.equals("TD_COAP_OBS_03")) {
	    plugtestServer.runTestServer(new ObservableResource());
	} else if (testId.equals("TD_COAP_OBS_04")) {
	    plugtestServer.runTestServer(new ObservableResource());
	} else if (testId.equals("TD_COAP_OBS_05")) {
	    plugtestServer.runTestServer(new ObservableResource());
	}
    }
    
    private void attachCoapResource(CoapResource resource) {
	resourceServer.createResource(resource);
    }
    
    private void setupServer() {
	if (resourceServer!=null)
	    resourceServer.stop();
	resourceServer = new CoapResourceServer();
    }
    
    private void run() {
	try {
	    resourceServer.start();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private void runTestServer(CoapResource resource) {
	setupServer();
	if (resource!=null) {
	    attachCoapResource(resource);
	}
	run();
    }
    
    private void runTestServer() {
	runTestServer(null);
    }
}
