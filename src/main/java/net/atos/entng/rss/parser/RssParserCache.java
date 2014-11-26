package net.atos.entng.rss.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.atos.entng.rss.model.RssParserCacheObject;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;

public class RssParserCache {

	private final ConcurrentHashMap<String, RssParserCacheObject> cacheMap;

	public RssParserCache() {
		this.cacheMap = new ConcurrentHashMap<String, RssParserCacheObject>();
	}

	public boolean has(String key){
		return cacheMap.containsKey(key);
	}

	public void put(String key, JsonObject value) {
		RssParserCacheObject rpco = new RssParserCacheObject(value);
		cacheMap.put(key, rpco);
	}

	public void get(String key, Handler<Either<String, JsonObject>> handler) {
		long now = System.currentTimeMillis();
		RssParserCacheObject rpco = cacheMap.get(key);
		if (rpco != null){
			// Update last access
			rpco.setLastUse(now);
			handler.handle(new Either.Right<String, JsonObject>(rpco.getValue()));
		}
		else{
			handler.handle(new Either.Left<String, JsonObject>("[RssParserCache][get] Could not extract the value of this key : " + key));
		}
	}

	public void remove(String key) {
		cacheMap.remove(key);
	}

	public int size() {
		return cacheMap.size();
	}

	public void cleanUp(long cleanTimeout) {
		long now = System.currentTimeMillis();
		List<String> keysToDelete = new ArrayList<String>();
		Iterator<?> itr = cacheMap.entrySet().iterator();
		Map.Entry<String, RssParserCacheObject> entry = null;
		RssParserCacheObject rpco = null;
		while (itr.hasNext()) {
			entry = (Entry<String, RssParserCacheObject>) itr.next();
			rpco = entry.getValue();
			if (rpco != null && (now > (cleanTimeout + rpco.getLastUse()))) {
				keysToDelete.add(entry.getKey());
			}
		}
		for (String key : keysToDelete) {
			cacheMap.remove(key);
		}
	}
}
