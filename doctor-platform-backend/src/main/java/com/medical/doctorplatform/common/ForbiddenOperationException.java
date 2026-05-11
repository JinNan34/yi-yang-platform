package com.medical.doctorplatform.common;

public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }

    public ForbiddenOperationException() {
        super("无权限执行该操作");
    }
}
