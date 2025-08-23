package com.sldlt.mcp.tool.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public class McpPage<T> {

    private List<T> content;
    private int pageNumber;
    private int totalPages;
    private int pageSize;
    private long totalElements;

    public McpPage(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / (double) pageSize);
    }

    public McpPage(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
    }

    public List<T> getContent() {
        return content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

}
