package com.dili.card.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 柜台业务办理记录
 * @author bob
 */
public class BusinessRecordDo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**  */
	private Long id; 
	/** 流水号 */
	private String serialNo; 
	/** 账务周期号 */
	private Long cycleNo; 
	/** 业务类型-办卡、充值、提现等 */
	private Integer type; 
	/** 账户ID */
	private Long accountId; 
	/** 关联卡号 */
	private String cardNo; 
	/** 客户ID */
	private Long customerId; 
	/** 客户编号 */
	private String customerNo; 
	/** 客户姓名 */
	private String customerName; 
	/** 期初余额-分 */
	private Long startBalance; 
	/** 操作金额-分 */
	private Long amount; 
	/** 期末余额-分 */
	private Long endBalance; 
	/** 交易类型-充值、提现、消费、转账、其他 */
	private Integer tradeType; 
	/** 交易渠道-现金、POS、网银 */
	private Integer tradeChannel; 
	/** 银行卡类型-借记卡、信用卡 */
	private Integer bankCardType; 
	/** 交易流水号 */
	private String tradeNo; 
	/** 委托合同编号 */
	private String contractNo; 
	/** 委托人ID */
	private Long consignorId; 
	/** 新卡卡号 */
	private String newCardNo; 
	/** 押金-分 */
	private Long deposit; 
	/** 工本费-分 */
	private Long cardCost; 
	/** 手续费-分 */
	private Long serviceCost; 
	/** 附加内容-存储不太重要的内容，否则请扩充该表字段 */
	private String attach; 
	/** 操作员ID */
	private Long operatorId; 
	/** 操作员工号 */
	private String operatorNo; 
	/** 操作员名称 */
	private String operatorName; 
	/** 操作时间 */
	private LocalDateTime operateTime; 
	/** 备注 */
	private String notes; 
	/** 办理状态-处理中、成功、失败 */
	private Integer state; 
	/** 商户ID */
	private Long firmId; 
	/** 修改时间 */
	private LocalDateTime modifyTime; 
	/** 数据版本号 */
	private Integer version; 
    /**
     * BusinessRecordEntity constructor
     */
	public BusinessRecordDo() {
		super();
	}

    /**
     * setter for 
     */
	public void setId(Long id) {
		this.id = id;
	}

    /**
     * getter for 
     */
	public Long getId() {
		return id;
	}

    /**
     * setter for 流水号
     */
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

    /**
     * getter for 流水号
     */
	public String getSerialNo() {
		return serialNo;
	}

    /**
     * setter for 账务周期号
     */
	public void setCycleNo(Long cycleNo) {
		this.cycleNo = cycleNo;
	}

    /**
     * getter for 账务周期号
     */
	public Long getCycleNo() {
		return cycleNo;
	}

    /**
     * setter for 业务类型-办卡、充值、提现等
     */
	public void setType(Integer type) {
		this.type = type;
	}

    /**
     * getter for 业务类型-办卡、充值、提现等
     */
	public Integer getType() {
		return type;
	}

    /**
     * setter for 账户ID
     */
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

    /**
     * getter for 账户ID
     */
	public Long getAccountId() {
		return accountId;
	}

    /**
     * setter for 关联卡号
     */
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

    /**
     * getter for 关联卡号
     */
	public String getCardNo() {
		return cardNo;
	}

    /**
     * setter for 客户ID
     */
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

    /**
     * getter for 客户ID
     */
	public Long getCustomerId() {
		return customerId;
	}

    /**
     * setter for 客户编号
     */
	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

    /**
     * getter for 客户编号
     */
	public String getCustomerNo() {
		return customerNo;
	}

    /**
     * setter for 客户姓名
     */
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

    /**
     * getter for 客户姓名
     */
	public String getCustomerName() {
		return customerName;
	}

    /**
     * setter for 期初余额-分
     */
	public void setStartBalance(Long startBalance) {
		this.startBalance = startBalance;
	}

    /**
     * getter for 期初余额-分
     */
	public Long getStartBalance() {
		return startBalance;
	}

    /**
     * setter for 操作金额-分
     */
	public void setAmount(Long amount) {
		this.amount = amount;
	}

    /**
     * getter for 操作金额-分
     */
	public Long getAmount() {
		return amount;
	}

    /**
     * setter for 期末余额-分
     */
	public void setEndBalance(Long endBalance) {
		this.endBalance = endBalance;
	}

    /**
     * getter for 期末余额-分
     */
	public Long getEndBalance() {
		return endBalance;
	}

    /**
     * setter for 交易类型-充值、提现、消费、转账、其他
     */
	public void setTradeType(Integer tradeType) {
		this.tradeType = tradeType;
	}

    /**
     * getter for 交易类型-充值、提现、消费、转账、其他
     */
	public Integer getTradeType() {
		return tradeType;
	}

    /**
     * setter for 交易渠道-现金、POS、网银
     */
	public void setTradeChannel(Integer tradeChannel) {
		this.tradeChannel = tradeChannel;
	}

    /**
     * getter for 交易渠道-现金、POS、网银
     */
	public Integer getTradeChannel() {
		return tradeChannel;
	}

    /**
     * setter for 银行卡类型-借记卡、信用卡
     */
	public void setBankCardType(Integer bankCardType) {
		this.bankCardType = bankCardType;
	}

    /**
     * getter for 银行卡类型-借记卡、信用卡
     */
	public Integer getBankCardType() {
		return bankCardType;
	}

    /**
     * setter for 交易流水号
     */
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

    /**
     * getter for 交易流水号
     */
	public String getTradeNo() {
		return tradeNo;
	}

    /**
     * setter for 委托合同编号
     */
	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

    /**
     * getter for 委托合同编号
     */
	public String getContractNo() {
		return contractNo;
	}

    /**
     * setter for 委托人ID
     */
	public void setConsignorId(Long consignorId) {
		this.consignorId = consignorId;
	}

    /**
     * getter for 委托人ID
     */
	public Long getConsignorId() {
		return consignorId;
	}

    /**
     * setter for 新卡卡号
     */
	public void setNewCardNo(String newCardNo) {
		this.newCardNo = newCardNo;
	}

    /**
     * getter for 新卡卡号
     */
	public String getNewCardNo() {
		return newCardNo;
	}

    /**
     * setter for 押金-分
     */
	public void setDeposit(Long deposit) {
		this.deposit = deposit;
	}

    /**
     * getter for 押金-分
     */
	public Long getDeposit() {
		return deposit;
	}

    /**
     * setter for 工本费-分
     */
	public void setCardCost(Long cardCost) {
		this.cardCost = cardCost;
	}

    /**
     * getter for 工本费-分
     */
	public Long getCardCost() {
		return cardCost;
	}

    /**
     * setter for 手续费-分
     */
	public void setServiceCost(Long serviceCost) {
		this.serviceCost = serviceCost;
	}

    /**
     * getter for 手续费-分
     */
	public Long getServiceCost() {
		return serviceCost;
	}

    /**
     * setter for 附加内容-存储不太重要的内容，否则请扩充该表字段
     */
	public void setAttach(String attach) {
		this.attach = attach;
	}

    /**
     * getter for 附加内容-存储不太重要的内容，否则请扩充该表字段
     */
	public String getAttach() {
		return attach;
	}

    /**
     * setter for 操作员ID
     */
	public void setOperatorId(Long operatorId) {
		this.operatorId = operatorId;
	}

    /**
     * getter for 操作员ID
     */
	public Long getOperatorId() {
		return operatorId;
	}

    /**
     * setter for 操作员工号
     */
	public void setOperatorNo(String operatorNo) {
		this.operatorNo = operatorNo;
	}

    /**
     * getter for 操作员工号
     */
	public String getOperatorNo() {
		return operatorNo;
	}

    /**
     * setter for 操作员名称
     */
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

    /**
     * getter for 操作员名称
     */
	public String getOperatorName() {
		return operatorName;
	}

    /**
     * setter for 操作时间
     */
	public void setOperateTime(LocalDateTime operateTime) {
		this.operateTime = operateTime;
	}

    /**
     * getter for 操作时间
     */
	public LocalDateTime getOperateTime() {
		return operateTime;
	}

    /**
     * setter for 备注
     */
	public void setNotes(String notes) {
		this.notes = notes;
	}

    /**
     * getter for 备注
     */
	public String getNotes() {
		return notes;
	}

    /**
     * setter for 办理状态-处理中、成功、失败
     */
	public void setState(Integer state) {
		this.state = state;
	}

    /**
     * getter for 办理状态-处理中、成功、失败
     */
	public Integer getState() {
		return state;
	}

    /**
     * setter for 商户ID
     */
	public void setFirmId(Long firmId) {
		this.firmId = firmId;
	}

    /**
     * getter for 商户ID
     */
	public Long getFirmId() {
		return firmId;
	}

    /**
     * setter for 修改时间
     */
	public void setModifyTime(LocalDateTime modifyTime) {
		this.modifyTime = modifyTime;
	}

    /**
     * getter for 修改时间
     */
	public LocalDateTime getModifyTime() {
		return modifyTime;
	}

    /**
     * setter for 数据版本号
     */
	public void setVersion(Integer version) {
		this.version = version;
	}

    /**
     * getter for 数据版本号
     */
	public Integer getVersion() {
		return version;
	}

    /**
     * BusinessRecordEntity.toString()
     */
    @Override
    public String toString() {
        return "BusinessRecordEntity{" +
               "id='" + id + '\'' +
               ", serialNo='" + serialNo + '\'' +
               ", cycleNo='" + cycleNo + '\'' +
               ", type='" + type + '\'' +
               ", accountId='" + accountId + '\'' +
               ", cardNo='" + cardNo + '\'' +
               ", customerId='" + customerId + '\'' +
               ", customerNo='" + customerNo + '\'' +
               ", customerName='" + customerName + '\'' +
               ", startBalance='" + startBalance + '\'' +
               ", amount='" + amount + '\'' +
               ", endBalance='" + endBalance + '\'' +
               ", tradeType='" + tradeType + '\'' +
               ", tradeChannel='" + tradeChannel + '\'' +
               ", bankCardType='" + bankCardType + '\'' +
               ", tradeNo='" + tradeNo + '\'' +
               ", contractNo='" + contractNo + '\'' +
               ", consignorId='" + consignorId + '\'' +
               ", newCardNo='" + newCardNo + '\'' +
               ", deposit='" + deposit + '\'' +
               ", cardCost='" + cardCost + '\'' +
               ", serviceCost='" + serviceCost + '\'' +
               ", attach='" + attach + '\'' +
               ", operatorId='" + operatorId + '\'' +
               ", operatorNo='" + operatorNo + '\'' +
               ", operatorName='" + operatorName + '\'' +
               ", operateTime='" + operateTime + '\'' +
               ", notes='" + notes + '\'' +
               ", state='" + state + '\'' +
               ", firmId='" + firmId + '\'' +
               ", modifyTime='" + modifyTime + '\'' +
               ", version='" + version + '\'' +
               '}';
    }

}