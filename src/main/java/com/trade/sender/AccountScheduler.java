package com.trade.sender;

import com.quqian.framework.config.ConfigureProvider;
import com.quqian.framework.resource.ResourceProvider;
import com.quqian.framework.service.ServiceProvider;
import com.quqian.framework.service.ServiceSession;
import com.trade.sender.service.OntManage;

public class AccountScheduler extends Thread {

	private transient boolean alive = true;
	private final ResourceProvider resourceProvider;
	private final ConfigureProvider configureProvider;
	private final ServiceProvider serviceProvider;

	public AccountScheduler(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.configureProvider = resourceProvider.getResource(ConfigureProvider.class);
		this.serviceProvider = resourceProvider.getResource(ServiceProvider.class);
	}

	@Override
	public void run() {
		while (alive) {
			try {
				// 创建新的用户临时钱包
				createNewAccount();

				// 休眠1分钟
				sleep(1000 * 60);
			} catch (InterruptedException e) {
				alive = false;
				break;
			}
		}
	}

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
}
