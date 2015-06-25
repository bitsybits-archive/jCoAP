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

package org.ws4d.coap.messages;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.ws4d.coap.interfaces.CoapRequest;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public class BasicCoapRequest extends AbstractCoapMessage implements CoapRequest {
	CoapRequestCode requestCode;

	public BasicCoapRequest(byte[] bytes, int length) {
		/* length ought to be provided by UDP header */
		this(bytes, length, 0);
	}

	public BasicCoapRequest(byte[] bytes, int length, int offset) {
		deserialize(bytes, length, offset);
		/* check if request code is valid, this function throws an error in case of an invalid argument */
		requestCode = CoapRequestCode.parseRequestCode(this.messageCodeValue);
		
		//TODO: check integrity of header options 
	}

	public BasicCoapRequest(CoapPacketType packetType, CoapRequestCode requestCode, int messageId) {
		this.version = 1;

		this.packetType = packetType;
		this.requestCode = requestCode; //TODO
		this.messageCodeValue = requestCode.getValue();
		this.messageId = messageId;
	}
	
    @Override
    public void setToken(byte[] token){
    	/* this function is only public for a request*/
    	super.setToken(token);
    }

	@Override
	public CoapRequestCode getRequestCode() {
		return requestCode;
	}

	@Override
	public void setUriHost(String host) {
		if (host == null) return;
		if (options.optionExists(CoapHeaderOptionType.Uri_Host)){
			throw new IllegalArgumentException("Uri-Host option already exists");
		}
		if (host.length() < 1 || host.length() > CoapHeaderOption.MAX_SEGMENT_LENGTH){
			throw new IllegalArgumentException("Invalid Uri-Host option length");
		}
		/*TODO: check if host is a valid address */
		options.addOption(CoapHeaderOptionType.Uri_Host, host.getBytes());
	}

	@Override
	public void setUriPort(int port) {
		if (port < 0) return;
		if (options.optionExists(CoapHeaderOptionType.Uri_Port)){
			throw new IllegalArgumentException("Uri-Port option already exists");
		}
		byte[] value = long2CoapUint(port);
    	if(value.length < 0 || value.length > 2){
    		throw new IllegalStateException("Illegal Uri-Port length");
    	}
		options.addOption(new CoapHeaderOption(CoapHeaderOptionType.Uri_Port, value));
	}

	@Override
	public void setUriPath(String path) {
		if (path == null) return;
		
		/* delete old options if present */
		options.removeOption(CoapHeaderOptionType.Uri_Path);
		
		/*create substrings */
		String[] pathElements = path.split("/");
		/* add a Uri Path option for each part */
		for (String element : pathElements) {
			/* check length */
			if(element.length() < 0 || element.length() > CoapHeaderOption.MAX_SEGMENT_LENGTH){
				throw new IllegalArgumentException("Invalid Uri-Path length!");
			} else if (element.length() > 0){
				/* ignore empty substrings */
				options.addOption(CoapHeaderOptionType.Uri_Path, element.getBytes());
			}
		}
	}

	@Override
	public void setUriQuery(String query) {
		if (query == null) return;
				
		/* delete old options if present */
		options.removeOption(CoapHeaderOptionType.Uri_Query);
		
		/*create substrings */
		String[] pathElements = query.split("&"); 
		/* add a Uri Path option for each part */
		for (String element : pathElements) {
			/* check length */
			if(element.length() < 0 || element.length() > CoapHeaderOption.MAX_SEGMENT_LENGTH){
				throw new IllegalArgumentException("Invalid Uri-Query");
			} else if (element.length() > 0){
				/* ignore empty substrings */
				options.addOption(CoapHeaderOptionType.Uri_Query, element.getBytes());
			}
		}
		
	}
	
	@Override
	public void setProxyUri(String proxyUri) {
		if (proxyUri == null) return;

		if (options.optionExists(CoapHeaderOptionType.Proxy_Uri)){
			throw new IllegalArgumentException("Proxy Uri already exists");
		}
		
		if (proxyUri.length() < 1){
			throw new IllegalArgumentException("Proxy Uri must be at least one byte long");
		}
		
		if (proxyUri.length() > CoapHeaderOption.MAX_PROXI_URI_LENGTH ){
			throw new IllegalArgumentException("Proxy Uri longer then 1034 bytes are not supported!");
		}
		
		options.addOption(CoapHeaderOptionType.Proxy_Uri, proxyUri.getBytes());
	}
	
    @Override
    public Vector<String> getUriQuery(){
    	Vector<String> queryList = new Vector<String>();

    	for (CoapHeaderOption option : options) {
			if(option.getOptionType() == CoapHeaderOptionType.Uri_Query){
				queryList.add(new String(option.getOptionData()));
			}
		}
    	return queryList;
    }
    
    @Override
    public String getUriHost(){
    	return new String(options.getOption(CoapHeaderOptionType.Uri_Host).getOptionData());
    }
    
    @Override
    public int getUriPort(){
    	CoapHeaderOption option = options.getOption(CoapHeaderOptionType.Uri_Port);
    	if (option == null){
    		return -1; //TODO: return default Coap Port?
    	}
    	byte[] value = option.getOptionData(); 
   	
    	if(value.length < 0 || value.length > 2){
    		/* should never happen because this is an internal variable and should be checked during serialization */
    		throw new IllegalStateException("Illegal Uri-Port Option length");
    	}
    	/* checked length -> cast is safe*/
      	return (int)coapUint2Long(options.getOption(CoapHeaderOptionType.Uri_Port).getOptionData());
    }
    
    @Override
	public String getUriPath() {
    	if (options.getOption(CoapHeaderOptionType.Uri_Path) == null){
    		return null;
    	}
    	
		StringBuilder uriPathBuilder = new StringBuilder();
		for (CoapHeaderOption option : options) {
			if (option.getOptionType() == CoapHeaderOptionType.Uri_Path) {
				String uriPathElement;
				try {
					uriPathElement = new String(option.getOptionData(), "UTF-8");
					uriPathBuilder.append("/");
					uriPathBuilder.append(uriPathElement);
				} catch (UnsupportedEncodingException e) {
					throw new IllegalArgumentException("Invalid Encoding");
				}
			}
		}
		return uriPathBuilder.toString();
	}
    
    @Override
    public void addAccept(CoapMediaType mediaType){
    	options.addOption(CoapHeaderOptionType.Accept, long2CoapUint(mediaType.getValue()));
    }
    @Override
    public Vector<CoapMediaType> getAccept(CoapMediaType mediaType){
    	if (options.getOption(CoapHeaderOptionType.Accept) == null){
    		return null;
    	}
    	Vector<CoapMediaType> acceptList = new Vector<CoapMediaType>();
		for (CoapHeaderOption option : options) {
			if (option.getOptionType() == CoapHeaderOptionType.Accept) {
				CoapMediaType accept = CoapMediaType.parse((int)coapUint2Long(option.optionData));
//				if (accept != CoapMediaType.UNKNOWN){
				/* add also UNKNOWN types to list */	
				acceptList.add(accept);
//				}
			}
		}
    	return acceptList;
    }
	
	@Override
	public String getProxyUri(){
		CoapHeaderOption option = options.getOption(CoapHeaderOptionType.Proxy_Uri);
		if (option == null)
			return null;
		return new String(option.getOptionData());
	}

	@Override
	public void addETag(byte[] etag) {
    	if (etag == null){
    		throw new IllegalArgumentException("etag MUST NOT be null");
    	}
    	if (etag.length < 1 || etag.length > 8){
    		throw new IllegalArgumentException("Invalid etag length");
    	}
    	options.addOption(CoapHeaderOptionType.Etag, etag);
	}

	@Override
	public Vector<byte[]> getETag() {
		if (options.getOption(CoapHeaderOptionType.Etag) == null){
			return null;
		}
		
    	Vector<byte[]> etagList = new Vector<byte[]>();
		for (CoapHeaderOption option : options) {
			if (option.getOptionType() == CoapHeaderOptionType.Etag) {
				byte[] data = option.getOptionData();
				if (data.length >= 1 && data.length <= 8){
					etagList.add(option.getOptionData());
				}
			}
		}
		return etagList;
	}
	
	public void setIfNoneMatchOption( boolean value ) {
		if( value ) {
			options.addOption( CoapHeaderOptionType.If_None_Match, null );
		} else {
			options.removeOption( CoapHeaderOptionType.If_None_Match );
		}
	}
	
	public boolean getIfNoneMatchOption() {
		return options.optionExists( CoapHeaderOptionType.If_None_Match );
	}
	
	public Vector<byte[]> getIfMatchOption() {
		if (options.getOption(CoapHeaderOptionType.If_Match) == null){
			return null;
		}
		
		Vector<byte[]> ifMatchList = new Vector<byte[]>();
		for (CoapHeaderOption option : options) {
			if (option.getOptionType() == CoapHeaderOptionType.If_Match) {
				byte[] data = option.getOptionData();
				if (data.length >= 1 && data.length <= 8){
					ifMatchList.add(option.getOptionData());
				}
			}
		}
		return ifMatchList;
	}
	
	public void addIfMatchOption( byte[] etag ){
		options.addOption( CoapHeaderOptionType.If_Match, etag);
	}

	
	@Override
	public boolean isRequest() {
		return true;
	}

	@Override
	public boolean isResponse() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
	
    @Override
	public String toString() {
    	return packetType.toString() + ", " + requestCode.toString() + ", MsgId: " + getMessageID() +", #Options: " + options.getOptionCount(); 
	}

    @Override
	public void setRequestCode(CoapRequestCode requestCode) {
		this.requestCode = requestCode;
	}
}
