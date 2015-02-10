package net.atos.entng.rss;

import net.atos.entng.rss.controller.RssController;
import net.atos.entng.rss.parser.RssParser;
import net.atos.entng.rss.service.RssRepositoryEvents;

import org.entcore.common.http.BaseServer;
import org.entcore.common.http.filter.ShareAndOwner;
import org.entcore.common.mongodb.MongoDbConf;


public class Rss extends BaseServer {

	public static final String RSS_COLLECTION = "rss.channels";

	@Override
	public void start() {
		super.start();

		// Subscribe to events published for transition
		setRepositoryEvents(new RssRepositoryEvents());

		addController(new RssController(vertx.eventBus()));
		MongoDbConf.getInstance().setCollection(RSS_COLLECTION);
		setDefaultResourceFilter(new ShareAndOwner());
		container.deployWorkerVerticle(RssParser.class.getName(), config);
	}

}
