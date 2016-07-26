package org.ws4d.coap.example.blockwise.server;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;
import org.ws4d.coap.core.rest.CoapResourceServer;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * 
 */
public class CoapSampleResourceServer {

	private static CoapSampleResourceServer sampleServer;
	private CoapResourceServer resourceServer;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		sampleServer = new CoapSampleResourceServer();
		sampleServer.run();
	}

	private void run() {
		if (this.resourceServer != null)
			this.resourceServer.stop();
		this.resourceServer = new CoapResourceServer();
		
		/* add resources */
		BasicCoapResource light = new BasicCoapResource("/test/light", "Content".getBytes(), CoapMediaType.text_plain);

		light.setResourceType("light");
		light.setObservable(true);
		
		this.resourceServer.createResource(light);
		
		try {
			this.resourceServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		int counter = 0;
		while(true){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// do nothing
			}
			counter++;
			light.setValue(("Message #" + counter).getBytes());
			light.changed();
		}
	}
}
