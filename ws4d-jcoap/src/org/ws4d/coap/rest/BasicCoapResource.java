package org.ws4d.coap.rest;

import java.util.Vector;

import org.ws4d.coap.messages.CoapMediaType;

public class BasicCoapResource implements CoapResource {
    private CoapMediaType mediaType;
    private String path;
    private byte[] value;

//    public BasicCoapResource(String path, byte[] value) {
//	this.path = path;
//	this.value = value;
//    }
//    
    public BasicCoapResource(String path, byte[] value, CoapMediaType mediaType) {
	this.path = path;
	this.value = value;
	this.mediaType = mediaType;
    }

    public void setCoapMediaType(CoapMediaType mediaType) {
	this.mediaType = mediaType;
    }

    @Override
    public CoapMediaType getCoapMediaType() {
	return mediaType;
    }
    
    public String getMimeType(){
    	//TODO: implement
    	return null;
    }

    @Override
    public String getPath() {
	return path;
    }

    @Override
    public String getShortName() {
	return null;
    }

    @Override
    public byte[] getValue() {
	return value;
    }

    @Override
    public byte[] getValue(Vector<String> query) {
	return value;
    }

    @Override
    public String getResourceType() {
    	//TODO: implement
	return null;
    }
    
    @Override
    public String toString() {
		return getPath(); //TODO implement 
	}

}
