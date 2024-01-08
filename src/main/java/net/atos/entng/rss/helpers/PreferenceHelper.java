package net.atos.entng.rss.helpers;

import fr.wseduc.webutils.Either;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.atos.entng.rss.constants.Field;
import net.atos.entng.rss.model.Channel;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;

import java.util.List;

public class PreferenceHelper {
    private static final Neo4j neo = Neo4j.getInstance();
    private static final Logger log = LoggerFactory.getLogger(PreferenceHelper.class);
    private PreferenceHelper() {
        throw new IllegalStateException("Utility class");
    }
    public static Future<JsonArray> getPreferences(String userId) {
        Promise<JsonArray> promise = Promise.promise();
        String query = "MATCH (u:User {id:{userId}})-[:PREFERS]->(uac:UserAppConf)"
                + " RETURN uac.rss LIMIT 1";
        JsonObject params = new JsonObject().put(Field.USER_ID, userId);
        neo.execute(query, params, Neo4jResult.validUniqueResultHandler(result -> {
            if (result.isLeft()) {
                String message = String.format("[RSS@%s::GetPreferences] Failed to get preferences : %s",
                        PreferenceHelper.class.getSimpleName(), result.left().getValue());
                log.error(message);
                promise.fail(result.left().getValue());
                return;
            }
            promise.complete(new JsonObject(result.right().getValue().getString(Field.PREFERENCES_RSS, "{}")).getJsonArray(Field.PREFERENCES_NO_DISPLAY_ID, new JsonArray()));
        }));
        return promise.future();
    }

    public static Future<JsonArray> addPreferences(String userId, List<Channel> channelsHidden) {
        Promise<JsonArray> promise = Promise.promise();
        getPreferences(userId)
            .onSuccess(preferences -> {
                channelsHidden.forEach(channel -> preferences.add(channel.getId()));
                JsonObject noDisplay = new JsonObject().put(Field.PREFERENCES_NO_DISPLAY_ID, preferences);
                String query = "MATCH (u:User {id:{userId}}) MERGE (u)-[:PREFERS]->(uac:UserAppConf)"
                        + " ON CREATE SET uac.rss= {channels}"
                        + " ON MATCH SET uac.rss= {channels}";
                JsonObject params = new JsonObject()
                        .put(Field.USER_ID, userId)
                        .put(Field.CHANNELS, noDisplay.toString());
                neo.execute(query, params, Neo4jResult.validResultHandler(PromiseHelper.handler(promise)));
            })
            .onFailure(error -> {
                String message = String.format("[RSS@%s::AddPreferences] Failed to add preferences : %s",
                        PreferenceHelper.class.getSimpleName(), error.getMessage());
                log.error(message);
                promise.fail(error.getMessage());
            });
        return promise.future();
    }
}
