package com.dili.card.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.dili.card.common.serializer.EnumTextDisplayAfterFilter;
import com.dili.card.dto.AccountCycleDto;
import com.dili.card.exception.CardAppBizException;
import com.dili.card.service.IAccountCycleService;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;

/**
 * 账务管理
 */
@Controller
@RequestMapping(value = "/cycle")
public class AccountCycleManagementController {

	@Autowired
	private IAccountCycleService iAccountCycleService;


    /**
     * 列表页面
     */
    @GetMapping("/list.html")
    public String listView() {
        return "cycle/list";
    }

    /**
    * 跳转详情
    * @date 2020/7/6
    */
    @GetMapping("/detail.html/{id}")
    public String detailFacadeView(@PathVariable Long id, ModelMap map) {
    	if (id == null || id < 0L) {
    		 throw new CardAppBizException(ResultCode.PARAMS_ERROR, "账务周期详情请求参数错误");
		}
    	String json = JSON.toJSONString(iAccountCycleService.detail(id),
                new EnumTextDisplayAfterFilter());
        map.put("detail", JSON.parseObject(json));   
        return "cycle/detail";
    }

	/**
	 * 对账
	 */
	@PostMapping("/settle.action")
	@ResponseBody
	public BaseOutput<Boolean> settle(@RequestBody AccountCycleDto accountCycleDto) {
		iAccountCycleService.settle(accountCycleDto.getId());
		return BaseOutput.success();
	}

	/**
	 * 平账
	 */
	@PostMapping("/flated.action")
	@ResponseBody
	public BaseOutput<Boolean> flated(@RequestBody AccountCycleDto accountCycleDto) {
		iAccountCycleService.flated(accountCycleDto.getId());
		return BaseOutput.success();
	}

	/**
	 * 账务列表
	 */
	@PostMapping("/page.action")
	@ResponseBody
	public PageOutput<List<AccountCycleDto>> page(@RequestBody AccountCycleDto accountCycleDto) {
		return iAccountCycleService.page(accountCycleDto);
	}

	/**
	 * 账务详情
	 */
	@PostMapping("/detail.action")
	@ResponseBody
	public BaseOutput<AccountCycleDto> detail(@RequestBody AccountCycleDto accountCycleDto) {
		return BaseOutput.successData(iAccountCycleService.detail(accountCycleDto.getId()));
	}

}
