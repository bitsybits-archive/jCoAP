/* Copyright 2011 University of Rostock
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.interfaces.CoapSocketHandler;
import org.ws4d.coap.messages.BasicCoapRequest;
import org.ws4d.coap.messages.BasicCoapResponse;
import org.ws4d.coap.messages.CoapBlockOption;
import org.ws4d.coap.messages.CoapBlockOption.CoapBlockSize;
import org.ws4d.coap.messages.CoapEmptyMessage;
import org.ws4d.coap.messages.CoapPacketType;
import org.ws4d.coap.messages.CoapRequestCode;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Bjoern Konieczek <bjoern.konieczek@uni-rostock.de>
 */

public class BasicCoapClientChannel extends BasicCoapChannel implements CoapClientChannel {
	CoapClient client = null;
	ClientBlockContext blockContext = null;
	CoapRequest lastRequest = null;
	Object trigger = null;
	
	public BasicCoapClientChannel(CoapSocketHandler socketHandler,
			CoapClient client, InetAddress remoteAddress,
			int remotePort) {
		super(socketHandler, remoteAddress, remotePort);
		this.client = client;
	}
	
	@Override
    public void close() {
        socketHandler.removeClientChannel(this);
    }
	
	public byte[] getLastToken() {
		if( lastRequest != null ) {
			return lastRequest.getToken();
		}
		return null;
	}

	@Override
	public void handleMessage(CoapMessage message) { 
		if (message.isRequest()){
			/* this is a client channel, no requests allowed */
			message.getChannel().sendMessage(new CoapEmptyMessage(CoapPacketType.RST, message.getMessageID()));
			return;
		}
		
		if (message.isEmpty() && message.getPacketType() == CoapPacketType.ACK){
			/* this is the ACK of a separate response */
			//TODO: implement a handler or listener, that informs a client when a sep. resp. ack was received
			return;
		}  
		
		if (message.getPacketType() == CoapPacketType.CON) {
			/* this is a separate response */
			/* send ACK */
			this.sendMessage(new CoapEmptyMessage(CoapPacketType.ACK, message.getMessageID()));
		} 
		
		/* check for blockwise transfer */
		CoapBlockOption block2 = message.getBlock2();
		CoapBlockOption block1 = message.getBlock1();
		if (blockContext == null && block2 != null){
			/* initiate blockwise transfer */
			blockContext = new ClientBlockContext(block2, maxReceiveBlocksize);
			blockContext.setFirstRequest(lastRequest);
			blockContext.setFirstResponse((CoapResponse) message);
		}
		
		if (blockContext!= null){
			/*blocking option*/
			if( blockContext.getFirstRequest() == null )
				System.err.println("first Request is null");
			if( blockContext.getFirstRequest().getRequestCode() == CoapRequestCode.GET) {
				if (!blockContext.addBlock(message, block2)){
					/*this was not a correct block*/
					/* TODO: implement either a RST or ignore this packet */
				}
			}
    		
			if (!blockContext.isFinished()){
				/* TODO: implement a counter to avoid an infinity req/resp loop:
				 *  		if the same block is received more than x times -> rst the connection 
				 *  implement maxPayloadSize to avoid an infinity payload */
				CoapBlockOption newBlock = blockContext.getNextBlock();
				if (lastRequest == null){
					/*TODO: this should never happen*/
					System.err.println("ERROR: client channel: lastRequest == null");
				} else {
					/* create a new request for the next block */
					BasicCoapRequest request =  new BasicCoapRequest(lastRequest.getPacketType(), lastRequest.getRequestCode(), channelManager.getNewMessageID());
					request.copyHeaderOptions((BasicCoapRequest) blockContext.getFirstRequest());
					request.setToken( blockContext.getFirstRequest().getToken() );
					if( request.getRequestCode() == CoapRequestCode.GET ) {
						request.setBlock2(newBlock);
					} else {
						request.setBlock1(newBlock);
						request.setPayload( blockContext.getNextPayload(newBlock) );
					}
					sendMessage(request);
				}
				/* TODO: implement handler, inform the client that a block (but not the complete message) was received*/
				return;
			} 
			/* blockwise transfer finished */
			
			message.setPayload(blockContext.getPayload());
			blockContext = null;
			/* TODO: give the payload separately and leave the original message as they is*/
		} 		

		/* normal or separate response */
		client.onResponse(this, (BasicCoapResponse) message);
	}

