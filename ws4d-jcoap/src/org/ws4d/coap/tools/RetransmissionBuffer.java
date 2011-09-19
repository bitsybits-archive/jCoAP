package org.ws4d.coap.tools;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.tools.TimeoutHashMap.TimeoutObject;

public class RetransmissionBuffer {
	/* The RetransmissionObject itselfs provides the hashkey */
	Hashtable<RetransmissionObject, RetransmissionObject> msgTable = new Hashtable<RetransmissionObject, RetransmissionObject>();
	
	/* chronological list to remove expired elements when update() is called */ 
	LinkedList<RetransmissionObject> timeoutQueue = new LinkedList<RetransmissionObject>();
	
	/* compare COAP RFC 4.1. Reliable Messages 
	 * "The same Message ID MUST NOT be re-used within the
   	      potential retransmission window, calculated as RESPONSE_TIMEOUT *
          RESPONSE_RANDOM_FACTOR * (2 ^ MAX_RETRANSMIT - 1) plus the expected
          maximum round trip time" 
	 --> 45s + Roundtrip Time. We assume a window of one minute */
	static final int EXPIRES_AFTER_MS = 60000;

	public RetransmissionBuffer(){
	}
	
	public void put(int msgID, InetAddress addr, int port, CoapMessage retransMsg){
		RetransmissionObject obj = new RetransmissionObject(msgID, addr, port, retransMsg, System.currentTimeMillis() + EXPIRES_AFTER_MS);
		timeoutQueue.add(obj);
		msgTable.put(obj, obj);		
	}
	
	public RetransmissionObject remove(int msgID, InetAddress addr, int port){
		RetransmissionObject obj = msgTable.remove(new RetransmissionObject(msgID, addr, port));
		if (obj != null && System.currentTimeMillis() < obj.expires){
				return obj;
		} 
		return null;
	}
	
	/* remove all expired elements */
	public void update(){
        while(true) {
        	RetransmissionObject obj = timeoutQueue.peek();
            if (obj != null && obj.expires <= System.currentTimeMillis()) {
            	timeoutQueue.poll();
            	msgTable.remove(obj);
            } else {
            	/* no more (expired) entries in the queue */
            	break;
            }
        }
	}
	
	
	private class RetransmissionObject{
		int msgID;
		InetAddress addr;
		int port;
		long expires;
		/* This message must be send when a retransmission was detected (null for none) */
		private CoapMessage retransMsg; 
		
		public RetransmissionObject(int msgID, InetAddress addr, int port,
				CoapMessage retransMsg, long expires) {
			super();
			this.msgID = msgID;
			this.addr = addr;
			this.port = port;
			this.retransMsg = retransMsg;
			this.expires = expires;
		}
		
		public RetransmissionObject(int msgID, InetAddress addr, int port) {
			super();
			this.msgID = msgID;
			this.addr = addr;
			this.port = port;
			this.retransMsg = null;
			this.expires = 0;
		}

		public CoapMessage getRetransMsg() {
			return retransMsg;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((addr == null) ? 0 : addr.hashCode());
			result = prime * result + msgID;
			result = prime * result + port;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RetransmissionObject other = (RetransmissionObject) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (addr == null) {
				if (other.addr != null)
					return false;
			} else if (!addr.equals(other.addr))
				return false;
			if (msgID != other.msgID)
				return false;
			if (port != other.port)
				return false;
			return true;
		}

		private RetransmissionBuffer getOuterType() {
			return RetransmissionBuffer.this;
		}
	}
		
}
