package net.atos.entng.rss.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.atos.entng.rss.model.RssParserCacheObject;
import net.atos.entng.rss.service.FeedServiceImpl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import fr.wseduc.webutils.Either;

public class RssParserCache {
	private final long timeToClean;
    private final HashMap<String, RssParserCacheObject> cacheMap;
    private static final long MIN_TO_MILLIS = 60000;
    private static final Logger log = LoggerFactory.getLogger(FeedServiceImpl.class);

    public RssParserCache(final long timeToClean) {
        this.timeToClean = timeToClean;
        this.cacheMap = new HashMap<String, RssParserCacheObject>();
        if (timeToClean > 0) {
            Thread t = new Thread(
            	new Runnable() {
	                @Override
					public void run() {
	                    while (true) {
	                        try {
	                            Thread.sleep(timeToClean * MIN_TO_MILLIS);
	                            cleanUp();
	                        } catch (InterruptedException ie) {
	                        	log.error("[RssParserCache][RssParserCache] InterruptedException " + ie.getMessage());
	                        }
	                    }
	                }
            	}
            );
            t.start();
        }
    }

    public boolean has(String key){
    	 synchronized (cacheMap) {
             return cacheMap.containsKey(key);
         }
    }

    public void put(String key, JsonObject value) {
        synchronized (cacheMap) {
        	RssParserCacheObject rpco = new RssParserCacheObject(value);
            cacheMap.put(key, rpco);
        }
    }

    public void get(String key, Handler<Either<String, JsonObject>> handler) {
        synchronized (cacheMap) {
        	long now = System.currentTimeMillis();
        	RssParserCacheObject rpco = cacheMap.get(key);
            if (rpco != null){
            	// Update last access
            	rpco.setLastUpdate(now);
                handler.handle(new Either.Right<String, JsonObject>(rpco.getValue()));
            }
            else{
            	handler.handle(new Either.Left<String, JsonObject>("[RssParserCache][get] Could not extract the value of this key : " + key));
            }
        }
    }

    public void remove(String key) {
        synchronized (cacheMap) {
            cacheMap.remove(key);
        }
    }

    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }

    public void cleanUp() {
        long now = System.currentTimeMillis();
        List<String> keysToDelete = new ArrayList<String>();
        synchronized (cacheMap) {
            Iterator<?> itr = cacheMap.entrySet().iterator();
            Map.Entry<String, RssParserCacheObject> entry = null;
            String key = null;
            RssParserCacheObject rpco = null;
            while (itr.hasNext()) {
            	entry = (Entry<String, RssParserCacheObject>) itr.next();
            	key = entry.getKey();
            	rpco = entry.getValue();
                if (rpco != null && (now > ((timeToClean * MIN_TO_MILLIS) + rpco.getLastUpdate()))) {
                	keysToDelete.add(key);
                }
            }
        }
        for (String key : keysToDelete) {
            synchronized (cacheMap) {
                cacheMap.remove(key);
            }
            Thread.yield();
        }
    }
}
