package com.mienmq.client;

import com.mienmq.client.annotation.EnableMienMq;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootApplication(scanBasePackages = {"com.mienmq.client"})
@EnableMienMq(whetherReconnect = true)
@EnableAspectJAutoProxy
public class MienmqClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(MienmqClientApplication.class, args);

    }

}
