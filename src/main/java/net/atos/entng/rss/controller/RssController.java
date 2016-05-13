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

import net.atos.entng.rss.Rss;
import net.atos.entng.rss.service.ChannelService;
import net.atos.entng.rss.service.ChannelServiceMongoImpl;
import net.atos.entng.rss.service.FeedService;
import net.atos.entng.rss.service.FeedServiceImpl;

import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;

public class RssController extends MongoDbControllerHelper {

	private final ChannelService channelService;
	private final FeedService feedService;

	public RssController(EventBus eb) {
		super(Rss.RSS_COLLECTION);
		this.channelService = new ChannelServiceMongoImpl(Rss.RSS_COLLECTION);
		this.feedService = new FeedServiceImpl(eb);
	}

	@Get("")
	@SecuredAction(value = "rss.view", type = ActionType.AUTHENTICATED)
	public void view(HttpServerRequest request) {
		renderView(request);
	}

	@Get("/channels")
	@SecuredAction(value = "channel.list", type = ActionType.AUTHENTICATED)
	public void getchannels(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				channelService.list(user, arrayResponseHandler(request));
			}
		});
	}

	@Post("/channel")
	@SecuredAction(value = "channel.create", type = ActionType.AUTHENTICATED)
	public void createchannel(HttpServerRequest request) {
		super.create(request);
	}

	@Get("/channel/:id")
	@SecuredAction(value = "channel.read", type = ActionType.RESOURCE)
	public void getchannel(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				String id = request.params().get("id");
				channelService.retrieve(id, user, notEmptyResponseHandler(request));
			}
		});
	}

	@Put("/channel/:id")
	@SecuredAction(value = "", type = ActionType.RESOURCE)
	public void updatechannel(HttpServerRequest request) {
		super.update(request);
	}

	@Delete("/channel/:id")
	@SecuredAction(value = "", type = ActionType.RESOURCE)
	public void deletechannel(HttpServerRequest request) {
		final String id = request.params().get("id");
		channelService.deleteChannel(id, defaultResponseHandler(request));
	}

	/* feeds */

	@Get("/feed/items")
	@SecuredAction(value = "feed.read", type = ActionType.AUTHENTICATED)
	public void getfeedItems(final HttpServerRequest request) {
		final String url = request.params().get("url");
		final String force = request.params().get("force");
		if(url != null && !url.trim().isEmpty()){
			feedService.getItems(request, url, force, defaultResponseHandler(request));
		} else {
			badRequest(request, "invalid.url");
		}
	}

}