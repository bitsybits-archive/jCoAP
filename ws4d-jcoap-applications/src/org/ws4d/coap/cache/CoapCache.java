/*
 * Copyright 2012 University of Rostock, Institute of Applied Microelectronics and Computer Engineering
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This work has been sponsored by Siemens Corporate Technology. 
 *
 */
package org.ws4d.coap.cache;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.messages.api.CoapResponse;
import org.ws4d.coap.proxy.ProxyMessageContext;
import org.ws4d.coap.proxy.ProxyResource;
import org.ws4d.coap.proxy.ProxyResourceKey;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */

/*
 * TODO's: - implement Date option as described in
 * "Connecting the Web with the Web of Things: Lessons Learned From Implementing a CoAP-HTTP Proxy"
 * - caching of HTTP resources not supported as HTTP servers may have enough
 * resources (in terms of RAM/ROM/batery/computation)
 */

public class CoapCache {
	private static final Logger logger = LogManager.getLogger();
	private static final int MAX_LIFETIME = Integer.MAX_VALUE;
	private static final int defaultMaxAge = CoapConstants.COAP_DEFAULT_MAX_AGE_S;
	private static final ProxyCacheTimePolicy cacheTimePolicy = ProxyCacheTimePolicy.Halftime;
	private static Cache cache;
	private static CacheManager cacheManager;
	private boolean enabled = true;

	public CoapCache() {
		cacheManager = CacheManager.create();
		cache = new Cache(new CacheConfiguration("proxy", 100)
				.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
				.overflowToDisk(true)
				.eternal(false)
				.diskPersistent(false)
				.diskExpiryThreadIntervalSeconds(0));
		
		cacheManager.addCache(cache);
	}

	public void removeKey(URI uri) {
		cache.remove(uri);
	}

	public boolean isInCache(ProxyResourceKey key) {
		if (isEnabled() && cache.isKeyInCache(key)) {
			return true;
		}
		return false;
	}

	private static boolean insertElement(ProxyResourceKey key, ProxyResource resource) {
		Element elem = new Element(key, resource);
		if (resource.expires() == -1) {
			/* never expires */
			cache.put(elem);
		} else {
			long ttl = resource.expires() - System.currentTimeMillis();
			if (ttl > 0) {
				/* limit the maximum lifetime */
				if (ttl > MAX_LIFETIME) {
					ttl = MAX_LIFETIME;
				}
				elem.setTimeToLive((int) ttl);
				cache.put(elem);
				logger.debug("cache insert: " + resource.getPath());

			} else {
				/* resource is already expired */
				return false;
			}
		}
		return true;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ProxyResource get(ProxyMessageContext context) {
		if (!isEnabled()) {
			return null;
		}

		String path = context.getUri().getPath();
		if (path == null) {
			/* no caching */
			return null;
		}
		Element elem = cache.get(new ProxyResourceKey(context.getServerAddress(), context.getServerPort(), path));
		logger.debug(
				"cache get: " + context.getServerAddress().toString() + " " + context.getServerPort() + " " + path);
		if (elem != null) {
			/* found cached entry */
			ProxyResource res = (ProxyResource) elem.getObjectValue();
			if (!res.isExpired()) {
				return res;
			}
		}
		return null;
	}

	public void cacheCoapResponse(ProxyMessageContext context) {
		if (!isEnabled()) {
			return;
		}

		CoapResponse response = context.getInCoapResponse();

		String path = context.getUri().getPath();
		if (path == null) {
			/* no caching */
			return;
		}

		ProxyResourceKey key = new ProxyResourceKey(context.getServerAddress(), context.getServerPort(), path);

		/*
		 * NOTE: - currently caching is only implemented for success error codes
		 * (2.xx) - not fresh resources are removed (could be used for
		 * validation model)
		 */

		switch (context.getInCoapResponse().getResponseCode()) {
		case Created_201:
			/*
			 * A cache SHOULD mark any stored response for the created resource
			 * as not fresh. This response is not cacheable.
			 */
			cache.remove(key);
			break;
		case Deleted_202:
			/*
			 * This response is not cacheable. However, a cache SHOULD mark any
			 * stored response for the deleted resource as not fresh.
			 */
			cache.remove(key);
			break;
		case Valid_203:
			/*
			 * When a cache receives a 2.03 (Valid) response, it needs to update
			 * the stored response with the value of the Max-Age Option included
			 * in the response (see Section 5.6.2).
			 */
			// TODO
			break;
		case Changed_204:
			/*
			 * This response is not cacheable. However, a cache SHOULD mark any
			 * stored response for the changed resource as not fresh.
			 */
			cache.remove(key);
			break;
		case Content_205:
			/*
			 * This response is cacheable: Caches can use the Max-Age Option to
			 * determine freshness (see Section 5.6.1) and (if present) the ETag
			 * Option for validation (see Section 5.6.2).
			 */
			/* CACHE RESOURCE */
			ProxyResource resource = new ProxyResource(path, response.getPayload(), response.getContentType());
			resource.setExpires(cacheTimePolicy.calcExpires(context.getRequestTime(), context.getResponseTime(),
					response.getMaxAge()));
			insertElement(key, resource);
			break;

		default:
			break;
		}
	}

	public enum ProxyCacheTimePolicy {
		Request, Response, Halftime;

		public long calcExpires(long requestTime, long responseTime, long maxAge) {
			long max = maxAge == -1 ? defaultMaxAge : maxAge;
			switch (this) {
			case Request:
				return requestTime + (max * 1000);
			case Response:
				return responseTime + (max * 1000);
			case Halftime:
				return requestTime + ((responseTime - requestTime) / 2) + (max * 1000);
			default:
				return 0;
			}
		}
	}
}
