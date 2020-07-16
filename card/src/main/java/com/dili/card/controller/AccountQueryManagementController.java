package com.dili.card.controller;

import com.alibaba.fastjson.JSON;
import com.dili.card.common.handler.IControllerHandler;
import com.dili.card.common.serializer.EnumTextDisplayAfterFilter;
import com.dili.card.dto.AccountListResponseDto;
import com.dili.card.dto.AccountSimpleResponseDto;
import com.dili.card.dto.UserAccountCardQuery;
import com.dili.card.dto.UserAccountCardResponseDto;
import com.dili.card.exception.CardAppBizException;
import com.dili.card.service.IAccountQueryService;
import com.dili.card.type.CardType;
import com.dili.card.validator.ConstantValidator;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 卡账户查询
 * @Auther: miaoguoxin
 * @Date: 2020/6/22 10:37
 */
@Controller
@RequestMapping("/accountQuery")
public class AccountQueryManagementController implements IControllerHandler {
    @Autowired
    private IAccountQueryService accountQueryService;

    /**
     * 跳转列表页面
     * @param
     * @return
     * @author miaoguoxin
     * @date 2020/6/22
     */
    @GetMapping("/list.html")
    public String listView() {
        return "accountquery/list";
    }

    /**
     * 跳转卡详情的tab页签入口
     * @param
     * @return
     * @author miaoguoxin
     * @date 2020/6/28
     */
    @GetMapping("/detailTab.html")
    public String detailFacadeView(String cardNo, ModelMap map) {
        if (StringUtils.isBlank(cardNo)) {
            throw new CardAppBizException(ResultCode.PARAMS_ERROR, "卡号不能为空");
        }
        UserAccountCardResponseDto userAccount = accountQueryService.getByCardNoWithReturnState(cardNo);
        map.put("isMaster", CardType.isMaster(userAccount.getCardType()));
        return "accountquery/detailTab";
    }

    /**
     * 跳转卡账户详情页面
     * @author miaoguoxin
     * @date 2020/6/28
     */
    @GetMapping("/accountDetail.html")
    public String accountDetailView(String cardNo, ModelMap map) {
        if (StringUtils.isBlank(cardNo)) {
            throw new CardAppBizException(ResultCode.PARAMS_ERROR, "卡号不能为空");
        }
        String json = JSON.toJSONString(accountQueryService.getDetailByCardNo(cardNo),
                new EnumTextDisplayAfterFilter());
        map.put("detail", JSON.parseObject(json));
        return "accountquery/accountDetail";
    }

    /**
     * 分页查询
     * @author miaoguoxin
     * @date 2020/6/22
     */
    @PostMapping("/page.action")
    @ResponseBody
    public Map<String, Object> page(@Validated(ConstantValidator.Page.class)
                                            UserAccountCardQuery param) {
        this.buildOperatorInfo(param);
        PageOutput<List<AccountListResponseDto>> page = accountQueryService.getPage(param);
        return successPage(page);

    }

    /**
     * 账户信息，包含余额
     * @author miaoguoxin
     * @date 2020/7/7
     */
    @GetMapping("simpleInfo.action")
    @ResponseBody
    public BaseOutput<AccountSimpleResponseDto> getInfoByCardNo(String cardNo) {
        if (StringUtils.isBlank(cardNo)) {
            throw new CardAppBizException(ResultCode.DATA_ERROR, "卡号不能为空");
        }
        return BaseOutput.successData(accountQueryService.getByCardNoWithBalance(cardNo));
    }
}

