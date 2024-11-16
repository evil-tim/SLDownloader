package com.sldlt.downloader.entity;

import static com.sldlt.downloader.TaskStatus.PENDING;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sldlt.downloader.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(indexes = { @Index(name = "idx_task_fund_date_from_date_to", columnList = "fund, dateFrom, dateTo"),
    @Index(name = "idx_task_status_attempt_next", columnList = "status, attempts, nextAttemptAfter") })
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateFrom;

    @Column(nullable = false)
    private LocalDate dateTo;

    @Column(nullable = false)
    private String fund;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status = PENDING;

    @Column(nullable = false)
    private int attempts = 0;

    @Column
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

}
