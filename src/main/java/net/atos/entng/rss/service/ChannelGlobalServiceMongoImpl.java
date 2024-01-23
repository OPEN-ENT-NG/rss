package net.atos.entng.rss.service;

import com.mongodb.QueryBuilder;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.constants.Field;
import net.atos.entng.rss.helpers.IModelHelper;
import net.atos.entng.rss.helpers.PromiseHelper;
import net.atos.entng.rss.model.Channel;
import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.service.impl.MongoDbCrudService;
import org.entcore.common.user.UserInfos;

import java.util.ArrayList;
import java.util.List;

public class ChannelGlobalServiceMongoImpl extends MongoDbCrudService implements ChannelGlobalService {

    private final String collection;
    private final MongoDb mongo;

    public ChannelGlobalServiceMongoImpl(final String collection) {
        super(collection);
        this.collection = collection;
        this.mongo = MongoDb.getInstance();
    }

    @Override
    public Future<JsonObject> createGlobalChannel(UserInfos user, JsonObject feeds) {
        Promise<JsonObject> promise = Promise.promise();
        JsonObject now = MongoDb.now();
        feeds.put(Field.GLOBAL, true);
        feeds.put(Field.MODIFIED, now);
        feeds.put(Field.CREATED, now);
        JsonObject owner = new JsonObject()
                .put(Field.USER_ID, user.getUserId())
                .put(Field.DISPLAY_NAME, user.getUsername());
        feeds.put(Field.OWNER, owner);
        mongo.insert(collection, feeds, MongoDbResult.validResultHandler(PromiseHelper.handler(promise)));
        return promise.future();
    }

    @Override
    public Future<List<Channel>> list() {
        Promise<List<Channel>> promise = Promise.promise();
        QueryBuilder query = QueryBuilder.start(Field.GLOBAL).is(true);
        mongo.find(collection, MongoQueryBuilder.build(query), null, null, MongoDbResult.validResultsHandler(results -> {
            if (results.isLeft()) {
                promise.fail(results.left().getValue());
                return;
            }
            List<Channel> globalsChannels = IModelHelper.toList(results.right().getValue(), Channel.class)
                    .stream()
                    .collect(ArrayList::new, List::add, List::addAll);
            promise.complete(globalsChannels);
        }));
        return promise.future();
    }

    @Override
    public Future<JsonObject> deleteGlobalChannel(String idChannel) {
        Promise<JsonObject> promise = Promise.promise();
        QueryBuilder builder = QueryBuilder.start(Field.MONGO_ID).is(idChannel);
        mongo.delete(collection,  MongoQueryBuilder.build(builder), MongoDbResult.validResultHandler(PromiseHelper.handler(promise)));
        return promise.future();
    }
}
