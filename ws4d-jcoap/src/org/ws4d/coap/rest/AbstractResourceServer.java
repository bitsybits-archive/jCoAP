package org.ws4d.coap.rest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

public abstract class AbstractResourceServer implements ResourceServer {
    protected HashMap<String, Resource> resources = new HashMap<String, Resource>();
    private CoreResource coreResource = new CoreResource();

    @Override
    public final boolean createResource(Resource resource) {
	if (resource==null) return false;
	
	if (!resources.containsKey(resource.getPath())) {
	    resources.put(resource.getPath(), resource);
	    coreResource.registerResource(resource);
	    return true;
	} else
	    return false;
    }

    @Override
    public final boolean updateResource(Resource resource) {
	if (resource==null) return false;
	
	if (resources.containsKey(resource.getPath())) {
	    resources.put(resource.getPath(), resource);
	    return true;
	} else
	    return false;
    }
    
    @Override
    public final boolean deleteResource(String path) {
	if (null!=resources.remove(path)){
		return true;
	}else return false;
    }
    
    @Override
    public final Resource readResource(String path) {
    	return resources.get(path);
    }
    
	@Override
	public Resource get(String path) {
		return readResource(path);
	}

	@Override
	public void post(String path, byte[] data) {
		Resource res = resources.get(path);
		if (res == null){
			return;
		}
		res.post(data);
	}

	@Override
	public boolean put(Resource resource) {
		Resource res = readResource(resource.getPath());
		if (res == null){
			createResource(resource);
		} else {
			updateResource(resource);
		}
		return true;
	}

	@Override
	public boolean delete(String path) {
		// TODO Auto-generated method stub
		return false;
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
