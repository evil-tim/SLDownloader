package com.sldlt.scheduled;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class RunningTaskHolder {

    private Set<Long> runningTaskIdSet = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());

    public boolean add(Long id) {
        return runningTaskIdSet.add(id);
    }

    public void remove(Long id) {
        runningTaskIdSet.remove(id);
    }

    public int size() {
        return runningTaskIdSet.size();
    }

    public Stream<Long> stream() {
        return runningTaskIdSet.stream();
    }

}
