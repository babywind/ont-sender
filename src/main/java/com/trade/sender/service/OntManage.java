package com.trade.sender.service;

import com.quqian.framework.service.Service;
import com.trade.sender.entity.BEntity;
import com.trade.sender.entity.Lsqbdz;

public abstract interface OntManage extends Service {

	/**
	 * ONT转入热钱包
	 * @param l
	 * @throws Throwable
	 */
	void ont_rqb(Lsqbdz l) throws Throwable;
	
	/**
	 *  ONT转入冷钱包
	 * 
	 * @throws Throwable
	 */
	void ont_lqb() throws Throwable;

	/**
	 * ONT转出到用户钱包
	 * @throws Exception
	 */
	void ontTransOut (BEntity b) throws Exception;

	/**
	 * 新建ONT钱包
	 * @throws Exception
	 */
	void createNewOntAccount() throws Exception;

	/**
	 * ONT转账记录
	 * @throws Throwable
	 */
	Lsqbdz[] getTranscationInfos() throws Throwable;

	BEntity[] getTransOutInfos () throws Exception;
}
