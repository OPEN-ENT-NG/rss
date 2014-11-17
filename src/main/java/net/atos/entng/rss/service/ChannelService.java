package net.atos.entng.rss.service;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;

public interface ChannelService {

	public void list(UserInfos user, Handler<Either<String, JsonArray>> arrayResponseHandler);

	public void retrieve(String id, UserInfos user, Handler<Either<String, JsonObject>> notEmptyResponseHandler);

	public void deleteChannel(String idChannel, Handler<Either<String, JsonObject>> handler);

}
