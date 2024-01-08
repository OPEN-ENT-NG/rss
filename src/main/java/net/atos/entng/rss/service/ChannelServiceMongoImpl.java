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

import fr.wseduc.mongodb.MongoUpdateBuilder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.atos.entng.rss.constants.Field;
import net.atos.entng.rss.helpers.IModelHelper;
import net.atos.entng.rss.helpers.PreferenceHelper;
import net.atos.entng.rss.helpers.ChannelsHelper;
import net.atos.entng.rss.model.Channel;
import net.atos.entng.rss.model.ChannelFeed;
import org.entcore.common.service.impl.MongoDbCrudService;
import org.entcore.common.user.UserInfos;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import com.mongodb.QueryBuilder;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import java.util.ArrayList;
import java.util.List;

import static org.entcore.common.mongodb.MongoDbResult.*;

public class ChannelServiceMongoImpl extends MongoDbCrudService implements ChannelService {

	private final String collection;
	private final MongoDb mongo;
	private final ChannelGlobalServiceMongoImpl channelGlobalServiceMongo;
	private static final Logger log = LoggerFactory.getLogger(ChannelServiceMongoImpl.class);

	public ChannelServiceMongoImpl(final String collection) {
		super(collection);
		this.collection = collection;
		this.mongo = MongoDb.getInstance();
		this.channelGlobalServiceMongo = new ChannelGlobalServiceMongoImpl(collection);
	}

	@Override
	public Future<Channel> create(UserInfos user, JsonObject feed) {
		// Create channel
		Promise<Channel> promise = Promise.promise();
		JsonObject now = MongoDb.now();
		feed.put(Field.MODIFIED, now);
		feed.put(Field.CREATED, now);
		JsonObject owner = new JsonObject()
				.put(Field.USER_ID, user.getUserId())
				.put(Field.DISPLAY_NAME, user.getUsername());
		feed.put(Field.OWNER, owner);
		mongo.insert(collection, feed, validActionResultHandler(IModelHelper.uniqueResultToIModel(promise, Channel.class)));
		return promise.future();
	}

	@Override
	public Future<List<Channel>> list(UserInfos user) {
		Promise<List<Channel>> promise = Promise.promise();
		QueryBuilder query = QueryBuilder.start("owner.userId").is(user.getUserId()).and(Field.GLOBAL).notEquals(true);
		// get channels
		mongo.find(collection, MongoQueryBuilder.build(query), null, null, validResultsHandler(result -> {
			if (result.isLeft()) {
				log.error("[RSS@ChannelServiceMongoImpl::list] Can't find user's channel");
				promise.fail(result.left().getValue());
				return;
			}
			List<Channel> channels = IModelHelper.toList(result.right().getValue(), Channel.class);
			// if user don't have a channel, create
			if (channels.isEmpty()) {
				// create your channel
				this.create(user, new JsonObject().put(Field.FEEDS, new JsonArray()))
					.compose(resultCreated -> this.list(user))
					.onSuccess(promise::complete) // retry list with new channel created
					.onFailure(error -> {
						log.error("[RSS@ChannelServiceMongoImpl::list] Can't create user's channel");
						promise.fail(error);
					});
			} else {
				promise.complete(channels);
			}
		}));
		return promise.future();
	}

	@Override
	public Future<Channel> retrieve(String idChannel, UserInfos user){
		// Query
		Promise<Channel> promise = Promise.promise();
		QueryBuilder builder = QueryBuilder.start(Field.MONGO_ID).is(idChannel);
		mongo.findOne(collection,  MongoQueryBuilder.build(builder), null, validResultHandler(IModelHelper.uniqueResultToIModel(promise, Channel.class)));
		return promise.future();
	}

	@Override
	public Future<Channel> deleteChannel(String userId, String idChannel) {
		// Delete the channel
		Promise<Channel> promise = Promise.promise();
		QueryBuilder builder = QueryBuilder.start(Field.MONGO_ID).is(idChannel);
		mongo.findOne(collection,  MongoQueryBuilder.build(builder), null, validResultHandler(result -> {
			if (result.isLeft()) {
				log.error("[RSS@ChannelServiceMongoImpl::delete] Can't delete user's channel");
				promise.fail(result.left().getValue());
				return;
			}
			JsonObject channel = result.right().getValue();
			// can't delete the global channel in this endpoint
			if (!Boolean.TRUE.equals(channel.getBoolean(Field.GLOBAL, false))) {
				mongo.delete(collection,  MongoQueryBuilder.build(builder), validResultHandler(IModelHelper.uniqueResultToIModel(promise, Channel.class)));
			} else {
				log.error("[RSS@ChannelServiceMongoImpl::delete] Can't delete global channel, use /channels/globals");
				promise.fail("Global channel cannot be deleted");
			}
		}));
		return promise.future();
	}

	@Override
	public Future<Channel> update(String userId, String idChannel, List<ChannelFeed> feeds) {
		Promise<Channel> promise = Promise.promise();
		QueryBuilder builder = QueryBuilder.start(Field.MONGO_ID).is(idChannel);
		mongo.findOne(collection,  MongoQueryBuilder.build(builder), null, validResultHandler(result -> {
			if (result.isLeft()) {
				log.error("[RSS@ChannelServiceMongoImpl::update] Can't find user's channel");
				promise.fail(result.left().getValue());
				return;
			}
			JsonObject channel = result.right().getValue();
			if (!Boolean.TRUE.equals(channel.getBoolean(Field.GLOBAL, false))) {
				// get globals channels
				this.channelGlobalServiceMongo.list()
					.compose(globalChannels -> ChannelsHelper.filterPreferences(userId, globalChannels, new ArrayList<>()))
					.onSuccess(userChannels -> {
						List<ChannelFeed> globalFeeds = ChannelsHelper.removeGlobalFeeds(feeds, userChannels);
						List<Channel> globalChannelsList = ChannelsHelper.removeGlobalChannels(feeds, userChannels);
						// remove global feeds
						feeds.removeAll(globalFeeds);
						// remove global channels
						userChannels.removeAll(globalChannelsList);
						if (!userChannels.isEmpty()) {
							// add preferences to hidden global channels
							PreferenceHelper.addPreferences(userId, userChannels)
								.onFailure(error -> {
									log.error("[RSS@ChannelServiceMongoImpl::update] Can't add preferences");
									promise.fail(error);
								});
						}
						// update channel
						MongoUpdateBuilder modifier = new MongoUpdateBuilder();
						JsonObject now = MongoDb.now();
						modifier.set(Field.FEEDS, IModelHelper.toJsonArray(feeds)).set(Field.MODIFIED, now);
						mongo.update(collection, MongoQueryBuilder.build(builder), modifier.build(), validActionResultHandler(IModelHelper.uniqueResultToIModel(promise, Channel.class)));
					})
					.onFailure(error -> {
						log.error("[RSS@ChannelServiceMongoImpl::update] Can't get channels");
						promise.fail(error);
					});
			} else {
				log.error("[RSS@ChannelServiceMongoImpl::update] Can't update global channel, use /channels/globals");
				promise.fail("Global channel cannot be updated here");
			}
		}));
		return promise.future();
	}
}
