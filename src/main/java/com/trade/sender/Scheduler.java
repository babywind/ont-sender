package com.trade.sender;

import com.quqian.framework.config.ConfigureProvider;
import com.quqian.framework.resource.ResourceProvider;
import com.quqian.framework.service.ServiceProvider;
import com.quqian.framework.service.ServiceSession;
import com.trade.sender.entity.BEntity;
import com.trade.sender.entity.Lsqbdz;
import com.trade.sender.service.OntManage;

public class Scheduler extends Thread {

	private transient boolean alive = true;
	private final ResourceProvider resourceProvider;
	private final ConfigureProvider configureProvider;
	private final ServiceProvider serviceProvider;
//	protected static int EXPIRES_TOKEN_TIME = 0;

	public Scheduler(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.configureProvider = resourceProvider.getResource(ConfigureProvider.class);
		this.serviceProvider = resourceProvider.getResource(ServiceProvider.class);
	}

	@Override
	public void run() {
		while (alive) {
			try {
				// 创建新的用户临时钱包
				//createNewAccount();

				// ONT转入热钱包
				ont_rqb();
				
				//ONT转入冷钱包
				ont_lqb();

				// ONT 转出
				//transOutOnt();

				// 休眠5分钟
				sleep(1000 * 60 * 5);
			} catch (InterruptedException e) {
				alive = false;
				break;
			}
		}
	}

	/*
	private void createNewAccount () {
		try (ServiceSession serviceSession = serviceProvider.createServiceSession()) {
			OntManage manage = serviceSession.getService(OntManage.class);
			serviceSession.openTransactions();
			try {
				manage.createNewOntAccount();

				serviceSession.commit();
			} catch (Exception e) {
				e.printStackTrace();
				serviceSession.rollback();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			resourceProvider.log(e);
		}
	}


	private void transOutOnt () {
		try (ServiceSession serviceSession = serviceProvider.createServiceSession()) {
			OntManage manage = serviceSession.getService(OntManage.class);

			BEntity[] bEntities = manage.getTransOutInfos();
			if (bEntities != null) {
				for (BEntity b : bEntities) {
					serviceSession.openTransactions();

					try {
						manage.ontTransOut(b);

						serviceSession.commit();
					} catch (Exception e) {
						e.printStackTrace();
						serviceSession.rollback();
					}
				}
			}
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			resourceProvider.log(throwable);
		}
	}
*/

	/**
	 * ONT转入热钱包
	 */
	private void ont_rqb() {
		try (ServiceSession serviceSession = serviceProvider.createServiceSession()) {
			OntManage manage = serviceSession.getService(OntManage.class);
			Lsqbdz[] ls = manage.getTranscationInfos();
			if (ls != null) {
				for (Lsqbdz l : ls) {
					serviceSession.openTransactions();
					try {
						manage.ont_rqb(l);

						serviceSession.commit();
					} catch (Exception e) {
						e.printStackTrace();
						serviceSession.rollback();
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			resourceProvider.log(e);
		}

	}

	/**
	 * ONT转入冷钱包
	 */
	private void ont_lqb() {
		try (ServiceSession serviceSession = serviceProvider.createServiceSession()) {
			OntManage manage = serviceSession.getService(OntManage.class);
			serviceSession.openTransactions();
			try {
				manage.ont_lqb();
				serviceSession.commit();
			} catch (Exception e) {
				e.printStackTrace();
				serviceSession.rollback();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			resourceProvider.log(e);
		}
	}
}
