package org.ws4d.coap.interfaces;

import org.ws4d.coap.messages.CoapResponseCode;

public interface CoapResponse extends CoapMessage{
	
	/* TODO: Response Code is part of BasicCoapResponse */
	public CoapResponseCode getResponseCode();
	public void setMaxAge(int maxAge);
    public long getMaxAge();
    
    public void setETag(byte[] etag);
    public byte[] getETag();

		
}
