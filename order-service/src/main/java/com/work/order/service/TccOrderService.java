package com.work.order.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

@LocalTCC
public interface TccOrderService {



    @TwoPhaseBusinessAction(name = "placeOrderTcc", commitMethod = "placeOrderConform", rollbackMethod = "placeOrderCancel")
    void placeOrderTcc(@BusinessActionContextParameter(paramName = "userId")String userId,
                              @BusinessActionContextParameter(paramName = "commodityCode")String commodityCode,
                              @BusinessActionContextParameter(paramName = "count")Integer count,
                                @BusinessActionContextParameter(paramName = "orderNo")String orderNo);



    void placeOrderConform(BusinessActionContext context);


    void placeOrderCancel(BusinessActionContext context);

}
