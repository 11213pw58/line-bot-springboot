/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pawin.linebotspringboot;

import com.google.common.io.ByteStreams;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author 585200
 */
@Slf4j
@LineMessageHandler
public class LineBotController {
    
    @Autowired
    private LineMessagingClient lineMessagingClient;
    
    //send text 
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
    
    //send sticker
    @EventMapping
    public void handleStickerMessage(MessageEvent<StickerMessageContent> event){
        log.info(event.toString());
        StickerMessageContent message = event.getMessage();
        reply(event.getReplyToken(), new StickerMessage(
                message.getPackageId(), message.getStickerId()
        ));
    }
    
    //send location
    @EventMapping
    public void handleLocationMessage(MessageEvent<LocationMessageContent> event){
        log.info(event.toString());
        LocationMessageContent message = event.getMessage();
        reply(event.getReplyToken(), new LocationMessage((message.getTitle()==null)?"Location replied": message.getTitle()
                , message.getAddress()
                , message.getLatitude()
                , message.getLongitude()
        ));
    }
    
    //send Image from user
    @EventMapping
    public void handleImageMessage(MessageEvent<ImageMessageContent> event) {
        log.info(event.toString());
        ImageMessageContent content = event.getMessage();
        String replyToken = event.getReplyToken();

        try {
            MessageContentResponse response = lineMessagingClient.getMessageContent(
                content.getId()).get();
            DownloadedContent jpg = saveContent("jpg", response);
            DownloadedContent previewImage = createTempFile("jpg");

            system("convert", "-resize", "242x", jpg.path.toString(), previewImage.path.toString());

            reply(replyToken, new ImageMessage(jpg.getUri(), previewImage.getUri()));

        } catch (InterruptedException | ExecutionException e) {
            reply(replyToken, new TextMessage("Cannot get image: " + content));
            throw new RuntimeException(e);
        }

    }

    private void system(String... args) {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        try {
            Process start = processBuilder.start();
            int i = start.waitFor();
            log.info("result: {} => {}", Arrays.toString(args), i);
        } catch (InterruptedException e) {
            log.info("Interrupted", e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DownloadedContent saveContent(String ext, 
                                                 MessageContentResponse response) {
        log.info("Content-type: {}", response);
        DownloadedContent tempFile = createTempFile(ext);
        try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
            ByteStreams.copy(response.getStream(), outputStream);
            log.info("Save {}: {}", ext, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DownloadedContent createTempFile(String ext) {
        String fileName = LocalDateTime.now() + "-" 
                          + UUID.randomUUID().toString() 
                          + "." + ext;
        Path tempFile = LineBotSpringBootApplication.downloadedContentDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        return new DownloadedContent(tempFile, createUri("/downloaded/" + tempFile.getFileName()));

    }

    private static String createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(path).toUriString();
    }
    //class download Content
    @Value
    public static class DownloadedContent {
        Path path;
        String uri;
    }    
}