	@Override
	public void lostConnection(boolean notReachable, boolean resetByServer) {
		client.onConnectionFailed(this, notReachable, resetByServer);		

	}
	
    @Override
    public BasicCoapRequest createRequest(boolean reliable, CoapRequestCode requestCode) {
    	BasicCoapRequest msg = new BasicCoapRequest(
                reliable ? CoapPacketType.CON : CoapPacketType.NON, requestCode,
                channelManager.getNewMessageID());
        msg.setChannel(this);
        return msg;
    }
    
    @Override
    public void sendMessage(CoapMessage msg) {
        super.sendMessage(msg);
        
        // Check whether msg is a CoapRequest --> otherwise do nothing
        if( msg.isRequest() )
        	lastRequest = (CoapRequest) msg;
    }
    
    /** This function should be called to initiate any blockwise POST or PUT request. Adds the context for
     * the blockwise transaction to the client channel.
     * 
     * @param	maxBlocksize	Maximal BlockSize for sending
     * @param	request			The CoapRequest-Object, that was obtained through ClientChannel.createRequest() function. The request
     * 							must already contain the payload!
     * 
     * @return	The first request that should be send via ClientChannel.sendMessage() function. The requests for the following 
     * 			Blocks are handled automatically 
     * 
     */
    public BasicCoapRequest addBlockContext( CoapRequest request){
    	
    	if( request.getRequestCode() == CoapRequestCode.POST || request.getRequestCode() == CoapRequestCode.PUT ) {
    		CoapBlockOption block1 = request.getBlock1();
    		CoapBlockSize bSize = maxSendBlocksize;
    		
    		if( block1 != null && block1.getBlockSize().getSize() < maxSendBlocksize.getSize() ) 
    			bSize = block1.getBlockSize();
  
    		this.blockContext = new ClientBlockContext( bSize, request.getPayload() );
	    	
	    	BasicCoapRequest firstRequest = createRequest(request.isReliable(), request.getRequestCode());
	    	firstRequest.copyHeaderOptions( (BasicCoapRequest)request );
	    	firstRequest.setToken( request.getToken() );
	    	
	    	if( request.getPayloadLength() <= bSize.getSize() )
	    		block1 = new CoapBlockOption(0, false, bSize);
	    	else
	    		block1 = new CoapBlockOption(0, true, bSize);
	    	
	    	firstRequest.setBlock1(block1);
	    	firstRequest.setPayload( this.blockContext.getNextPayload(block1) );
	    	this.blockContext.setFirstRequest(firstRequest);
	    	return firstRequest;
	    } else {
	    	System.err.println("ERROR: Tried to manually add BlockContext to GET request!");
	    	return (BasicCoapRequest)request;
	    }
    }
    
    private class ClientBlockContext{

    	private ByteArrayOutputStream incomingStream;
    	private ByteArrayInputStream outgoingStream;
    	boolean finished = false;
    	boolean sending = false; //false=receiving; true=sending
    	CoapBlockSize blockSize; //null means no block option
    	int blockNumber;
    	int maxBlockNumber;
    	CoapRequest request;
    	CoapResponse response;
    	
    	/** Create BlockContext for GET requests. This is done automatically, if the sent GET request or the obtained response
    	 *  contain a Block2-Option.
    	 * 
    	 * @param blockOption	The CoapBlockOption object, that contains the block size indicated by the server
    	 * @param maxBlocksize	Indicates the maximum block size supported by the client
    	 */
    	public ClientBlockContext(CoapBlockOption blockOption, CoapBlockSize maxBlocksize) {
    		
    		this.incomingStream = new ByteArrayOutputStream();
    		this.outgoingStream = null;
    		
    		/* determine the right blocksize (min of remote and max)*/
    		if (maxBlocksize == null){
    			blockSize = blockOption.getBlockSize();
    		} else {
    			int max = maxBlocksize.getSize();
    			int remote = blockOption.getBlockSize().getSize();
    			if (remote < max){
    				blockSize = blockOption.getBlockSize();
    			} else {
    				blockSize = maxBlocksize;
    			}
    		}
    		this.blockNumber = blockOption.getNumber();
    		this.sending=false;
    	}
    	
