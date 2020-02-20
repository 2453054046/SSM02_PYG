app.controller('seckillGoodsController' ,function($scope,$location,seckillGoodsService,$interval){

    //获得所有审过通过正在秒杀的商品
    $scope.findList=function () {
        seckillGoodsService.findList().success(
             function (response) {
                 $scope.list=response;
              }
        )
    }

    //获得商品详情
    $scope.findOne=function () {
        //接收参数
        var id =$location.search()['id'];
        seckillGoodsService.fingOen(id).success(
            function (response) {
                $scope.entity=response;
                $scope.second=$scope.entity.endTime;
                allsecond=Math.floor(((new Date($scope.entity.endTime).getTime())-new Date().getTime())/1000);
                         // Math.floor(((new Date($scope.entity.endTime).getTime())- new Date().getTime())/1000);
                /**
                 * 测试定时器
                 * $interval（执行的方法，每隔多少秒执行，执行几次）;
                 * $interval.cancel(结束的定时器);
                 */
                time=$interval(function () {
                    allsecond=allsecond-1;
                    $scope.timeString=convertTimeString(allsecond);
                    if($scope.second<=0){
                        $interval.cancel(time);
                    }
                },1000);
            }
        )
    }

    //转换秒为时间格式
    convertTimeString=function (allsecond) {
        var days=Math.floor(allsecond/(24*60*60));//天数
        var hours=Math.floor((allsecond-days*24*60*60)/(60*60));//小时
        var minutes=Math.floor((allsecond-days*60*60*24-hours*60*60)/60);//分钟
        var seconds=allsecond-days*60*60*24-hours*60*60-minutes*60;//秒数
        if(days>0){
            timeday=days+"天";
        }
        return timeday+hours+":"+minutes+":"+seconds;

    }

    //提交订单
    $scope.submitOrder=function(){
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function(response){
                if(response.success){
                    alert("下单成功，请在1分钟内完成支付");
                    location.href="pay.html";
                }else{
                    alert(response.message);
                }
            }
        );
    }


});