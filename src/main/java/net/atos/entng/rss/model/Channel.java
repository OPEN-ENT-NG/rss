package net.atos.entng.rss.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.constants.Field;
import net.atos.entng.rss.helpers.IModelHelper;

import java.util.ArrayList;
import java.util.List;

public class Channel implements IModel<Channel> {
    private String _id;
    private JsonObject created;
    private JsonObject modified;
    private List<ChannelFeed> feeds;
    private JsonObject owner;
    private boolean global;
    public Channel () {
    }
    public Channel(JsonObject channel) {
        this._id = channel.getString(Field.MONGO_ID, null);
        this.created = channel.getJsonObject(Field.CREATED, null);
        this.modified = channel.getJsonObject(Field.MODIFIED, null);
        this.owner = channel.getJsonObject(Field.OWNER, null);
        this.global = channel.getBoolean(Field.GLOBAL, false);
        this.feeds = IModelHelper.toList(channel.getJsonArray(Field.FEEDS, new JsonArray()), ChannelFeed.class);
    }
    public String getId() {
        return _id;
    }
    public void setId(String id) {
        this._id = id;
    }
    public List<ChannelFeed> getFeeds() {
        return feeds;
    }
    public void setFeeds(List<ChannelFeed> feeds) {
        this.feeds = feeds;
    }
    public JsonObject getCreated() {
        return created;
    }
    public void setCreated(JsonObject created) {
        this.created = created;
    }
    public JsonObject getModified() {
        return modified;
    }
    public void setModifiedDate(JsonObject modified) {
        this.modified = modified;
    }
    public boolean getGlobal() {
        return global;
    }
    public void setGlobal(boolean global) {
        this.global = global;
    }
    public JsonObject getOwner() {
        return owner;
    }
    public void setOwner(JsonObject owner) {
        this.owner = owner;
    }
    public void addFeeds(List<ChannelFeed> feeds) {
        this.feeds.addAll(feeds);
    }
    @Override
    public JsonObject toJson() {
        return new JsonObject()
            .put(Field.CREATED, this.created)
            .put(Field.MODIFIED, this.modified)
            .put(Field.MONGO_ID, this._id)
            .put(Field.OWNER, this.owner)
            .put(Field.GLOBAL, this.global)
            .put(Field.FEEDS, this.feeds);
    }
}
