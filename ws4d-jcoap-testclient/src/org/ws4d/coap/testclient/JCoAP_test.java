/* Copyright [2011] [University of Rostock]
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

/* WS4D Java CoAP Implementation
 * (c) 2011 WS4D.org
 * 
 * written by Sebastian Unger 
 */

package org.ws4d.coap.testclient;

import org.ws4d.coap.messages.CoapHeaderOption;
import org.ws4d.coap.messages.CoapPacketType;
import org.ws4d.coap.messages.CoapHeaderOptions.HeaderOptionNumber;
import org.ws4d.coap.messages.CoapMessageCode.MessageCode;
import org.ws4d.coap.messages.DefaultCoapMessage;

public class JCoAP_test {

    public static void main(String[] args) {
        DefaultCoapMessage messageOut = new DefaultCoapMessage(CoapPacketType.ACK,
                MessageCode.Method_Not_Allowed_405, 1);
        DefaultCoapMessage messageIn = null;

        /* create a CoAP Message */
        try {
            messageOut.getHeader().addOption(
                    new CoapHeaderOption(HeaderOptionNumber.Location_Path,
                            "I/AM/a/very/very/long/option/value/so/longer/than/15/hell/yeah"
                                    .getBytes()));
            messageOut.getHeader().addOption(HeaderOptionNumber.Etag, "Etag1".getBytes());
            messageOut.getHeader().addOption(HeaderOptionNumber.Content_Type, "4".getBytes());
            messageOut.getHeader().addOption(HeaderOptionNumber.Etag, "Etag2".getBytes());
            messageOut.setPayload("I am the Payload!");
        } catch (Exception e) {

        }

        /* print CoAP Message */
        System.out.println(messageOut.toString());

        /* serialize message */
        byte[] binary = messageOut.serialize();
        System.out.println("Header in binary/hex representation:");
        System.out.println(Integer.toBinaryString((int) (binary[0] & 0xFF))
                + " " + Integer.toBinaryString((int) (binary[1] & 0xFF)) + "("
                + Integer.toHexString((int) (binary[1] & 0xFF)) + ")\n"
                + Integer.toBinaryString((int) (binary[2] & 0xFF)) + " "
                + Integer.toBinaryString((int) (binary[3] & 0xFF))
                + "(" + Integer.toHexString((int) (binary[2] & 0xFF)) + " "
                + Integer.toHexString((int) (binary[3] & 0xFF)) + ")\n\n");

        /* deserialize message */
        messageIn = new DefaultCoapMessage(binary, binary.length);
        /* length is supposed to be provided from UDP-Header */

        /* print deserialized message */
        System.out.println(messageIn.toString());
    }

}
