package net.atos.entng.rss.model;

import org.vertx.java.core.json.JsonObject;

public class RssParserCacheObject {
    private long lastUse;
    private JsonObject value;

    public RssParserCacheObject(JsonObject value) {
        this.value = value;
        this.lastUse = System.currentTimeMillis();
    }

	public long getLastUse() {
		return lastUse;
	}

	public void setLastUse(long lastUse) {
		this.lastUse = lastUse;
	}

	public JsonObject getValue() {
		return value;
	}

	public void setValue(JsonObject value) {
		this.value = value;
	}
}
