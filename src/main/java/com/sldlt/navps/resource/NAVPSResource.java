package com.sldlt.navps.resource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sldlt.navps.dto.NAVPSEntryDto;
import com.sldlt.navps.dto.NAVPSPredictionDto;
import com.sldlt.navps.service.NAVPSPredictionService;
import com.sldlt.navps.service.NAVPSService;

@RestController
public class NAVPSResource {

    @Autowired
    private NAVPSService navpsService;

    @Autowired
    private NAVPSPredictionService navpsPredictionService;

    @RequestMapping(path = "/api/navps", method = RequestMethod.GET)
    public Page<NAVPSEntryDto> getNAVPS(@RequestParam(name = "fund", required = false) String fund,
        @DateTimeFormat(iso = ISO.DATE) @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom,
        @DateTimeFormat(iso = ISO.DATE) @RequestParam(name = "dateTo", required = false) LocalDate dateTo,
        @PageableDefault(sort = { "date" }, direction = Direction.DESC) Pageable page) {
        return navpsService.listNAVPS(fund, dateFrom, dateTo, page);
    }

    @RequestMapping(path = "/api/navps/all", method = RequestMethod.GET)
    public List<NAVPSEntryDto> getNAVPS(@RequestParam("fund") String fund) {
        return navpsService.listAllNAVPS(fund);
    }

    @RequestMapping(path = "/api/navps/correlations", method = RequestMethod.GET)
    public Map<String, Map<String, BigDecimal>> getNAVPSCorrelations(
        @DateTimeFormat(iso = ISO.DATE) @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom) {
        return navpsService.listAllCorrelations(dateFrom);
    }

    @RequestMapping(path = "/api/navps/scatter", method = RequestMethod.GET)
    public List<Pair<BigDecimal, BigDecimal>> getScatterNAVPS(@RequestParam("fundX") String fundX, @RequestParam("fundY") String fundY,
        @DateTimeFormat(iso = ISO.DATE) @RequestParam(name = "dateFrom") LocalDate dateFrom,
        @DateTimeFormat(iso = ISO.DATE) @RequestParam(name = "dateTo") LocalDate dateTo) {
        return navpsService.listNAVPSPaired(fundX, fundY, dateFrom, dateTo);
    }

    @RequestMapping(path = "/api/navps/predictions", method = RequestMethod.GET)
    public Map<LocalDate, Set<NAVPSPredictionDto>> getNAVPSPredictions(@RequestParam(name = "type") String type,
        @RequestParam(name = "fund") String fund, @DateTimeFormat(iso = ISO.DATE) @RequestParam(name = "dateFrom") LocalDate dateFrom,
        @DateTimeFormat(iso = ISO.DATE) @RequestParam(name = "dateTo") LocalDate dateTo) {
        return navpsPredictionService.getPredictions(fund, type, dateFrom, dateTo);
    }

    @RequestMapping(path = "/api/navps/predictions/types", method = RequestMethod.GET)
    public Set<String> getNAVPSPredictionTypes() {
        return navpsPredictionService.getPredictionsTypes();
    }

}
