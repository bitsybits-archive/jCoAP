package org.ws4d.coap.tools;
import java.util.HashMap;
import java.util.LinkedList;

public class TimeoutHashMap<K, V> {
	/* The RetransmissionObject itselfs provides the hashkey */
	HashMap<K, TimoutType<V>> hashmap = new HashMap<K, TimoutType<V>>();
	
	/* chronological list to remove expired elements when update() is called */ 
	LinkedList<TimoutType<K>> timeoutQueue = new LinkedList<TimoutType<K>>();
	
	/* Default Timeout is one minute */
	long timeout = 60000;
	
	public TimeoutHashMap(long timeout){
		this.timeout = timeout;
	}
	
	public V put(K key, V value){
		long expires = System.currentTimeMillis() + timeout;
		TimoutType<V> timeoutValue = new TimoutType<V>(value, expires);
		TimoutType<K> timeoutKey = new TimoutType<K>(key, expires);
		timeoutQueue.add(timeoutKey);
		timeoutValue = hashmap.put(key, timeoutValue);
		if (timeoutValue != null){
			return timeoutValue.object;
		}
		return null;
	}
	
	public Object get(Object key) {
		TimoutType<V> timeoutValue = hashmap.get(key);	
		if (timeoutValueIsValid(timeoutValue)){
			return timeoutValue.object;
		} 
		return null;
	}	
	
	public Object remove(Object key) {
		TimoutType<V> timeoutValue = hashmap.remove(key);
		if (timeoutValueIsValid(timeoutValue)){
			return timeoutValue.object;
		} 		
		return null;
	}
	
	public void clear() {
		hashmap.clear();
		timeoutQueue.clear();
	}
	
	/* remove expired elements */
	public void update(){
        while(true) {
        	TimoutType<K> timeoutKey = timeoutQueue.peek();
        	if (timeoutKey == null){
        		/* if the timeoutKey queue is empty, there must be no more elements in the hashmap 
        		 * otherwise there is a bug in the implementation */
        		if (!hashmap.isEmpty()){
        			throw new IllegalStateException("Error in TimeoutHashMap. Timeout queue is empty but hashmap not!");
        		}
        		return;
        	}
        	
        	long now = System.currentTimeMillis();
        	if (now > timeoutKey.expires){
        		timeoutQueue.poll();
        		TimoutType<V> timeoutValue = hashmap.remove(timeoutKey.object);

        		if (timeoutValueIsValid(timeoutValue)){
        			/* This is a very special case which happens if an entry is overridden:
        			 * - put V with K 
        			 * - put V2 with K
        			 * - K is expired but V2 not 
        			 * because this is expected to be happened very seldom, we "reput" V2 to the hashmap 
        			 * wich is better than every time to making a get and than a remove */
        			hashmap.put(timeoutKey.object, timeoutValue);
        		}
        	} else {
        		/* Key is not expired -> break the loop */
        		break;
        	}
        }
	}
	
	private boolean timeoutValueIsValid(TimoutType<V> timeoutValue){
		return timeoutValue != null && System.currentTimeMillis() < timeoutValue.expires;		
	}
	
	
	
	
	/* TODO: implement these sekeletons */
//	public Object remove(Object key) {
//		return null;
//	}
//	public boolean containsKey(Object key) {
//		return false;
//	}
//	public boolean containsValue(Object value){
//		return false;
//	}
//	public boolean isEmpty() {
//		this.update();
//		return hashtable.isEmpty();
//	}
//	public Object clone(){
//		
//	}

	@Override
	public String toString() {
		return hashmap.toString();
	}




	private class TimoutType<T>{
		public T object;
		public long expires;
		
		public TimoutType(T object, long expires) {
			super();
			this.object = object;
			this.expires = expires;
		}
	}
}
