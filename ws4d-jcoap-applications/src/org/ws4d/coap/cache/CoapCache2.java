package org.ws4d.coap.cache;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Constants;
import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * 5.6. Caching
 * 
 * CoAP endpoints MAY cache responses in order to reduce the response time and
 * network bandwidth consumption on future, equivalent requests.
 * 
 * The goal of caching in CoAP is to reuse a prior response message to satisfy a
 * current request. In some cases, a stored response can be reused without the
 * need for a network request, reducing latency and network round-trips; a
 * "freshness" mechanism is used for this purpose (see Section 5.6.1). Even when
 * a new request is required, it is often possible to reuse the payload of a
 * prior response to satisfy the request, thereby reducing network bandwidth
 * usage; a "validation" mechanism is used for this purpose (see Section 5.6.2).
 * 
 * @author Björn Butzin <bjoern.butzin@uni-rostock.de>
 *
 */
public class CoapCache2 implements CoapClient {

	private static Cache cache;
	private static final Logger logger = LogManager.getLogger();

	public CoapCache2() {
		cache = new Cache("Coap Cache", 0, null, false, null, false, 0, 0, false, 0, null, null, 0, 0, false, false,
				false);
		CacheManager.create().addCache(cache);
	}

	public List<CoapResponse> request(CoapRequest request) {
		List<TimedCacheEntry<CoapResponse>> cacheHits = getFromCache(request);
		List<CoapResponse> result = new ArrayList<>();

		if (cacheHits.size() > 0) {
			for (TimedCacheEntry<CoapResponse> cacheHit : cacheHits) {
				// fresh?
				if (cacheHit.getExpires() < System.currentTimeMillis()) {
					CoapResponse response = cacheHit.getValue();
					response.setMaxAge((int) (cacheHit.getExpires() - System.currentTimeMillis()));
					result.add(response);
				}
				// revalidate
			}
			// if(Multicast){
			// send request to server
			// add results to cache
			// }
		} else {
			// cache miss
			// send request to server
			// add results to cache
		}

		return result;
	}

	/**
	 * 5.6.1. Freshness Model
	 * 
	 * When a response is "fresh" in the cache, it can be used to satisfy
	 * subsequent requests without contacting the origin server, thereby
	 * improving efficiency.
	 * 
	 * The mechanism for determining freshness is for an origin server to
	 * provide an explicit expiration time in the future, using the Max-Age
	 * Option (see Section 5.10.5). The Max-Age Option indicates that the
	 * response is to be considered not fresh after its age is greater than the
	 * specified number of seconds.
	 * 
	 * The Max-Age Option defaults to a value of 60. Thus, if it is not present
	 * in a cacheable response, then the response is considered not fresh after
	 * its age is greater than 60 seconds. If an origin server wishes to prevent
	 * caching, it MUST explicitly include a Max-Age Option with a value of zero
	 * seconds.
	 * 
	 * If a client has a fresh stored response and makes a new request matching
	 * the request for that stored response, the new response invalidates the
	 * old response.
	 */
	private boolean isFresh(CoapResponse response) {
		return false;
	}

	/**
	 * Unlike HTTP, the cacheability of CoAP responses does not depend on the
	 * request method, but it depends on the Response Code. The cacheability of
	 * each Response Code is defined along the Response Code definitions in
	 * Section 5.9. Response Codes that indicate success and are unrecognized by
	 * an endpoint MUST NOT be cached.
	 * 
	 * For a presented request, a CoAP endpoint MUST NOT use a stored response,
	 * unless:
	 * 
	 * the presented request method and that used to obtain the stored response
	 * match,
	 * 
	 * all options match between those in the presented request and those of the
	 * request used to obtain the stored response (which includes the request
	 * URI), except that there is no need for a match of any request options
	 * marked as NoCacheKey (Section 5.4) or recognized by the Cache and fully
	 * interpreted with respect to its specified cache behavior (such as the
	 * ETag request option described in Section 5.10.6; see also Section 5.4.2),
	 * and
	 * 
	 * the stored response is either fresh or successfully revalidated
	 */
	private boolean isCacheable(CoapResponse response) {
		return false;
	}

	private List<TimedCacheEntry<CoapResponse>> getFromCache(CoapRequest request) {
		return null;
	}

	private boolean addToCache(CoapRequest request, CoapResponse response) {
		if (!isCacheable(response)) {
			return false;
		}

		long expires = System.currentTimeMillis();
		expires += (-1 == response.getMaxAge()) ? CoapConstants.COAP_DEFAULT_MAX_AGE_MS : response.getMaxAge();
		TimedCacheEntry<CoapResponse> e = new TimedCacheEntry<>(expires, response);

		if (null == getFromCache(request)) {
			// add to cache
		} else {
			// update response
		}

		return true;
	}

	/**
	 * 5.6.2. Validation Model
	 * 
	 * When an endpoint has one or more stored responses for a GET request, but
	 * cannot use any of them (e.g., because they are not fresh), it can use the
	 * ETag Option (Section 5.10.6) in the GET request to give the origin server
	 * an opportunity both to select a stored response to be used, and to update
	 * its freshness. This process is known as "validating" or "revalidating"
	 * the stored response.
	 * 
	 * When sending such a request, the endpoint SHOULD add an ETag Option
	 * specifying the entity-tag of each stored response that is applicable.
	 * 
	 * A 2.03 (Valid) response indicates the stored response identified by the
	 * entity-tag given in the response's ETag Option can be reused after
	 * updating it as described in Section 5.9.1.3.
	 * 
	 * Any other Response Code indicates that none of the stored responses
	 * nominated in the request is suitable. Instead, the response SHOULD be
	 * used to satisfy the request and MAY replace the stored response.
	 */
	private void revalidate(CoapResponse response) {
		if (null != response.getETag()) {

			// FIXME where to get them
			String sAddress = null;
			int sPort = CoapConstants.COAP_DEFAULT_PORT;

			CoapClientChannel clientChannel;
			try {
				clientChannel = BasicCoapChannelManager.getInstance().connect(this, InetAddress.getByName(sAddress),
						sPort);

				CoapRequest request = clientChannel.createRequest(true, CoapRequestCode.GET);
				request.setUriPath("/statistic");
				request.addETag(response.getETag());

				clientChannel.sendMessage(request);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void onResponse(CoapClientChannel channel, CoapResponse response) {
		// TODO match request and response
		// add to cache

	}

	public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort) {
		// TODO match request and response
		// add to cache
	}

	public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
		if (notReachable) {
			logger.warn("Target not reachable");
		} else if (resetByServer) {
			logger.warn("Connection reset by the server");
		}
		logger.warn("Connection failed for unknown reason");
	}

	private class TimedCacheEntry<A> {

		private final Long expires;
		private final A value;

		TimedCacheEntry(Long expires, A value) {
			this.expires = expires;
			this.value = value;
		}

		public Long getExpires() {
			return this.expires;
		}

		public A getValue() {
			return this.value;
		}
	}
}
