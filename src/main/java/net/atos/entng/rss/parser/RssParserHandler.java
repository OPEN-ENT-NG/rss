/*
 * Copyright © Région Nord Pas de Calais-Picardie,  Département 91, Région Aquitaine-Limousin-Poitou-Charentes, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package net.atos.entng.rss.parser;

import net.atos.entng.rss.model.Feed;
import net.atos.entng.rss.model.Item;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class RssParserHandler extends DefaultHandler {

	private StringBuffer buffer;
	private Feed feed;
	private Item item;
	private final Handler<JsonObject> handler;
	private boolean parent;

	public RssParserHandler(Handler<JsonObject> handler) {
		super();
		this.handler = handler;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		if(qName != null){
			if(qName.equals("channel")){
				parent = true;
				feed = new Feed();
			}else if(qName.equals("rss")){
				buffer = new StringBuffer();
			}else if(qName.equals("item")){
				parent = false;
				item = new Item();
			}else {
				buffer = new StringBuffer();
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		if(qName != null){
			if(qName.equals("rss")){
			}else if(qName.equals("item")){
				if(item != null && feed != null){
					feed.addItem(item);
					item = null;
				}
				buffer = null;
			}else if(qName.equals("title")){
				if(buffer != null){
					if(parent){
						if(feed != null)
							feed.setTitle(buffer.toString().trim());
					}else{
						if(item != null)
							item.setTitle(buffer.toString().trim());
					}
					buffer = null;
				}
			}else if(qName.equals("link")){
				if(buffer != null){
					if(parent){
						if(feed != null)
							feed.setLink(buffer.toString().trim());
					}else{
						if(item != null)
							item.setLink(buffer.toString().trim());
					}
					buffer = null;
				}
			}else if(qName.equals("description")){
				if(buffer != null){
					if(parent){
						if(feed != null)
							feed.setDescription(buffer.toString().trim());
					}else{
						if(item != null)
							item.setDescription(buffer.toString().trim());
					}
					buffer = null;
				}
			}else if(qName.equals("pubDate")){
				if(buffer != null){
					if(parent){
						if(feed != null)
							feed.setPubDate(buffer.toString().trim());
					}else{
						if(item != null)
							item.setPubDate(buffer.toString().trim());
					}
					buffer = null;
				}
			}else if(qName.equals("dc:date")){
				if(buffer != null){
					if(parent){
						if(feed != null && feed.getPubDate() == null)
							feed.setPubDate(buffer.toString().trim());
					}else{
						if(item != null && item.getPubDate() == null)
							item.setPubDate(buffer.toString().trim());
					}
					buffer = null;
				}
			}else if(qName.equals("language")){
				if(buffer != null && feed != null){
					feed.setLanguage(buffer.toString().trim());
					buffer = null;
				}
			}
		}
	}

	@Override
	public void characters(char[] ch,int start, int length) throws SAXException{
		String lecture = new String(ch,start,length);
		if(buffer != null) buffer.append(lecture);
	}

	@Override
	public void endDocument() throws SAXException {
		JsonObject results = (feed != null) ? feed.toJson() : new JsonObject();
		results.put("status", 200);
		handler.handle(results);
	}
}
