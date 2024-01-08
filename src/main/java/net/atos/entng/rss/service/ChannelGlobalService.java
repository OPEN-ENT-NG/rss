package net.atos.entng.rss.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.model.Channel;
import org.entcore.common.user.UserInfos;

import java.util.List;

public interface ChannelGlobalService {
    /**
     * create global channel
     *  @param user         User session token
     *  @param feed        Feed for the channel
     */
    public Future<Channel> createGlobalChannel(UserInfos user, JsonObject feed);

    /**
     * list all globals channels
     * @return Future of all globals channels
     */
    public Future<List<Channel>> list();

    /**
     * delete a global channel
     * @param idChannel     global channel id
     */
    public Future<Channel> deleteGlobalChannel(String idChannel);
}
