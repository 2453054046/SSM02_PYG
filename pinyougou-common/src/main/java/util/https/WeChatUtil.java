/*
package util.https;

import jdk.nashorn.internal.objects.Global;

import java.text.DecimalFormat;
import java.util.SortedMap;
import java.util.TreeMap;

public class WeChatUtil {

    */
/**
     * 基本常量设置
     *//*


    */
/**
     * APPID
     *//*

    public static String APP_ID = Global.getConfig("wechat.appid");
    */
/**
     * 微信支付商户号
     *//*

    public static String MCH_ID= Global.getConfig("wechat.mch_id");
    */
/**
     * 请求路径
     *//*

    public static String UFDODER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    */
/**
     * 密匙
     *//*

    public static String API_KEY = Global.getConfig("wechat.key");
    */
/**
     * 发起支付IP
     *//*

    public static String CREATE_IP = "112.117.94.77";
    */
/**
     * 回调url
     *//*

    public static String NOTIFY_URL = "http%3a%2f%2fqqz33fe9841.ngrok.wendal.cn%2fapp%2fhome%2f";

    */
/**
     * 生成微信签名
     * @param order_id
     *         订单ID
     * @param body
     *         描述
     * @param order_price
     *         价格
     * @return
     *//*

    public static  String GetWeChatXML(String order_id, String body, double order_price ){
        String currTime = PayCommonUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = PayCommonUtil.buildRandom(4) + "";
        //随机字符串
        String nonce_str = strTime + strRandom;//UUID.randomUUID().toString();
        nonce_str=nonce_str.substring(0,16);
        // 获取发起电脑 ip
        String spbill_create_ip = WeChatUtil.CREATE_IP;
        // 回调接口
        String notify_url = WeChatUtil.NOTIFY_URL;
        //交易类型
        String trade_type = "JSAPI";
        //微信价格最小单位分 转换为整数
        DecimalFormat df = new DecimalFormat("#######.##");
        order_price = order_price * 100;
        order_price = Math.ceil(order_price);
        String price = df.format(order_price);
        SortedMap<Object,Object> packageParams = new TreeMap<Object,Object>();
        packageParams.put("appid", APP_ID);
        packageParams.put("body", body);
        packageParams.put("mch_id", MCH_ID);
        packageParams.put("nonce_str", nonce_str);
        packageParams.put("notify_url", notify_url);
        packageParams.put("out_trade_no", order_id);
        packageParams.put("spbill_create_ip", spbill_create_ip);
        packageParams.put("total_fee", price);
        packageParams.put("trade_type", trade_type);
        packageParams.put("openid", "o_6gNwi23RLC97cSPfHE-DEg3OLA");
        String sign = PayCommonUtil.createSign("UTF-8", packageParams,API_KEY);
        packageParams.put("sign", sign);
        String requestXML = PayCommonUtil.getRequestXml(packageParams);
        System.out.println(requestXML);
        return requestXML;
    }



}
*/
