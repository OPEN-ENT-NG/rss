package net.atos.entng.rss.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.user.UserInfos;

public interface ChannelGlobalService {
    public void createGlobalChannel(UserInfos user, JsonObject channel, Handler<Either<String, JsonObject>> handler);
    public void list(Handler<Either<String, JsonArray>> arrayResponseHandler);
    public void deleteGlobalChannel(String idChannel, Handler<Either<String, JsonObject>> handler);
}
