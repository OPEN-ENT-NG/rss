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

package net.atos.entng.rss.parser;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import net.atos.entng.rss.service.FeedServiceImpl;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.wseduc.webutils.Either;

public class RssParser extends AbstractVerticle implements Handler<Message<JsonObject>> {

	private RssParserCache rssParserCache;
	private static final Logger log = LoggerFactory.getLogger(FeedServiceImpl.class);
	public static final long DEFAULT_CLEAN_TIMEOUT = 30; // Minutes
	public static final String PARSER_ADDRESS = "rss.parser";
	public static final String ACTION_CLEANUP = "cleanUp";
	public static final String ACTION_GET = "get";
	private HttpClient httpClient;

	@Override
	public void start() throws Exception {
		super.start();
		final long cleanTimeout = config().getLong("clean-timeout", DEFAULT_CLEAN_TIMEOUT) * 60000;
		vertx.eventBus().localConsumer("rss.parser", this);
		rssParserCache = new RssParserCache();
		httpClient = vertx.createHttpClient(new HttpClientOptions()
						.setKeepAlive(false).setConnectTimeout(5000));
		vertx.setPeriodic(cleanTimeout, new Handler<Long>() {
			@Override
			public void handle(Long cleanTimeout) {
				JsonObject message = new JsonObject();
				message.put("action", RssParser.ACTION_CLEANUP);
				message.put("cleanTimeout", cleanTimeout);
				vertx.eventBus().send(RssParser.PARSER_ADDRESS, message, new Handler<AsyncResult<Message<JsonObject>>>() {
					@Override
					public void handle(AsyncResult<Message<JsonObject>> ar) {
						if (ar.succeeded()) {
							if (log.isDebugEnabled()) {
								log.debug("Received reply: " + ar.result().body());
							}
						} else {
							log.error("Error Receive reply.", ar.cause());
						}
					}
				});
			}
		});
	}

	@Override
	public void handle(final Message<JsonObject> message) {
		JsonObject msg = message.body();
		if(msg != null){
			String action = msg.getString("action", "");
			switch(action){
			case ACTION_CLEANUP:
				long cleanTimeout = msg.getLong("cleanTimeout", DEFAULT_CLEAN_TIMEOUT);
				rssParserCache.cleanUp(cleanTimeout);
				message.reply(new JsonObject().put("status", "ok"));
				break;
			case ACTION_GET:
				String force = msg.getString("force", "0");
				final String url = msg.getString("url", "");
				if(force.equals("0") && rssParserCache.has(url)){
					rssParserCache.get(url, new Handler<Either<String, JsonObject>>(){
						@Override
						public void handle(Either<String, JsonObject> event) {
							JsonObject results = new JsonObject();
							if(event.isRight()){
								results = event.right().getValue();
								results.put("status", 200); // OK
							}
							else {
								results.put("status", 204); // KO
								log.error("[FeedServiceImpl][getItems] Error : " + event.left().getValue());
							}
							message.reply(results);
						}
					});
				}
				else{
					final HttpClientRequest req = httpClient.getAbs(url, response -> {
						final JsonObject results = new JsonObject();
						if (response.statusCode() == 200) {
							response.bodyHandler(buffer -> {
								final String content = buffer.toString();
								if (content.isEmpty()) {
									results.put("status", 204);
									message.reply(results);
									return;
								}

								try {
									final SAXParserFactory factory = SAXParserFactory.newInstance();
									factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
									final SAXParser parser = factory.newSAXParser();
									parser.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
									factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
									factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

									final DefaultHandler handler = new RssParserHandler(new Handler<JsonObject>(){
										@Override
										public void handle(JsonObject results1) {
											rssParserCache.put(url, results1);
											message.reply(results1);
										}
									});
									parser.parse(new InputSource(new StringReader(content)), handler);
								} catch (SAXException | IOException | ParserConfigurationException se) {
									results.put("status", 204);
									message.reply(results);
								}
							});
						} else {
							results.put("status", 204);
							message.reply(results);
						}
					});
					req.setTimeout(15000l);
					req.end();
				}
			}
		}
	}

}
