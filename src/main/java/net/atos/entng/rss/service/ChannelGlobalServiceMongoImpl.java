package net.atos.entng.rss.service;

import com.mongodb.QueryBuilder;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.constants.Field;
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
        channel.put(Field.GLOBAL, true);
        channel.put(Field.MODIFIED, now);
        channel.put(Field.CREATED, now);
        JsonObject owner = new JsonObject()
                .put(Field.USER_ID, user.getUserId())
                .put(Field.DISPLAY_NAME, user.getUsername());
        channel.put(Field.OWNER, owner);
        mongo.insert(collection, channel, validResultHandler(handler));
    }

    @Override
    public void list(Handler<Either<String, JsonArray>> arrayResponseHandler) {
        // Start
        QueryBuilder query = QueryBuilder.start(Field.GLOBAL).is(true);
        mongo.find(collection, MongoQueryBuilder.build(query), null, null, validResultsHandler(arrayResponseHandler));
    }

    @Override
    public void deleteGlobalChannel(String idChannel, Handler<Either<String, JsonObject>> handler) {
        // Delete the channel
        QueryBuilder builder = QueryBuilder.start(Field.ID).is(idChannel);
        mongo.delete(collection,  MongoQueryBuilder.build(builder), validResultHandler(handler));
    }
}
