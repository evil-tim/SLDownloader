package com.sldlt.downloader.service.impl;

import static com.sldlt.downloader.TaskStatus.FAILED;
import static com.sldlt.downloader.TaskStatus.PENDING;
import static com.sldlt.downloader.TaskStatus.SUCCESS;
import static com.sldlt.downloader.entity.QTask.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.sldlt.downloader.TaskStatus;
import com.sldlt.downloader.dto.TaskDto;
import com.sldlt.downloader.entity.Task;
import com.sldlt.downloader.exception.MissingTaskException;
import com.sldlt.downloader.repository.TaskRepository;
import com.sldlt.downloader.service.TaskService;
import com.sldlt.navps.dto.FundDto;
import com.sldlt.navps.service.FundService;
import com.sldlt.scheduled.RunningTaskHolder;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private RunningTaskHolder runningTaskHolder;

    @Autowired
    private FundService fundService;

    @Value("${task.maxRetries}")
    private long taskMaxRetries;

    @Value("${task.retryCooldown}")
    private long taskRetryCooldown;

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
    public List<TaskDto> getExecutableTasks(int count) {
        final PageRequest pageable = PageRequest.of(0, count, Sort.by(new Order(Sort.Direction.DESC, "dateTo")));
        return taskRepository
            .findAll(task.status.in(PENDING, FAILED).and(task.attempts.lt(taskMaxRetries))
                .and(task.nextAttemptAfter.isNull().or(task.nextAttemptAfter.before(LocalDateTime.now()))), pageable)
            .getContent().stream().map(item -> mapper.map(item, TaskDto.class)).toList();
    }

    @Override
    public void updateTaskSucceeded(Long id) {
        taskRepository.findOne(task.id.eq(id)).ifPresent(task -> {
            task.setStatus(SUCCESS);
            task.setAttempts(task.getAttempts() + 1);
            taskRepository.save(task);
        });

    }

    @Override
    public void updateTaskFailed(Long id) {
        taskRepository.findOne(task.id.eq(id)).ifPresent(task -> {
            task.setStatus(FAILED);
            task.setAttempts(task.getAttempts() + 1);
            long cooldownFactor = task.getAttempts();
            cooldownFactor = cooldownFactor * cooldownFactor * cooldownFactor;
            task.setNextAttemptAfter(LocalDateTime.now().plusSeconds(taskRetryCooldown * cooldownFactor));
            taskRepository.save(task);
        });

    }

    @Override
    public TaskDto resetTaskStatus(Long id) {
        return taskRepository.findOne(task.id.eq(id)).map(task -> {
            task.setStatus(PENDING);
            task.setAttempts(0);
            task.setNextAttemptAfter(null);
            return taskRepository.save(task);
        }).map(item -> mapper.map(item, TaskDto.class)).orElseThrow(MissingTaskException::new);
    }

    @Override
    public Page<TaskDto> listTasks(LocalDate date, String fund, TaskStatus status, Pageable pageable) {
        BooleanBuilder predicate = new BooleanBuilder();

        if (date != null) {
            predicate.and(task.dateFrom.loe(date));
            predicate.and(task.dateTo.goe(date));
        }
        if (StringUtils.hasText(fund)) {
            predicate.and(task.fund.eq(fund));
        }
        if (status != null) {
            predicate.and(task.status.eq(status));
        }

        final List<FundDto> funds = fundService.listAllFunds();

        return taskRepository.findAll(predicate, pageable).map(item -> {
            TaskDto mappedTask = mapper.map(item, TaskDto.class);
            mappedTask.setRetryable(mappedTask.getAttempts() < taskMaxRetries);
            mappedTask.setFundName(getFundName(funds, mappedTask.getFund()));
            return mappedTask;
        });
    }

    @Override
    public List<TaskDto> listRunningTasks() {
        final List<FundDto> funds = fundService.listAllFunds();

        return runningTaskHolder.stream().map(id -> mapper.map(taskRepository.findOne(task.id.eq(id)), TaskDto.class)).map(task -> {
            task.setRetryable(task.getAttempts() < taskMaxRetries);
            task.setFundName(getFundName(funds, task.getFund()));
            return task;
        }).sorted().toList();
    }

    private String getFundName(List<FundDto> funds, String fundCode) {
        return funds.stream().filter(fund -> fund.getCode().equals(fundCode)).findFirst().map(FundDto::getName).orElse("");
    }

}
