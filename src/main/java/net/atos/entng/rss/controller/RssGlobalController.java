package net.atos.entng.rss.controller;

import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import net.atos.entng.rss.Rss;
import net.atos.entng.rss.constants.Field;
import net.atos.entng.rss.helpers.IModelHelper;
import net.atos.entng.rss.service.*;
import org.entcore.common.events.EventHelper;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.http.filter.SuperAdminFilter;
import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.user.UserUtils;

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
        RequestUtils.bodyToJson(request, feeds -> UserUtils.getAuthenticatedUserInfos(eb, request)
            .compose(userInfos -> channelGlobalService.createGlobalChannel(userInfos, feeds))
            .onSuccess(result -> ok(request))
            .onFailure(error -> {
                String message = String.format("[RSS@%s::CreateGlobalsChannels] Failed to create global channel : %s",
                        this.getClass().getSimpleName(), error.getMessage());
                log.error(message);
                renderError(request);
            }));
    }

    @Get("/channels/globals")
    @ResourceFilter(SuperAdminFilter.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void getGlobalChannels(HttpServerRequest request) {
        UserUtils.getAuthenticatedUserInfos(eb, request)
            .compose(userInfos -> channelGlobalService.list())
            .onSuccess(channels -> renderJson(request, IModelHelper.toJsonArray(channels)))
            .onFailure(error -> {
                String message = String.format("[RSS@%s::GetGlobalsChannels] Failed to get globals channels : %s",
                        this.getClass().getSimpleName(), error.getMessage());
                log.error(message);
                renderError(request);
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
        UserUtils.getAuthenticatedUserInfos(eb, request)
            .compose(userInfos -> channelGlobalService.deleteGlobalChannel(request.params().get(Field.ID)))
            .onSuccess(result -> ok(request))
            .onFailure(error -> {
                String message = String.format("[RSS@%s::DeleteGlobalChannel] Failed to delete global channel : %s",
                        this.getClass().getSimpleName(), error.getMessage());
                log.error(message);
                renderError(request);
            });
    }
}
