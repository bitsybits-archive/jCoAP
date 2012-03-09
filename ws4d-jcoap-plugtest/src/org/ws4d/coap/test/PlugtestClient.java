/**
 * Client Application for Plugtest 2012, Paris, France
 * 
 * Execute with argument Identifier (e.g., TD_COAP_CORE_01)
 */
package org.ws4d.coap.test;

/**
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * 
 */
public class PlugtestClient {

    public static void main(String[] args) {
	if (args.length > 1 || args.length < 1) {
	    System.err.println("Too many arguments");
	    System.exit(1);
	}
	
	String testId = args[0];
	if (testId.equals("TD_COAP_CORE_01")) {
	    
	}
    }
}
