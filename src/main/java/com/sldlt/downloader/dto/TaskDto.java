package com.sldlt.downloader.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sldlt.downloader.TaskStatus;

public class TaskDto {

    private Long id;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private String fund;

    private TaskStatus status;

    private int attempts = 0;

    private LocalDateTime nextAttemptAfter;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public LocalDateTime getNextAttemptAfter() {
        return nextAttemptAfter;
    }

    public void setNextAttemptAfter(LocalDateTime nextAttemptAfter) {
        this.nextAttemptAfter = nextAttemptAfter;
    }

    @Override
    public String toString() {
        return "Task - " + fund + " - " + this.id + " " + this.dateTo.toString() + " -> " + this.dateFrom.toString();
    }

}
