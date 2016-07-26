package org.ws4d.coap.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;
import org.ws4d.coap.core.rest.CoapResourceServer;

public class ProxyRestInterface {
	private static Logger logger = LogManager.getLogger();
	private CoapResourceServer resourceServer;

	public void start() {
		if (this.resourceServer != null){
			this.resourceServer.stop();
		}
		this.resourceServer = new CoapResourceServer();
		this.resourceServer.createResource(new ProxyStatisticResource());
		try {
			this.resourceServer.start(CoapConstants.COAP_DEFAULT_PORT+1);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	public class ProxyStatisticResource extends BasicCoapResource {

		public ProxyStatisticResource() {
			super("/statistic", new byte[0], CoapMediaType.text_plain);
		}

		public byte[] getValue() {
			StringBuilder val = new StringBuilder();
			ProxyMapper.getInstance().getCoapRequestCount();
			val.append("Number of HTTP Requests: " + ProxyMapper.getInstance().getHttpRequestCount() + "\n");
			val.append("Number of CoAP Requests: " + ProxyMapper.getInstance().getCoapRequestCount() + "\n");
			val.append("Number of Reqeusts served from cache: " + ProxyMapper.getInstance().getServedFromCacheCount()
					+ "\n");
			return val.toString().getBytes();
		}
	}
}
