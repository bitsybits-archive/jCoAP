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

package org.ws4d.coap.core.test;

import org.ws4d.coap.core.tools.TimeoutHashMap;

/**
 * @author Björn Butzin <bjoern.butzin[at]uni-rostock.de>
 */
public class TimeoutHashMapTest {
	
	public static void main(String[] args) {
		
		TimeoutHashMap<String, String> map = new TimeoutHashMap<String, String>(2000);
		
		map.put("key", "value");
		System.out.println(map.entrySet());
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// nothing
		}
		System.out.println(map.entrySet());
		
		map.put("key2", "value2");
		System.out.println(map.entrySet());
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// nothing
		}
		System.out.println(map.entrySet());
		
		map.put("key3", "value3");
		System.out.println(map.entrySet());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// nothing
		}
		map.put("key4", "value4");
		System.out.println(map.entrySet());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// nothing
		}
		map.put("key5", "value5");
		System.out.println(map.entrySet());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// nothing
		}
		map.put("key6", "value6");
		System.out.println(map.entrySet());
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// nothing
		}
		System.out.println(map.entrySet());
	}
}