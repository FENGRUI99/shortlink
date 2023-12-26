package com.fengrui.shortlink.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
public class ApplicationStartupLogger implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${server.port}")
    private String port;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Unable to retrieve IP address: {}", e.getMessage());
            ip = "localhost";
        }
        log.info("----------------------------------------------------------");
        log.info("Application hsd-smart-dormitory is running! Access URLs:");
        log.info("Local: \thttp://localhost:" + port + "/doc.html");
        log.info("External: \thttp://" + ip + ":" + port + "/doc.html");
        log.info("Swagger文档: \thttp://" + ip + ":" + port + "/doc.html");
        log.info("----------------------------------------------------------");
    }
}
