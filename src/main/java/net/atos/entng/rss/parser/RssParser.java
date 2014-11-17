package net.atos.entng.rss.parser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class RssParser extends Verticle implements Handler<Message<String>> {

	@Override
	public void start() {
		super.start();
		vertx.eventBus().registerHandler("rss.parser", this);
	}

	@Override
	public void handle(final Message<String> message) {
		String url = message.body();
		JsonObject results = new JsonObject();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			DefaultHandler handler = new RssParserHandler(new Handler<JsonObject>(){
				@Override
				public void handle(JsonObject results) {
					message.reply(results);
				}
			});
			parser.parse(url, handler);
		} catch (SAXException | IOException se) {
			results.putNumber("status", 204);
			message.reply(results);
		} catch (ParserConfigurationException pce) {
			results.putNumber("status", 204);
			message.reply(results);
		}
	}

}
