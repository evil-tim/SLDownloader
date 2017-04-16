package com.sldlt.downloader.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Component;

import com.sldlt.downloader.entity.Task;

@Component
public interface TaskRepository extends JpaRepository<Task, Long>, QueryDslPredicateExecutor<Task> {

}
