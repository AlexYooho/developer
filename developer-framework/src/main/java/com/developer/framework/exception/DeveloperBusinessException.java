package com.developer.framework.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

public class DeveloperBusinessException extends RuntimeException{

    private final String serialNo;

    public DeveloperBusinessException(String serialNo,String message){
        super(message);
        this.serialNo = serialNo;
    }

    public String getSerialNo() {
        return serialNo;
    }
}
