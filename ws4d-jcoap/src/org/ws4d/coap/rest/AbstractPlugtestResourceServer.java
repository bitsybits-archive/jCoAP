package org.ws4d.coap.rest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

public abstract class AbstractPlugtestResourceServer implements ResourceServer {
    protected HashMap<String, Resource> resources = new HashMap<String, Resource>();
    private CoreResource coreResource = new CoreResource();

    public final boolean createResource(CoapResource resource) {
	if (resource==null) return false;
	    resources.put(resource.getPath(), resource);
    	coreResource.registerResource(resource);
		System.out.println("Created Resource: " + resource.toString()); //Output for Plugtest
	    return true;
	}
    public final boolean updateResource(CoapResource resource) {
	if (resource==null) return false;
	
	if (resources.containsKey(resource.getPath())) {
	    resources.put(resource.getPath(), resource);
		System.out.println("Updated Resource: " + resource.toString()); //Output for Plugtest
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
    	//fake delete
    	System.out.println("Deleted Resource: " + path); 
    	return true;
	}
    
    public final Resource readResource(String path) {
    	System.out.println("Read Resource: " + path); //Output for Plugtest
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
