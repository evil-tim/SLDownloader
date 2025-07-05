package com.sldlt.navps.dto.mcp.tool;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sldlt.metrics.annotation.Instrumented;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.service.FundService;

@Service
public class FundTool {

    @Autowired
    private FundService fundService;

    @Tool(description = "Returns a list of all funds, which includes their codes and names")
    @Instrumented
    public List<FundDto> listAllFunds() {
        return fundService.listAllFunds();
    }

    @Tool(description = "Returns a fund by its code")
    @Instrumented
    public FundDto getFundByCode(@ToolParam(required = true, description = "The fund code") String code) {
        return fundService.getFundByCode(code);
    }

    @Tool(description = "Returns a fund by its exact name")
    @Instrumented
    public FundDto getFundByName(@ToolParam(required = true, description = "The fund name") String name) {
        return fundService.getFundByName(name);
    }
}
