package net.atos.entng.rss.service;

import net.atos.entng.rss.parser.RssParser;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;

public class FeedServiceImpl implements FeedService {

	private final EventBus eb;

	public FeedServiceImpl(EventBus eb){
		this.eb = eb;
	}

	@Override
	public void getItems(final HttpServerRequest request, final String url, String force, Handler<Either<String, JsonObject>> JsonObjectHandler) {
		JsonObject message = new JsonObject();
		message.putString("url", url);
		message.putString("force", force);
		message.putString("action", RssParser.ACTION_GET);
		eb.send(RssParser.PARSER_ADDRESS, message, new Handler<Message<JsonObject>>(){
			@Override
			public void handle(Message<JsonObject> reply) {
				JsonObject response = reply.body();
				Integer status = response.getInteger("status");
				Renders.renderJson(request, response, status);
			}
		});
	}
}
