package com.dili.card.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.dili.card.dao.IBindETCDao;
import com.dili.card.dto.ETCQueryDto;
import com.dili.card.dto.ETCRequestDto;
import com.dili.card.dto.ETCResponseDto;
import com.dili.card.dto.UserAccountCardResponseDto;
import com.dili.card.entity.BindETCDo;
import com.dili.card.exception.CardAppBizException;
import com.dili.card.rpc.resolver.CustomerRpcResolver;
import com.dili.card.service.IAccountQueryService;
import com.dili.card.service.IETCService;
import com.dili.card.type.CardStatus;
import com.dili.card.type.CardType;
import com.dili.card.type.DisableState;
import com.dili.card.util.PageUtils;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.ss.domain.PageOutput;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Auther: miaoguoxin
 * @Date: 2021/4/26 17:12
 * @Description:
 */
@Service
public class ETCService implements IETCService {
    @Autowired
    private IAccountQueryService accountQueryService;
    @Autowired
    private IBindETCDao bindETCDao;
    @Autowired
    private CustomerRpcResolver customerRpcResolver;

    @Override
    public String bind(ETCRequestDto requestDto) {
        //绑定车牌是挂在账号名下，因此这里要以账号为基准
        UserAccountCardResponseDto responseDto = this.getAndValidateAccountCard(requestDto);

        //流程见doc/etc绑定流程.jpg
        BindETCDo saveDo = this.doBind(requestDto, responseDto);

        //TODO 先查询是否开通免密，再向支付发起免密协议申请

        saveDo.setLicenseNo("123");
        if (saveDo.getId() != null) {
            bindETCDao.updateById(saveDo);
        } else {
            bindETCDao.insert(saveDo);
        }
        return saveDo.getLicenseNo();
    }


    @Override
    public void unBind(ETCRequestDto requestDto) {
        this.getAndValidateAccountCard(requestDto);
        BindETCDo bindETCDo = bindETCDao.findById(requestDto.getId());
        if (bindETCDo == null) {
            throw new CardAppBizException("绑定记录不存在");
        }
        this.doUnbind(bindETCDo, requestDto.getOpId(), requestDto.getOpName());
        bindETCDao.updateById(bindETCDo);
    }

    @Override
    public PageOutput<List<ETCResponseDto>> getPage(ETCQueryDto queryDto) {
        Page<?> page = PageHelper.startPage(queryDto.getPage(), queryDto.getRows());
        List<BindETCDo> list = bindETCDao.findByCondition(queryDto);
        return PageUtils.convert2PageOutput(page, this.convert2RespDto(list));
    }

    private BindETCDo doBind(ETCRequestDto requestDto, UserAccountCardResponseDto responseDto) {
        BindETCDo queryDo = bindETCDao.findByPlateNo(requestDto.getPlateNo(), requestDto.getFirmId());
        BindETCDo saveDo;
        if (queryDo == null) {
            saveDo = this.createETCDo(requestDto, responseDto);
        } else {
            if (responseDto.getAccountId().equals(queryDo.getAccountId())) {
                if (queryDo.getState() == 1) {
                    throw new CardAppBizException("该车牌号已被绑定，请勿重复操作");
                }
            } else {
                UserAccountCardResponseDto userAccountCard = accountQueryService.getByAccountIdWithoutValidate(queryDo.getAccountId());
                if (CardStatus.RETURNED.getCode() != userAccountCard.getCardState()
                        && DisableState.ENABLED.getCode().equals(userAccountCard.getDisabledState())
                        && queryDo.getState() == 1) {
                    throw new CardAppBizException(String.format("该车牌号已被卡号【%s】绑定", queryDo.getCardNo()));
                }
            }
            saveDo = this.updateETCDo(queryDo, requestDto, responseDto);
        }
        return saveDo;
    }

    private void doUnbind(BindETCDo bindETCDo, Long operatorId, String operatorName) {
        bindETCDo.setOperatorId(operatorId);
        bindETCDo.setOperatorName(operatorName);
        bindETCDo.setState(0);
        bindETCDo.setModifyTime(LocalDateTime.now());
    }

    private UserAccountCardResponseDto getAndValidateAccountCard(ETCRequestDto requestDto) {
        UserAccountCardResponseDto responseDto = accountQueryService.getByAccountId(requestDto.getAccountId());
        if (!CardType.isMaster(responseDto.getCardType())) {
            throw new CardAppBizException("请使用主卡进行操作");
        }
        if (!requestDto.getAccountId().equals(responseDto.getAccountId())
                || !requestDto.getCardNo().equals(responseDto.getCardNo())) {
            throw new CardAppBizException("绑定信息不匹配");
        }
        return responseDto;
    }

    private BindETCDo updateETCDo(BindETCDo bindETCDo, ETCRequestDto requestDto, UserAccountCardResponseDto responseDto) {
        bindETCDo.setAccountId(responseDto.getAccountId());
        bindETCDo.setCustomerId(responseDto.getCustomerId());
        bindETCDo.setCardNo(responseDto.getCardNo());
        bindETCDo.setHoldName(responseDto.getHoldName());

        bindETCDo.setDescription(requestDto.getDescription());
        bindETCDo.setState(1);

        bindETCDo.setOperatorId(requestDto.getOpId());
        bindETCDo.setOperatorName(requestDto.getOpName());
        bindETCDo.setCreateTime(LocalDateTime.now());
        bindETCDo.setModifyTime(LocalDateTime.now());
        return bindETCDo;
    }

    private BindETCDo createETCDo(ETCRequestDto requestDto, UserAccountCardResponseDto responseDto) {
        BindETCDo bindETCDo = new BindETCDo();
        bindETCDo.setAccountId(responseDto.getAccountId());
        bindETCDo.setCustomerId(responseDto.getCustomerId());
        bindETCDo.setCardNo(responseDto.getCardNo());
        bindETCDo.setHoldName(responseDto.getHoldName());

        bindETCDo.setPlateNo(requestDto.getPlateNo());
        bindETCDo.setDescription(requestDto.getDescription());
        bindETCDo.setState(1);

        bindETCDo.setOperatorId(requestDto.getOpId());
        bindETCDo.setOperatorName(requestDto.getOpName());
        bindETCDo.setFirmId(requestDto.getFirmId());
        bindETCDo.setFirmName(requestDto.getFirmName());
        return bindETCDo;
    }

    private List<ETCResponseDto> convert2RespDto(List<BindETCDo> bindETCDos) {
        if (CollectionUtil.isEmpty(bindETCDos)) {
            return new ArrayList<>();
        }
        List<Long> customerIds = bindETCDos.stream().map(BindETCDo::getCustomerId).collect(Collectors.toList());
        Map<Long, CustomerExtendDto> customerMap = customerRpcResolver.findCustomerMapByCustomerIds(customerIds, bindETCDos.get(0).getFirmId());
        return bindETCDos.stream().map(etc -> {
            ETCResponseDto responseDto = new ETCResponseDto();
            BeanUtils.copyProperties(etc, responseDto);
            CustomerExtendDto customerExtendDto = customerMap.getOrDefault(etc.getCustomerId(), new CustomerExtendDto());
            responseDto.setCustomerName(customerExtendDto.getName());
            return responseDto;
        }).collect(Collectors.toList());
    }
}
