package net.atos.entng.rss.helpers;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.constants.Field;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;

public class PreferenceHelper {
    private static final Neo4j neo = Neo4j.getInstance();

    public static void getPreferences(String userId, Handler<Either<String, JsonArray>> handler) {
        String query = String.format("MATCH (u:User {id:\"%s\"})-[:PREFERS]->(uac:UserAppConf)"
                + " RETURN uac.rss LIMIT 1", userId);

        neo.execute(query, new JsonObject().put(Field.USER_ID, userId), Neo4jResult.validUniqueResultHandler(result -> {
            if (result.isLeft()) {
                handler.handle(new Either.Left<>(result.left().getValue()));
                return;
            }
            JsonArray pref;
            String rss = result.right().getValue().getString(Field.PREFERENCES_RSS);
            if (rss == null) {
                pref = new JsonArray();
            } else {
                JsonObject obj = new JsonObject(rss);
                pref = obj.getJsonArray(Field.PREFERENCES_NO_DISPLAY_ID);
            }
            handler.handle(new Either.Right<>(pref));
        }));
    }

    public static void addPreferences(String userId, JsonArray globalArray, Handler<Either<String, JsonArray>> handler) {
        getPreferences(userId, resultPreferences -> {
            if (resultPreferences.isLeft()) {
                handler.handle(new Either.Left<>(resultPreferences.left().getValue()));
                return;
            }
            JsonArray ids = resultPreferences.right().getValue();
            // convert globalArray to idArray
            for (int i = 0; i < globalArray.size(); i++) {
                JsonObject global = globalArray.getJsonObject(i);
                ids.add(global.getString(Field.ID));
            }
            String idString = ids.toString().replace("\"", "\\\"");
            String query = String.format("MATCH (u:User {id:\"%s\"}) MERGE (u)-[:PREFERS]->(uac:UserAppConf)"
                    + " ON CREATE SET uac.rss= \"{\\\"no-display-rss-id\\\": %s}\""
                    + " ON MATCH SET uac.rss= \"{\\\"no-display-rss-id\\\": %s}\"", userId, idString, idString);
            neo.execute(query, new JsonObject(), Neo4jResult.validResultHandler(handler));
        });
    }
}
