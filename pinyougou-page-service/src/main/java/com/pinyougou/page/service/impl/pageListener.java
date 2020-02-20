package com.pinyougou.page.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * 监听mavager-web涩会给你成静态模板
 */
@Component
public class pageListener implements MessageListener {

    @Autowired
    ItemPageServiceImpl itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText();
            boolean b = itemPageService.getItemHtml(Long.parseLong(text));
            System.out.println("生成静态模板结果："+b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
