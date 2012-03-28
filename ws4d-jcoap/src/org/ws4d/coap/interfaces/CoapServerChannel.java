
package org.ws4d.coap.interfaces;

import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapResponseCode;


public interface CoapServerChannel extends CoapChannel {
    public CoapResponse createResponse(CoapMessage request, CoapResponseCode responseCode);

    public CoapResponse createResponse(CoapMessage request, CoapResponseCode responseCode, CoapMediaType contentType);

	public CoapResponse createSeparateResponse(CoapRequest request,
			CoapResponseCode responseCode);

	public void sendSeparateResponse(CoapResponse response);

}
