package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeiXinPayservice;
import org.springframework.beans.factory.annotation.Value;
import util.https.HttpClient;

import java.util.HashMap;
import java.util.Map;


@Service
public class WeiXinPayserviceImpl implements WeiXinPayservice {

    @Value("${appid}")//公众号id
    String appid;
    @Value("${partner}")//商户号
    String partner;
    @Value("${notifyurl}")//终端IP:随意一个可访问的地址
    String notifyurl;
    @Value("${partnerkey}")//密匙
    String partnerkey;
    /**
     * 生成二维码
     * @param out_trade_no      交易订单号
     * @param total_fee         商品标价
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {//https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=9_1

        //1.参数封装
        Map param=new HashMap();
        param.put("appid", appid);//公众账号ID
        param.put("mch_id", partner);//商户
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "品优购");
        param.put("out_trade_no", out_trade_no);//交易订单号
        param.put("total_fee", total_fee);//金额（分）
        param.put("spbill_create_ip", "127.0.0.1");
        param.put("notify_url", "http://www.itcast.cn");
        param.put("trade_type", "NATIVE");//交易类型

        try {
            String s = WXPayUtil.generateSignature(param, partnerkey);
            System.out.println(s);
            param.put("sign", s);//签名
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求的参数："+xmlParam);

            //2.发送请求
            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");//生成二维码的数据
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3.获取结果
            String xmlResult = httpClient.getContent();

            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("微信返回结果"+mapResult);
            Map map=new HashMap<>();
            map.put("code_url", mapResult.get("code_url"));//生成支付二维码的链接
            map.put("out_trade_no", out_trade_no);
            map.put("total_fee", total_fee);

            return map;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new HashMap();
        }

       /* //封装参数
        Map param = new HashMap<>();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body","品优购");
        param.put("out_trade_no",out_trade_no);
        param.put("total_fee",total_fee);
        param.put("spbill_create_ip","127.0.0.1");
        param.put("notify_url","http://www.itcast.cn");//通知地址
        param.put("trade_type","NATIVE ");//交易类型

        try {
            //生成XML请求参数
            String xml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求的参数："+xml);

            //发送请求
            HttpClient httpClient = new HttpClient("https://api2.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);//是否是https协议
            httpClient.setXmlParam(xml);//发送的xml数据
            httpClient.post();


            //获得结果
            String content = httpClient.getContent();
            Map<String, String> mapResult = WXPayUtil.xmlToMap(content);
            System.out.println("微信返回结果："+mapResult);
            Map map = new HashMap<>();
            map.put("code_url",mapResult.get("code_url"));//获得二维码链接
            map.put("out_trade_no",out_trade_no);
            map.put("total_fee",total_fee);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }*/
    }

    /**
     * 根据订单号查询订单支付的状态（是否支付成功，是否正在支付）
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {

        //1.封装参数
        Map param=new HashMap();
        param.put("appid", appid);
        param.put("mch_id", partner);
        param.put("out_trade_no", out_trade_no);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //2.发送请求
            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");//查询订单状态
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3.获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("调动查询API返回结果："+xmlResult);

            return map;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 关闭订单
     * @param out_trade_no
     * @return
     */
    @Override
    public Map closePay(String out_trade_no) {
        //1.封装参数
        Map param=new HashMap();
        param.put("appid", appid);
        param.put("mch_id", partner);
        param.put("out_trade_no", out_trade_no);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //2.发送请求
            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");//查询订单状态
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3.获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("调动查询API返回结果 :"+xmlResult);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
