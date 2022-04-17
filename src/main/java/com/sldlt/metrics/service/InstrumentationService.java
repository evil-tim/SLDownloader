package com.sldlt.metrics.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.util.Pair;

public interface InstrumentationService {

    Runnable instrument(Pair<String, Runnable> namedRunnable);

    Object instrument(ProceedingJoinPoint pjp) throws Throwable;

}
