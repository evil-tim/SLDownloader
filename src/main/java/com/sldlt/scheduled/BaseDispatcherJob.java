package com.sldlt.scheduled;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import com.sldlt.metrics.service.InstrumentationService;

@Component
public abstract class BaseDispatcherJob {

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private TaskExecutor taskExecutor;

    @Autowired
    private InstrumentationService instrumentationService;

    protected void dispatchJob(String name, Runnable runnable) {
        dispatchJob(Pair.of(name, runnable));
    }

    protected void dispatchJob(Pair<String, Runnable> namedRunnable) {
        dispatchJobs(Collections.singletonList(namedRunnable));
    }

    protected void dispatchJobs(List<Pair<String, Runnable>> namedRunnables) {
        namedRunnables.stream().map(instrumentationService::instrument).forEach(taskExecutor::execute);
    }

}
