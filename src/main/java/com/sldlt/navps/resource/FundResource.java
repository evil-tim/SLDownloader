package com.sldlt.navps.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.service.FundService;

@RestController
public class FundResource {

    @Autowired
    private FundService fundService;

    @RequestMapping(path = "/fund/all", method = RequestMethod.GET)
    public List<FundDto> getFunds() {
        return fundService.listAllFunds();
    }
}
