package com.zhang.itcastsmsservice.itcast;


import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListener {

    @Autowired
    SmsUtil smsUtil;
    //消息队列监听注解
    @JmsListener(destination = "sms")
    public void sendSms(Map<String,String> map){
        try {
            System.out.println(map.get("mobile")+" "+
                    map.get("template_code") +" "+
                    map.get("sign_name")  +" "+
                    map.get("param"));
            SendSmsResponse response = smsUtil.sendSms(map.get("mobile"),
                    map.get("template_code") ,
                    map.get("sign_name")  ,
                    map.get("param") );
            System.out.println("code:"+response.getCode());
            System.out.println("message:"+response.getMessage());
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}
