package com.trade.sender.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class Lsqbdz implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 自增id
	 */
	public long id;
	/**
	 * 用户id
	 */
	public long userid;
	/**
	 * 用户钱包地址
	 */
	public String qbdz;
	/**
	 * 用户私钥
	 */
	public String sy;
	/**
	 * 转入金额
	 */
	public BigDecimal zrje = new BigDecimal(0);

	/**
	 * T6012_id
	 */
	public long T6012_id;

}