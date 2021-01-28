package com.work.account.controller;

import com.work.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Description：
 *
 * @author fangliangsheng
 * @date 2017/12/25
 */
@RestController
public class AccountController {

    @Autowired
    private AccountService accountService;

    @RequestMapping("/reduce")
    public Boolean reduce(String userId, BigDecimal money) {
        accountService.reduce(userId, money);

        return true;
    }
}
