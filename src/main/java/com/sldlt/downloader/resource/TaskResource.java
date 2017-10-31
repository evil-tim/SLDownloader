package com.sldlt.downloader.resource;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sldlt.downloader.TaskStatus;
import com.sldlt.downloader.dto.TaskDto;
import com.sldlt.downloader.service.TaskService;

@RestController
public class TaskResource {

    @Autowired
    private TaskService taskService;

    @RequestMapping(path = "/tasks", method = RequestMethod.GET)
    public Page<TaskDto> getTasks(
        @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(name = "fund", required = false) String fund, @RequestParam(name = "status", required = false) String status,
        Pageable pageable) {
        TaskStatus taskStatus = null;
        if (StringUtils.hasText(status)) {
            try {
                taskStatus = TaskStatus.valueOf(status);
            } catch (IllegalArgumentException ex) {
                // noting to do
            }
        }
        return taskService.listTasks(date, fund, taskStatus, pageable);
    }

    @RequestMapping(path = "/tasks/running", method = RequestMethod.GET)
    public List<TaskDto> getRunningTasks() {
        return taskService.listRunningTasks();
    }
}
