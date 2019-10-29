/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pawin.linebot;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

/**
 *
 * @author 585200
 */
@LineMessageHandler
public class LineBotController {
    
    @EventMapping
    public Message handleTextMessage(MessageEvent<TextMessageContent> e){
        System.out.println("event: " + e);
        TextMessageContent message = e.getMessage();
        return new TextMessage(message.getText());
    }
}
