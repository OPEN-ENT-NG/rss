package fr.wseduc.rss.service;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;

public class FeedServiceImpl implements FeedService {

	private static final String PARSER_ADDRESS = "rss.parser";
	private final EventBus eb;

	public FeedServiceImpl(EventBus eb){
		this.eb = eb;
	}

	@Override
	public void getItems(final HttpServerRequest request, String url, Handler<Either<String, JsonObject>> JsonObjectHandler) {
		eb.send(PARSER_ADDRESS, url, new Handler<Message<JsonObject>>(){
			@Override
			public void handle(Message<JsonObject> message) {
				JsonObject response  = message.body();
				Integer status = response.getInteger("status");
				Renders.renderJson(request, response, status);
			}
		});
	}
}
