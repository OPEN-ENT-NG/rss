package fr.wseduc.rss;

import org.entcore.common.http.BaseServer;
import org.entcore.common.http.filter.ShareAndOwner;
import org.entcore.common.mongodb.MongoDbConf;

import fr.webeduc.rss.parser.RssParser;
import fr.wseduc.rss.controller.RssController;

public class Rss extends BaseServer {

	public static final String RSS_COLLECTION = "rss.channels";

	@Override
	public void start() {
		super.start();
		addController(new RssController(RSS_COLLECTION, vertx.eventBus()));
		MongoDbConf.getInstance().setCollection(RSS_COLLECTION);
		setDefaultResourceFilter(new ShareAndOwner());
		container.deployWorkerVerticle(RssParser.class.getName());
	}

}
