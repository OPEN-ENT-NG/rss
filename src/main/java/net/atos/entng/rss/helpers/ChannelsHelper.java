package net.atos.entng.rss.helpers;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.atos.entng.rss.model.Channel;
import net.atos.entng.rss.model.ChannelFeed;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChannelsHelper {
    private static final Logger log = LoggerFactory.getLogger(ChannelsHelper.class);
    private ChannelsHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static Future<List<Channel>> filterPreferences(String userId, List<Channel> usersChannels, List<Channel> globalsChannels) {
        Promise<List<Channel>> promise = Promise.promise();
        PreferenceHelper.getPreferences(userId)
            .onSuccess(preferences -> {
                // merge preferences to show only the channels that are not in preferences
                List<Channel> mergedArray = Stream.concat(
                    usersChannels.stream()
                            .filter(channel -> !preferences.contains(channel.getId())),
                    globalsChannels.stream()
                            .filter(channel -> !preferences.contains(channel.getId()))
                )
                .collect(Collectors.toList());
                promise.complete(mergedArray);
            })
            .onFailure(error -> {
                String message = String.format("[RSS@%s::filterPreferences] Failed to filter preferences : %s",
                        ChannelsHelper.class.getSimpleName(), error.getMessage());
                log.error(message);
                promise.fail(error.getMessage());
            });
        return promise.future();
    }

    public static List<ChannelFeed> removeGlobalFeeds(List<ChannelFeed> feeds, List<Channel> channelsGlobals) {
        return feeds.stream().filter(feed -> channelsGlobals.stream()
            .flatMap(channel -> channel.getFeeds().stream())
            .anyMatch(globalFeed ->
                globalFeed.getShow().equals(feed.getShow()) &&
                globalFeed.getLink().equals(feed.getLink()) &&
                globalFeed.getTitle().equals(feed.getTitle()))
        ).collect(Collectors.toList());
    }

    public static List<Channel> removeGlobalChannels(List<ChannelFeed> feeds, List<Channel> channelsGlobals) {
        return channelsGlobals.stream().filter(channel -> feeds.stream()
            .anyMatch(feed -> channel.getFeeds().stream()
                .anyMatch(globalFeed ->
                    globalFeed.getShow().equals(feed.getShow()) &&
                    globalFeed.getLink().equals(feed.getLink()) &&
                    globalFeed.getTitle().equals(feed.getTitle())
                )
            )
        ).collect(Collectors.toList());
    }

    public static Future<Optional<Channel>> mergeToOneChannel(List<Channel> channels) {
        Promise<Optional<Channel>> promise = Promise.promise();
        Optional<Channel> finalChannel = channels.stream().reduce((channel1, channel2) -> {
            channel1.addFeeds(channel2.getFeeds());
            return channel1;
        });
        promise.complete(finalChannel);
        return promise.future();
    }
}
