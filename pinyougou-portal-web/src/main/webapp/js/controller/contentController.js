app.controller('contentController',function($scope,contentService){
	
	$scope.contentList=[];//广告列表,存储首页所有广告，以广告id为下标
	
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){
				$scope.contentList[categoryId]=response;
			}
		);		
	}
	
	//转发搜索页面
    $scope.search=function () {
        //angularjs静态请求
        location.href="http://localhost:9003/search.html#?keywords="+$scope.keywords;
    }
	
});