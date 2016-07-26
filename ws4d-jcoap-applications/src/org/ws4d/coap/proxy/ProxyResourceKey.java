package org.ws4d.coap.proxy;

import java.net.InetAddress;

public class ProxyResourceKey {
	/* these variables are unique for one resource */
	private InetAddress inetAddr;
	private int port;
	private String path;

	public ProxyResourceKey(InetAddress inetAddr, int port, String path) {
		super();
		this.inetAddr = inetAddr;
		this.port = port;
		this.path = path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.inetAddr == null) ? 0 : this.inetAddr.hashCode());
		result = prime * result + ((this.path == null) ? 0 : this.path.hashCode());
		result = prime * result + this.port;
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
		ProxyResourceKey other = (ProxyResourceKey) obj;
		if (this.inetAddr == null) {
			if (other.inetAddr != null)
				return false;
		} else if (!this.inetAddr.equals(other.inetAddr))
			return false;
		if (this.path == null) {
			if (other.path != null)
				return false;
		} else if (!this.path.equals(other.path))
			return false;
		if (this.port != other.port)
			return false;
		return true;
	}
}
