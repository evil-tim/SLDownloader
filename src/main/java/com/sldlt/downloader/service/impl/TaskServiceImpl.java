package com.sldlt.downloader.service.impl;

import static com.sldlt.downloader.TaskStatus.FAILED;
import static com.sldlt.downloader.TaskStatus.PENDING;
import static com.sldlt.downloader.entity.QTask.task;
import static org.springframework.data.domain.Sort.Direction.DESC;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.sldlt.downloader.TaskStatus;
import com.sldlt.downloader.dto.TaskDto;
import com.sldlt.downloader.entity.Task;
import com.sldlt.downloader.repository.TaskRepository;
import com.sldlt.downloader.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public void createTask(String fund, LocalDate dateFrom, LocalDate dateTo) {
        if (taskRepository.count(task.fund.eq(fund).and(task.dateFrom.eq(dateFrom)).and(task.dateTo.eq(dateTo))) <= 0) {
            Task newTask = new Task();
            newTask.setFund(fund);
            newTask.setDateFrom(dateFrom);
            newTask.setDateTo(dateTo);
            taskRepository.save(newTask);
        }
    }

    @Override
    public void updateTaskStatus(Long id, TaskStatus newStatus) {
        Optional.ofNullable(taskRepository.findOne(id)).ifPresent(task -> {
            task.setStatus(newStatus);
            taskRepository.save(task);
        });
    }

    @Override
    public List<TaskDto> getExecutableTasks(int count) {
        Sort sort = new Sort(new Order(DESC, "status"), new Order(DESC, "dateTo"));
        PageRequest pageable = new PageRequest(0, count, sort);
        return taskRepository.findAll(task.status.in(PENDING, FAILED), pageable).getContent().stream()
                .map(task -> mapper.map(task, TaskDto.class)).collect(Collectors.toList());
    }

}
