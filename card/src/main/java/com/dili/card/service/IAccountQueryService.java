package com.dili.card.service;

import com.dili.card.dto.AccountDetailResponseDto;
import com.dili.card.dto.AccountListResponseDto;
import com.dili.card.dto.UserAccountCardQuery;
import com.dili.card.dto.UserAccountCardResponseDto;
import com.dili.ss.domain.PageOutput;

import java.util.List;

/**
 * @Auther: miaoguoxin
 * @Date: 2020/6/22 13:59
 * @Description:
 */
public interface IAccountQueryService {

    /**
     * 分页多条件查询
     * @param
     * @return
     * @author miaoguoxin
     * @date 2020/6/22
     */
    PageOutput<List<AccountListResponseDto>> getPage(UserAccountCardQuery param);

    /**
     * 根据卡号查询详情
     * @author miaoguoxin
     * @date 2020/6/28
     */
    AccountDetailResponseDto getDetailByCardNo(String cardNo);

    /**
    * 根据卡号查询（只查询卡账户信息）
    * @param
    * @return
    * @author miaoguoxin
    * @date 2020/7/2
    */
    UserAccountCardResponseDto getByCardNo(String cardNo);
}
