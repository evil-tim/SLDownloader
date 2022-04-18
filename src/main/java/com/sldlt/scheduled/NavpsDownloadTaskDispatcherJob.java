package com.sldlt.scheduled;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sldlt.downloader.dto.TaskDto;
import com.sldlt.downloader.service.NAVPSTaskExecutorService;
import com.sldlt.downloader.service.TaskService;

@Component
public class NavpsDownloadTaskDispatcherJob extends BaseDispatcherJob {

    private static final Logger LOG = LogManager.getLogger(NavpsDownloadTaskDispatcherJob.class);

    @Value("${task.maxRunning}")
    private Integer maxRunningTasks;

    @Autowired
    private RunningTaskHolder runningTaskHolder;

    @Autowired
    private TaskService taskService;

    @Autowired
    private NAVPSTaskExecutorService navpsTaskExecutorService;

    @Scheduled(initialDelay = 120000, fixedRateString = "${task.executor.rate:30000}")
    public void run() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Running NAVPS download task dispatcher");
        }

        if (runningTaskHolder.size() >= maxRunningTasks) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping, will exceed max running tasks");
            }
            return;
        }

        final List<TaskDto> tasks = taskService.getExecutableTasks(maxRunningTasks);

        if (tasks == null || tasks.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping, no tasks available");
            }
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatching {} tasks", tasks.size());
        }

        dispatchJobs(tasks.stream().filter(this::tryAddTask).map(this::wrapTask).toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Completed NAVPS download task dispatcher");
        }
    }

    private boolean tryAddTask(TaskDto task) {
        return runningTaskHolder.add(task.getId());
    }

    private Pair<String, Runnable> wrapTask(TaskDto task) {
        return Pair.of("Download NAVPS", () -> runTask(task));
    }

    private void runTask(TaskDto task) {
        navpsTaskExecutorService.executeTask(task);
        runningTaskHolder.remove(task.getId());
    }
}
