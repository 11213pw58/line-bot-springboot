/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pawin.linebotspringboot;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;

/**
 *
 * @author 585200
 */
@Slf4j
@LineMessageHandler
public class LineBotController {
    
    @Autowired
    private LineMessagingClient lineMessagingClient;
    
    @EventMapping
    public void handleTextMessage(MessageEvent<TextMessageContent> e){//public Message handleTextMessage(MessageEvent<TextMessageContent> e){
        log.info(e.toString());
        System.out.println("event: " + e);
        TextMessageContent message = e.getMessage();
        handleTextContent(e.getReplyToken() , e , message);//return new TextMessage(message.getText());
    }

    private void handleTextContent(String replyToken, MessageEvent<TextMessageContent> e, TextMessageContent message) {
        String text = message.getText();
        
        log.info("Got text message from %s : %s ", replyToken , text);
        
        switch(text){
            case "Profile":{
                String userId = e.getSource().getUserId();
                if(userId!=null){
                    CompletableFuture<UserProfileResponse> whenComplete = lineMessagingClient.getProfile(userId)
                            .whenComplete((profile , throwable)->{
                                if(throwable!=null){
                                    this.replyText(replyToken, throwable.getMessage());
                                    return;
                                }
                                this.reply(replyToken, Arrays.asList( 
                                        new TextMessage("Display name : " + profile.getDisplayName()),
                                        new TextMessage("Display name : " + profile.getStatusMessage()),
                                        new TextMessage("Display name : " + profile.getUserId())
                                ));
                            });
                }
                break;
            }
            default: 
                log.info("Return echo message %s : %s" , replyToken , text);
                this.replyText(replyToken, text);
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void replyText(@NonNull  String replyToken, @NonNull String message) {
        if(replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken is not empty");
        }

        if(message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "...";
        }
        this.reply(replyToken, new TextMessage(message));
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            BotApiResponse response = lineMessagingClient.replyMessage(
                    new ReplyMessage(replyToken, messages)
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
