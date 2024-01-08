package net.atos.entng.rss.service;

import com.mongodb.QueryBuilder;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.service.impl.MongoDbCrudService;
import org.entcore.common.user.UserInfos;

import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultsHandler;

public class ChannelGlobalServiceMongoImpl extends MongoDbCrudService implements ChannelGlobalService {

    private final String collection;
    private final MongoDb mongo;

    public ChannelGlobalServiceMongoImpl(final String collection) {
        super(collection);
        this.collection = collection;
        this.mongo = MongoDb.getInstance();
    }

    @Override
    public void createGlobalChannel(UserInfos user, JsonObject channel, Handler<Either<String, JsonObject>> handler) {
        JsonObject now = MongoDb.now();
        channel.put("global", true);
        channel.put("modified", now);
        channel.put("created", now);
        JsonObject owner = new JsonObject()
                .put("userId", user.getUserId())
                .put("displayName", user.getUsername());
        channel.put("owner", owner);
        mongo.insert(collection, channel, validResultHandler(handler));
    }

    @Override
    public void list(UserInfos user, Handler<Either<String, JsonArray>> arrayResponseHandler) {
        // Start
        QueryBuilder query = QueryBuilder.start("global").is(true);
        mongo.find(collection, MongoQueryBuilder.build(query), null, null, validResultsHandler(arrayResponseHandler));
    }

    @Override
    public void deleteGlobalChannel(String idChannel, Handler<Either<String, JsonObject>> handler) {
        // Delete the channel
        QueryBuilder builder = QueryBuilder.start("_id").is(idChannel);
        mongo.delete(collection,  MongoQueryBuilder.build(builder), validResultHandler(handler));
    }
}
