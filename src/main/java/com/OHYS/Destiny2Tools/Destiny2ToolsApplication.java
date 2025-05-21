package com.OHYS.Destiny2Tools;


import love.forte.simboot.spring.autoconfigure.EnableSimbot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSimbot // 启用 Simbot 自动配置
public class Destiny2ToolsApplication {

	public static void main(String[] args) {
		SpringApplication.run(Destiny2ToolsApplication.class, args);
	}

}
