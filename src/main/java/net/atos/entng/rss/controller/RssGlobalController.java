package net.atos.entng.rss.controller;

import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.Rss;
import net.atos.entng.rss.service.*;
import org.entcore.common.events.EventHelper;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.http.filter.SuperAdminFilter;
import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class RssGlobalController extends MongoDbControllerHelper {
    private final ChannelGlobalService channelGlobalService;

    private final EventHelper eventHelper;

    public RssGlobalController() {
        super(Rss.RSS_COLLECTION);
        this.channelGlobalService = new ChannelGlobalServiceMongoImpl(Rss.RSS_COLLECTION);
        final EventStore eventStore = EventStoreFactory.getFactory().getEventStore(Rss.class.getSimpleName());
        this.eventHelper = new EventHelper(eventStore);
    }

    @Post("/channels/globals")
    @ResourceFilter(SuperAdminFilter.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void createGlobalChannel(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                RequestUtils.bodyToJson(request, channel -> channelGlobalService.createGlobalChannel(user, channel, defaultResponseHandler(request)));
            } else {
                unauthorized(request);
            }
        });

    }

    @Get("/channels/globals")
    @ResourceFilter(SuperAdminFilter.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void getGlobalChannels(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                channelGlobalService.list(arrayResponseHandler(request));
            } else {
                unauthorized(request);
            }
        });
    }

    @Put("/channels/globals/:id")
    @ResourceFilter(SuperAdminFilter.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void updateGlobalChannel(HttpServerRequest request) {
        super.update(request);
    }

    @Delete("/channels/globals/:id")
    @ResourceFilter(SuperAdminFilter.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void deleteGlobalChannel(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                final String id = request.params().get("id");
                channelGlobalService.deleteGlobalChannel(id, defaultResponseHandler(request));
            } else {
                unauthorized(request);
            }
        });
    }

}
