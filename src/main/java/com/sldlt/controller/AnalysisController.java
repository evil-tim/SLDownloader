package com.sldlt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sldlt.metrics.annotation.Instrumented;

@Controller
public class AnalysisController {

    @GetMapping("/analysis-navps")
    @Instrumented
    public String analysisNavps(Model model) {
        model.addAttribute("page", "analysis-navps");
        return "analysis-navps";
    }

    @GetMapping("/analysis-correlation")
    @Instrumented
    public String analysisCorrelation(Model model) {
        model.addAttribute("page", "analysis-correlation");
        return "analysis-correlation";
    }

    @GetMapping("/analysis-prediction")
    @Instrumented
    public String analysisPrediction(Model model) {
        model.addAttribute("page", "analysis-prediction");
        return "analysis-prediction";
    }

}
