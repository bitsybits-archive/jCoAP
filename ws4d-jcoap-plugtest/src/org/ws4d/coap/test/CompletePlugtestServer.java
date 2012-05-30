/**
 * Server Application for Plugtest 2012, Paris, France
 * 
 * Execute with argument Identifier (e.g., TD_COAP_CORE_01)
 */
package org.ws4d.coap.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.connection.BasicCoapSocketHandler;
import org.ws4d.coap.rest.CoapResourceServer;
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
	    resourceServer.createResource(new TestResource());
	    resourceServer.createResource(new LongPathResource());
	    resourceServer.createResource(new QueryResource());
	    run();
    }

    private void init() {
    BasicCoapChannelManager.getInstance().setMessageId(2000);
    if (resourceServer != null)
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
}
