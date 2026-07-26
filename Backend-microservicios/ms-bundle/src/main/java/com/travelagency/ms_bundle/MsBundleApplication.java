package com.travelagency.ms_bundle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MsBundleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsBundleApplication.class, args);
	}

}
