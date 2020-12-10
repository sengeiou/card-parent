package com.dili.card.service.impl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.dili.card.dao.IAccountCycleDao;
import com.dili.card.dao.IAccountCycleDetailDao;
import com.dili.card.dto.AccountCycleDetailDto;
import com.dili.card.dto.AccountCycleDto;
import com.dili.card.dto.AccountCyclePageListDto;
import com.dili.card.dto.CycleStatistcDto;
import com.dili.card.dto.UserCashDto;
import com.dili.card.entity.AccountCycleDetailDo;
import com.dili.card.entity.AccountCycleDo;
import com.dili.card.entity.UserCashDo;
import com.dili.card.exception.CardAppBizException;
import com.dili.card.service.IAccountCycleService;
import com.dili.card.service.IUserCashService;
import com.dili.card.type.CashAction;
import com.dili.card.type.CycleState;
import com.dili.card.type.CycleStatisticType;
import com.dili.card.util.PageUtils;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.PageOutput;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

@Service("accountCycleService")
public class AccountCycleServiceImpl implements IAccountCycleService {

	private static final Logger log = LoggerFactory.getLogger(AccountCycleServiceImpl.class);

	@Autowired
	private IAccountCycleDao accountCycleDao;
	@Autowired
	private IAccountCycleDetailDao accountCycleDetailDao;
	@Autowired
	private IUserCashService userCashService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public AccountCycleDto settle(AccountCycleDto accountCycleDto) {

		// 获取最新的账务周期
		AccountCycleDo accountCycle = this.findLatestCycleByUserId(accountCycleDto.getUserId());

		// 对账状态校验和交款金额校验
		this.validateCycleSettledState(accountCycle, accountCycleDto);

		// 生成交款信息
		userCashService.save(buildUserCash(accountCycleDto));

		// 更新账务周期状态
		this.updateStateById(accountCycle.getId(), CycleState.SETTLED.getCode(), accountCycle.getVersion());

		accountCycleDto.setEndTime(LocalDateTime.now());
		return accountCycleDto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void flated(Long id, Long auditorId, String auditorName) {

		AccountCycleDo cycle = this.findById(id);

		// 平账状态校验
		this.validateCycleFlatedState(cycle);

		// 账务周期详情统计信息
		AccountCycleDetailDto acd = this.buildCycleDetail(cycle.getCycleNo());
		AccountCycleDetailDo accountCycleDetail = this.buildAccountCycleDetailDo(acd);
		
		accountCycleDetail.setFirmId(cycle.getFirmId());
		accountCycleDetail.setFirmName(cycle.getFirmName());

		// 更新账务周期状态
		this.updateStateById(id, CycleState.FLATED.getCode(), cycle.getVersion(), auditorId, auditorName);

		// 保存账务周期详情信息
		accountCycleDetailDao.save(accountCycleDetail);

		// 更新领取款记录
		userCashService.flatedByCycle(cycle.getCycleNo());
	}

	@Override
	public List<AccountCycleDto> list(AccountCycleDto accountCycleDto) {

		// 封装返回数据
		return this.buildAccountCycleWrapperDetailList(accountCycleDao.findByCondition(accountCycleDto));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public AccountCycleDo findActiveCycleByUserId(Long userId, String userName, String userCode) {
		if (userId == null || StringUtils.isBlank(userName) || StringUtils.isBlank(userCode)) {
			throw new CardAppBizException("查询账务周期参数错误");
		}
		AccountCycleDo accountCycle = this.findLatestCycleByUserId(userId);
		if (accountCycle != null && accountCycle.getState().equals(CycleState.SETTLED.getCode())) {
			throw new CardAppBizException("当前员工账务周期正在对账中,不能操作");
		}
		if (accountCycle == null || accountCycle.getState().equals(CycleState.FLATED.getCode())) {
			accountCycle = AccountCycleDo.Factory.create(userId, userName, userCode);
			accountCycleDao.save(accountCycle);
		}
		return accountCycle;
	}

	@Override
	public PageOutput<List<AccountCyclePageListDto>> page(AccountCycleDto accountCycleDto) {
		Page<?> page = PageHelper.startPage(accountCycleDto.getPage(), accountCycleDto.getRows());
		List<AccountCycleDto> accountCycles = this.list(accountCycleDto);
		return PageUtils.convert2PageOutput(page, buildCyclePageList(accountCycles));
	}

	@Override
	public AccountCycleDo findLatestCycleByUserId(Long userId) {
		return accountCycleDao.findLatestCycleByUserId(userId);
	}

	@Override
	public AccountCycleDto detail(Long id) {
		return this.buildAccountCycleWrapperDetail(accountCycleDao.getById(id), true);
	}

	@Override
	public AccountCycleDto applyDetail(Long userId) {
		AccountCycleDo accountCycleDo = accountCycleDao.findLatestCycleByUserId(userId);
		if (accountCycleDo.getState().equals(CycleState.FLATED.getCode())) {
			AccountCycleDto accountCycleDto = new AccountCycleDto();
			accountCycleDto.setUserCode(accountCycleDo.getUserCode());
			accountCycleDto.setUserId(accountCycleDo.getUserId());
			accountCycleDto.setUserName(accountCycleDo.getUserName());
			accountCycleDto.setAccountCycleDetailDto(new AccountCycleDetailDto());
			return accountCycleDto;
		}
		return this.buildAccountCycleWrapperDetail(accountCycleDo, true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void increaseCashBox(Long cycleNo, Long amount) {
		this.updateCashBox(cycleNo, amount);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void decreaseeCashBox(Long cycleNo, Long amount) {
		this.updateCashBox(cycleNo, -amount);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateCashBox(Long cycleNo, Long amount) {
		if (amount == 0) {
			return;
		}
		AccountCycleDo accountCycleDo = accountCycleDao.findByCycleNo(cycleNo);
		if (!accountCycleDao.updateCashBox(cycleNo, amount, accountCycleDo.getVersion())) {
			throw new CardAppBizException("更新现金柜失败");
		}
	}

	/**
	 * 更新账务周期状态
	 */
	@Override
	public void updateStateById(Long id, Integer state, Integer version, Long auditorId, String auditorName) {
		AccountCycleDo accountCycle = new AccountCycleDo();
		accountCycle.setAuditorId(auditorId);
		accountCycle.setAuditorName(auditorName);
		accountCycle.setId(id);
		accountCycle.setVersion(version);
		accountCycle.setState(state);
		int update = accountCycleDao.update(accountCycle);
		if (update == 0) {
			throw new CardAppBizException("结账失败");
		}
	}

	/**
	 * 更新账务周期状态
	 */
	@Override
	public void updateStateById(Long id, Integer state, Integer version) {
		int update = accountCycleDao.updateStateById(id, state, version);
		if (update == 0) {
			throw new CardAppBizException("结账失败");
		}
	}

	@Override
	public Boolean checkExistActiveCycle(Long userId) {
		AccountCycleDo accountCycleDo = this.findLatestCycleByUserId(userId);
		if (userId == null) {
			return true;
		}
		if (accountCycleDo == null) {
			return true;
		}
		if (accountCycleDo.getState() == CycleState.SETTLED.getCode()) {
			throw new CardAppBizException("当前员工账务周期正在对账中,不能操作");
		}
		return true;
	}

	/**
	 * 获取活跃的账务周期
	 */
	@Override
	public AccountCycleDo findActiveCycleByUserId(Long userId) {
		return accountCycleDao.findByUserIdAndState(userId, CycleState.ACTIVE.getCode());
	}

	@Override
	public Boolean isActiveByCycleNo(Long cycleNo) {
		AccountCycleDto queryDto = new AccountCycleDto();
		queryDto.setCycleNo(cycleNo);
		queryDto.setState(CycleState.ACTIVE.getCode());
		Long count = accountCycleDao.findCountByCondition(queryDto);
		return count != null && count > 0;
	}

	@Override
	public AccountCycleDo findById(Long id) {
		AccountCycleDo accountCycle = accountCycleDao.getById(id);
		if (accountCycle == null) {
			throw new CardAppBizException("账务周期不存在");
		}
		return accountCycle;
	}

	@Override
	public AccountCycleDto buildAccountCycleWrapper(AccountCycleDo cycle) {
		return buildAccountCycleWrapperDetail(cycle, false);
	}
	
	@Override
	public AccountCycleDto buildAccountCycleWrapperDetail(AccountCycleDo cycle, boolean detail) {
		return buildAccountCycleWrapperDetailList(Arrays.asList(cycle), detail).get(0);
	}
	
	@Override
	public List<AccountCycleDto> buildAccountCycleWrapperDetailList(List<AccountCycleDo> cycles) {
		return buildAccountCycleWrapperDetailList(cycles, false);
	}
	
	private List<AccountCycleDto> buildAccountCycleWrapperDetailList(List<AccountCycleDo> cycles, boolean detail) {
		// 构建账务周期实体
		List<AccountCycleDto> accountCycleDtos = buildAccountCycleDtoList(cycles);
		List<Long> cycleNos = accountCycleDtos.stream().map( o -> o.getCycleNo()).collect(Collectors.toList());
		
		Map<Long, List<CycleStatistcDto>> cycleStatistcs = findCycleStatistcDtoMapByCycleNos(accountCycleDetailDao.statisticCycleBussinessRecord(cycleNos));
		Map<Long, List<CycleStatistcDto>> cycleReversedStatistcs = findCycleStatistcDtoMapByCycleNos(accountCycleDetailDao.statisticReverseByCycleNo(cycleNos));
		
		for (AccountCycleDto accountCycleDto : accountCycleDtos) {
			
			//账务周期详情构建
			AccountCycleDetailDto accountCycleBussinessDetail = buildCycleDetail(accountCycleDto.getCycleNo(), cycleStatistcs.get(accountCycleDto.getCycleNo()), cycleReversedStatistcs.get(accountCycleDto.getCycleNo()));
			
			//最后账务周期数据的计算用于展示
			calculateLastCycleDetail(accountCycleBussinessDetail, accountCycleDto, detail);

			accountCycleDto.setAccountCycleDetailDto(accountCycleBussinessDetail);
		}
		return accountCycleDtos;
	}
	
	/**
	 * 账务周期详情构建
	 * @return
	 */
	private AccountCycleDetailDto buildCycleDetail(Long cycleNo) {
		return this.buildCycleDetail(cycleNo,
				accountCycleDetailDao.statisticCycleBussinessRecord(Arrays.asList(cycleNo)),
				accountCycleDetailDao.statisticReverseByCycleNo(Arrays.asList(cycleNo)));
	}
	
	/**
	 * 账务周期详情构建
	 * @return
	 */
	private AccountCycleDetailDto buildCycleDetail(Long cycleNo, List<CycleStatistcDto> cycleStatistcs, List<CycleStatistcDto> cycleReversedStatistcs) {
		// 构建业务产生的金额 账务周期详情 不包括冲正数据
		AccountCycleDetailDto accountCycleBussinessDetail = this.transferStatisticToCycleDetail(cycleNo, cycleStatistcs);

		// 统计详情只包括冲正数据
		AccountCycleDetailDto accountCycleReverseDetail = this.transferStatisticToCycleDetail(cycleNo, cycleReversedStatistcs);

		// 合并账务周期详情统计信息
		mergeAccountCycleDetail(accountCycleBussinessDetail, accountCycleReverseDetail);

		return calculateBussinessCashBalanceAmount(accountCycleBussinessDetail);
	}
	
	/**
	 * 分组
	 */
	public Map<Long, List<CycleStatistcDto>> findCycleStatistcDtoMapByCycleNos(List<CycleStatistcDto> cycleStatistcs) {
        return cycleStatistcs.stream()
                .collect(Collectors.groupingBy(CycleStatistcDto::getCycleNo));
	}

	/**
	 * 最后账务周期数据的计算用于展示
	 */
	private void calculateLastCycleDetail(AccountCycleDetailDto accountCycleBussinessDetail, AccountCycleDto accountCycleDto, boolean detail) {
		// 计算网银存取款
		accountCycleBussinessDetail
				.setInOutBankAmount(accountCycleBussinessDetail.getBankInAmount() - accountCycleBussinessDetail.getBankOutAmount());
		
		// 计算未交现金金额 领款金额 + 现金收支收益金额 - 现金交款金额
		Long unDeliverAmount = accountCycleBussinessDetail.getReceiveAmount() + accountCycleBussinessDetail.getRevenueAmount()
				- accountCycleBussinessDetail.getDeliverAmount();
		
		if (accountCycleDto.getCycleNo() != null || accountCycleDto.getCycleNo() != -1L ) {
			if (CycleState.ACTIVE.getCode() == accountCycleDto.getState()) {// 活跃期
				accountCycleBussinessDetail.setUnDeliverAmount(unDeliverAmount);
			} else {// 非活跃期 最近一次交现金金额就是最终交款金额
				UserCashDo userCashDo = userCashService.getLastestUesrCash(accountCycleDto.getUserId(), accountCycleDto.getCycleNo(),
						CashAction.PAYER.getCode());
				accountCycleBussinessDetail.setLastDeliverAmount(userCashDo.getAmount());
				// 结账后此字段为0
				accountCycleBussinessDetail.setUnDeliverAmount(0L);
				// 详情 平账 和 结账申请 后不统计结账申请交款记录    对账管理列表  交款金额 不统计最后一次交款金额
				if ((detail && CycleState.ACTIVE.getCode() != accountCycleDto.getState()) || !detail) {
					accountCycleBussinessDetail.setDeliverAmount(accountCycleBussinessDetail.getDeliverAmount() - userCashDo.getAmount());
					accountCycleBussinessDetail.setDeliverTimes(accountCycleBussinessDetail.getDeliverTimes() - 1);
				}
			}
		}
		
		// 现金收款包括充值和工本费
		accountCycleBussinessDetail.setDepoCashAmount(accountCycleBussinessDetail.getDepoCashAmount()
				+ accountCycleBussinessDetail.getOpenCostAmount() + accountCycleBussinessDetail.getChangeCostAmount());
		accountCycleBussinessDetail.setDepoCashTimes(accountCycleBussinessDetail.getDepoCashTimes()
				+ accountCycleBussinessDetail.getOpenCostFeetimes() + accountCycleBussinessDetail.getChangeCostFeetimes());
		
	}
	
	/**
	 *计算业务现金收支余额 开卡/换卡工本费+ 现金充值 -现金取款
	 */
	private AccountCycleDetailDto calculateBussinessCashBalanceAmount(AccountCycleDetailDto accountCycleDetail) {
		accountCycleDetail
		.setRevenueAmount(accountCycleDetail.getOpenCostAmount() + accountCycleDetail.getChangeCostAmount()
				+ accountCycleDetail.getDepoCashAmount() - accountCycleDetail.getDrawCashAmount());
		return accountCycleDetail;
	}
	
	
	/**
	 * 业务操作统计金额转化为具体账务详情
	 */
	private AccountCycleDetailDto transferStatisticToCycleDetail(Long cycleNo, List<CycleStatistcDto> cycleStatistcs) {
		AccountCycleDetailDto accountCycleDetail = new AccountCycleDetailDto();
		accountCycleDetail.setCycleNo(cycleNo);
		if (!CollectionUtils.isEmpty(cycleStatistcs)) {
			for (CycleStatistcDto cycleStatistc : cycleStatistcs) {
				CycleStatisticType cycleStatisticType = CycleStatisticType.getCycleStatisticType(cycleStatistc.getType(),
						cycleStatistc.getTradeChannel());
				Field times;
				Field amount;
				try {
					times = accountCycleDetail.getClass().getDeclaredField(cycleStatisticType.getTimes());
					times.setAccessible(true);
					times.set(accountCycleDetail, cycleStatistc.getTimes());
					amount = accountCycleDetail.getClass().getDeclaredField(cycleStatisticType.getAmount());
					amount.setAccessible(true);
					amount.set(accountCycleDetail, cycleStatistc.getAmount() == null ? 0 : cycleStatistc.getAmount());
				} catch (Exception e) {
					log.error("账务周期详情统计出错", e);
				}
			}
		}
		return accountCycleDetail;
	}

	/**
	 * 正向操作数据和冲正数据进行合并
	 */
	private void mergeAccountCycleDetail(AccountCycleDetailDto masterCycleDetail, AccountCycleDetailDto accountCycleReverseDetail) {
		masterCycleDetail.setBeforeReverseBankInAmount(masterCycleDetail.getBankInAmount());
		masterCycleDetail.setBeforeReverseBankOutAmount(masterCycleDetail.getBankOutAmount());
		masterCycleDetail.setBeforeReverseDepoCashAmount(masterCycleDetail.getDepoCashAmount());
		masterCycleDetail.setBeforeReverseDepoPosAmount(masterCycleDetail.getDepoPosAmount());
		masterCycleDetail.setBeforeReverseDrawCashAmount(masterCycleDetail.getDrawCashAmount());
		masterCycleDetail.setBankInAmount(masterCycleDetail.getBankInAmount() + accountCycleReverseDetail.getBankInAmount());
		masterCycleDetail.setBankOutAmount(masterCycleDetail.getBankOutAmount() + accountCycleReverseDetail.getBankOutAmount());
		masterCycleDetail.setDepoCashAmount(masterCycleDetail.getDepoCashAmount() + accountCycleReverseDetail.getDepoCashAmount());
		masterCycleDetail.setDepoPosAmount(masterCycleDetail.getDepoPosAmount() + accountCycleReverseDetail.getDepoPosAmount());
		masterCycleDetail.setDrawCashAmount(masterCycleDetail.getDrawCashAmount() + accountCycleReverseDetail.getDrawCashAmount());
	}


	/**
	 * 对账前状态校验
	 */
	private void validateCycleSettledState(AccountCycleDo accountCycle, AccountCycleDto accountCycleDto) {
		if (accountCycle == null) {
			throw new CardAppBizException("当前没有账务周期");
		}
		if (accountCycleDto.getCashAmount() < 0) {
			throw new CardAppBizException("结账申请交款金额不能小于0元");
		}
		AccountCycleDto aCycleDto = this.buildAccountCycleWrapper(accountCycle);
		if (!accountCycleDto.getCashAmount().equals(aCycleDto.getAccountCycleDetailDto().getUnDeliverAmount())) {
			throw new CardAppBizException("交款金额不等于现金余额");
		}
		if (accountCycle.getState() == CycleState.SETTLED.getCode()) {
			throw new CardAppBizException("当前账务周期已结账,不能重复操作");
		}
		if (accountCycle.getState() == CycleState.FLATED.getCode()) {
			throw new CardAppBizException("当前账务周期已平账,不能进行结账操作");
		}
	}

	/**
	 * 平账前状态校验
	 */
	private void validateCycleFlatedState(AccountCycleDo accountCycle) {
		if (accountCycle.getState() == CycleState.ACTIVE.getCode()) {
			throw new CardAppBizException(ResultCode.DATA_ERROR, "当前账务周期未结账,不能进行此操作");
		}
		if (accountCycle.getState() == CycleState.FLATED.getCode()) {
			throw new CardAppBizException(ResultCode.DATA_ERROR, "当前账务周期已平账,不能重复操作");
		}
	}

	/**
	 * 构建账务周期相应实体
	 */
	private AccountCycleDto buildAccountCycleDto(AccountCycleDo cycle) {
		AccountCycleDto accountCycleDto = new AccountCycleDto();
		if (cycle == null) {
			UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
			accountCycleDto.setUserCode(userTicket.getSerialNumber());
			accountCycleDto.setUserId(userTicket.getId());
			accountCycleDto.setUserName(userTicket.getRealName());
			accountCycleDto.setCycleNo(-1L);
			return accountCycleDto;
		}
		accountCycleDto.setId(cycle.getId());
		accountCycleDto.setUserCode(cycle.getUserCode());
		accountCycleDto.setUserId(cycle.getUserId());
		accountCycleDto.setUserName(cycle.getUserName());
		accountCycleDto.setCycleNo(cycle.getCycleNo() == null ? -1L : cycle.getCycleNo());
		accountCycleDto.setCashBox(cycle.getCashBox());
		accountCycleDto.setStartTime(cycle.getStartTime());
		accountCycleDto.setEndTime(cycle.getEndTime());
		accountCycleDto.setState(cycle.getState());
		return accountCycleDto;
	}
	
	/**
	 * 构建账务周期相应实体
	 */
	private List<AccountCycleDto> buildAccountCycleDtoList(List<AccountCycleDo> cycles) {
		List<AccountCycleDto> accountCycleDtos = new ArrayList<AccountCycleDto>();
		for (AccountCycleDo cycle : cycles) {
			accountCycleDtos.add(buildAccountCycleDto(cycle));
		}
		return accountCycleDtos;
	}

	/**
	 * 构建账务周期详情相应实体从dto复制数据到do
	 */
	private AccountCycleDetailDo buildAccountCycleDetailDo(AccountCycleDetailDto accountCycleDetail) {
		AccountCycleDetailDo accountCycleDetailDo = new AccountCycleDetailDo();
		// 数据复制
		BeanUtils.copyProperties(accountCycleDetail, accountCycleDetailDo);
		return accountCycleDetailDo;
	}

	/**
	 * 生成交款信息
	 */
	private UserCashDto buildUserCash(AccountCycleDto accountCycleDto) {
		// 校验结账交款余额
		UserCashDto cashDto = new UserCashDto();
		cashDto.setUserId(accountCycleDto.getUserId());
		cashDto.setUserCode(accountCycleDto.getUserCode());
		cashDto.setUserName(accountCycleDto.getUserName());
		cashDto.setAmount(accountCycleDto.getCashAmount());
		cashDto.setAction(CashAction.PAYER.getCode());
		cashDto.setSettledApply(true);
		cashDto.setNotes("结账交款记录");
		cashDto.setSettled(false);
		return cashDto;
	}

	/**
	 * 账务周期列表数据
	 */
	private List<AccountCyclePageListDto> buildCyclePageList(List<AccountCycleDto> accountCycles) {
		List<AccountCyclePageListDto> accountCyclePageListDtos = new ArrayList<AccountCyclePageListDto>();
		if (CollectionUtils.isEmpty(accountCycles)) {
			return accountCyclePageListDtos;
		}
		for (AccountCycleDto accountCycleDto : accountCycles) {
			AccountCyclePageListDto accountCyclePageListDto = new AccountCyclePageListDto();
			accountCyclePageListDto.setId(accountCycleDto.getId());
			accountCyclePageListDto.setUserId(accountCycleDto.getUserId());
			accountCyclePageListDto.setUserCode(accountCycleDto.getUserCode());
			accountCyclePageListDto.setUserName(accountCycleDto.getUserName());
			accountCyclePageListDto.setCycleNo(accountCycleDto.getCycleNo().toString());
			accountCyclePageListDto.setState(accountCycleDto.getState());
			accountCyclePageListDto.setReceiveAmount(accountCycleDto.getAccountCycleDetailDto().getReceiveAmount());
			accountCyclePageListDto.setRevenueAmount(accountCycleDto.getAccountCycleDetailDto().getRevenueAmount());
			accountCyclePageListDto.setDepoPosAmount(accountCycleDto.getAccountCycleDetailDto().getDepoPosAmount());
			accountCyclePageListDto.setInOutBankAmount(accountCycleDto.getAccountCycleDetailDto().getInOutBankAmount());
			accountCyclePageListDto.setDeliverAmount(accountCycleDto.getAccountCycleDetailDto().getDeliverAmount());
			accountCyclePageListDto.setUnDeliverAmount(accountCycleDto.getAccountCycleDetailDto().getUnDeliverAmount());
			accountCyclePageListDto.setBankInAmount(accountCycleDto.getAccountCycleDetailDto().getBankInAmount());
			accountCyclePageListDto.setBankOutAmount(accountCycleDto.getAccountCycleDetailDto().getBankOutAmount());
			accountCyclePageListDto
					.setLastDeliverAmount(accountCycleDto.getAccountCycleDetailDto().getLastDeliverAmount());
			accountCyclePageListDtos.add(accountCyclePageListDto);
		}
		return accountCyclePageListDtos;
	}

}
