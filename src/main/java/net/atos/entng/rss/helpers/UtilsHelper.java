package net.atos.entng.rss.helpers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.constants.Field;

public class UtilsHelper {
    public static JsonArray mergeArray(JsonArray channels, JsonArray globalArray, JsonArray pref) {
        JsonArray mergedArray = new JsonArray();
        for (int i = 0; i < channels.size(); i++) {
            JsonObject channel = channels.getJsonObject(i);
            String id = channel.getString(Field.ID);
            if (!pref.contains(id)) {
                mergedArray.add(channel);
            }
        }
        for (int i = 0; i < globalArray.size(); i++) {
            JsonObject channel = globalArray.getJsonObject(i);
            String id = channel.getString(Field.ID);
            if (!pref.contains(id)) {
                mergedArray.add(channel);
            }
        }
        return mergedArray;
    }

    public static void removeGlobalFeeds(JsonArray feeds, JsonArray globalArray) {
        for (int i = 0; i < globalArray.size(); i++) {
            JsonObject channel = globalArray.getJsonObject(i);
            JsonArray globalFeeds = channel.getJsonArray(Field.FEEDS);
            for (int j = 0; j < globalFeeds.size(); j++) {
                JsonObject globalFeed = globalFeeds.getJsonObject(j);
                String link = globalFeed.getString(Field.LINK);
                String title = globalFeed.getString(Field.TITLE);
                Integer show = globalFeed.getInteger(Field.SHOW);
                for (int k = 0; k < feeds.size(); k++) {
                    JsonObject feed = feeds.getJsonObject(k);
                    if (link.equals(feed.getString(Field.LINK)) && title.equals(feed.getString(Field.TITLE)) && show.equals(feed.getInteger(Field.SHOW))) {
                        feeds.remove(k);
                        globalArray.remove(i);
                        removeGlobalFeeds(feeds, globalArray);
                        return;
                    }
                }
            }
        }
    }
}
