package com.trackwize.cloud.notification.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
public class TrackWizeException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;
    private final String messageCode;

    /**
     * Constructs a new KWAPException with the specified message code and message.
     *
     * @param messageCode the code representing the specific error
     * @param message     the detail message explaining the error
     */
    public TrackWizeException(String messageCode, String message) {
        super(message);
        this.messageCode = messageCode;
    }

    /**
     * Constructs a new KWAPException with the specified message.
     *
     * @param message the detail message explaining the error
     */
    public TrackWizeException(String message) {
        super(message);
        this.messageCode = null;
    }
}
