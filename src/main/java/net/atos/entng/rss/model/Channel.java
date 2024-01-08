package net.atos.entng.rss.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.constants.Field;
import net.atos.entng.rss.helpers.IModelHelper;

import java.util.List;

public class Channel implements IModel<Channel> {
    private String _id;
    private JsonObject created;
    private JsonObject modified;
    private List<ChannelFeed> feeds;
    private JsonObject owner;
    private boolean global;
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
    public Channel setId(String id) {
        this._id = id;
        return this;
    }
    public List<ChannelFeed> getFeeds() {
        return feeds;
    }
    public Channel setFeeds(List<ChannelFeed> feeds) {
        this.feeds = feeds;
        return this;
    }
    public JsonObject getCreated() {
        return created;
    }
    public Channel setCreated(JsonObject created) {
        this.created = created;
        return this;
    }
    public JsonObject getModified() {
        return modified;
    }
    public Channel setModifiedDate(JsonObject modified) {
        this.modified = modified;
        return this;
    }
    public boolean getGlobal() {
        return global;
    }
    public Channel setGlobal(boolean global) {
        this.global = global;
        return this;
    }
    public JsonObject getOwner() {
        return owner;
    }
    public Channel setOwner(JsonObject owner) {
        this.owner = owner;
        return this;
    }
    public void addFeeds(List<ChannelFeed> feeds) {
        this.feeds.addAll(feeds);
    }
    @Override
    public JsonObject toJson() {
        return IModelHelper.toJson(this, false, false);
    }
}
