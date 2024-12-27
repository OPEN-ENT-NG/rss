package net.atos.entng.rss.service;

import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.atos.entng.rss.constants.Field;
import net.atos.entng.rss.helpers.IModelHelper;
import net.atos.entng.rss.model.Channel;
import org.bson.conversions.Bson;
import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.service.impl.MongoDbCrudService;
import org.entcore.common.user.UserInfos;

import java.util.ArrayList;
import java.util.List;
import static com.mongodb.client.model.Filters.*;

public class ChannelGlobalServiceMongoImpl extends MongoDbCrudService implements ChannelGlobalService {

    private final String collection;
    private final MongoDb mongo;
    private static final Logger log = LoggerFactory.getLogger(ChannelGlobalServiceMongoImpl.class);


    public ChannelGlobalServiceMongoImpl(final String collection) {
        super(collection);
        this.collection = collection;
        this.mongo = MongoDb.getInstance();
    }

    @Override
    public Future<Channel> createGlobalChannel(UserInfos user, JsonObject feed) {
        Promise<Channel> promise = Promise.promise();
        JsonObject now = MongoDb.now();
        feed.put(Field.GLOBAL, true);
        feed.put(Field.MODIFIED, now);
        feed.put(Field.CREATED, now);
        JsonObject owner = new JsonObject()
                .put(Field.USER_ID, user.getUserId())
                .put(Field.DISPLAY_NAME, user.getUsername());
        feed.put(Field.OWNER, owner);
        mongo.insert(collection, feed, MongoDbResult.validResultHandler(IModelHelper.uniqueResultToIModel(promise, Channel.class)));
        return promise.future();
    }

    @Override
    public Future<List<Channel>> list() {
        Promise<List<Channel>> promise = Promise.promise();
        Bson query = eq(Field.GLOBAL, true);
        mongo.find(collection, MongoQueryBuilder.build(query), null, null, MongoDbResult.validResultsHandler(results -> {
            if (results.isLeft()) {
                log.error("[RSS@ChannelGlobalServiceMongoImpl::list] Can't find global channels : ", results.left().getValue());
                promise.fail(results.left().getValue());
                return;
            }
            List<Channel> globalsChannels = new ArrayList<>(IModelHelper.toList(results.right().getValue(), Channel.class));
            promise.complete(globalsChannels);
        }));
        return promise.future();
    }

    @Override
    public Future<Channel> deleteGlobalChannel(String idChannel) {
        Promise<Channel> promise = Promise.promise();
        Bson builder = eq(Field.MONGO_ID, idChannel);
        mongo.delete(collection,  MongoQueryBuilder.build(builder), MongoDbResult.validResultHandler(IModelHelper.uniqueResultToIModel(promise, Channel.class)));
        return promise.future();
    }
}
