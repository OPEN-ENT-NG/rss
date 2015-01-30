db.rss.channels.find().forEach(function(rss) {
	if(rss.feeds && rss.feeds.length > 0){
		var new_feeds = new Array();
		var new_feed = new Object();
		rss.feeds.forEach(function(feed) {
			new_feed.link = feed;
			new_feed.show = 5;
			new_feeds.push(new_feed);
		});
		db.rss.channels.update({"_id" : rss._id}, { $set : { "feeds" : new_feeds}});
	}
});