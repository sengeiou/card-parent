package com.dili.card.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dili.card.dao.IBindBankCardDao;
import com.dili.card.dto.BindBankCardDto;
import com.dili.card.entity.BindBankCardDo;
import com.dili.card.service.IBindBankCardService;
import com.dili.card.type.BindBankStatus;
import com.dili.card.util.PageUtils;
import com.dili.ss.domain.PageOutput;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

/**
 * @description： 卡片入库相关功能实现
 *
 * @author ：WangBo
 * @time ：2020年7月17日上午10:21:03
 */
@Service
public class BindBankCardServiceImpl implements IBindBankCardService {
	@Autowired
	private IBindBankCardDao bankCardDao;
	
	@Override
	public PageOutput<List<BindBankCardDto>> list(BindBankCardDto queryParam) {
		if(StringUtils.isBlank(queryParam.getSort())) {
			queryParam.setSort("created_time");
			queryParam.setOrder("DESC");
		}
		Page<Object> startPage = PageHelper.startPage(queryParam.getPage(), queryParam.getRows());
		List<BindBankCardDto> selectList = bankCardDao.selectList(queryParam);
		return PageUtils.convert2PageOutput(startPage, selectList);
	}

	@Override
	public boolean addBind(BindBankCardDto newDataDto) {
		BindBankCardDo newData = new BindBankCardDo();
		BeanUtils.copyProperties(newDataDto, newData);
		newDataDto.setCreatedTime(LocalDateTime.now());
		bankCardDao.save(newData);
		return true;
	}

	@Override
	public boolean unBind(BindBankCardDto data) {
		BindBankCardDo unBind = new BindBankCardDo();
		unBind.setId(data.getId());
		unBind.setStatus(BindBankStatus.INVALID.getCode());
		unBind.setModifiedTime(LocalDateTime.now());
		bankCardDao.update(unBind);
		return true;
	}

}
