package com.developer.payment.controller;

import com.developer.framework.model.DeveloperResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("wallet")
public class WalletController {


    /**
     * 开通钱包
     * @return
     */
    @PostMapping("create")
    public DeveloperResult<Boolean> CreateWallet(){
        return DeveloperResult.success();
    }


}
