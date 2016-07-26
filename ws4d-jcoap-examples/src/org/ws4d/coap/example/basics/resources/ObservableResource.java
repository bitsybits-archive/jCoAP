/*
 * Copyright 2015 University of Rostock
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
 */

package org.ws4d.coap.example.basics.resources;

import java.util.List;
import java.util.Locale;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;
import org.ws4d.coap.core.rest.CoapData;

/**
 * This class demonstrates a CoAP ressource
 * 
 * @author Björn Konieczeck <bjoern.konieczeck@uni-rostock.de>
 * @author Björn Butzin <bjoern.butzin@uni-rostock.de>
 */
public class ObservableResource extends BasicCoapResource {

	private static double MAX_TEMP = 30.0;
	private static double MIN_TEMP = 20.0;

	private ObservableResource(String path, String value, CoapMediaType mediaType) {
		super(path, value, mediaType);
		this.setResourceType("Temperature");
		this.setInterfaceDescription("testInterface");
		this.setObservable(true);
	}

	public ObservableResource() {
		this("/temperature", "LongPathResource Payload", CoapMediaType.text_plain);
	}

	@Override
	public synchronized CoapData get(List<String> query, List<CoapMediaType> mediaTypesAccepted) {
		return get(mediaTypesAccepted);
	}

	@Override
	public synchronized CoapData get(List<CoapMediaType> mediaTypesAccepted) {
		double randTemp = MIN_TEMP + (Math.random() * (MAX_TEMP - MIN_TEMP));
		String result = String.format(Locale.US, "%1$,.2f", randTemp);
		return new CoapData(result.getBytes(), CoapMediaType.text_plain);
	}
}
