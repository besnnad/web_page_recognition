package org.example.work.crawl;

public class WorkerException extends RuntimeException {
    public WorkerException(String message, Throwable cause){
        super(message, cause);
    }
    public WorkerException(String message){
        super(message);
    }
}
