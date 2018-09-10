package com.trade.sender;

import java.util.HashSet;
import java.util.Set;

import com.quqian.framework.config.achieve.DefaultConfigureProvider;
import com.quqian.framework.log.Logger;
import com.quqian.framework.resource.ResourceProvider;
import com.quqian.framework.resource.achieve.ResourceInitializer;
import com.quqian.framework.service.achieve.SimpleServiceProvider;
import com.quqian.framework.service.achieve.SimpleServiceProvider.SimpleServiceSession;
import com.trade.sender.config.Master;
import com.trade.sender.config.TradeDefine;
import com.trade.sender.service.achieve.OntManageImpl;

public class Sender {

	public static void main(String... args) {
		Set<Class<?>> classes = new HashSet<>();
		classes.add(TradeDefine.class);
		classes.add(Master.class);
		classes.add(SimpleServiceProvider.class);
		classes.add(SimpleServiceSession.class);
		classes.add(DefaultConfigureProvider.class);

		classes.add(OntManageImpl.OntManageFactory.class);

		ResourceProvider resourceProvider = new ResourceInitializer().initialize(classes, new Logger() {
			@Override
			public void log(String message) {
				System.out.println(message);
			}

			@Override
			public void log(Throwable throwable) {
				throwable.printStackTrace();
			}
		}, args);

		// 用户资产转入
		Scheduler scheduler = new Scheduler(resourceProvider);
		scheduler.start();

		// 用户临时账本创建
		new AccountScheduler(resourceProvider).start();

		// 用户资产转出
		new WithdrawScheduler(resourceProvider).start();
	}
}
