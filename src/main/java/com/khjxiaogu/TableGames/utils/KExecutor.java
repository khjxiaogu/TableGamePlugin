/**
 * Mirai Tablegames Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.UserIdentifierSerializer;
import com.khjxiaogu.TableGames.utils.WaitThread.TerminatedException;

public class KExecutor implements ExecutorService {
	ExecutorService exec;
	AbstractRoom tosend;
	/**
	 * @param nthread
	 * @param group
	 */
	public KExecutor(int nthread, AbstractRoom group) {
		exec=new ThreadPoolExecutor(0, Integer.MAX_VALUE,
				0L, TimeUnit.MILLISECONDS,
				new SynchronousQueue<Runnable>());
		tosend=group;
	}

	@Override
	public void execute(Runnable command) {

		exec.execute(()->{try {
			command.run();
		}catch(Exception ex) {
			if(!(ex instanceof TerminatedException||ex instanceof InterruptedException)) {
				ByteArrayOutputStream baos=new ByteArrayOutputStream();
				ex.printStackTrace(new PrintStream(baos));
				UserIdentifier uid=UserIdentifierSerializer.readOptional("1905387052").or(()->
				UserIdentifierSerializer.readOptional("sb:841570688")).orElse(null);
				if(uid!=null) {
					AbstractUser author=tosend.get(uid);
					if(author!=null) {
						author.sendPrivate(baos.toString());
						return;
					}
				}
				tosend.getOwner().sendPrivate("哎呀，机器人出错了！请把以下信息报告给作者以便于修复。");
				tosend.getOwner().sendPrivate(baos.toString());
				
				
			}
			throw ex;
		}});

	}
	public void executeLater(Runnable command,long time) {
		execute(()->{
			try {
				Thread.sleep(time);
				command.run();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	@Override
	public void shutdown() {
		exec.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return exec.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return exec.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return exec.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return exec.awaitTermination(timeout, unit);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return exec.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return exec.submit(task, result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return exec.submit(task);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return exec.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return exec.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return exec.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return exec.invokeAny(tasks, timeout, unit);
	}

}
