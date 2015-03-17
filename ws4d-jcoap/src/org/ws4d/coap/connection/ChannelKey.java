/* Copyright 2015 University of Rostock
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

package org.ws4d.coap.connection;

import java.net.InetAddress;

public class ChannelKey {
	public InetAddress inetAddr;
	public int port;

	public ChannelKey(InetAddress inetAddr, int port) {
		this.inetAddr = inetAddr;
		this.port = port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((inetAddr == null) ? 0 : inetAddr.hashCode());
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
		ChannelKey other = (ChannelKey) obj;
		if (inetAddr == null) {
			if (other.inetAddr != null)
				return false;
		} else if (!inetAddr.equals(other.inetAddr))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
}
