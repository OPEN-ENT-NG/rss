var rssBehaviours = {
	resources: {
		getChannel: {
			right: 'fr-wseduc-rss-controllers-RssController|getchannel'
		},
		editChannel: {
			right: 'fr-wseduc-rss-controllers-RssController|updateChannel'
		},
		deleteChannel: {
			right: 'fr-wseduc-rss-controllers-RssController|deleteChannel'
		}
	},
	workflow: {
		view: 'fr.wseduc.rss.controllers.RssController|view',
		list: 'fr.wseduc.rss.controllers.RssController|getchannels',
		create: 'fr.wseduc.rss.controllers.RssController|createchannel',
		read: 'fr.wseduc.rss.controllers.RssController|getfeedItems'
	}
};

Behaviours.register('rss', {
	behaviours: rssBehaviours,
	resource: function(resource){
		var rightsContainer = resource;
		if(!resource.myRights){
			resource.myRights = {};
		}
		for(var behaviour in rssBehaviours.resources){
			if(model.me.hasRight(rightsContainer, rssBehaviours.resources[behaviour]) || model.me.userId === resource.owner.userId){
				if(resource.myRights[behaviour] !== undefined){
					resource.myRights[behaviour] = resource.myRights[behaviour] && rssBehaviours.resources[behaviour];
				}
				else{
					resource.myRights[behaviour] = rssBehaviours.resources[behaviour];
				}
			}
		}
		return resource;
	},
	workflow: function(){
		var workflow = { };
		var rssWorkflow = rssBehaviours.workflow;
		for(var prop in rssWorkflow){
			if(model.me.hasWorkflow(rssWorkflow[prop])){
				workflow[prop] = true;
			}
		}
		return workflow;
	},
	resourceRights: function(){
		return ['read', 'contrib', 'manager', 'comment'];
	}
});