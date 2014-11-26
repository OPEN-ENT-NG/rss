package net.atos.entng.rss.parser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.atos.entng.rss.service.FeedServiceImpl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Verticle;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.wseduc.webutils.Either;

public class RssParser extends Verticle implements Handler<Message<JsonObject>> {

	private RssParserCache rssParserCache;
	private long cleanTimeout;
	private static final Logger log = LoggerFactory.getLogger(FeedServiceImpl.class);
	public static final long DEFAULT_CLEAN_TIMEOUT = 30; // Minutes
	public static final String PARSER_ADDRESS = "rss.parser";
	public static final String ACTION_CLEANUP = "cleanUp";
	public static final String ACTION_GET = "get";

	@Override
	public void start() {
		super.start();
		cleanTimeout = container.config().getLong("clean-timeout", DEFAULT_CLEAN_TIMEOUT) * 60000;
		vertx.eventBus().registerHandler("rss.parser", this);
		rssParserCache = new RssParserCache();
		vertx.setPeriodic(cleanTimeout, new Handler<Long>() {
			@Override
			public void handle(Long cleanTimeout) {
				JsonObject message = new JsonObject();
				message.putString("action", RssParser.ACTION_CLEANUP);
				message.putNumber("cleanTimeout", cleanTimeout);
				vertx.eventBus().send(RssParser.PARSER_ADDRESS, message, new Handler<Message<String>>() {
					@Override
					public void handle(Message<String> message) {
						log.info("Received reply: " + message.body());
					}
				});
			}
		});
	}

	@Override
	public void handle(final Message<JsonObject> message) {
		JsonObject msg = message.body();
		if(msg != null){
			String action = msg.getString("action", "");
			switch(action){
			case ACTION_CLEANUP:
				long cleanTimeout = msg.getLong("cleanTimeout", DEFAULT_CLEAN_TIMEOUT);
				rssParserCache.cleanUp(cleanTimeout);
				break;
			case ACTION_GET:
				String force = msg.getString("force", "0");
				final String url = msg.getString("url", "");
				if(force.equals("0") && rssParserCache.has(url)){
					rssParserCache.get(url, new Handler<Either<String, JsonObject>>(){
						@Override
						public void handle(Either<String, JsonObject> event) {
							JsonObject results = new JsonObject();
							if(event.isRight()){
								results = event.right().getValue();
								results.putNumber("status", 200); // OK
							}
							else {
								results.putNumber("status", 204); // KO
								log.error("[FeedServiceImpl][getItems] Error : " + event.left().getValue());
							}
							message.reply(results);
						}
					});
				}
				else{
					JsonObject results = new JsonObject();
					try {
						SAXParserFactory factory = SAXParserFactory.newInstance();
						SAXParser parser = factory.newSAXParser();
						DefaultHandler handler = new RssParserHandler(new Handler<JsonObject>(){
							@Override
							public void handle(JsonObject results) {
								rssParserCache.put(url, results);
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
		}
	}

}
