package com.nbc.custom_reports.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Profile("!default")
@Configuration
@EnableDiscoveryClient
public class ServiceRegistryConfig {

}
