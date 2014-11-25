package net.atos.entng.rss.model;

import org.vertx.java.core.json.JsonObject;

public class RssParserCacheObject {
    private long lastUpdate;
    private JsonObject value;

    public RssParserCacheObject(JsonObject value) {
        this.value = value;
        this.lastUpdate = System.currentTimeMillis();
    }

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public JsonObject getValue() {
		return value;
	}

	public void setValue(JsonObject value) {
		this.value = value;
	}
}
