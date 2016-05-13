package net.atos.entng.rss.service;

import static net.atos.entng.rss.Rss.RSS_COLLECTION;

import com.mongodb.QueryBuilder;

import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;

import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.user.RepositoryEvents;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

public class RssRepositoryEvents implements RepositoryEvents {

	private static final Logger log = LoggerFactory.getLogger(RssRepositoryEvents.class);
	private final MongoDb mongo = MongoDb.getInstance();

	@Override
	public void exportResources(String exportId, String userId, JsonArray groups, String exportPath, String locale, String host, Handler<Boolean> handler) {
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
		if(users == null || users.size() == 0) {
			log.warn("[RssRepositoryEvents][deleteUsers] JsonArray users is null or empty");
			return;
		}

		final String [] usersIds = new String[users.size()];
		for (int i = 0; i < users.size(); i++) {
			JsonObject j = users.get(i);
			usersIds[i] = j.getString("id");
		}

		// Delete the Rss collections of each user in the list
		final JsonObject criteria = MongoQueryBuilder.build(QueryBuilder.start("owner.userId").in(usersIds));

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
