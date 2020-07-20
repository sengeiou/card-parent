package com.dili.card.service.recharge;

import com.dili.card.dto.FundRequestDto;
import com.dili.card.dto.pay.TradeRequestDto;

/**
 * @Auther: miaoguoxin
 * @Date: 2020/7/2 10:20
 */
public interface IRechargeManager {
    /**
    * 获取充值金额（这里是包含计算手续费和本金）
    * @author miaoguoxin
    * @date 2020/7/6
    */
    Long getRechargeAmount(FundRequestDto requestDto);
    /**
    * 构建交易请求
    * @author miaoguoxin
    * @date 2020/7/2
    */
    TradeRequestDto buildTradeRequest(FundRequestDto requestDto);

}
