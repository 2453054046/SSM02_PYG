//购物车服务层
app.service('cartService',function($http){
    //购物车列表
    this.findCartList=function(){
        return $http.get('cart/findCartList.do');
    }
    //新增收获地址
    this.addAddress=function (address){
        return $http.post('address/add.do',address);
    }
    //地址回显数据
    this.findOne=function (id) {
        return $http.get('address/findOne.do?id='+id);
    }
    //修改地址
    this.addressUpdate=function (address) {
        return $http.post('address/update.do',address);
    }
    //删除地址
    this.dele=function (id) {
        return $http.get('address/delete.do?id='+id);
    }
    //更改购物车商品数量
    this.addGoodsToCartList=function(itemId,num){
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }

    //求合计
    this.sum=function(cartList){
        var totalValue={totalNum:0, totalMoney:0.00 };//合计实体
        for(var i=0;i<cartList.length;i++){
            var cart=cartList[i];
            for(var j=0;j<cart.orderItemList.length;j++){
                var orderItem=cart.orderItemList[j];//购物车明细
                totalValue.totalNum+=orderItem.num;
                totalValue.totalMoney+= orderItem.totalFee;
            }
        }
        return totalValue;
    }

    //获取收货地址列表
    this.findAddressList=function(){
        return $http.get('address/findListByLoginUser.do');
    }


    //提交订单
    this.submitOrder=function(order){
        return $http.post('order/add.do',order);
    }



});
