package com.developer.framework.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class RemoteInvokeException extends RuntimeException{

    private Integer code;

    private String errMsg;

}
