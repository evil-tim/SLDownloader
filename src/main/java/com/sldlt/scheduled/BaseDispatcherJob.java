package com.sldlt.scheduled;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public abstract class BaseDispatcherJob {

    private static final Logger LOG = LogManager.getLogger(BaseDispatcherJob.class);

    @Autowired
    private TaskExecutor taskExecutor;

    protected void dispatchJob(String name, Runnable runnable) {
        dispatchJob(Pair.of(name, runnable));
    }

    protected void dispatchJob(Pair<String, Runnable> namedRunnable) {
        dispatchJobs(Collections.singletonList(namedRunnable));
    }

    protected void dispatchJobs(List<Pair<String, Runnable>> namedRunnables) {
        namedRunnables.stream().map(this::instrumentRunnable).forEach(taskExecutor::execute);
    }

    private Runnable instrumentRunnable(Pair<String, Runnable> namedRunnable) {
        return () -> {
            String name = namedRunnable.getFirst();
            Runnable runnable = namedRunnable.getSecond();

            if (LOG.isInfoEnabled()) {
                LOG.info("Executing " + name);
            }
            StopWatch stopwatch = new StopWatch();
            stopwatch.start();

            runnable.run();

            stopwatch.stop();
            if (LOG.isInfoEnabled()) {
                LOG.info("Completed " + name + " in " + stopwatch.getTotalTimeMillis() + "ms");
            }
        };
    }

}
