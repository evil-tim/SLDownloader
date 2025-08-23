package com.sldlt.navps.dto.mcp.tool;

import java.time.LocalDate;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.sldlt.mcp.tool.dto.McpPage;
import com.sldlt.metrics.annotation.Instrumented;
import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.service.NAVPSService;

@Service
public class NAVPSTool {

    @Autowired
    private NAVPSService navpsService;

    @Tool(description = "Get the NAVPS for a specified fund code and date range")
    @Instrumented
    public McpPage<NAVPSEntryDto> getNAVPS(
        @ToolParam(required = true, description = "The fund code of the NAVPS, See the fund tool for valid fund codes") String fund,
        @ToolParam(required = true, description = "The start date of the date range") LocalDate dateFrom,
        @ToolParam(required = true, description = "The end date of the date range") LocalDate dateTo,
        @ToolParam(required = false, description = "Page number, starting at 0. Default 0") Integer page,
        @ToolParam(required = false, description = "Page size. Default 10") Integer size) {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        return new McpPage<>(navpsService.listNAVPS(//
            fund, //
            dateFrom, //
            dateTo, //
            PageRequest.of(//
                page, //
                size, //
                Sort.by("entryDate").descending())));
    }

}
