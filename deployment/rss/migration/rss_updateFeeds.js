db.rss.channels.find().forEach(function(rss) {
        if(rss.feeds && rss.feeds.length > 0){
                var new_feeds = new Array();
                rss.feeds.forEach(function(feed) {
                        if (feed) {
                                var new_feed;
                                if (typeof(feed) === 'object') {
                                        var link = feed.link;
                                        if (typeof(link) === 'object') {
                                                new_feed = link;
                                        } else {
                                                new_feed = feed;
                                        }
                                } else {
                                        new_feed = new Object();
                                        new_feed.link = feed;
                                        new_feed.show = 5;
                                        if (rss.title) {
                                                new_feed.title = rss.title;
                                        }
                                }
                                if (new_feed && new_feed.link) {
                                        new_feeds.push(new_feed);
                                }
                        }
                });
                db.rss.channels.update({"_id" : rss._id}, { $set : { "feeds" : new_feeds}});
        }
});
