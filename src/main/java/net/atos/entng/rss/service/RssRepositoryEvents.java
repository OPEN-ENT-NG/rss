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

import static net.atos.entng.rss.Rss.RSS_COLLECTION;

import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;

import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.user.RepositoryEvents;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import static com.mongodb.client.model.Filters.*;

public class RssRepositoryEvents implements RepositoryEvents {

	private static final Logger log = LoggerFactory.getLogger(RssRepositoryEvents.class);
	private final MongoDb mongo = MongoDb.getInstance();

	@Override
	public void exportResources(JsonArray resourcesIds, boolean exportDocuments, boolean exportSharedResources, String exportId, String userId,
								JsonArray groups, String exportPath, String locale, String host, Handler<Boolean> handler) {
		// TODO Implement exportResources
		log.warn("[RssRepositoryEvents] exportResources is not implemented");
	}

	@Override
	public void deleteGroups(JsonArray groups) {
		// FIXME : deleteGroups is not relevant for RSS
		log.warn("[RssRepositoryEvents] deleteGroups is not implemented");
	}

	@Override
	public void deleteUsers(JsonArray users) {
        //FIXME: anonymization is not relevant
		if(users == null) {
			log.warn("[RssRepositoryEvents][deleteUsers] JsonArray users is null or empty");
			return;
		}
		for(int i = users.size(); i-- > 0;)
		{
			if(users.hasNull(i))
				users.remove(i);
			else if (users.getJsonObject(i) != null && users.getJsonObject(i).getString("id") == null)
				users.remove(i);
		}
		if(users.size() == 0)
		{
			log.warn("[RssRepositoryEvents][deleteUsers] JsonArray users is null or empty");
			return;
		}

		final String [] usersIds = new String[users.size()];
		for (int i = 0; i < users.size(); i++) {
			JsonObject j = users.getJsonObject(i);
			usersIds[i] = j.getString("id");
		}

		// Delete the Rss collections of each user in the list
		final JsonObject criteria = MongoQueryBuilder.build(in("owner.userId", usersIds));

		mongo.delete(RSS_COLLECTION, criteria, MongoDbResult.validActionResultHandler(new Handler<Either<String,JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					log.info("[RssRepositoryEvents][deleteUsers] The resources created by users are deleted");
				} else {
					log.error("[RssRepositoryEvents][deleteUsers] Error deleting the resources created by users. Message : " + event.left().getValue());
				}
			}
		}));
	}

}
