package fr.wseduc.rss.controller;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.rss.service.ChannelService;
import fr.wseduc.rss.service.ChannelServiceMongoImpl;
import fr.wseduc.rss.service.FeedService;
import fr.wseduc.rss.service.FeedServiceImpl;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;

public class RssController extends MongoDbControllerHelper {

	private final ChannelService channelService;
	private final FeedService feedService;

	public RssController(final String collection, EventBus eb) {
		super(collection);
		this.channelService = new ChannelServiceMongoImpl(collection);
		this.feedService = new FeedServiceImpl(eb);
	}

	@Get("")
	@SecuredAction("rss.view")
	public void view(HttpServerRequest request) {
		renderView(request);
	}

	@Get("/channels")
	@SecuredAction("rss.read")
	public void getchannels(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				channelService.list(user, arrayResponseHandler(request));
			}
		});
	}

	@Post("/channel")
	@SecuredAction("rss.manager")
	public void createchannel(HttpServerRequest request) {
		super.create(request);
	}

	@Get("/channel/:id")
	@SecuredAction(value = "channel.read", type = ActionType.RESOURCE)
	public void getchannel(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				String id = request.params().get("id");
				channelService.retrieve(id, user, notEmptyResponseHandler(request));
			}
		});
	}

	@Put("/channel/:id")
	@SecuredAction(value = "channel.manager", type = ActionType.RESOURCE)
	public void updatechannel(HttpServerRequest request) {
		super.update(request);
	}

	@Delete("/channel/:id")
	@SecuredAction(value = "channel.manager", type = ActionType.RESOURCE)
	public void deletechannel(HttpServerRequest request) {
		final String id = request.params().get("id");
		channelService.deleteChannel(id, defaultResponseHandler(request));
	}

	/* feeds */

	@Get("/feed/items")
	@SecuredAction("rss.read")
	public void getfeedItems(final HttpServerRequest request) {
		final String url = request.params().get("url");
		feedService.getItems(request, url, defaultResponseHandler(request));
	}

}