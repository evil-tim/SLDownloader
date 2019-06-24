package com.sldlt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalysisController {

    @GetMapping("/analysis-navps")
    public String analysisNavps(Model model) {
        model.addAttribute("page", "analysis-navps");
        return "analysis-navps";
    }

    @GetMapping("/analysis-correlation")
    public String analysisCorrelation(Model model) {
        model.addAttribute("page", "analysis-correlation");
        return "analysis-correlation";
    }

    @GetMapping("/analysis-prediction")
    public String analysisPrediction(Model model) {
        model.addAttribute("page", "analysis-prediction");
        return "analysis-prediction";
    }

}
