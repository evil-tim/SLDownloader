package com.sldlt.downloader.resource;

import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sldlt.downloader.TaskStatus;
import com.sldlt.downloader.dto.TaskDto;
import com.sldlt.downloader.service.TaskService;

@RestController
public class TaskResource {

    private static final Logger LOG = LogManager.getLogger(TaskResource.class);

    @Autowired
    private TaskService taskService;

    @GetMapping("/api/tasks")
    public Page<TaskDto> getTasks(
        @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(name = "fund", required = false) String fund, @RequestParam(name = "status", required = false) String status,
        Pageable pageable) {
        TaskStatus taskStatus = null;
        if (StringUtils.hasText(status)) {
            try {
                taskStatus = TaskStatus.valueOf(status);
            } catch (IllegalArgumentException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
        return taskService.listTasks(date, fund, taskStatus, pageable);
    }

    @GetMapping("/api/tasks/running")
    public List<TaskDto> getRunningTasks() {
        return taskService.listRunningTasks();
    }

    @PostMapping("/api/task/{id}/retry")
    public TaskDto retryTask(@PathVariable("id") Long id) {
        return taskService.resetTaskStatus(id);
    }
}
