package com.work.order.service;


import com.work.order.feign.AccountFeignClient;
import com.work.order.feign.StorageFeignClient;
import com.work.order.model.Order;
import com.work.order.repository.OrderDAO;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class TccOrderServiceImpl implements TccOrderService{

    @Resource
    private AccountFeignClient accountFeignClient;
    @Resource
    private StorageFeignClient storageFeignClient;
    @Resource
    private OrderDAO orderDAO;



    @Override
    public void placeOrderTcc(String userId,
                              String commodityCode,
                              Integer count,String orderNo) {
        //下单
        BigDecimal orderMoney = new BigDecimal(count).multiply(new BigDecimal(5));
        Order order = new Order()
                .setUserId(userId)
                .setCommodityCode(commodityCode)
                .setCount(count)
                .setOrderNo(orderNo)
                .setMoney(orderMoney);
        orderDAO.insert(order);

        //扣钱
        accountFeignClient.reduce(userId, orderMoney);
    }

    @Override
    public void placeOrderConform(BusinessActionContext context) {
        //减库存
        Map<String, Object> actionContext = context.getActionContext();
        String commodityCode= (String) actionContext.get("commodityCode");
        Integer count= (Integer) actionContext.get("count");
        String orderNo= (String) actionContext.get("orderNo");
        storageFeignClient.deduct(commodityCode, count);

        //改订单状态
        Order order = orderDAO.findByOrderNo(orderNo);
        order.setStatus(1);
        orderDAO.updateById(order);

    }
    @Override
    public void placeOrderCancel(BusinessActionContext context) {
        Map<String, Object> actionContext = context.getActionContext();
        String orderNo= (String) actionContext.get("orderNo");
        //改订单状态
        Order order = orderDAO.findByOrderNo(orderNo);
        order.setStatus(2);
        orderDAO.updateById(order);
    }
}
