package fr.webeduc.rss.parser;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.webeduc.rss.model.Feed;
import fr.webeduc.rss.model.Item;

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

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		if(qName.equals("rss")){
		}else if(qName.equals("item")){
			feed.addItem(item);
			item = null;
			buffer = null;
		}else if(qName.equals("title")){
			if(parent){
				feed.setTitle(buffer.toString().trim());
			}else{
				item.setTitle(buffer.toString().trim());
			}
			buffer = null;
		}else if(qName.equals("link")){
			if(parent){
				feed.setLink(buffer.toString().trim());
			}else{
				item.setLink(buffer.toString().trim());
			}
			buffer = null;
		}else if(qName.equals("description")){
			if(parent){
				feed.setDescription(buffer.toString().trim());
			}else{
				item.setDescription(buffer.toString().trim());
			}
			buffer = null;
		}else if(qName.equals("pubDate")){
			if(parent){
				feed.setPubDate(buffer.toString().trim());
			}else{
				item.setPubDate(buffer.toString().trim());
			}
			buffer = null;
		}else if(qName.equals("language")){
			feed.setLanguage(buffer.toString().trim());
			buffer = null;
		}
	}

	@Override
	public void characters(char[] ch,int start, int length) throws SAXException{
		String lecture = new String(ch,start,length);
		if(buffer != null) buffer.append(lecture);
	}

	@Override
	public void endDocument() throws SAXException {
		JsonObject results = feed.toJson();
		results.putNumber("status", 200);
		handler.handle(results);
	}
}
