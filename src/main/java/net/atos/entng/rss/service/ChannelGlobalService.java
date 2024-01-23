package net.atos.entng.rss.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.model.Channel;
import org.entcore.common.user.UserInfos;

import java.util.List;

public interface ChannelGlobalService {
    /**
     * create global channel
     *  @param user         User session token
     *  @param feeds        Feeds for the channel
     */
    public Future<JsonObject> createGlobalChannel(UserInfos user, JsonObject feeds);

    /**
     * list all globals channels
     * @return Future of all globals channels
     */
    public Future<List<Channel>> list();

    /**
     * delete a global channel
     * @param idChannel     global channel id
     */
    public Future<JsonObject> deleteGlobalChannel(String idChannel);
}
