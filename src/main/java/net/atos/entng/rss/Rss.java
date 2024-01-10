/*
 * Copyright © Région Nord Pas de Calais-Picardie,  Département 91, Région Aquitaine-Limousin-Poitou-Charentes, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package net.atos.entng.rss;

import io.vertx.core.DeploymentOptions;
import net.atos.entng.rss.controller.RssController;
import net.atos.entng.rss.controller.RssGlobalController;
import net.atos.entng.rss.parser.RssParser;
import net.atos.entng.rss.service.RssRepositoryEvents;

import org.entcore.common.http.BaseServer;
import org.entcore.common.http.filter.ShareAndOwner;
import org.entcore.common.mongodb.MongoDbConf;


public class Rss extends BaseServer {

	public static final String RSS_COLLECTION = "rss.channels";
	@Override
	public void start() throws Exception {
		super.start();

		// Subscribe to events published for transition
		setRepositoryEvents(new RssRepositoryEvents());

		addController(new RssController(vertx.eventBus()));
		addController(new RssGlobalController());
		MongoDbConf.getInstance().setCollection(RSS_COLLECTION);
		vertx.deployVerticle(RssParser.class.getName(), new DeploymentOptions()
				.setWorker(true).setConfig(config));
	}

}
