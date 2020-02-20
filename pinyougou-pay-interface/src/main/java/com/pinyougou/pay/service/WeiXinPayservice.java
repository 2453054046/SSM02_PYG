package com.pinyougou.pay.service;

import java.util.Map;

public interface WeiXinPayservice {

    /**
     * 生成二维码
     * @param out_trade_no      商户订单号
     * @param total_fee         商品标价
     * @return
     */
    Map createNative(String out_trade_no,String total_fee);

    /**
     * 根据订单号查询订单支付的状态（是否支付成功，是否正在支付）
     * @param out_trade_no
     * @return
     */
    Map queryPayStatus(String out_trade_no);

    /**
     * 关闭订单
     * @param out_trade_no
     * @return
     */
    Map closePay(String out_trade_no);
}
