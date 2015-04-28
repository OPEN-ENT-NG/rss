package net.atos.entng.rss.parser;

import net.atos.entng.rss.model.Feed;
import net.atos.entng.rss.model.Item;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
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
		results.putNumber("status", 200);
		handler.handle(results);
	}
}
