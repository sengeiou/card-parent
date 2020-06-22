package com.dili.card.rpc.resolver;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.dili.card.dto.UserAccountCardQuery;
import com.dili.card.dto.UserAccountCardResponseDto;
import com.dili.card.exception.CardAppBizException;
import com.dili.card.rpc.AccountQueryRpc;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;

/**
 * 账户查询解析器
 * @author zhangxing 
 */
@Component
public class AccountQueryRpcResolver {
	
	@Autowired
	private AccountQueryRpc accountQueryRpc;
	
	/**
	 * 通过账号批量查询
	 */
	public List<UserAccountCardResponseDto> findBacthByAccountIds(List<Long> accountIds){
		UserAccountCardQuery userAccountCardQuery = new UserAccountCardQuery();
		userAccountCardQuery.setAccountIds(accountIds);
		BaseOutput<List<UserAccountCardResponseDto>> baseOutput = accountQueryRpc.findUserCards(userAccountCardQuery);
		if (!baseOutput.isSuccess()) {
			throw new CardAppBizException(ResultCode.DATA_ERROR, "远程调用账户服务失败");
		}
		if (CollectionUtils.isEmpty(baseOutput.getData())) {
			throw new CardAppBizException(ResultCode.DATA_ERROR, "卡信息不存在");
		}
		return baseOutput.getData();
	}
	
	/**
	 * 通过账号查询单个账户信息
	 */
	public UserAccountCardResponseDto findByAccountId(Long accountId){
		return this.findBacthByAccountIds(Arrays.asList(accountId)).get(0);
	}
	
	/**
	 * 通过卡号批量查询多个账户信息
	 */
	public List<UserAccountCardResponseDto> findBacthByCardNos(List<String> cardNos){
		UserAccountCardQuery userAccountCardQuery = new UserAccountCardQuery();
		userAccountCardQuery.setCardNos(cardNos);
		BaseOutput<List<UserAccountCardResponseDto>> baseOutput = accountQueryRpc.findUserCards(userAccountCardQuery);
		if (!baseOutput.isSuccess()) {
			throw new CardAppBizException(ResultCode.DATA_ERROR, "远程调用账户服务失败");
		}
		if (CollectionUtils.isEmpty(baseOutput.getData())) {
			throw new CardAppBizException(ResultCode.DATA_ERROR, "卡信息不存在");
		}
		return baseOutput.getData();
	}
	
	/**
	 * 通过卡号查询单个账户信息
	 */
	public UserAccountCardResponseDto findByCardNo(String cardNo){
		return this.findBacthByCardNos(Arrays.asList(cardNo)).get(0);
	}
}