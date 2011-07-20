
package org.ws4d.coap.tools;

import java.util.HashMap;
import java.util.LinkedList;

public class TimeoutHashMap<K, V> extends HashMap<K, V> {
    LinkedList<TimeoutObject> timeoutQueue = new LinkedList<TimeoutObject>();
    int timeout;

    public class TimeoutObject {
        public Object key;
        public long timeout = 0;

        public TimeoutObject(Object key, long timeout) {
            this.timeout = timeout;
            this.key = key;
        }
    }

    public TimeoutHashMap(int timeout) {
        super();
        this.timeout = timeout;
    }

    @Override
    public synchronized V put(K key, V value) {
        /* no timeout means infinity */
        removeExpiredEntries();
        if (timeout != 0) {
            timeoutQueue.add(new TimeoutObject(key, (System.currentTimeMillis() + timeout)));
        }
        return super.put(key, value);
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        removeExpiredEntries();
        return super.containsKey(key);
    }

    @Override
    public synchronized V get(Object key) {
        removeExpiredEntries();
        return (V) super.get(key);
    }

    // public synchronized Integer getNextTimeoutIn(){
    // removeExpiredEntries();
    // TimeoutObject o = timeoutQueue.peek();
    // if(o != null){
    // return (int) (o.timeout - System.currentTimeMillis());
    // }
    // return null;
    // }

    private void removeExpiredEntries() {
        do {
            TimeoutObject o = timeoutQueue.peek();
            if (o != null && o.timeout <= System.currentTimeMillis()) {
                o = timeoutQueue.poll();
                this.remove(o.key);
                continue;
            }
        } while (false);
    }

}
