package com.sldlt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sldlt.metrics.annotation.Instrumented;

@Controller
public class TasksController {

    @GetMapping("/tasks")
    @Instrumented
    public String index(Model model) {
        model.addAttribute("page", "tasks");
        return "tasks";
    }

}
