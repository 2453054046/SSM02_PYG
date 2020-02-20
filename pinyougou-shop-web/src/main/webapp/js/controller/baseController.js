app.controller('baseController',function ($scope) {
    /**
     *分页控件配置currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法
     */
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();
        }
    };
    //刷新列表
    $scope.reloadList = function(){
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }
    $scope.selectIds=[];//选中的ID集合

    //更新复选
    $scope.updateSelection = function($event, id) {
        if($event.target.checked){//如果是被选中,则增加到数组
            $scope.selectIds.push(id);
        }else{
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除
        }
    }

    $scope.jsonToString=function (jsonString,key) {
        //将传入的字符串转换对象
        var json = JSON.parse(jsonString);
        var value = "";
        for(var i=0; i<json.length;i++){
            if(i>0){
                value+=",";
            }
            //json对象第二种获得属性值的写法
            value += json[i][key];
        }
        return value;
    }

    //集合中根据某key的值查询对象，如果存在返回该值，不存在返回null
    /**
     * list[i][key]:获得集合中对应key的值
     * @param list      要遍历的集合
     * @param key       集合中的key
     * @param keyValue  要查询的值
     */
    $scope.searchObjectBykey=function (list,key,keyValue) {
        for(var i=0;i<list.length;i++){
            if(list[i][key]==keyValue){
                return list[i];
            }
        }
        return null;
    }
})