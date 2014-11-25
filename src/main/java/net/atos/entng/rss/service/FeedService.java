package net.atos.entng.rss.service;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;

public interface FeedService {

	public void getItems(HttpServerRequest request, String url, String force, Handler<Either<String, JsonObject>> handler);

}
