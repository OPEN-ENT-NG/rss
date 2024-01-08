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
	private final Neo4j neo;

	public ChannelServiceMongoImpl(final String collection) {
		super(collection);
		this.collection = collection;
		this.mongo = MongoDb.getInstance();
		this.neo = Neo4j.getInstance();
	}

	@Override
	public void create(JsonObject channel, Handler<Either<String, JsonObject>> handler) {
		// Create channel
		mongo.insert(collection, channel, validResultHandler(handler));
	}

	@Override
	public void list(UserInfos user, Handler<Either<String, JsonArray>> arrayResponseHandler) {
		// Start
		QueryBuilder query = QueryBuilder.start("owner.userId").is(user.getUserId());
		mongo.find(collection, MongoQueryBuilder.build(query), null, null, validResultsHandler(arrayResponseHandler));
	}

	@Override
	public void retrieve(String idChannel, UserInfos user, Handler<Either<String, JsonObject>> notEmptyResponseHandler){
		// Query
		QueryBuilder builder = QueryBuilder.start("_id").is(idChannel);
		mongo.findOne(collection,  MongoQueryBuilder.build(builder), null, validResultHandler(notEmptyResponseHandler));
	}

	@Override
	public void deleteChannel(String userId, String idChannel, Handler<Either<String, JsonObject>> handler) {
		// Delete the channel
		QueryBuilder builder = QueryBuilder.start("_id").is(idChannel);
		mongo.findOne(collection,  MongoQueryBuilder.build(builder), null, validResultHandler(result -> {
			if (result.isLeft()) {
				handler.handle(new Either.Left<>(result.left().getValue()));
				return;
			}
			JsonObject channel = result.right().getValue();
			if (Boolean.TRUE.equals(channel.getBoolean("global", false))) {
				this.getGlobalRemoved(userId, event -> {
                    if (event.isLeft()) {
                        handler.handle(new Either.Left<>(event.left().getValue()));
                        return;
                    }
                    JsonArray array;
                    if (event.right().getValue() == null) {
                        array = new JsonArray();
                    } else {
                        String str = event.right().getValue().getString("uac.rss");
                        JsonObject obj = new JsonObject(str);
                        array = obj.getJsonArray("no-display-rss-id");
                    }
                    array.add(idChannel);
                    String arrayString = array.toString().replace("\"", "\\\"");
                    String query = String.format("MATCH (u:User {id:\"%s\"}) MERGE (u)-[:PREFERS]->(uac:UserAppConf)"
                            + " ON CREATE SET uac.rss= \"{\\\"no-display-rss-id\\\": %s}\""
                            + " ON MATCH SET uac.rss= \"{\\\"no-display-rss-id\\\": %s}\"", userId, arrayString, arrayString);
                    neo.execute(query, new JsonObject(), Neo4jResult.validUniqueResultHandler(handler));
                });
			} else {
				mongo.delete(collection,  MongoQueryBuilder.build(builder), validResultHandler(handler));
			}
		}));
	}

	@Override
	public void update(String userId, String idChannel, JsonArray feeds, Handler<Either<String, JsonObject>> handler) {
		QueryBuilder builder = QueryBuilder.start("_id").is(idChannel);
		mongo.findOne(collection,  MongoQueryBuilder.build(builder), null, validResultHandler(result -> {
			if (result.isLeft()) {
				handler.handle(new Either.Left<>(result.left().getValue()));
				return;
			}
			JsonObject channel = result.right().getValue();
			if (Boolean.TRUE.equals(channel.getBoolean("global", false))) {
				this.getGlobalRemoved(userId, event -> {
					if (event.isLeft()) {
						handler.handle(new Either.Left<>(event.left().getValue()));
						return;
					}
					JsonArray array;
					if (event.right().getValue() == null) {
						array = new JsonArray();
					} else {
						String str = event.right().getValue().getString("uac.rss");
						JsonObject obj = new JsonObject(str);
						array = obj.getJsonArray("no-display-rss-id");
					}
					array.add(idChannel);
					String arrayString = array.toString().replace("\"", "\\\"");
					String query = String.format("MATCH (u:User {id:\"%s\"}) MERGE (u)-[:PREFERS]->(uac:UserAppConf)"
							+ " ON CREATE SET uac.rss= \"{\\\"no-display-rss-id\\\": %s}\""
							+ " ON MATCH SET uac.rss= \"{\\\"no-display-rss-id\\\": %s}\"", userId, arrayString, arrayString);
					neo.execute(query, new JsonObject(), Neo4jResult.validUniqueResultHandler(handler));
					// enregistrer le nouveau dans rss perso
					QueryBuilder builder2 = QueryBuilder.start("owner.userId").is(userId);
					MongoUpdateBuilder modifier = new MongoUpdateBuilder();
					JsonObject now = MongoDb.now();
					feeds.addAll(channel.getJsonArray("feeds"));
					modifier.set("feeds", feeds).set("modified", now);
					mongo.update(collection, MongoQueryBuilder.build(builder2), modifier.build(), validActionResultHandler(handler));
				});
			} else {
				// update the non global channel
				MongoUpdateBuilder modifier = new MongoUpdateBuilder();
				JsonObject now = MongoDb.now();
				modifier.set("feeds", feeds).set("modified", now);
				mongo.update(collection, MongoQueryBuilder.build(builder), modifier.build(), validActionResultHandler(handler));
			}
		}));
	}

	private void getGlobalRemoved(String userId, Handler<Either<String, JsonObject>> handler) {
		String query = String.format("MATCH (u:User {id:\"%s\"})-[:PREFERS]->(uac:UserAppConf)"
		+ " RETURN uac.rss LIMIT 1", userId);

		neo.execute(query, new JsonObject().put("userId", userId), Neo4jResult.validUniqueResultHandler(handler));
	}
}
