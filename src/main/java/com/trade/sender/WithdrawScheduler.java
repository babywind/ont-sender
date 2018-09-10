package com.trade.sender;

import com.quqian.framework.config.ConfigureProvider;
import com.quqian.framework.resource.ResourceProvider;
import com.quqian.framework.service.ServiceProvider;
import com.quqian.framework.service.ServiceSession;
import com.trade.sender.entity.BEntity;
import com.trade.sender.service.OntManage;

public class WithdrawScheduler extends Thread {

	private transient boolean alive = true;
	private final ResourceProvider resourceProvider;
	private final ConfigureProvider configureProvider;
	private final ServiceProvider serviceProvider;

	public WithdrawScheduler(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.configureProvider = resourceProvider.getResource(ConfigureProvider.class);
		this.serviceProvider = resourceProvider.getResource(ServiceProvider.class);
	}

	@Override
	public void run() {
		while (alive) {
			try {
				// ONT提取
				withdrawOnt();

				// 休眠5分钟
				sleep(1000 * 60 * 5);
			} catch (InterruptedException e) {
				alive = false;
				break;
			}
		}
	}

	private void withdrawOnt () {
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
}
