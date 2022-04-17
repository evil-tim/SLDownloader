package com.sldlt.downloader.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sldlt.downloader.TaskStatus;

public class TaskDto implements Comparable<TaskDto> {

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
        return "Download NAVPS task - " + fund + " - " + this.id + " - " + this.dateFrom.toString() + " -> " + this.dateTo.toString();
    }

    @Override
    public int compareTo(TaskDto task) {
        long ownId = Optional.ofNullable(id).map(Long::longValue).orElse(0L);
        long otherId = Optional.ofNullable(task).map(TaskDto::getId).map(Long::longValue).orElse(0L);

        if (ownId == otherId) {
            return 0;
        }
        if (ownId > otherId) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TaskDto)) {
            return false;
        }

        final TaskDto task = (TaskDto) object;
        long ownId = Optional.ofNullable(id).map(Long::longValue).orElse(0L);
        long otherId = Optional.ofNullable(task).map(TaskDto::getId).map(Long::longValue).orElse(0L);

        return ownId == otherId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(Optional.ofNullable(id).orElse(0L));
    }

}
