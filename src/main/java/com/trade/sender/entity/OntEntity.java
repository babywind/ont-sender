package com.trade.sender.entity;

import com.quqian.p2p.common.enums.IsPass;

import java.io.Serializable;
import java.math.BigDecimal;

public class OntEntity implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * 热钱包地址
	 */
	public String r_qbdz;
	/**
	 *  热钱包私钥
	 */
	public String r_sy;
	/**
	 * 冷钱包地址
	 */
	public String l_qbdz;
	/**
	 * 是否自动转入冷钱包
	 */
	public IsPass is;
	/**
	 * 大于多少自动转入
	 */
	public BigDecimal count=new BigDecimal(0);	
	
	/**
	 * 钱包服务器ip
	 */
	public String ip;
}
