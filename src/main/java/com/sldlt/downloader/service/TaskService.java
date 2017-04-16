package com.sldlt.downloader.service;

import java.time.LocalDate;
import java.util.List;

import com.sldlt.downloader.TaskStatus;
import com.sldlt.downloader.dto.TaskDto;

public interface TaskService {

    void createTask(String fund, LocalDate dateFrom, LocalDate dateTo);

    void updateTaskStatus(Long id, TaskStatus newStatus);

    List<TaskDto> getExecutableTasks(int count);

}
