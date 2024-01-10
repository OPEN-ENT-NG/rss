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
import net.atos.entng.rss.constants.Field;
import net.atos.entng.rss.helpers.PreferenceHelper;
import net.atos.entng.rss.helpers.UtilsHelper;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.MongoDbCrudService;
import org.entcore.common.user.UserInfos;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.mongodb.QueryBuilder;

import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;

import static org.entcore.common.mongodb.MongoDbResult.*;

public class ChannelServiceMongoImpl extends MongoDbCrudService implements ChannelService {

	private final String collection;
	private final MongoDb mongo;
	private final ChannelGlobalServiceMongoImpl channelGlobalServiceMongo;

	public ChannelServiceMongoImpl(final String collection) {
		super(collection);
		this.collection = collection;
		this.mongo = MongoDb.getInstance();
		this.channelGlobalServiceMongo = new ChannelGlobalServiceMongoImpl(collection);
	}

	@Override
	public void create(UserInfos user, JsonObject channel, Handler<Either<String, JsonObject>> handler) {
		// Create channel
		JsonObject now = MongoDb.now();
		channel.put(Field.MODIFIED, now);
		channel.put(Field.CREATED, now);
		JsonObject owner = new JsonObject()
				.put(Field.USER_ID, user.getUserId())
				.put(Field.DISPLAY_NAME, user.getUsername());
		channel.put(Field.OWNER, owner);
		mongo.insert(collection, channel, validActionResultHandler(handler));
	}

	@Override
	public void list(UserInfos user, Handler<Either<String, JsonArray>> arrayResponseHandler) {
		QueryBuilder query = QueryBuilder.start("owner.userId").is(user.getUserId()).and(Field.GLOBAL).notEquals(true);
		// get channels
		mongo.find(collection, MongoQueryBuilder.build(query), null, null, validResultsHandler(result -> {
			if (result.isLeft()) {
				arrayResponseHandler.handle(new Either.Left<>(result.left().getValue()));
				return;
			}
			JsonArray channels = result.right().getValue();
			// if user don't have a channel, create
			if (channels.isEmpty()) {
				// create your channel
				this.create(user, new JsonObject().put(Field.FEEDS, new JsonArray()), createResult -> {
					if (createResult.isLeft()) {
						arrayResponseHandler.handle(new Either.Left<>(createResult.left().getValue()));
					}
				});
				// retry list with new channel created
				this.list(user, arrayResponseHandler);
				return;
			}
			// get preferences
			PreferenceHelper.getPreferences(user.getUserId(), resultPref -> {
				if (resultPref.isLeft()) {
					arrayResponseHandler.handle(new Either.Left<>(resultPref.left().getValue()));
					return;
				}
				JsonArray preferences = resultPref.right().getValue();
				// get global channels
				this.channelGlobalServiceMongo.list(globalList -> {
					if (globalList.isLeft()) {
						arrayResponseHandler.handle(new Either.Left<>(globalList.left().getValue()));
						return;
					}
					JsonArray globalArray = globalList.right().getValue();
					// merge channels, preferences and global channels
					JsonArray mergedArray = UtilsHelper.mergeArray(channels, globalArray, preferences);
					// merge all feeds in the user channel
					JsonObject firstItem = mergedArray.getJsonObject(0);
					for (int i = 1; i < mergedArray.size(); i++) {
						JsonObject channel = mergedArray.getJsonObject(i);
						firstItem.put(Field.FEEDS, firstItem.getJsonArray(Field.FEEDS).addAll(channel.getJsonArray(Field.FEEDS)));
					}
					arrayResponseHandler.handle(new Either.Right<>(new JsonArray().add(firstItem)));
				});
			});
		}));
	}

	@Override
	public void retrieve(String idChannel, UserInfos user, Handler<Either<String, JsonObject>> notEmptyResponseHandler){
		// Query
		QueryBuilder builder = QueryBuilder.start(Field.ID).is(idChannel);
		mongo.findOne(collection,  MongoQueryBuilder.build(builder), null, validResultHandler(notEmptyResponseHandler));
	}

	@Override
	public void deleteChannel(String userId, String idChannel, Handler<Either<String, JsonObject>> handler) {
		// Delete the channel
		QueryBuilder builder = QueryBuilder.start(Field.ID).is(idChannel);
		mongo.findOne(collection,  MongoQueryBuilder.build(builder), null, validResultHandler(result -> {
			if (result.isLeft()) {
				handler.handle(new Either.Left<>(result.left().getValue()));
				return;
			}
			JsonObject channel = result.right().getValue();
			// can't delete the global channel in this endpoint
			if (!Boolean.TRUE.equals(channel.getBoolean(Field.GLOBAL, false))) {
				mongo.delete(collection,  MongoQueryBuilder.build(builder), validResultHandler(handler));
			} else {
				handler.handle(new Either.Left<>("Global channel cannot be deleted"));
			}
		}));
	}

	@Override
	public void update(String userId, String idChannel, JsonArray feeds, Handler<Either<String, JsonObject>> handler) {
		QueryBuilder builder = QueryBuilder.start(Field.ID).is(idChannel);
		mongo.findOne(collection,  MongoQueryBuilder.build(builder), null, validResultHandler(result -> {
			if (result.isLeft()) {
				handler.handle(new Either.Left<>(result.left().getValue()));
				return;
			}
			JsonObject channel = result.right().getValue();
			if (!Boolean.TRUE.equals(channel.getBoolean(Field.GLOBAL, false))) {
				// get globals channels
				this.channelGlobalServiceMongo.list(globalList -> {
					if (globalList.isLeft()) {
						handler.handle(new Either.Left<>(globalList.left().getValue()));
						return;
					}
					JsonArray globalArray = globalList.right().getValue();
					// get preferences
					PreferenceHelper.getPreferences(userId, resultPreferences -> {
						if (resultPreferences.isLeft()) {
							handler.handle(new Either.Left<>(resultPreferences.left().getValue()));
							return;
						}
						JsonArray globalAndPref = UtilsHelper.mergeArray(new JsonArray(), globalArray, resultPreferences.right().getValue());
						// remove global feeds and keep removed global in globalAndPref to add in preferences
						UtilsHelper.removeGlobalFeeds(feeds, globalAndPref);
						if (!globalAndPref.isEmpty()) {
							PreferenceHelper.addPreferences(userId, globalAndPref, resultAddPreferences -> {
								if (resultAddPreferences.isLeft()) {
									handler.handle(new Either.Left<>(resultPreferences.left().getValue()));
								}
							});
						}
						// update channel
						MongoUpdateBuilder modifier = new MongoUpdateBuilder();
						JsonObject now = MongoDb.now();
						modifier.set(Field.FEEDS, feeds).set(Field.MODIFIED, now);
						mongo.update(collection, MongoQueryBuilder.build(builder), modifier.build(), validActionResultHandler(handler));
					});
				});
			} else {
				handler.handle(new Either.Left<>("Global channel cannot be updated"));
			}
		}));
	}
}
