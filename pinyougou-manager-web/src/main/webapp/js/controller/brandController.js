app.controller('brandController',function ($scope,$controller,brandService) {

    $controller('baseController',{$scope:$scope});//继承
    $scope.findAll = function(){
        brandService.findAll().success(
            function (data) {
                $scope.list = data;
            }
        )
    }
    /*/!**
     *分页控件配置currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法
     *!/
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
    }*/

    $scope.pageAll = function (page,size) {
        brandService.pageAll(page,size).success(
            function (data) {
                $scope.list = data.rows;//显示当前页数据
                $scope.paginationConf.totalItems = data.total; //更新总记录数
            }
        )
    }
    $scope.searchEntity={};
    //条件查询
    $scope.search=function (page,size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function (data) {
                $scope.list = data.rows;//显示当前页数据
                $scope.paginationConf.totalItems = data.total; //更新总记录数
            }
        )
    }
    //新增和修改
    $scope.save = function () {
        var object = null;
        if($scope.entity.id != null){
            object = brandService.update($scope.entity);
        }else{
            object = brandService.add($scope.entity);
        }
        object.success(
            function (data) {
                if(data.status==200)
                    $scope.reloadList();
                else
                    alert(data.msg);
            }
        );
    }

    //回显商品
    $scope.findById = function (id) {
        brandService.findById.success(
            function (data) {
                $scope.entity = data;
            }
        )
    }
    /*$scope.dataId = [];
    //勾选商品
    $scope.updateSelection = function ($event,id) {
        if($event.target.checked){
            $scope.dataId.push(id);
        }else {
            var index= $scope.dataId.indexOf(id);//查找值的 位置
            $scope.dataId.splice(index,1);//参数1：移除的位置 参数2：移除的个数
        }
    }*/
    //删除品牌
    $scope.del =function () {
        brandService.del($scope.selectIds).success(
            function (data) {
                if(data.status==200)
                    $scope.reloadList();
                else
                    alert(data.msg);
            }
        )
    }



});