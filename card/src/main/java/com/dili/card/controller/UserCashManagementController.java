package com.dili.card.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dili.card.dto.UserCashDto;
import com.dili.card.service.IUserCashService;
import com.dili.card.type.CashAction;
import com.dili.card.validator.ConstantValidator;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;

/**
 * 现金管理
 */
@Controller
@RequestMapping(value = "/cash")
public class UserCashManagementController {
	
	@Autowired
	private IUserCashService iUserCashService;
	
    /**
     * 领款列表页面
     */
    @GetMapping("/payeeList.html")
    public String payeeList() {
        return "usercash/payeeList";
    }
    
    /**
     * 交款列表页面
     */
    @GetMapping("/payerList.html")
    public String payerList() {
        return "usercash/payerList";
    }
    
    /**
     * 领款新增
     */
    @GetMapping("/addPayee.html")
    public String addPayee(ModelMap modelMap) {
    	UserCashDto userCashDto = new UserCashDto();
    	userCashDto.setAction(CashAction.PAYEE.getCode());
    	modelMap.put("usercash", userCashDto);
        return "usercash/add";
    }
    
    /**
     * 取款新增iUserCashService.detail(userCashDto.getId())
     */
    @GetMapping("/addPayer.html")
    public String addPayer(ModelMap modelMap) {
    	UserCashDto userCashDto = new UserCashDto();
    	userCashDto.setAction(CashAction.PAYER.getCode());
    	modelMap.put("usercash", userCashDto);
        return "usercash/add";
    }
    
    /**
     * 修改数据页面
     */
    @GetMapping("/modify.html/{id}")
    public String modify(@PathVariable Long id, ModelMap modelMap) {
    	modelMap.put("usercash", iUserCashService.detail(id));
        return "usercash/modify";
    }
	
	/**
	 * 新增领款
	 */
	@PostMapping("/save.action")
	@ResponseBody
	public BaseOutput<Boolean> savePayee(@RequestBody @Validated(value = ConstantValidator.Insert.class)UserCashDto userCashDto) {
		iUserCashService.save(userCashDto);
		return BaseOutput.success();
	}
	
	/**
	 * 列表领款
	 */
	@PostMapping("/payeeList.action")
	@ResponseBody
	public PageOutput<List<UserCashDto>> listPayee(@RequestBody UserCashDto userCashDto) {
		return iUserCashService.listPayee(userCashDto);
	}
	
	/**
	 * 列表交款
	 */
	@PostMapping("/payerList.action")
	@ResponseBody
	public PageOutput<List<UserCashDto>> listPayer(@RequestBody UserCashDto userCashDto) {
		return iUserCashService.listPayer(userCashDto);
	}
	
	
	/**
	 * 删除领款
	 */
	@PostMapping("/delete.action")
	@ResponseBody
	public BaseOutput<Boolean> delete(@RequestBody UserCashDto userCashDto) {
		iUserCashService.delete(userCashDto.getId());
		return BaseOutput.success();
	}
	
	/**
	 * 修改
	 */
	@PostMapping("/modify.action")
	@ResponseBody
	public BaseOutput<UserCashDto> modify(@RequestBody @Validated(value = ConstantValidator.Update.class) UserCashDto userCashDto) {
		iUserCashService.modify(userCashDto);
		return BaseOutput.success();
	}
	
}
