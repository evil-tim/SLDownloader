package com.sldlt.downloader.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sldlt.downloader.TaskStatus;
import com.sldlt.downloader.dto.TaskDto;

public interface TaskService {

    void createTask(String fund, LocalDate dateFrom, LocalDate dateTo);

    List<TaskDto> getExecutableTasks(int count);

    void updateTaskSucceeded(Long id);

    void updateTaskFailed(Long id);

    Page<TaskDto> listTasks(LocalDate date, String fund, TaskStatus status, Pageable pageable);

}
