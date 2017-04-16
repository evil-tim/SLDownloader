package com.sldlt.downloader.service;

import com.sldlt.downloader.TaskStatus;
import com.sldlt.downloader.dto.TaskDto;

public interface NAVPSTaskExecutorService {

    TaskStatus executeTask(TaskDto task);
}
