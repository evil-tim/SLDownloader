package com.sldlt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sldlt.controller.IndexController;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppApplicationTests {

    @Autowired
    private IndexController indexController;

    @Test
    public void contextLoads() {
        assertThat(indexController).isNotNull();
    }

}
