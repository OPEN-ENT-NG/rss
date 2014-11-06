var rssWidget = model.widgets.findWidget('rss');
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

rssWidget.loadFeeds = function(){
	http().get('/rss/channels').done(function(channels){
		rssWidget.channels = channels;
		rssWidget.feeds = [];
		if(channels.length > 0){
			rssWidget.channels[0].feeds.forEach(function(url){
				if(url !== null && url !== ""){
					http().get('/rss/feed/items?url=' + encodeURIComponent(url)).done(function(feed){
						if(feed.status === 200){
							rssWidget.feeds.push(feed);
						}
					});
				}
			});
			model.widgets.apply();
		}
	});
};

rssWidget.loadFeeds();

rssWidget.createChannel = function(callback){
	http().postJson('/rss/channel', rssWidget.selectedChannel).done(function(response){
		rssWidget.channels[0] = rssWidget.selectedChannel;
		model.widgets.apply();
		if(typeof callback === 'function'){
			callback();
		}
	}.bind(this));
};

rssWidget.newChannel = function(){
	rssWidget.selectedChannel = new Channel();
	rssWidget.selectedChannel.feeds = new Array(rssWidget.totalFeeds);
	rssWidget.display.edition = true;
};

rssWidget.editChannel = function(){
	rssWidget.selectedChannel = rssWidget.channels[0];
	rssWidget.display.edition = true;
};

rssWidget.closeEdition = function(){
	rssWidget.selectedChannel = undefined;
	rssWidget.display.edition = false;
};

rssWidget.saveChannel = function(callback){
	if(this._id){
		rssWidget.saveModifications(callback);
	}
	else{
		rssWidget.createChannel(callback);
	}
	rssWidget.closeEdition();
	rssWidget.loadFeeds();
};

rssWidget.saveModifications= function(callback){
	http().putJson('/rss/channel/' + rssWidget.selectedChannel._id, rssWidget.selectedChannel).done(function(e){
		rssWidget.channels[0] = rssWidget.selectedChannel;
		if(typeof callback === 'function'){
			callback();
		}
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
