package com.trade.sender.entity;

import java.io.Serializable;

public class OutWalletEntity implements Serializable{
	private static final long serialVersionUID = 1L;

	public int id;

	public int bid;

	public String address;

	public String privateKey;

	public String ip;

	public String port;

	public String serverName;

	public String serverPasswd;

}
