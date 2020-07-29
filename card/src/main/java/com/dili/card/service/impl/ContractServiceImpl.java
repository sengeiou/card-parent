package com.dili.card.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dili.card.service.IAccountQueryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.dili.card.dao.IFundConsignorDao;
import com.dili.card.dao.IFundContractDao;
import com.dili.card.dto.FundConsignorDto;
import com.dili.card.dto.FundContractQueryDto;
import com.dili.card.dto.FundContractRequestDto;
import com.dili.card.dto.FundContractResponseDto;
import com.dili.card.dto.UserAccountCardQuery;
import com.dili.card.dto.UserAccountCardResponseDto;
import com.dili.card.entity.FundConsignorDo;
import com.dili.card.entity.FundContractDo;
import com.dili.card.exception.CardAppBizException;
import com.dili.card.rpc.resolver.AccountQueryRpcResolver;
import com.dili.card.rpc.resolver.CustomerRpcResolver;
import com.dili.card.rpc.resolver.UidRpcResovler;
import com.dili.card.service.IContractService;
import com.dili.card.type.BizNoType;
import com.dili.card.type.ContractState;
import com.dili.card.util.PageUtils;
import com.dili.customer.sdk.domain.Customer;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.util.DateUtils;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

@Service
public class ContractServiceImpl implements IContractService {

	@Autowired
	private IFundConsignorDao fundConsignorDao;
	@Autowired
	private IFundContractDao contractDao;
	@Autowired
	private UidRpcResovler uidRpcResovler;
	@Autowired
	private CustomerRpcResolver customerRpcResolver;
	@Autowired
	private AccountQueryRpcResolver accountQueryRpcResolver;
	@Autowired
	private IAccountQueryService accountQueryService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void save(FundContractRequestDto fundContractRequest) {
		// 构建合同主体
		FundContractDo fundContract = this.buildContractEntity(fundContractRequest);
		// 构建被委托人主体
		List<FundConsignorDo> consignors = this.buildConsignorEntities(fundContractRequest);
		// 数据保存操作
		contractDao.save(fundContract);
		fundConsignorDao.saveBatch(consignors);
	}

	@Override
	public List<FundContractResponseDto> list(FundContractQueryDto contractQueryDto) {
		// 构建查询条件
		this.buildQueryContractConditon(contractQueryDto);
		// 查询条件
		List<FundContractDo> fundContracts = contractDao.findEntityByCondition(contractQueryDto);
		// 数据转换
		return this.buildPageResponseContracts(fundContracts);
	}

	@Override
	public PageOutput<List<FundContractResponseDto>> page(FundContractQueryDto contractQueryDto) {
		Page<?> page = PageHelper.startPage(contractQueryDto.getPage(), contractQueryDto.getRows());
		List<FundContractResponseDto> contractResponses = this.list(contractQueryDto);
		return PageUtils.convert2PageOutput(page, contractResponses);
	}

	@Override
	public void remove(FundContractRequestDto fundContractRequest) {
		FundContractDo fundContract = contractDao.getById(fundContractRequest.getId());
		if (fundContract == null) {
			throw new CardAppBizException(ResultCode.DATA_ERROR, "该合同号不存在");
		}
		if (ContractState.REMOVED.getCode() == fundContract.getState()) {
			return;
		}
		FundContractDo updateFundContract = new FundContractDo();
		updateFundContract.setId(fundContractRequest.getId());
		updateFundContract.setState(ContractState.REMOVED.getCode());
		UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
		updateFundContract.setTerminater(userTicket.getRealName());
		updateFundContract.setTerminateNotes(fundContractRequest.getNotes());
		contractDao.update(updateFundContract);
	}

	@Override
	public FundContractResponseDto detail(Long id) {
		FundContractDo fundContract = contractDao.getById(id);
		if (fundContract == null) {
			throw new CardAppBizException(ResultCode.DATA_ERROR, "该合同号不存在");
		}
		UserAccountCardResponseDto userAccountCard = accountQueryService.getByAccountId(fundContract.getConsignorAccountId());
		Customer customer = customerRpcResolver.getWithNotNull(userAccountCard.getCustomerId(),
				fundContract.getFirmId());
		return this.buildContractDetail(fundContract, userAccountCard, customer);
	}

