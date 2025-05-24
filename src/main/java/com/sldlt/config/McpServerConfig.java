package com.sldlt.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sldlt.navps.dto.mcp.tool.FundTool;
import com.sldlt.navps.dto.mcp.tool.NAVPSTool;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider slDownloaderTools(FundTool fundTool, NAVPSTool navpsTool) {
        return MethodToolCallbackProvider.builder() //
            .toolObjects(//
                fundTool, //
                navpsTool) //
            .build();
    }

}
