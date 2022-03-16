package com.sldlt.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class BuildInfoControllerAdvice {

    @Value("${application.build.version}")
    private String applicationBuildVersion;

    @Value("${application.build.time}")
    private String applicationBuildTime;

    @ModelAttribute("applicationBuildVersion")
    public String getApplicationBuildVersion() {
        return applicationBuildVersion;
    }

    @ModelAttribute("applicationBuildTime")
    public String getApplicationBuildTime() {
        return applicationBuildTime;
    }

}
