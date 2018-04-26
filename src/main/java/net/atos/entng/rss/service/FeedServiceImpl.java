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

package net.atos.entng.rss.service;

import io.vertx.core.AsyncResult;
import net.atos.entng.rss.parser.RssParser;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

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
		message.put("url", url);
		message.put("force", force);
		message.put("action", RssParser.ACTION_GET);
		eb.send(RssParser.PARSER_ADDRESS, message, new Handler<AsyncResult<Message<JsonObject>>>(){
			@Override
			public void handle(AsyncResult<Message<JsonObject>> ar) {
				if (ar.succeeded()) {
					JsonObject response = ar.result().body();
					Integer status = response.getInteger("status");
					Renders.renderJson(request, response, status);
				} else {
					Renders.renderJson(request, new JsonObject().put("status", 204), 204);
				}
			}
		});
	}
}