	@Override
	public FundContractResponseDto removeToPage(Long id) {
		FundContractDo fundContract = contractDao.getById(id);
		if (fundContract == null) {
			throw new CardAppBizException(ResultCode.DATA_ERROR, "该合同号不存在");
		}
		UserAccountCardResponseDto userAccountCard = accountQueryService.getByAccountId(fundContract.getConsignorAccountId());
		Customer customer = customerRpcResolver.getWithNotNull(userAccountCard.getCustomerId(),
				fundContract.getFirmId());
		return this.buildContractResponse(fundContract, userAccountCard, customer);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void closeOverdueContract() {
		contractDao.closeOverdueContract();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void activeOverdueContract() {
		contractDao.activeOverdueContract();
	}

	/**
	 * 构建查询条件
	 */
	private void buildQueryContractConditon(FundContractQueryDto contractQueryDto) {
		UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
		contractQueryDto.setFirmId(userTicket.getFirmId());
		if (!StringUtils.isBlank(contractQueryDto.getCardNo())) {
			// 构建卡数据
			UserAccountCardResponseDto userAccountCard = accountQueryService.getByCardNo(contractQueryDto.getCardNo());
			contractQueryDto.setConsignorAccountId(userAccountCard.getAccountId());
		}
		// 过期时间构建
		if (contractQueryDto.getDays() != null && contractQueryDto.getDays() > 0) {
			contractQueryDto.setExpirationTime(
					DateUtils.format(DateUtils.addDays(new Date(), contractQueryDto.getDays()), "yyyy-MM-dd"));
		}
	}

	/**
	 * 合同页面信息列表
	 */
	private List<FundContractResponseDto> buildPageResponseContracts(List<FundContractDo> fundContracts) {
		List<FundContractResponseDto> contractResponseDtos = new ArrayList<FundContractResponseDto>();
		if (CollectionUtils.isEmpty(fundContracts)) {
			return contractResponseDtos;
		}
		//账户信息构建
		List<Long> accountIds = fundContracts.stream().map(c -> c.getConsignorAccountId()).collect(Collectors.toList());
		UserAccountCardQuery userAccountCardQuery = new UserAccountCardQuery();
		userAccountCardQuery.setAccountIds(accountIds);
		userAccountCardQuery.setFirmId(fundContracts.get(0).getFirmId());
		Map<Long, UserAccountCardResponseDto> userAccountCardMsp = accountQueryRpcResolver
				.findAccountCardsMapByAccountIds(userAccountCardQuery);
		//客户信息构建
		List<Long> customerIds = fundContracts.stream().map(c -> c.getConsignorCustomerId())
				.collect(Collectors.toList());
		Map<Long, Customer> customerMap = customerRpcResolver.findCustomerMapByCustomerIds(customerIds,
				fundContracts.get(0).getFirmId());
		//合同信息构建
		for (FundContractDo fundContractDo : fundContracts) {
			contractResponseDtos.add(this.buildPageContracts(fundContractDo,
					userAccountCardMsp.get(fundContractDo.getConsignorAccountId()),
					customerMap.get(fundContractDo.getConsignorCustomerId())));
		}
		return contractResponseDtos;
	}

	/**
	 * 合同列表页面信息
	 */
	private FundContractResponseDto buildPageContracts(FundContractDo fundContract,
			UserAccountCardResponseDto accountCard, Customer customer) {
		return this.buildContractResponse(fundContract, accountCard, customer);
	}

	/**
	 * 构建页面合同详情数据
	 */
	private FundContractResponseDto buildContractDetail(FundContractDo fundContract,
			UserAccountCardResponseDto accountCard, Customer customer) {
		return this.buildContractResponse(fundContract, accountCard, customer);
	}

	/**
	 * 构建页面相应数据
	 */
	private FundContractResponseDto buildContractResponse(FundContractDo fundContractDo,
			UserAccountCardResponseDto accountCard, Customer customer) {
		FundContractResponseDto contractResponseDto = new FundContractResponseDto();
		// 构建合同核心数据
		contractResponseDto.setId(fundContractDo.getId());
		contractResponseDto.setContractNo(fundContractDo.getContractNo());
		contractResponseDto.setCreator(fundContractDo.getCreator());
		contractResponseDto.setCreateTime(fundContractDo.getCreateTime());
		contractResponseDto.setStartTime(fundContractDo.getStartTime());
		contractResponseDto.setEndTime(fundContractDo.getEndTime());
		contractResponseDto.setTerminater(fundContractDo.getTerminater());
		contractResponseDto.setTerminateNotes(fundContractDo.getTerminateNotes());
		contractResponseDto.setTerminateTime(fundContractDo.getTerminateTime());
		contractResponseDto.setState(fundContractDo.getState());

		List<FundConsignorDo> consignors = fundConsignorDao.findConsignorsByContractNo(fundContractDo.getContractNo());
		// 列表被委托人信息
		List<FundConsignorDto> consignorDtos = new ArrayList<FundConsignorDto>();
		StringBuilder mobiles = new StringBuilder();
		StringBuilder names = new StringBuilder();
		for (FundConsignorDo fundConsignorDo : consignors) {
			mobiles.append(fundConsignorDo.getConsigneeIdMobile() + "、");
			names.append(fundConsignorDo.getConsigneeName() + "、");
			consignorDtos.add(this.buildConsignorForPage(fundConsignorDo));
		}
		contractResponseDto.setConsigneeNames(names.substring(0, names.lastIndexOf("、")));
		contractResponseDto.setConsigneeMobiles(mobiles.substring(0, mobiles.lastIndexOf("、")));
		contractResponseDto.setConsignorDtos(consignorDtos);
		// 构建卡数据
		contractResponseDto.setConsignorCard(accountCard.getCardNo());
		// 获取客户信息
		contractResponseDto.setConsignorCode(customer.getCode());
		contractResponseDto.setConsignorName(customer.getName());
		return contractResponseDto;
	}

	/**
	 * 构建被委托人信息列表
	 */
	private List<FundConsignorDo> buildConsignorEntities(FundContractRequestDto fundContractRequest) {
		List<FundConsignorDo> consignorDos = new ArrayList<FundConsignorDo>();
		for (FundConsignorDto consignorRequestDto : fundContractRequest.getConsignors()) {
			consignorDos.add(this.buildConsignorForDataBase(consignorRequestDto, fundContractRequest.getContractNo()));
		}
		return consignorDos;
	}

	/**
	 * 构建被委托人信息for data base
	 */
	private FundConsignorDo buildConsignorForDataBase(FundConsignorDto consignorRequestDto, String contractNo) {
		FundConsignorDo fundConsignorDo = new FundConsignorDo();
		// 构建合同被委托人核心数据
		fundConsignorDo.setConsigneeIdCode(consignorRequestDto.getConsigneeIdCode());
		fundConsignorDo.setConsigneeIdMobile(consignorRequestDto.getConsigneeIdMobile());
		fundConsignorDo.setConsigneeName(consignorRequestDto.getConsigneeName());
		fundConsignorDo.setSignatureImagePath(consignorRequestDto.getSignatureImagePath());
		// 构建商户信息
		UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
		fundConsignorDo.setFirmId(userTicket.getFirmId());
		fundConsignorDo.setFirmName(userTicket.getFirmName());
		fundConsignorDo.setContractNo(contractNo);
		return fundConsignorDo;
	}

	/**
	 * 构建被委托人信息for page
	 */
	private FundConsignorDto buildConsignorForPage(FundConsignorDo fundConsignorDo) {
		FundConsignorDto fundConsignorDto = new FundConsignorDto();
		fundConsignorDto.setConsigneeName(fundConsignorDo.getConsigneeName());
		fundConsignorDto.setConsigneeIdMobile(fundConsignorDo.getConsigneeIdMobile());
		fundConsignorDto.setConsigneeIdCode(fundConsignorDo.getConsigneeIdCode());
		fundConsignorDto.setContractNo(fundConsignorDo.getContractNo());
		fundConsignorDto.setId(fundConsignorDo.getId());
		return fundConsignorDto;
	}

	/**
	 * 构建合同主体详情
	 */
	private FundContractDo buildContractEntity(FundContractRequestDto fundContractRequest) {
		FundContractDo fundContractDo = new FundContractDo();
		// 构建合同委托人核心数据
		fundContractDo.setConsignorAccountId(fundContractRequest.getConsignorAccountId());
		if (Timestamp.valueOf(fundContractRequest.getEndTime()).getTime() < Timestamp.valueOf(LocalDateTime.now())
				.getTime()) {
			throw new CardAppBizException(ResultCode.DATA_ERROR, "合同结束时间不低于今天");
		}
		if (Timestamp.valueOf(fundContractRequest.getEndTime()).getTime() < Timestamp
				.valueOf(fundContractRequest.getStartTime()).getTime()) {
			throw new CardAppBizException(ResultCode.DATA_ERROR, "合同结束时间不低于开始时间");
		}
		fundContractDo.setStartTime(fundContractRequest.getStartTime());
		fundContractDo.setEndTime(fundContractRequest.getEndTime());
		fundContractDo.setSignatureImagePath(fundContractRequest.getSignatureImagePath());
		fundContractDo.setNotes(fundContractRequest.getNotes());
		// 构建商户信息
		UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
		fundContractDo.setCreatorId(userTicket.getId());
		fundContractDo.setCreator(userTicket.getRealName());
		fundContractDo.setFirmId(userTicket.getFirmId());
		fundContractDo.setFirmName(userTicket.getFirmName());
		fundContractDo.setState(ContractState.UNSTARTED.getCode());
		if (Timestamp.valueOf(fundContractRequest.getStartTime()).getTime() <= Timestamp.valueOf(LocalDateTime.now())
				.getTime()) {
			fundContractDo.setState(ContractState.ENTUST.getCode());
		}
		// 获取业务编号
		String contractNo = uidRpcResovler.bizNumber(BizNoType.CONTRACT_NO.getCode());
		fundContractDo.setContractNo(contractNo);
		fundContractRequest.setContractNo(contractNo);
		// 获取客户信息
		Customer customer = customerRpcResolver.getWithNotNull(fundContractRequest.getCustomerId(), (userTicket.getFirmId()));
		fundContractDo.setConsignorCustomerCode(customer.getCode());
		fundContractDo.setConsignorCustomerId(customer.getId());
		return fundContractDo;
	}

}
