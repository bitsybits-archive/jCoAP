
package org.ws4d.coap.interfaces;

import org.ws4d.coap.messages.CoapRequestCode;


public interface CoapClientChannel extends CoapChannel {
    public CoapRequest createRequest(boolean reliable, CoapRequestCode requestCode);
}
