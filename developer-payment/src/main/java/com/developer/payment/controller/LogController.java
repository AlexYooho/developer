package com.developer.payment.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.payment.dto.ModifyRedPacketsMessageStatusRequestDTO;
import com.developer.payment.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("log")
public class LogController {

    @Autowired
    private LogService logService;

    @PostMapping("/modify-red-packets-message-status")
    public DeveloperResult<Boolean> modifyRedPacketsMessageStatus(@RequestBody ModifyRedPacketsMessageStatusRequestDTO req){
        return logService.modifyRedPacketMessageStatus(req.getSerialNo(), req.getMessageStatus());
    }

}
