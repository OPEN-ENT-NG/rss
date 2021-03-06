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

import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultsHandler;

import org.entcore.common.service.impl.MongoDbCrudService;
import org.entcore.common.user.UserInfos;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.mongodb.QueryBuilder;

import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;

public class ChannelServiceMongoImpl extends MongoDbCrudService implements ChannelService {

	private final String collection;
	private final MongoDb mongo;

	public ChannelServiceMongoImpl(final String collection) {
		super(collection);
		this.collection = collection;
		this.mongo = MongoDb.getInstance();
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
	public void deleteChannel(String idChannel, Handler<Either<String, JsonObject>> handler) {
		// Delete the channel
		QueryBuilder builder = QueryBuilder.start("_id").is(idChannel);
		mongo.delete(collection,  MongoQueryBuilder.build(builder), validResultHandler(handler));
	}

}
