package com.work.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.work.account.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * Description：
 *
 * @author fangliangsheng
 * @date 2019/3/28
 */
@Mapper
@Repository
public interface AccountDAO extends BaseMapper<Account> {

    @Select("select * from account_tbl where user_id=#{userId}")
    Account findByUserId(String userId);
}
