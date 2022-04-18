package com.sldlt.metrics.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.sldlt.metrics.service.InstrumentationService;

@Component
public class InstrumentationServiceImpl implements InstrumentationService {

    private static final Logger LOG = LogManager.getLogger(InstrumentationServiceImpl.class);

    @Override
    public Runnable instrument(Pair<String, Runnable> namedRunnable) {
        return () -> {
            String name = namedRunnable.getFirst();
            Runnable runnable = namedRunnable.getSecond();
            StopWatch stopwatch = new StopWatch();

            logExecution(name);

            stopwatch.start();
            runnable.run();
            stopwatch.stop();

            logCompletion(name, stopwatch.getTotalTimeMillis());
        };
    }

    @Override
    public Object instrument(ProceedingJoinPoint joinPoint) throws Throwable {
        String name = joinPoint.getSignature().toString();
        StopWatch stopwatch = new StopWatch();

        try {
            logExecution(name);

            stopwatch.start();
            return joinPoint.proceed();
        } finally {
            stopwatch.stop();
            logCompletion(name, stopwatch.getTotalTimeMillis());
        }
    }

    private void logExecution(String name) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing [{}]", name);
        }
    }

    private void logCompletion(String name, long totalTimeMs) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Completed [{}] in {}ms", name, totalTimeMs);
        }
    }

}
