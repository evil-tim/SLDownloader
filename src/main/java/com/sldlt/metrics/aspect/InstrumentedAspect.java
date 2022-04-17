package com.sldlt.metrics.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sldlt.metrics.service.InstrumentationService;

@Aspect
@Component
public class InstrumentedAspect {

    @Autowired
    private InstrumentationService instrumentationService;

    @Around("@annotation(com.sldlt.metrics.annotation.Instrumented)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return instrumentationService.instrument(joinPoint);
    }
}
