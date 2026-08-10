package com.way2jobs.scraper.config;
import org.springframework.boot.context.properties.EnableConfigurationProperties; import org.springframework.context.annotation.Configuration; import org.springframework.scheduling.annotation.EnableScheduling;
@Configuration @EnableScheduling @EnableConfigurationProperties(ScraperProperties.class) public class ScraperConfig { }
