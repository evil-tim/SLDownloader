package com.sldlt.navps.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sldlt.metrics.annotation.Instrumented;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.service.FundService;

@RestController
public class FundResource {

    @Autowired
    private FundService fundService;

    @GetMapping("/api/funds")
    @Instrumented
    public List<FundDto> getFunds() {
        return fundService.listAllFunds();
    }
}
