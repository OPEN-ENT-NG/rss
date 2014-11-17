package net.atos.entng.rss.service;

import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultsHandler;

import org.entcore.common.service.impl.MongoDbCrudService;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

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
