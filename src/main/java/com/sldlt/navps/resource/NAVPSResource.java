package com.sldlt.navps.resource;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.service.NAVPSService;

@RestController
public class NAVPSResource {

    @Autowired
    private NAVPSService navpsService;

    @RequestMapping(path = "/navps", method = RequestMethod.GET)
    public Page<NAVPSEntryDto> getNAVPS(@RequestParam("fund") String fund,
            @DateTimeFormat(iso = ISO.DATE) @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom,
            @DateTimeFormat(iso = ISO.DATE) @RequestParam(name = "dateTo", required = false) LocalDate dateTo,
            @PageableDefault(sort = { "date" }, direction = Direction.DESC) Pageable page) {
        return navpsService.listNAVPS(fund, dateFrom, dateTo, page);
    }

    @RequestMapping(path = "/navps/all", method = RequestMethod.GET)
    public List<NAVPSEntryDto> getNAVPS(@RequestParam("fund") String fund) {
        return navpsService.listAllNAVPS(fund);
    }

}
