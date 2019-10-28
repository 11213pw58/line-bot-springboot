package com.pawin.linebotspringboot;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@LineMessageHandler
public class LineBotSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(LineBotSpringBootApplication.class, args);
	}

        @EventMapping
        public Message handleTexMessage(MessageEvent<TextMessageContent> e){
            System.out.println("event : " + e);
            TextMessageContent message =  e.getMessage();
            return new TextMessage(message.getText());
        }
}
