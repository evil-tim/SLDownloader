package com.sldlt.downloader.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sldlt.downloader.TaskStatus;

public class TaskDto {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;

    private String fund;

    private String fundName;

    private TaskStatus status;

    private int attempts = 0;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextAttemptAfter;

    private boolean retryable = true;

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

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
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

    public boolean isRetryable() {
        return retryable;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    @Override
    public String toString() {
        return "Task - " + fund + " - " + this.id + " " + this.dateTo.toString() + " -> " + this.dateFrom.toString();
    }

}
