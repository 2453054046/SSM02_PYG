//购物车控制层
app.controller('cartController',function($scope,$http,cartService){
    //查询购物车列表
    $scope.findCartList=function(){
        cartService.findCartList().success(
            function(response){
                $scope.cartList=response;
                $scope.totalValue=cartService.sum($scope.cartList);//求合计数
            }
        );
    }
    //更改购物车商品数量
    $scope.addGoodsToCartList=function(itemId,num){
        //var item_num=$("#item_num").getAttribute("id").val;
        var item_num=document.getElementById("item_num").value;
        console.log(item_num);
        if(item_num==1&&num<=-1){
            return;
        }
        cartService.addGoodsToCartList(itemId,num).success(
            function(response){
                if(response.success){
                    $scope.findCartList();//刷新列表
                }else{
                    alert(response.message);//弹出错误提示
                }
            }
        );
    }

    //修改回显商品
    $scope.findOne=function(id){
        cartService.findOne(id).success(
            function (response) {
                $scope.address=response;
            }
        )
    }

    //获取地址列表
    $scope.findAddressList=function(){
        cartService.findAddressList().success(
            function(response){
                $scope.addressList=response;
            }
        );
    }


    //选择地址
    $scope.selectAddress=function(address){
        $scope.address=address;
    }



    //判断是否是当前选中的地址
    $scope.isSelectedAddress=function(address){
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }

    $scope.address={};
    //别名点击
    $scope.aliass=function (alias) {
        $scope.address.alias=alias;
    }

    //新增和修改收货地址
   $scope.addAddress=function () {
       var serviceObject;//服务层对象
        if($scope.address.id==null){
            serviceObject=cartService.addAddress($scope.address);
        }else {
            serviceObject=cartService.addressUpdate($scope.address);
        }
       serviceObject.success(
           function (response) {
               if(response.success){
                   $scope.findAddressList();
               }else{
                   alert(response.message);
               }
           }
       )
   }
    //删除地址
    $scope.dele=function(id){
        cartService.dele(id).success(
            function (response) {
                if(response.success){
                    $scope.findAddressList();
                }else{
                    alert(response.message);
                }
            }
        )
    }

   //获得省份
    $scope.findProvince=function () {
        $http.get('address/fingProvincesList.do').success(
            function (response) {
                $scope.provincesList=response.provincesList;
            }
        )
    }
    //获得市区
    $scope.$watch('address.provinceId',function (newValue) {
        $http.get('address/findcitiesList.do?id='+newValue).success(
            function (response) {
                $scope.citiesList=response.citiesList;
            }
        )
    })
    //获得县区
    $scope.$watch('address.cityId',function (newValue) {
        $http.get('address/findareasList.do?id='+newValue).success(
            function (response) {
                $scope.areasList=response.areasList;
            }
        )
    })

    $scope.order={paymentType:'1'};
    //选择支付方式
    $scope.selectPayType=function(type){
        $scope.order.paymentType= type;
    }

    //保存订单
    $scope.submitOrder=function(){
        //补全信息
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人

        cartService.submitOrder( $scope.order ).success(
            function(response){
                //alert(response.message);
                if(response.success){
                    //页面跳转
                    if($scope.order.paymentType=='1'){//如果是微信支付，跳转到支付页面
                        location.href="pay.html";
                    }else{//如果货到付款，跳转到提示页面
                        location.href="paysuccess.html";
                    }

                }else{
                    alert(response.message);	//也可以跳转到提示页面
                }

            }
        );
    }
});
