/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pawin.linebotspringboot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author 585200
 */
@Slf4j
@Configuration
public class LineBotConfigure implements WebMvcConfigurer{
  
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        String downloadedContentUri = LineBotSpringBootApplication.downloadedContentDir.toUri().toASCIIString();
//        log.info("downloaded Uri: {}", downloadedContentUri);
//        registry.addResourceHandler("/downloaded/**")
//                .addResourceLocations(downloadedContentUri);
//    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        String downloadedContentUri = LineBotSpringBootApplication.downloadedContentDir.toUri().toASCIIString();
        log.info("downloaded Uri: {}", downloadedContentUri);
        registry.addResourceHandler("/downloaded/**").addResourceLocations(downloadedContentUri);
    }
}
