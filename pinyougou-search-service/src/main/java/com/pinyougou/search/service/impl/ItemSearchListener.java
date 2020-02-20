package com.pinyougou.search.service.impl;


import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * 监听manager-web的消息
 */
@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        System.out.println("走过ItemSearchListenter监听器");
        TextMessage textMessage = (TextMessage)message;
        try {
            String text = textMessage.getText();
            List<TbItem> parse = JSON.parseArray(text, TbItem.class);
            itemSearchService.importList(parse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
