package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;
@Component
public class PageDeleteListener implements MessageListener {

    @Autowired
    ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage= (ObjectMessage) message;
        try {
            Long[] object = (Long[]) objectMessage.getObject();
            boolean b = itemPageService.deleteItemHtml(object);
            System.out.println("删除网页："+b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
