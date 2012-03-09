package org.ws4d.coap.messages;

import org.ws4d.coap.messages.AbstractCoapMessage.CoapHeaderOptions;

public class CoapRequest extends AbstractCoapMessage {
	public enum CoapRequestCode {
		GET(1), POST(2), PUT(3), DELETE(4);

		private int code;

		private CoapRequestCode(int code) {
			this.code = code;
		}
		
		public static CoapRequestCode parseRequestCode(int codeValue){
			switch (codeValue) {
			case 1:
				return GET;
			case 2:
				return POST;
			case 3:
				return PUT;
			case 4:
				return DELETE;
			default:
				throw new IllegalArgumentException("Invalid Request Code");
			}
		}

		public int getValue() {
			return code;
		}
		
		@Override
		public String toString() {
			switch (this) {
			case GET:
				return "GET";
			case POST:
				return "POST";
			case PUT:
				return "PUT";
			case DELETE:
				return "DELETE";
			}
			return null;
		}
	}

	CoapRequestCode requestCode;

	public CoapRequest(byte[] bytes, int length) {
		/* length ought to be provided by UDP header */
		this(bytes, length, 0);
	}

	public CoapRequest(byte[] bytes, int length, int offset) {
		serialize(bytes, length, offset);
		/* check if request code is valid, this function throws an error in case of an invalid argument */
		requestCode = CoapRequestCode.parseRequestCode(this.messageCodeValue);
	}

	public CoapRequest(CoapPacketType packetType, CoapRequestCode requestCode, int messageId) {
		this.version = 1;
		this.optionCount = 0;

		this.packetType = packetType;
		this.requestCode = requestCode;
		this.messageCodeValue = requestCode.getValue();
		this.messageId = messageId;
		
		this.options = new CoapHeaderOptions();
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

}
