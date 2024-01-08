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

import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.Rss;
import net.atos.entng.rss.constants.Field;
import net.atos.entng.rss.helpers.ChannelsHelper;
import net.atos.entng.rss.helpers.IModelHelper;
import net.atos.entng.rss.model.Channel;
import net.atos.entng.rss.model.ChannelFeed;
import net.atos.entng.rss.service.*;
import org.entcore.common.events.EventHelper;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.http.filter.ShareAndOwner;
import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.user.UserUtils;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import java.util.Collections;
import java.util.List;

public class RssController extends MongoDbControllerHelper {
	static final String RESOURCE_NAME = "rss";

	private final EventHelper eventHelper;
	private final ChannelService channelService;
	private final ChannelGlobalService channelGlobalService;
	private final FeedService feedService;

	public RssController(EventBus eb) {
		super(Rss.RSS_COLLECTION);
		this.channelService = new ChannelServiceMongoImpl(Rss.RSS_COLLECTION);
		this.channelGlobalService = new ChannelGlobalServiceMongoImpl(Rss.RSS_COLLECTION);
		this.feedService = new FeedServiceImpl(eb);
		final EventStore eventStore = EventStoreFactory.getFactory().getEventStore(Rss.class.getSimpleName());
		this.eventHelper = new EventHelper(eventStore);
	}

	@Get("")
	@SecuredAction(value = "", type = ActionType.AUTHENTICATED)
	public void view(HttpServerRequest request) {
		renderView(request);
	}

	@Get("/channels")
	@SecuredAction(value = "", type = ActionType.AUTHENTICATED)
	public void getchannels(final HttpServerRequest request) {
		JsonObject composeInfos = new JsonObject();
		UserUtils.getAuthenticatedUserInfos(eb, request)
			.compose(user -> {
				composeInfos.put(Field.USER, user.getUserId());
				return channelService.list(user);
			})
			.compose(channels -> {
				composeInfos.put(Field.CHANNELS, channels);
				return channelGlobalService.list();
			})
			.compose(globalChannels -> {
				List<Channel> channels = composeInfos.getJsonArray(Field.CHANNELS).getList();
				return ChannelsHelper.filterPreferences(composeInfos.getString(Field.USER), channels, globalChannels);
			})
			.compose(ChannelsHelper::mergeToOneChannel)
			.compose(userChannel -> {
				if (!userChannel.isPresent()) {
					return Future.failedFuture("No channel found");
				}
				return Future.succeededFuture(userChannel.get());
			})
			.onSuccess(userChannel -> renderJson(request, IModelHelper.toJsonArray(Collections.singletonList(userChannel))))
			.onFailure(error -> {
				String message = String.format("[RSS@%s::GetChannels] Failed to get channels : %s",
						this.getClass().getSimpleName(), error.getMessage());
				log.error(message);
				renderError(request);
			});
	}

	@Post("/channel")
	@SecuredAction(value = "", type = ActionType.AUTHENTICATED)
	public void createchannel(HttpServerRequest request) {
		super.create(request, r -> {
			if (r.succeeded()) {
				eventHelper.onCreateResource(request, RESOURCE_NAME);
			}
		});
	}

	@Get("/channel/:id")
	@ResourceFilter(ShareAndOwner.class)
	@SecuredAction(value = "", type = ActionType.RESOURCE)
	public void getchannel(final HttpServerRequest request) {
		UserUtils.getAuthenticatedUserInfos(eb, request)
			.compose(userInfos -> channelService.retrieve(request.params().get(Field.ID), userInfos))
			.onSuccess(result -> renderJson(request, IModelHelper.toJson(result, false, false)))
			.onFailure(error -> {
				String message = String.format("[RSS@%s::GetChannel] Failed to get channel %s : %s",
						this.getClass().getSimpleName(), request.params().get(Field.ID), error.getMessage());
				log.error(message);
				renderError(request);
			});
	}

	@Put("/channel/:id")
	@ResourceFilter(ShareAndOwner.class)
	@SecuredAction(value = "", type = ActionType.RESOURCE)
	public void updatechannel(HttpServerRequest request) {
		RequestUtils.bodyToJson(request, feeds -> UserUtils.getAuthenticatedUserInfos(eb, request)
			.compose(userInfos -> channelService.update(userInfos.getUserId(), request.params().get(Field.ID), IModelHelper.toList(feeds.getJsonArray(Field.FEEDS), ChannelFeed.class)))
			.onSuccess(result -> ok(request))
			.onFailure(error -> {
				String message = String.format("[RSS@%s::PutChannel] Failed to update channel %s : %s",
						this.getClass().getSimpleName(), request.params().get(Field.ID), error.getMessage());
				log.error(message);
				renderError(request);
			}));
	}

	@Delete("/channel/:id")
	@ResourceFilter(ShareAndOwner.class)
	@SecuredAction(value = "", type = ActionType.RESOURCE)
	public void deletechannel(HttpServerRequest request) {
		UserUtils.getAuthenticatedUserInfos(eb, request)
			.compose(userInfos -> channelService.deleteChannel(userInfos.getUserId(), request.params().get(Field.ID)))
			.onSuccess(result -> ok(request))
			.onFailure(error -> {
				String message = String.format("[RSS@%s::GetChannel] Failed to get channel %s : %s",
						this.getClass().getSimpleName(), request.params().get(Field.ID), error.getMessage());
				log.error(message);
				renderError(request);
			});
	}

	/* feeds */
	@Get("/feed/items")
	@SecuredAction(value = "", type = ActionType.AUTHENTICATED)
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