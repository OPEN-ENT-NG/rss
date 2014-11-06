function RssController($scope, template, model){
	
	$scope.template = template;
	$scope.display = {edition : false};
	$scope.me = model.me;
	$scope.channels = model.channels;
	$scope.totalFeeds = 3;
	template.open('main', 'channels');
    
    $scope.newChannel = function(){
		$scope.newFeeds = new Array($scope.totalFeeds);
    	$scope.selectedChannel = new Channel();
    	template.open('main', 'edit-channel');
	};
	
	$scope.closeEdition = function(){
		$scope.channel = undefined;
    	$scope.display.edition = false;
	};
	
	$scope.editChannel = function(){
		$scope.selectedChannel = $scope.channels[0];
		$scope.newFeeds = $scope.channels[0].feeds;
		template.open('main', 'edit-channel');
	};
	
	$scope.saveChannel = function(){
		$scope.selectedChannel.feeds = $scope.newFeeds;
		$scope.selectedChannel.save(function(){
			$scope.channels.sync(function(){
				$scope.$apply();
			});
		});
		$scope.cancelEditChannel();
	};
	
	$scope.cancelEditChannel = function(){
		$scope.newFeeds = undefined;
		$scope.selectedChannel = undefined;
		template.open('main', 'channels');
	};
	
	$scope.openMainPage = function(){
		template.open('main', 'channels');
	};
	
}