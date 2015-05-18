/* Copyright 2015 University of Rostock
 
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
 *****************************************************************************/

package org.ws4d.coap.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.rest.BasicCoapResource;
import org.ws4d.coap.rest.CoapResource;
import org.ws4d.coap.rest.CoapResourceServer;

/**
 * Tests for jCoAP.
 *
 * @author Bj�rn Butzin <bjoern.butzin[at]uni-rostock.de>
 */
public class BasicResourceTest {

	private static CoapResourceServer resourceServer;

	// @Test //indicates a test method
	// @Test(expected= IndexOutOfBoundsException.class) //indicates a test
	// method expecting an exception
	// @Test(timeout=1000) //indicates a test method that fails after 1000
	// milliseconds execution time
	// org.junit.Assert.* // compare result & expectation

	/*
	 * ########################################################################
	 * General Test preparations
	 * ########################################################################
	 */

	@BeforeClass
	public static void setUpClass() {
		// set up server
		if (resourceServer != null) {
			resourceServer.stop();
		}
		resourceServer = new CoapResourceServer();
		try {
			resourceServer.start();
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(-1);
		}
	}

	@AfterClass
	public static void tearDownClass() {
		// tear down server
		if (resourceServer != null) {
			resourceServer.stop();
			resourceServer = null;
		}
	}

	@Before
	public void setUp() {
		// intentionally left blank
	}

	@After
	public void tearDown() {
		// reset server
		//FIXME: causes ConcurrentModificationException
		for (String path : resourceServer.getResources().keySet()) {
			if(path!="/.well-known/core"){
				System.out.println(path);
				resourceServer.deleteResource(path);
			}
		}
	}

	/*
	 * Tests
	 */

	@Test
	public void thisAlwaysPasses() {
		// validate test fixtures
	}

	/*
	 * test percentage encoding
	 * Path separator: (U+002F SOLIDUS "/") 
	 * First Argument led by: (U+003F QUESTION MARK "?")
	 * Argument separator: (U+0026 AMPERSAND "&") 
	 * An empty path component is equivalent to an absolute path of "/"
	 * The scheme and host are case insensitive and normally provided in lowercase
	 * 
	 * For example, the following three URIs are equivalent and cause the same
	 * options and option values to appear in the CoAP messages:
	 * coap://example.com:5683/~sensors/temp.xml
	 * coap://EXAMPLE.com/%7Esensors/temp.xml
	 * coap://EXAMPLE.com:/%7esensors/temp.xml
	 */

	/*
	 * ########################################################################
	 * invalid resources
	 * ########################################################################
	 */

	@Test(expected = Exception.class)
	public void invalidPathBadSeparatorResource() {
		CoapResource res = new BasicCoapResource("\\", "".getBytes(),
				CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}

	@Test(expected = Exception.class)
	public void invalidNameTooLongResource() {
		CoapResource res = new BasicCoapResource("/shouldbetoolong",
				"".getBytes(), CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}

	@Test(expected = Exception.class)
	public void invalidPathTooLongResource() {
		CoapResource res = new BasicCoapResource(
				"/1/2/3/4/5/6/7/8/9/0/1/2/3/4/5/6/7/8/9/0", "".getBytes(),
				CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}

	/*
	 * ########################################################################
	 * valid resources
	 * ########################################################################
	 */

	@Test
	public void validPathShortestResources() {
		//An empty path component is equivalent to an absolute path of "/"
		CoapResource res = new BasicCoapResource("/test", "".getBytes(),
				CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}
	
	@Test
	public void validPathRootResources() {
		//An empty path component is equivalent to an absolute path of "/"
		CoapResource res = new BasicCoapResource("/test", "".getBytes(),
				CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}

	@Test
	public void validNameLongestResources() {
		CoapResource res = new BasicCoapResource("/1234567890", "".getBytes(),
				CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}

	@Test
	public void validPathLongestResources() {
		CoapResource res = new BasicCoapResource("/1/2/3/4/5", "".getBytes(),
				CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}

	/*
	 * ########################################################################
	 * CoreResource (/.well-known/core)
	 * ########################################################################
	 */
}