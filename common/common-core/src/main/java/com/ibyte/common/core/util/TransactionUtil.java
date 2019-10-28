package com.ibyte.common.core.util;

import com.ibyte.framework.support.ApplicationContextHolder;
import org.springframework.core.Ordered;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 手工事务处理<br>
 * <code>
 * 	TransactionStatus status = TransactionUtil.beginTransaction();
 *	try {
 * 		do something
 * 		TransactionUtils.commit(status);
 * 	}catch(Exception e){
 * 		TransactionUtils.rollback(status);
 * 		log.error("", e);
 * 	}
 * </code>
 *
 * @author li.shangzhi
 */
public class TransactionUtil {
	private static final ThreadLocal<Boolean> IN_COMMIT = new ThreadLocal<>();

	private static PlatformTransactionManager getTransactionManager() {
		return ApplicationContextHolder.getApplicationContext()
				.getBean(PlatformTransactionManager.class);
	}

	/**
	 * 若没有事务则开启事务，有则使用当前事务。
	 */
	public static TransactionStatus beginTransaction() {
		DefaultTransactionDefinition td = new DefaultTransactionDefinition(
				TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status = getTransactionManager().getTransaction(td);
		return status;
	}

	/**
	 * 开启新事务
	 */
	public static TransactionStatus beginNewTransaction() {
		DefaultTransactionDefinition td = new DefaultTransactionDefinition(
				TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		TransactionStatus status = getTransactionManager().getTransaction(td);
		return status;
	}

	/**
	 * 开启新的只读事务
	 */
	public static TransactionStatus beginNewReadTransaction() {
		DefaultTransactionDefinition td = new DefaultTransactionDefinition(
				TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		td.setReadOnly(true);
		TransactionStatus status = getTransactionManager().getTransaction(td);
		return status;
	}

	/**
	 * 提交事务
	 */
	public static void commit(TransactionStatus status) {
		getTransactionManager().commit(status);
	}

	/**
	 * 回滚事务
	 */
	public static void rollback(TransactionStatus status) {
		if (!status.isCompleted()) {
			getTransactionManager().rollback(status);
		}
	}

	/**
	 * 若存在事务，则事务提交后触发，否则马上触发
	 */
	public static void afterCommit(Runnable runner) {
		onCommit(runner, false);
	}

	/**
	 * 若存在事务，则事务提交前触发，否则马上触发
	 */
	public static void beforeCommit(Runnable runner) {
		onCommit(runner, true);
	}

	private static void onCommit(Runnable runner, boolean before) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()
				|| IN_COMMIT.get() != null) {
			runner.run();
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(
				new OnCommitSynchronization(runner, before));
	}

	/**
	 * 获取已经执行过afterCommit的runner
	 */
	public static <T extends Runnable> T getAfterCommit(Class<T> clazz) {
		return getOnCommit(clazz, false);
	}

	/**
	 * 获取已经执行过beforeCommit的runner
	 */
	public static <T extends Runnable> T getBeforeCommit(Class<T> clazz) {
		return getOnCommit(clazz, true);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Runnable> T getOnCommit(Class<T> clazz,
			boolean before) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			return null;
		}
		for (TransactionSynchronization synchronization : TransactionSynchronizationManager
				.getSynchronizations()) {
			if (synchronization instanceof OnCommitSynchronization) {
				OnCommitSynchronization onCommit = (OnCommitSynchronization) synchronization;
				if (before == onCommit.before
						&& onCommit.runner.getClass() == clazz) {
					return (T) onCommit.runner;
				}
			}
		}
		return null;
	}

	/**
	 * 事务提交触发器
	 * 
	 * @author Li.Shangzhi
	 */
	private static class OnCommitSynchronization
			extends TransactionSynchronizationAdapter {
		private Runnable runner;
		private boolean before;

		@Override
		public void beforeCommit(boolean readOnly) {
			if (before) {
				run();
			}
		}

		@Override
		public void afterCommit() {
			if (!before) {
				run();
			}
		}

		private void run() {
			IN_COMMIT.set(Boolean.TRUE);
			try {
				runner.run();
			} finally {
				IN_COMMIT.remove();
			}
		}

		@Override
		public int getOrder() {
			if (runner instanceof Ordered) {
				return ((Ordered) runner).getOrder();
			}
			return Ordered.LOWEST_PRECEDENCE;
		}

		public OnCommitSynchronization(Runnable runner, boolean before) {
			super();
			this.runner = runner;
			this.before = before;
		}
	}
}
