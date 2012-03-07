/**
 * 
 */
package org.ws4d.coap.test;

import org.ws4d.coap.rest.CoapResourceServer;
import org.ws4d.coap.test.resources.LongPathResource;
import org.ws4d.coap.test.resources.TestResource;

/**
 * @author Nico Laum <nico.laum@uni-rostock.de>
 *
 */
public class PlugtestServer {

    private static PlugtestServer plugtestServer;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
	plugtestServer = new PlugtestServer();
	plugtestServer.runTest(12);
    }
    
    private void runTest(int test) {
	CoapResourceServer resourceServer = new CoapResourceServer();

	switch (test) {
	case 1:
	    resourceServer.createResource(new TestResource());
	    break;
	case 2:
	    break;
	case 12:
	    resourceServer.createResource(new LongPathResource());
	    break;
	default:
	    break;
	}
	
	
	try {
	    resourceServer.start();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
