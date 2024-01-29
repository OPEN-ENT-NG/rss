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

package net.atos.entng.rss.service;

import io.vertx.core.Future;
import net.atos.entng.rss.model.Channel;
import net.atos.entng.rss.model.ChannelFeed;
import org.entcore.common.user.UserInfos;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface ChannelService {
	/**
	 * create channel
	 *  @param user         User session token
	 *  @param feed       Feed for the channel
	 */
	public Future<Channel> create(UserInfos user, JsonObject feed);

	/**
	 * list all channels
	 * @param user			User session token
	 * @return 				List all user channels
	 */
	public Future<List<Channel>> list(UserInfos user);

	/**
	 * retrieve one channel
	 * @param id			Channel id
	 * @param user			User session token
	 * @return 				The channel asked
	 */
	public Future<Channel> retrieve(String id, UserInfos user);

	/**
	 * delete a channel
	 * @param userId		User session token
	 * @param idChannel		Channel id
	 */
	public Future<Channel> deleteChannel(String userId, String idChannel);

	/**
	 * update a channel
	 * @param userId		User session token
	 * @param id			Channel id
	 * @param feeds			Feeds for the channel
	 */
	public Future<Channel> update(String userId, String id, List<ChannelFeed> feeds);
}
