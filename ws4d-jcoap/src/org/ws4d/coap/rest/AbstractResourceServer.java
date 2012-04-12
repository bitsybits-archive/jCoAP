package org.ws4d.coap.rest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

public abstract class AbstractResourceServer implements ResourceServer {
    protected HashMap<String, Resource> resources = new HashMap<String, Resource>();
    private CoreResource coreResource = new CoreResource();

    public final boolean createResource(CoapResource resource) {
	if (resource==null) return false;
	
	if (!resources.containsKey(resource.getPath())) {
	    resources.put(resource.getPath(), resource);
	    coreResource.registerResource(resource);
	    return true;
	} else
	    return false;
    }

    public final boolean updateResource(CoapResource resource) {
	if (resource==null) return false;
	
	if (resources.containsKey(resource.getPath())) {
	    resources.put(resource.getPath(), resource);
	    return true;
	} else
	    return false;
    }
    
//    public void createUpdateResource(CoapResource resource){
//	if (resource==null) 
//	    return;
//	
//	if (resources.containsKey(resource.getPath())) {
//	    coreResource.registerResource(resource);
//	} 
//	resources.put(resource.getPath(), resource);
//    }
    
    public final boolean deleteResource(String path) {
	if (null!=resources.remove(path)){
		return true;
	}else return false;
    }
    
    public final Resource readResource(String path) {
    	return resources.get(path);
    }

    @Override
    public void start() throws Exception {
	resources.put(coreResource.getPath(), coreResource);
    }

    protected String getLocalIpAddress() {
	try {
	    for (Enumeration<NetworkInterface> en = NetworkInterface
		    .getNetworkInterfaces(); en.hasMoreElements();) {
		NetworkInterface intf = en.nextElement();
		for (Enumeration<InetAddress> enumIpAddr = intf
			.getInetAddresses(); enumIpAddr.hasMoreElements();) {
		    InetAddress inetAddress = enumIpAddr.nextElement();
		    if (!inetAddress.isLoopbackAddress()) {
			return inetAddress.getHostAddress().toString();
		    }
		}
	    }
	} catch (SocketException ex) {
	}
	return null;
    }
}
