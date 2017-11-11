package com.sldlt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TasksController {

    @RequestMapping("/tasks")
    public String index(Model model) {
        model.addAttribute("page", "tasks");
        return "tasks";
    }

}
