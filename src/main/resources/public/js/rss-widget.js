var rssWidget = model.widgets.findWidget('rss');
rssWidget.channel = undefined;
rssWidget.feeds = undefined;
rssWidget.selectedChannel = undefined;
rssWidget.totalFeeds = 10; // limit of feeds
rssWidget.defaultShow = 5; // limit of article by feeds
rssWidget.showValues = [1,2,3,4,5,6,7,8,9,10];
rssWidget.display = {
	edition: false,
	addFeed: false,
	selectedFeed: undefined,
	selectedItem: undefined
};

function Channel(){}
model.makeModel(Channel);
model.makePermanent(Channel, { fromApplication: 'rss' });

rssWidget.updateFeeds = function(force){
	rssWidget.feeds = [];
	model.widgets.apply();
	rssWidget.channel.feeds.forEach(function(feed){
		if(feed.link !== null && feed.link !== ""){
			http().get('/rss/feed/items?url=' + encodeURIComponent(feed.link) + '&force=' + force).done(function(result){
				if(result !== undefined && result.status === 200 && rssWidget.feeds.length < rssWidget.totalFeeds){
					if(result.Items !== undefined && feed.show != undefined && result.Items.length > feed.show){
						result.Items = result.Items.slice(0, feed.show);
					}
					rssWidget.feeds.push(result);
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
		rssWidget.channel = angular.copy(rssWidget.selectedChannel);
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
	rssWidget.selectedChannel.feeds = new Array();
	rssWidget.addFeed();
	rssWidget.display.addFeed = true;
	rssWidget.display.edition = true;
};

rssWidget.editChannel = function(){
	rssWidget.selectedChannel = angular.copy(rssWidget.channel);
	//(new feed) hide/show the add button
	if(rssWidget.selectedChannel.feeds.length < rssWidget.totalFeeds){
		if(rssWidget.selectedChannel.feeds.length === 0){
			rssWidget.addFeed();
		}
		rssWidget.display.addFeed = true;
	}else{
		rssWidget.display.addFeed = false;
	}
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

rssWidget.addFeed = function(){
	var feed = new Object();
	feed.link = "";
	feed.show = rssWidget.defaultShow;
	rssWidget.selectedChannel.feeds.unshift(feed);
	if(rssWidget.selectedChannel.feeds.length === rssWidget.totalFeeds){
		rssWidget.display.addFeed = false;
	}
};

rssWidget.removeFeed = function(index){
	rssWidget.selectedChannel.feeds.splice(index, 1);
	if(rssWidget.selectedChannel.feeds.length < rssWidget.totalFeeds){
		rssWidget.display.addFeed = true;
	}
	if(rssWidget.selectedChannel.feeds.length === 0){
		rssWidget.addFeed();
	}
}

rssWidget.closeEdition = function(){
	rssWidget.selectedChannel = undefined;
	rssWidget.display.edition = false;
};

rssWidget.saveChannel = function(callback){
	//remove the blank fields
	var feeds = angular.copy(rssWidget.selectedChannel.feeds);
	rssWidget.selectedChannel.feeds = new Array();
	feeds.forEach(function(feed){
		if(feed.link !== null && feed.link.trim() !== ""){
			rssWidget.selectedChannel.feeds.push(feed);
		}
	});
	if(rssWidget.selectedChannel._id){
		rssWidget.saveModifications(callback);
	}
	else{
		rssWidget.createChannel(callback);
	}
};

rssWidget.saveModifications= function(callback){
	http().putJson('/rss/channel/' + rssWidget.selectedChannel._id, {title:  rssWidget.selectedChannel.title
			, content: rssWidget.selectedChannel.content, feeds: rssWidget.selectedChannel.feeds}).done(function(e){
		rssWidget.channel = angular.copy(rssWidget.selectedChannel);
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

/* Util */

rssWidget.formatDate = function(date){
	var momentDate;
	if (typeof date === "number"){
		momentDate = moment.unix(date);
	} else {
		momentDate = moment(date);
	}
	return moment(momentDate, "YYYY-MM-DDTHH:mm:ss.SSSZ").lang('fr').format('dddd DD MMMM YYYY HH:mm');
};