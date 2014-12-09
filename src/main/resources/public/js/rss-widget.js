var rssWidget = model.widgets.findWidget('rss');
rssWidget.channel = undefined;
rssWidget.feeds = undefined;
rssWidget.selectedChannel = undefined;
rssWidget.totalFeeds = 3;
rssWidget.display = {
	edition: false,
	selectedFeed: undefined,
	selectedItem: undefined
};

function Channel(){}
model.makeModel(Channel);
model.makePermanent(Channel, { fromApplication: 'rss' });

rssWidget.updateFeeds = function(force){
	rssWidget.feeds = [];
	model.widgets.apply();
	rssWidget.channel.feeds.forEach(function(url){
		if(url !== null && url !== ""){
			http().get('/rss/feed/items?url=' + encodeURIComponent(url) + '&force=' + force).done(function(feed){
				if(feed !== undefined && feed.status === 200 rssWidget.feeds.length < rssWidget.totalFeeds){
					rssWidget.feeds.push(feed);
					model.widgets.apply();
				}
			});
		}
	});
};

rssWidget.initFeeds = function(){
	if(rssWidget.channel === undefined){
		http().get('/rss/channels').done(function(channels){
			if(channels.length > 0){
				rssWidget.channel = channels[0];
				model.widgets.apply();
				rssWidget.updateFeeds(0); // 0 : default, from the cache
			}
		});
	}
	else{
		rssWidget.updateFeeds(0); // 0 : default, from the cache
	}
};

// init channel & feeds
rssWidget.initFeeds();

rssWidget.createChannel = function(callback){
	http().postJson('/rss/channel', rssWidget.selectedChannel).done(function(response){
		rssWidget.channel = rssWidget.selectedChannel;
		model.widgets.apply();
		rssWidget.updateFeeds(0); // 0 : default, from the cache
		if(typeof callback === 'function'){
			callback();
		}
		rssWidget.closeEdition();
	}.bind(this));
};

rssWidget.newChannel = function(){
	rssWidget.selectedChannel = new Channel();
	rssWidget.selectedChannel.feeds = new Array(rssWidget.totalFeeds);
	rssWidget.display.edition = true;
};

rssWidget.editChannel = function(){
	rssWidget.selectedChannel = rssWidget.channel;
	rssWidget.display.edition = true;
};

rssWidget.configChannel = function(){
	if(rssWidget.channel){
		rssWidget.editChannel();
	}
	else{
		rssWidget.newChannel();
	}
};

rssWidget.closeEdition = function(){
	rssWidget.selectedChannel = undefined;
	rssWidget.display.edition = false;
};

rssWidget.saveChannel = function(callback){
	if(rssWidget.selectedChannel._id){
		rssWidget.saveModifications(callback);
	}
	else{
		rssWidget.createChannel(callback);
	}
};

rssWidget.saveModifications= function(callback){
	http().putJson('/rss/channel/' + rssWidget.selectedChannel._id, {title:  rssWidget.channel.title
			, content: rssWidget.channel.content, feeds: rssWidget.channel.feeds}).done(function(e){
		rssWidget.channel = rssWidget.selectedChannel;
		model.widgets.apply();
		rssWidget.updateFeeds(0); // 0 : default, from the cache
		if(typeof callback === 'function'){
			callback();
		}
		rssWidget.closeEdition();
	});
};


rssWidget.showOrHideFeed = function(index){
	if(rssWidget.display.selectedFeed === index){
		rssWidget.display.selectedFeed = undefined;
	}
	else{
		if(rssWidget.display.selectedFeed !== index){
			rssWidget.display.selectedFeed = index;
		}
	}
};

rssWidget.showOrHideItem = function(index){
	if(rssWidget.display.selectedItem === index){
		rssWidget.display.selectedItem = undefined;
	}
	else{
		if(rssWidget.display.selectedItem !== index){
			rssWidget.display.selectedItem = index;
		}
	}
};
