/* Copyright 2015 University of Rostock
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
 *****************************************************************************/

package org.ws4d.coap.tools;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

public class TimeoutHashMap2<K, V> implements Map<K, V> {

	private PriorityBlockingQueue<TimedEntry<K>> timequeue = new PriorityBlockingQueue<TimedEntry<K>>();
	private Map<K, V> map = new Hashtable<K, V>();
	private Long timeout;
	private Timer timer = new Timer(this.timequeue,this.map);
	private Thread thread = new Thread(this.timer);

	public TimeoutHashMap2(long timeout){
		this.timeout = timeout;
	}
	
	public void clear() {
		this.map.clear();
		this.timequeue.clear();
	}

	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return this.map.containsValue(value);
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return this.map.entrySet();
	}

	public V get(Object key) {
		return this.map.get(key);
	}

	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	public Set<K> keySet() {
		return this.map.keySet();
	}

	public V put(K key, V value) {
		
		Long expires = System.currentTimeMillis() + this.timeout;
		if (this.map.containsKey(key)) {
			for (TimedEntry<K> entry : this.timequeue) {
				if (entry.getValue().equals(key)) {
					entry.setExpires(expires);
				}
			}
		} else {
			this.timequeue.add(new TimedEntry<K>(expires, key));
		}
		if(!this.thread.isAlive()){
			this.thread.start();
		} else {
			this.thread.interrupt();
		}
		return this.map.put(key, value);
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> e : m.entrySet()) {
			this.put(e.getKey(), e.getValue());
		}
	}

	public V remove(Object key) {
		if (this.map.containsKey(key)) {
			for (TimedEntry<K> entry : this.timequeue) {
				if (entry.getValue().equals(key)) {
					this.timequeue.remove(entry);
				}
			}
		}
		return this.map.remove(key);
	}

	public int size() {
		return this.map.size();
	}

	public Collection<V> values() {
		return this.map.values();
	}

	private class TimedEntry<A> implements Comparable<TimedEntry<A>> {

		private Long expires;
		private A value;

		TimedEntry(Long expires, A value) {
			this.expires = expires;
			this.value = value;
		}

		public Long getExpires() {
			return this.expires;
		}

		public void setExpires(Long expires) {
			this.expires = expires;
		}

		public A getValue() {
			return this.value;
		}

		public int compareTo(TimedEntry<A> o) {
			return this.expires.compareTo(o.getExpires());
		}
	}

	private class Timer implements Runnable {
		private PriorityBlockingQueue<TimedEntry<K>> timerQueue;
		private Map<K, V> timerMap;

		Timer(PriorityBlockingQueue<TimedEntry<K>> timequeue, Map<K, V> map) {
			this.timerQueue = timequeue;
			this.timerMap = map;
		}
		
			public void run() {
			while (true) {
				if (!this.timerQueue.isEmpty()) {
					long time = this.timerQueue.peek().getExpires() - System.currentTimeMillis();
					if (time > 0) {
						try {
							Thread.sleep(time);
						} catch (InterruptedException e) {
							// do nothing
						}
					} else {
						this.timerMap.remove(this.timerQueue.poll().getValue());
					}
				} else {
					try {
						Thread.sleep(99999999);
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
		}
	}
}
