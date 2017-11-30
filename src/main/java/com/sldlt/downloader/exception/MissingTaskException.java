package com.sldlt.downloader.exception;

public class MissingTaskException extends RuntimeException {

    private static final long serialVersionUID = -6428079534502888446L;

    public MissingTaskException() {
        super("Task does not exist");
    }
}
