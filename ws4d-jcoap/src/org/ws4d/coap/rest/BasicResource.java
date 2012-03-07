package org.ws4d.coap.rest;

public class BasicResource implements Resource {
    private String mimeType;
    private String path;
    private byte[] value;

    public BasicResource(String path, byte[] value) {
	this.path = path;
	this.value = value;
    }

    public void setMimeType(String mimeType) {
	this.mimeType = mimeType;
    }

    @Override
    public String getMimeType() {
	return mimeType;
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
    public byte[] getValue(String query) {
	return null;
    }

}
