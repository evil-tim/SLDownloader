package com.sldlt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TasksController {

    @GetMapping("/tasks")
    public String index(Model model) {
        model.addAttribute("page", "tasks");
        return "tasks";
    }

}
