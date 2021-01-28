package com.work.account.service;

import com.work.account.entity.Account;
import com.work.account.repository.AccountDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Description：
 *
 * @author fangliangsheng
 * @date 2019-04-05
 */
@Service
public class AccountService {

    private static final String ERROR_USER_ID = "1002";
    @Autowired
    private AccountDAO accountDAO;

    @Transactional(rollbackFor = Exception.class)
    public void reduce(String userId, BigDecimal num) {
        Account account = accountDAO.findByUserId(userId);
        account.setMoney(account.getMoney().subtract(num));
        accountDAO.updateById(account);

        if (ERROR_USER_ID.equals(userId)&&num.compareTo(BigDecimal.ZERO)>0) {
            throw new RuntimeException("account branch exception");
        }
    }
}
