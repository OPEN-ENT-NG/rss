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

package net.atos.entng.rss.controller;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.Rss;
import net.atos.entng.rss.constants.Field;
import net.atos.entng.rss.security.CustomOwner;
import net.atos.entng.rss.service.ChannelService;
import net.atos.entng.rss.service.ChannelServiceMongoImpl;
import net.atos.entng.rss.service.FeedService;
import net.atos.entng.rss.service.FeedServiceImpl;

import org.entcore.common.events.EventHelper;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;

import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;

public class RssController extends MongoDbControllerHelper {
	static final String RESOURCE_NAME = "rss";

	private final EventHelper eventHelper;
	private final ChannelService channelService;
	private final FeedService feedService;

	public RssController(EventBus eb) {
		super(Rss.RSS_COLLECTION);
		this.channelService = new ChannelServiceMongoImpl(Rss.RSS_COLLECTION);
		this.feedService = new FeedServiceImpl(eb);
		final EventStore eventStore = EventStoreFactory.getFactory().getEventStore(Rss.class.getSimpleName());
		this.eventHelper = new EventHelper(eventStore);
	}

	@Get("")
	@SecuredAction(value = "rss.view", type = ActionType.AUTHENTICATED)
	public void view(HttpServerRequest request) {
		renderView(request);
	}

	@Get("/channels")
	@SecuredAction(value = "channel.list", type = ActionType.AUTHENTICATED)
	public void getchannels(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, user -> channelService.list(user, arrayResponseHandler(request)));
	}

	@Post("/channel")
	@SecuredAction(value = "channel.create", type = ActionType.AUTHENTICATED)
	public void createchannel(HttpServerRequest request) {
		super.create(request, r -> {
			if (r.succeeded()) {
				eventHelper.onCreateResource(request, RESOURCE_NAME);
			}
		});
	}

	@Get("/channel/:id")
	@SecuredAction(value = "channel.read", type = ActionType.RESOURCE)
	public void getchannel(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, user -> {
            String id = request.params().get("id");
            channelService.retrieve(id, user, notEmptyResponseHandler(request));
        });
	}

	@Put("/channel/:id")
	@SecuredAction(value = "", type = ActionType.RESOURCE)
	public void updatechannel(HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                final String id = request.params().get("id");
                RequestUtils.bodyToJson(request, data -> {
					JsonArray allFeeds = data.getJsonArray(Field.FEEDS);
					channelService.update(user.getUserId(), id, allFeeds, defaultResponseHandler(request));
				});
            } else {
                unauthorized(request);
            }
        });
	}

	@Delete("/channel/:id")
	@SecuredAction(value = "", type = ActionType.RESOURCE)
	public void deletechannel(HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, (Handler<UserInfos>) user -> {
            if (user != null) {
                final String id = request.params().get("id");
                channelService.deleteChannel(user.getUserId(), id, defaultResponseHandler(request));
            } else {
                unauthorized(request);
            }
        });
	}

	/* feeds */
	@Get("/feed/items")
	@SecuredAction(value = "feed.read", type = ActionType.AUTHENTICATED)
	public void getfeedItems(final HttpServerRequest request) {
		final String url = request.params().get("url");
		final String force = request.params().get("force");
		if(url != null && !url.trim().isEmpty()){
			feedService.getItems(request, url, force, ar -> {
				if (ar.succeeded()) {
					JsonObject response = ar.result().body();
					Integer status = response.getInteger("status");
					renderJson(request, response, status);
				} else {
					renderJson(request, new JsonObject().put("status", 204), 204);
				}
			});
		} else {
			badRequest(request, "invalid.url");
		}
	}
}