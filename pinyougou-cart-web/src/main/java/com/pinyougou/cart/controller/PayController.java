/*
package com.pinyougou.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeiXinPayservice;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

*/
/**
 * 订单支付
 *//*

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 30000)
    private WeiXinPayservice weiXinPayservice;

    @Reference(timeout = 20000)
    private OrderService orderService;
    */
/**
     * 生成二维码
     * @return
     *//*

    @RequestMapping("/createNative")
    public Map createNative(){
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis查询支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        //判断支付日志存在
        if(payLog!=null){
            return weiXinPayservice.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else{
            return new HashMap();
        }

    }

    */
/**
     * 根据订单ID查询订单的支付状态
     * @param out_trade_no 订单ID
     * @return
     *//*

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
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
                orderService.updateOrderStatus(out_trade_no, (String) map.get("transaction_id"));//修改订单状态
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
                break;
            }
        }
        return result;
    }
}
*/
