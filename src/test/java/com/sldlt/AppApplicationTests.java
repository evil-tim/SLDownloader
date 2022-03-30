package com.sldlt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sldlt.controller.IndexController;

@SpringBootTest
class AppApplicationTests {

    @Autowired
    private IndexController indexController;

    @Test
    void contextLoads() {
        assertThat(indexController).isNotNull();
    }

}
