package com.pinyougou.seckill.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeiXinPayservice;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单支付
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 30000)
    private WeiXinPayservice weiXinPayservice;
    @Reference(timeout = 20000)
    SeckillOrderService orderService;
    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis查询支付日志
        TbSeckillOrder order = orderService.searchOrderFromRedisByUserId(userId);
        //判断支付日志存在
        if(order!=null){
            return weiXinPayservice.createNative(order.getId()+"", (long)(order.getMoney().doubleValue()*100)+"");
        }else{
            return new HashMap();
        }

    }

    /**
     * 根据订单ID查询订单的支付状态
     * @param out_trade_no 订单ID
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        Result result=null;
        int x=0;
        while (true){
            Map map = weiXinPayservice.queryPayStatus(out_trade_no);
            if(map==null){
                result=new Result(true,"支付发生错误");
                break;
            }
            if(map.get("trade_state").equals("SUCCESS")){
                result=new Result(true,"支付成功");
                orderService.saveOrderFromRedisToDb(userId,Long.valueOf(out_trade_no), (String) map.get("transaction_id"));//修改订单状态
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if(x>=100){
                result=new Result(false, "二维码超时");
                Map<String,String > closePay = weiXinPayservice.closePay(out_trade_no);
                if(closePay!=null&&"FAIL".equals(closePay.get("return_code"))){
                    //订单已经交易err_code返回：ORDERPAID
                    if("ORDERPAID".equals(closePay.get("err_code"))){
                        result=new Result(true,"支付成功");
                        orderService.saveOrderFromRedisToDb(userId,Long.valueOf(out_trade_no), (String) map.get("transaction_id"));//修改订单状态
                    }
                }
                //删除订单
                if(result.isSuccess()==false){
                    orderService.deleteOrderFromRedis(userId,Long.valueOf(out_trade_no));
                }
                break;
            }
        }
        return result;
    }
}
