package com.pawin.linebotspringboot;

import com.fasterxml.jackson.databind.ser.std.FileSerializer;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


//@LineMessageHandler
@SpringBootApplication
public class LineBotSpringBootApplication {

        static Path downloadedContentDir;
        
	public static void main(String[] args) throws IOException {
                downloadedContentDir = Files.createTempDirectory("line-bot");
		SpringApplication.run(LineBotSpringBootApplication.class, args);
	}

//        @EventMapping
//        public Message handleTexMessage(MessageEvent<TextMessageContent> e){
//            System.out.println("event : " + e);
//            TextMessageContent message =  e.getMessage();
//            return new TextMessage(message.getText());
//        }
}
