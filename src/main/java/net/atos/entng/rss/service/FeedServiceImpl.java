package net.atos.entng.rss.service;

import net.atos.entng.rss.parser.RssParserCache;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;

public class FeedServiceImpl implements FeedService {

	private static final String PARSER_ADDRESS = "rss.parser";
	private final EventBus eb;
	private final RssParserCache rssParserCache;
	private static final long CLEANING_TIMEOUT = 30; // minutes
	private static final Logger log = LoggerFactory.getLogger(FeedServiceImpl.class);

	public FeedServiceImpl(EventBus eb){
		this.eb = eb;
		rssParserCache = new RssParserCache(CLEANING_TIMEOUT);
	}

	@Override
	public void getItems(final HttpServerRequest request, final String url, String force, Handler<Either<String, JsonObject>> JsonObjectHandler) {
		if(force.equals("0") && rssParserCache.has(url)){
			rssParserCache.get(url, new Handler<Either<String, JsonObject>>(){
				@Override
				public void handle(Either<String, JsonObject> event) {
					JsonObject response = null;
					Integer status = 204; // KO
					if(event.isRight()){
						response = event.right().getValue();
						status = 200; // OK
					}
					else {
						log.error("[FeedServiceImpl][getItems] Error : " + event.left().getValue());
					}
					Renders.renderJson(request, response, status);
				}
			});
		}
		else{
			eb.send(PARSER_ADDRESS, url, new Handler<Message<JsonObject>>(){
				@Override
				public void handle(Message<JsonObject> message) {
					JsonObject response = message.body();
					Integer status = response.getInteger("status");
					if(status == 200){
						rssParserCache.put(url, response);
					}
					Renders.renderJson(request, response, status);
				}
			});
		}
	}
}