    	/** Create BlockContext for POST or PUT requests. Is only called by addBlockContext().
    	 * 
    	 * @param maxBlocksize	Indicates the block size for the transaction
    	 * @param payload		The whole payload, that should be transferred
    	 */
    	public ClientBlockContext(CoapBlockSize maxBlocksize, byte[] payload ){
    		this.outgoingStream = new ByteArrayInputStream( payload );
    		this.incomingStream = null;
    		this.blockSize = maxBlocksize;
    		
    		this.blockNumber = 0;
    		this.maxBlockNumber = payload.length / this.blockSize.getSize() - 1;
    		if( payload.length%this.blockSize.getSize() > 0 )
    			this.maxBlockNumber++;

    		this.sending = true;
    	}

		public byte[] getPayload() {
			
			if (!this.sending ) {
				return this.incomingStream.toByteArray();
			} else if( this.outgoingStream != null ) {
				byte[] payload = new byte[ this.outgoingStream.available() ];
				outgoingStream.read(payload, 0, this.outgoingStream.available() );
				return payload;
			} else
				return null;
		}

		/** Adds the new obtained data block to the complete payload, in the case of blockwise GET requests.
		 * 
		 * @param msg		The received CoAP message
		 * @param block		The block option of the CoAP message, indicating which block of data was received 
		 * 					and whether there are more to follow.
		 * @return			Indicates whether the operation was successful.
		 */
		public boolean addBlock(CoapMessage msg, CoapBlockOption block){
			int number = block.getNumber();
			if( number > this.blockNumber )
				return false;
			
			this.blockNumber++;
			try{
				this.incomingStream.write( msg.getPayload() );
			} catch( IOException e) {
				System.err.println("ERROR: Cannot write data block to input buffer!");
			}
			if( block.isLast() ) {
				finished = true;
			}

    		return true;
    	}
    	
		/** Retrieve the next block option, indicating the requested (GET) or send (POST or PUT) block
		 * 
		 * @return	BlockOption to indicate the next block, that should be send (POST or PUT) or received (GET)
		 */
		public CoapBlockOption getNextBlock() {
			if( !sending ) {
				blockNumber++; //ignore the rest (no rest should be there)
				return new CoapBlockOption( blockNumber, false, blockSize);
			}
			else { 
				blockNumber++;
				if( blockNumber == maxBlockNumber) 
					return new CoapBlockOption(blockNumber, false, blockSize);
				else
					return new CoapBlockOption(blockNumber, true, blockSize);
			}
		}
		
		/** Get the next block of payload, that should be send in a POST or PUT request
		 * 
		 * @param block		Indicates which block of data should be send next.
		 * @return			The next part of the payload
		 */
		public byte[] getNextPayload(CoapBlockOption block){
			int number = block.getNumber();
			byte[] payloadBlock;
					
			if( number == maxBlockNumber ) {
				payloadBlock = new byte[ this.outgoingStream.available() ];
				this.outgoingStream.read(payloadBlock, 0, this.outgoingStream.available());
				finished = true;
			} else {
				payloadBlock = new byte[ block.getBlockSize().getSize() ];
				this.outgoingStream.read(payloadBlock, 0, block.getBlockSize().getSize());
			}		
			
			return payloadBlock;
		}
    	
		public boolean isFinished() {
			return finished;
		}

		public CoapRequest getFirstRequest() {
			return request;
		}

		public void setFirstRequest(CoapRequest request) {
			this.request = request;
		}

		public CoapResponse getFirstResponse() {
			return response;
		}

		public void setFirstResponse(CoapResponse response) {
			this.response = response;
		}
    }

	@Override
	public void setTrigger(Object o) {
		trigger = o;
		
	}

	@Override
	public Object getTrigger() {
		return trigger;
	}

}

