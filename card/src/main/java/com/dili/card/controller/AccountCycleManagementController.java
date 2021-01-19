package com.dili.card.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dili.card.common.handler.IControllerHandler;
import com.dili.card.common.serializer.EnumTextDisplayAfterFilter;
import com.dili.card.dto.AccountCycleDto;
import com.dili.card.dto.BusinessRecordResponseDto;
import com.dili.card.dto.SerialQueryDto;
import com.dili.card.exception.CardAppBizException;
import com.dili.card.service.IAccountCycleService;
import com.dili.card.service.ISerialService;
import com.dili.card.type.CashAction;
import com.dili.card.type.CycleState;
import com.dili.card.validator.ConstantValidator;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;

/**
 * 账务管理
 */
@Controller
@RequestMapping(value = "/cycle")
public class AccountCycleManagementController implements IControllerHandler {
	
	private static final Logger log = LoggerFactory.getLogger(AccountCycleManagementController.class);

	
	@Autowired
	private IAccountCycleService iAccountCycleService;
	@Resource
    private ISerialService serialService;

	/**
	 * 列表页面
	 */
	@GetMapping("/list.html")
	public String listView(ModelMap map) {
		map.put("state", CycleState.SETTLED.getCode());
		return "cycle/list";
	}

	/**
	 * 跳转详情
	 *
	 * @date 2020/7/6
	 */
	@GetMapping("/detail.html")
	public String detailFacadeView(@RequestParam("id") Long id, ModelMap map) {
		if (id == null || id < 0L) {
			throw new CardAppBizException(ResultCode.PARAMS_ERROR, "账务周期详情请求参数错误");
		}
		String json = JSON.toJSONString(iAccountCycleService.detail(id), new EnumTextDisplayAfterFilter());
		map.put("detail", JSON.parseObject(json));
		map.put("settled", CycleState.SETTLED.getCode());
		return "cycle/detail";
	}
	
    /**
     * 跳转到操作流水页面
     * @return
     */
    @RequestMapping(value = "/serialTab.html")
    public String serialTab() {
        return "cycle/serialTab";
    }

	/**
	 * 跳转结账申请
	 *
	 * @date 2020/7/6
	 */
	@GetMapping("/applyDetail.html")
	public String applyDetail(ModelMap map) {
		UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
		AccountCycleDto accountCycleDto = iAccountCycleService.applyDetail(userTicket.getId());
		String json = JSON.toJSONString(accountCycleDto, new EnumTextDisplayAfterFilter());
		map.put("detail", JSON.parseObject(json));
		map.put("settled", CycleState.ACTIVE.getCode());
		return "cycle/detail";
	}

	/**
	 * 跳转平账页面
	 *
	 * @date 2020/7/6
	 */
	@GetMapping("/flated.html")
	public String flatedHtml(Long id, ModelMap map) {
		if (id == null || id < 0L) {
			throw new CardAppBizException(ResultCode.PARAMS_ERROR, "账务周期对账请求参数错误");
		}
		map.put("detail", iAccountCycleService.findById(id));
		return "cycle/flated";
	}

	/**
	 * 跳转发起交款页面
	 *
	 * @date 2020/7/6
	 */
	@GetMapping("/addPayer.html")
	public String addPayer(Long id, ModelMap map) {
		if (id == null || id < 0L) {
			throw new CardAppBizException(ResultCode.PARAMS_ERROR, "账务周期发起交款请求参数错误");
		}
		map.put("detail", iAccountCycleService.findById(id));
		map.put("action", CashAction.PAYER.getCode());
		map.put("actionText", CashAction.PAYER.getName());
		return "cycle/addPayer";
	}

	/**
	 * 结账申请对账
	 */
	@PostMapping("/applySettle.action")
	@ResponseBody
	public BaseOutput<AccountCycleDto> applySettle(@RequestBody @Validated(value = {ConstantValidator.Update.class}) AccountCycleDto accountCycleDto) {
		log.info("结账申请对账*****{}", JSONObject.toJSONString(accountCycleDto));
		return BaseOutput.successData(iAccountCycleService.settle(accountCycleDto));
	}

	/**
	 * 平账
	 */
	@PostMapping("/flated.action")
	@ResponseBody
	public BaseOutput<Boolean> flated(@RequestBody @Validated(value = {ConstantValidator.Default.class}) AccountCycleDto accountCycleDto) {
		log.info("平账*****{}", JSONObject.toJSONString(accountCycleDto));
		// 构建商户相关信息
		UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
		if (userTicket == null) {
			throw new CardAppBizException(ResultCode.PARAMS_ERROR, "登录过期,请重新登录");
		}
		iAccountCycleService.flated(accountCycleDto.getId(), userTicket.getFirmId(), userTicket.getFirmName());
		return BaseOutput.success("平账成功！");
	}

	/**
	 * 账务列表
	 */
	@PostMapping("/page.action")
	@ResponseBody
	public Map<String,Object> page(@RequestBody @Validated(ConstantValidator.Page.class) AccountCycleDto accountCycleDto) {
		log.info("对账管理列表查询*****{}", JSONObject.toJSONString(accountCycleDto));
		UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
		if(userTicket == null) {
			throw new CardAppBizException("登录过期，请重新登录");
		}
		accountCycleDto.setFirmId(userTicket.getFirmId());
		return successPage(iAccountCycleService.page(accountCycleDto));
	}

	/**
	 * 账务详情
	 */
	@PostMapping("/detail.action")
	@ResponseBody
	public BaseOutput<AccountCycleDto> detail(@RequestBody @Validated(value = {ConstantValidator.Default.class}) AccountCycleDto accountCycleDto) {
		return BaseOutput.successData(iAccountCycleService.detail(accountCycleDto.getId()));
	}
	
	/**
	 * 校验是否存在活跃的账务周期
	 */
	@PostMapping("/checkExistActiveCycle.action")
	@ResponseBody
	public BaseOutput<Boolean> checkExistActiveCycle(@RequestBody AccountCycleDto accountCycleDto) {
		log.info("校验是否存在活跃的账务周期*****{}", JSONObject.toJSONString(accountCycleDto));
		return BaseOutput.successData(iAccountCycleService.checkExistActiveCycle(accountCycleDto.getUserId()));
	}
	
	/**
	 * 冲正记录
	 */
	@PostMapping("/reverse/page.action")
	@ResponseBody
	public Map<String, Object> businessPage(SerialQueryDto queryDto) {
		log.info("冲正记录分页*****{}", JSONObject.toJSONString(queryDto));
		queryDto.setOperateTypeList(Arrays.asList(31));
		PageOutput<List<BusinessRecordResponseDto>> lists = serialService.queryPage(queryDto);
		return successPage(lists);
	}
	
	/**
	 * 打印
	 */
	@PostMapping("/print.action")
	@ResponseBody
	public BaseOutput<AccountCycleDto> print(@RequestBody AccountCycleDto accountCycleDto) {
		log.info("结帐申请打印*****{}", JSONObject.toJSONString(accountCycleDto));
		return BaseOutput.successData(iAccountCycleService.detail(accountCycleDto.getId()));
	}
	
}
