package com.sldlt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
@Profile("aws")
public class AWSConfig {

    @Bean
    public AmazonS3 amazonS3() {
        InstanceProfileCredentialsProvider provider = new InstanceProfileCredentialsProvider(true);
        return AmazonS3ClientBuilder.standard().withCredentials(provider).build();
    }
}
