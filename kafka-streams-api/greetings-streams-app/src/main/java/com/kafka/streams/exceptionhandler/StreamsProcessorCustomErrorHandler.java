package com.kafka.streams.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;

@Slf4j
public class StreamsProcessorCustomErrorHandler implements StreamsUncaughtExceptionHandler {

    @Override
    public StreamThreadExceptionResponse handle(Throwable exception) {
        log.error("Exception in the application : {} ",exception.getMessage(), exception);
        if(exception instanceof StreamsException) {
            if(exception.getCause().getMessage().equals("Transient Error")){
                // return StreamThreadExceptionResponse.REPLACE_THREAD; // replacing the thread will constantly retry that failed message
                return StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
            }
        }
        log.error("Shutdown the client");
         return StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        // return StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
    }
}
