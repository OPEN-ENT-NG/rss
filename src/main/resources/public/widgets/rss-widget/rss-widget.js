var rssWidget = model.widgets.findWidget('rss-widget');
rssWidget.channel = undefined;
rssWidget.feeds = undefined;
rssWidget.selectedFeed = undefined;
rssWidget.selectedFeedIndex = undefined;
rssWidget.totalFeeds = 10; // limit of feeds
rssWidget.defaultShow = 5; // limit of article by feeds
rssWidget.showValues = [1,2,3,4,5,6,7,8,9,10];
rssWidget.display = {
	edition: false,
	addFeed: false,
	feedEdition: false
};

function Channel(){
	this.feeds = new Array();
};

function Feed(){
	this.title;
	this.link;
	this.show = rssWidget.defaultShow;
};

model.makeModels(Channel);

rssWidget.loadFeeds = function(force){
	rssWidget.feeds = [];
	model.widgets.apply();
	rssWidget.channel.feeds.forEach(function(feed){
		var mytitle = feed.title;
		if(feed.link !== null && feed.link !== ""){
			http().get('/rss/feed/items?url=' + encodeURIComponent(feed.link) + '&force=' + force).done(function(result){
				if (!result.title)result.title = mytitle;
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
			if(channels && channels.length > 0){
				rssWidget.channel = channels[0];
			}else{
				rssWidget.channel = new Channel();
			}
			model.widgets.apply();
			rssWidget.loadFeeds(0); // 0 : default, from the cache
		});
	}
	else{
		rssWidget.loadFeeds(0); // 0 : default, from the cache
	}
};

rssWidget.createChannel = function(){
	if(rssWidget.channel){
		http().postJson('/rss/channel', rssWidget.channel).done(function(response){
			rssWidget.channel._id = response._id;
			model.widgets.apply();
			rssWidget.loadFeeds(0); // 0 : default, from the cache
		}.bind(this));
	}else{
		console.log("createChannel : channel is undefined");
	}
};

rssWidget.editChannel= function(){
	if(rssWidget.channel && rssWidget.channel._id){
		http().putJson('/rss/channel/' + rssWidget.channel._id, {feeds: rssWidget.channel.feeds}).done(function(response){
			model.widgets.apply();
			rssWidget.loadFeeds(0); // 0 : default, from the cache
		});
	}else{
		console.log("editChannel : channel is undefined");
	}
};

rssWidget.saveChannel = function(){
	if(rssWidget.channel._id){
		rssWidget.editChannel();
	}
	else{
		rssWidget.createChannel();
	}
};

rssWidget.openConfig = function(){
	if(rssWidget.channel.feeds.length < rssWidget.totalFeeds){
		rssWidget.display.addFeed = true;
	}else{
		rssWidget.display.addFeed = false;
	}
	rssWidget.display.edition = true;
};

rssWidget.closeConfig = function(){
	rssWidget.display.edition = false;
};

rssWidget.saveFeed = function(){
	if(rssWidget.selectedFeedIndex && rssWidget.validFeed(rssWidget.selectedFeed)){
		if(rssWidget.selectedFeedIndex >= 0 && rssWidget.selectedFeedIndex < rssWidget.totalFeeds){
			rssWidget.channel.feeds[rssWidget.selectedFeedIndex] = rssWidget.selectedFeed;
		}else{
			rssWidget.channel.feeds.push(rssWidget.selectedFeed);
		}
		rssWidget.saveChannel();
	}
	rssWidget.closeFeedEdition();
};

rssWidget.removeFeed = function(index){
	if(index && index >= 0 && index < rssWidget.totalFeeds){
		rssWidget.channel.feeds.splice(index, 1);
		if(rssWidget.channel.feeds.length < rssWidget.totalFeeds){
			rssWidget.display.addFeed = true;
		}
		rssWidget.saveChannel();
	}
};

rssWidget.openFeedEdition = function(index){
	if(index && index >= 0 && index < rssWidget.totalFeeds){
		rssWidget.selectedFeed = angular.copy(rssWidget.channel.feeds[index]);
		rssWidget.selectedFeedIndex = index;
	}else{
		rssWidget.selectedFeed = new Feed();
		rssWidget.selectedFeedIndex = -1;
	}
	rssWidget.display.feedEdition = true;
};

rssWidget.closeFeedEdition = function(){
	if(rssWidget.channel.feeds.length === rssWidget.totalFeeds){
		rssWidget.display.addFeed = false;
	}
	rssWidget.display.feedEdition = false;
	rssWidget.selectedFeed = undefined;
	rssWidget.selectedFeedIndex = undefined;
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

// init channel & feeds
rssWidget.initFeeds();

/* Util */

rssWidget.formatDate = function(date){
	var momentDate;
	if (typeof date === "number"){
		momentDate = moment.unix(date);
	} else {
		momentDate = moment(date, undefined, 'en');
	}
	return momentDate.lang('fr').format('dddd DD MMMM YYYY HH:mm');
};

rssWidget.validFeed = function(feed){
	if(feed && feed.title && feed.title.trim() !== "" && feed.link && feed.link.trim() !== ""){
		return true;
	}
	return false;
};
