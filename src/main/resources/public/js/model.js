function Channel(){
	//this.feeds = new Array();
}

Channel.prototype.open = function(cb){
	
};

Channel.prototype.createChannel = function(callback){
	http().postJson('/rss/channel', this).done(function(response){
		this._id = response._id;
		model.channels.sync();
		if(typeof callback === 'function'){
			callback();
		}
	}.bind(this));
};

Channel.prototype.saveModifications = function(callback){
	http().putJson('/rss/channel/' + this._id, this).done(function(e){
		notify.info('rss.channel.modification.saved');
		if(typeof callback === 'function'){
			callback();
		}
	});
};

Channel.prototype.save = function(callback){
	if(this._id){
		this.saveModifications();
	}
	else{
		this.createChannel(callback);
	}
};

Channel.prototype.toJSON = function(){
	return {
		title: this.title,
		content: this.content,
		feeds: this.feeds
	}
};

model.build = function(){
	this.makeModels([Channel]);

	this.collection(Channel, {
		sync: function(callback){
			http().get('/rss/channels').done(function(channels){
				this.load(channels);
				if(typeof callback === 'function'){
					callback();
				}
			}.bind(this));
		},
		behaviours: 'rss'
	});
};