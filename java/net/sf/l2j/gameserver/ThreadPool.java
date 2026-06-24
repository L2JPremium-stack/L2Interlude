package net.sf.l2j.gameserver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;

public final class ThreadPool
{
	protected static final Logger LOG = Logger.getLogger(ThreadPool.class.getName());
	
	private static final int CPU = Runtime.getRuntime().availableProcessors();
	private static final long MAX_DELAY = TimeUnit.DAYS.toMillis(365);
	
	// Limites conservadores para evitar crescimento infinito de fila.
	private static final int INSTANT_QUEUE_LIMIT = 5000;
	private static final int SCHEDULED_QUEUE_WARNING = 10000;
	private static final int SCHEDULED_QUEUE_HARD_LIMIT = 20000;
	
	private static final AtomicLong POOL_BALANCER = new AtomicLong(0);
	
	private static ScheduledThreadPoolExecutor[] _scheduledPools = new ScheduledThreadPoolExecutor[0];
	private static ThreadPoolExecutor[] _instantPools = new ThreadPoolExecutor[0];
	
	// Pools separados. O objetivo é impedir que AI/effects/save/world disputem a mesma fila.
	private static ScheduledThreadPoolExecutor _aiPool;
	private static ScheduledThreadPoolExecutor _effectPool;
	private static ScheduledThreadPoolExecutor _worldPool;
	private static ScheduledThreadPoolExecutor _savePool;
	
	private static volatile boolean SHUTTING_DOWN = false;
	private static volatile boolean INITIALIZED = false;
	
	private static final AtomicLong scheduledTasks = new AtomicLong(0);
	private static final AtomicLong periodicTasks = new AtomicLong(0);
	private static final AtomicLong instantTasks = new AtomicLong(0);
	private static final AtomicLong rejectedTasks = new AtomicLong(0);
	private static final AtomicLong taskErrors = new AtomicLong(0);
	
	private static final ConcurrentHashMap<String, TaskStat> TASK_STATS = new ConcurrentHashMap<>();
	
	private static final class TaskStat
	{
		final AtomicLong created = new AtomicLong();
		final AtomicLong executed = new AtomicLong();
		final AtomicLong errors = new AtomicLong();
		final AtomicLong totalRuntimeMs = new AtomicLong();
		final AtomicLong maxRuntimeMs = new AtomicLong();
		
		void addRuntime(long ms)
		{
			executed.incrementAndGet();
			totalRuntimeMs.addAndGet(ms);
			
			long current;
			do
			{
				current = maxRuntimeMs.get();
				if (ms <= current)
					return;
			}
			while (!maxRuntimeMs.compareAndSet(current, ms));
		}
	}
	
	public enum PoolType
	{
		GENERAL,
		AI,
		EFFECT,
		WORLD,
		SAVE
	}
	
	private ThreadPool()
	{
	}
	
	public static synchronized void init()
	{
		if (INITIALIZED)
		{
			LOG.warning("ThreadPool: init() chamado mais de uma vez. Ignorando.");
			return;
		}
		
		SHUTTING_DOWN = false;
		
		final int schedCount = Math.max(1, (Config.SCHEDULED_THREAD_POOL_COUNT == -1) ? CPU : Config.SCHEDULED_THREAD_POOL_COUNT);
		final int instCount = Math.max(1, (Config.INSTANT_THREAD_POOL_COUNT == -1) ? CPU : Config.INSTANT_THREAD_POOL_COUNT);
		
		_scheduledPools = new ScheduledThreadPoolExecutor[schedCount];
		for (int i = 0; i < schedCount; i++)
		{
			final ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(Config.THREADS_PER_SCHEDULED_THREAD_POOL, new NamedThreadFactory("ScheduledPool-" + i));
			
			// Essencial: task cancelada sai da fila interna do executor.
			pool.setRemoveOnCancelPolicy(true);
			pool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
			pool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
			pool.prestartAllCoreThreads();
			
			_scheduledPools[i] = pool;
		}
		
		final RejectedExecutionHandler rejectionHandler = (r, executor) -> {
			rejectedTasks.incrementAndGet();
			LOG.warning("ThreadPool: task descartada por sobrecarga. Active=" + executor.getActiveCount() + ", Queue=" + executor.getQueue().size());
		};
		
		_instantPools = new ThreadPoolExecutor[instCount];
		for (int i = 0; i < instCount; i++)
		{
			final ThreadPoolExecutor pool = new ThreadPoolExecutor(Config.THREADS_PER_INSTANT_THREAD_POOL, Config.THREADS_PER_INSTANT_THREAD_POOL, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(INSTANT_QUEUE_LIMIT), new NamedThreadFactory("InstantPool-" + i), rejectionHandler);
			
			pool.prestartAllCoreThreads();
			_instantPools[i] = pool;
		}
		
		_aiPool = newScheduledSingleGroup("AIPool", Math.max(2, CPU / 2));
		_effectPool = newScheduledSingleGroup("EffectPool", Math.max(1, CPU / 4));
		_worldPool = newScheduledSingleGroup("WorldPool", Math.max(1, CPU / 4));
		_savePool = newScheduledSingleGroup("SavePool", 1);
		
		// Limpeza periódica das filas internas dos executores.
		scheduleAtFixedRate("ThreadPoolCleanup", () -> {
			for (ScheduledThreadPoolExecutor pool : _scheduledPools)
			{
				if (pool != null)
					pool.purge();
			}
			
			for (ThreadPoolExecutor pool : _instantPools)
			{
				if (pool != null)
					pool.purge();
			}
		}, 300000L, 300000L);
		
		// Monitor de carga. Não guarda ScheduledFuture em Set global para evitar retenção de Runnable/Player/NPC.
		scheduleAtFixedRate("ThreadPoolMonitor", () -> {
			for (int i = 0; i < _scheduledPools.length; i++)
			{
				final ScheduledThreadPoolExecutor pool = _scheduledPools[i];
				if (pool == null)
					continue;
				
				final int active = pool.getActiveCount();
				final int queue = pool.getQueue().size();
				final int core = pool.getCorePoolSize();
				
				if ((queue > 500) || (active >= core))
					LOG.warning("ThreadPool Monitor: Scheduled #" + i + " sob carga. Active=" + active + ", Core=" + core + ", Queue=" + queue + ", Completed=" + pool.getCompletedTaskCount());
			}
			
			for (int i = 0; i < _instantPools.length; i++)
			{
				final ThreadPoolExecutor pool = _instantPools[i];
				if (pool == null)
					continue;
				
				final int active = pool.getActiveCount();
				final int queue = pool.getQueue().size();
				final int max = pool.getMaximumPoolSize();
				
				if (queue > 1000)
					LOG.warning("ThreadPool Monitor: Instant #" + i + " sob carga. Active=" + active + ", Max=" + max + ", Queue=" + queue + ", Completed=" + pool.getCompletedTaskCount());
			}
			
			logDedicatedPool("AI", _aiPool);
			logDedicatedPool("Effect", _effectPool);
			logDedicatedPool("World", _worldPool);
			logDedicatedPool("Save", _savePool);
			
			if (getScheduledQueueSize() > 500)
				LOG.warning(getStats());
			
		}, 60000L, 60000L);
		
		INITIALIZED = true;
		LOG.info("ThreadPool: iniciado com " + schedCount + " scheduled pools e " + instCount + " instant pools.");
	}
	
	private static ScheduledThreadPoolExecutor newScheduledSingleGroup(String name, int threads)
	{
		final ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(threads, new NamedThreadFactory(name));
		pool.setRemoveOnCancelPolicy(true);
		pool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		pool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		pool.prestartAllCoreThreads();
		return pool;
	}
	
	private static void logDedicatedPool(String name, ScheduledThreadPoolExecutor pool)
	{
		if (pool == null)
			return;
		
		final int active = pool.getActiveCount();
		final int queue = pool.getQueue().size();
		final int core = pool.getCorePoolSize();
		
		if ((queue > 200) || (active >= core))
			LOG.warning("ThreadPool Monitor: " + name + " sob carga. Active=" + active + ", Core=" + core + ", Queue=" + queue + ", Completed=" + pool.getCompletedTaskCount());
	}
	
	private static boolean canSchedule()
	{
		return INITIALIZED && !SHUTTING_DOWN && (_scheduledPools.length > 0) && (_instantPools.length > 0);
	}
	
	public static ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return schedule("UnnamedScheduledTask", r, delay);
	}
	
	public static ScheduledFuture<?> schedule(String name, Runnable r, long delay)
	{
		return schedule(PoolType.GENERAL, name, r, delay);
	}
	
	public static ScheduledFuture<?> schedule(PoolType type, String name, Runnable r, long delay)
	{
		if ((r == null) || !canSchedule())
			return null;
		
		final ScheduledThreadPoolExecutor pool = getScheduledPool(type);
		if (!canAcceptScheduled(pool, name))
			return null;
		
		try
		{
			final String safe = safeName(name);
			TASK_STATS.computeIfAbsent(safe, k -> new TaskStat()).created.incrementAndGet();
			final ScheduledFuture<?> future = pool.schedule(new SafeTask(safe, r), validate(delay), TimeUnit.MILLISECONDS);
			scheduledTasks.incrementAndGet();
			return future;
		}
		catch (Throwable t)
		{
			rejectedTasks.incrementAndGet();
			LOG.log(Level.WARNING, "ThreadPool: falha ao agendar task [" + safeName(name) + "]", t);
			return null;
		}
	}
	
	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period)
	{
		return scheduleAtFixedRate("UnnamedPeriodicTask", r, delay, period);
	}
	
	public static ScheduledFuture<?> scheduleAtFixedRate(String name, Runnable r, long delay, long period)
	{
		return scheduleAtFixedRate(PoolType.GENERAL, name, r, delay, period);
	}
	
	public static ScheduledFuture<?> scheduleAtFixedRate(PoolType type, String name, Runnable r, long delay, long period)
	{
		
		if ((r == null) || !canSchedule())
			return null;
		
		final ScheduledThreadPoolExecutor pool = getScheduledPool(type);
		if (!canAcceptScheduled(pool, name))
			return null;
		
		try
		{
			final String safe = safeName(name);
			TASK_STATS.computeIfAbsent(safe, k -> new TaskStat()).created.incrementAndGet();
			final ScheduledFuture<?> future = pool.scheduleAtFixedRate(new SafeTask(safe, r), validate(delay), validatePeriod(period), TimeUnit.MILLISECONDS);
			periodicTasks.incrementAndGet();
			return future;
		}
		catch (Throwable t)
		{
			rejectedTasks.incrementAndGet();
			LOG.log(Level.WARNING, "ThreadPool: falha ao agendar task periódica [" + safeName(name) + "]", t);
			return null;
		}
	}
	
	public static void execute(Runnable r)
	{
		execute("UnnamedInstantTask", r);
	}
	
	public static void execute(String name, Runnable r)
	{
		if ((r == null) || !canSchedule())
			return;
		
		try
		{
			final String safe = safeName(name);
			TASK_STATS.computeIfAbsent(safe, k -> new TaskStat()).created.incrementAndGet();
			getInstantPool().execute(new SafeTask(safe, r));
			
			instantTasks.incrementAndGet();
		}
		catch (Throwable t)
		{
			rejectedTasks.incrementAndGet();
			LOG.log(Level.WARNING, "ThreadPool: falha ao executar task [" + safeName(name) + "]", t);
		}
	}
	
	private static ScheduledThreadPoolExecutor getScheduledPool(PoolType type)
	{
		switch (type)
		{
			case AI:
				return _aiPool;
			case EFFECT:
				return _effectPool;
			case WORLD:
				return _worldPool;
			case SAVE:
				return _savePool;
			case GENERAL:
			default:
				return getScheduledPool();
		}
	}
	
	private static int getScheduledQueueSize()
	{
		int total = 0;
		for (ScheduledThreadPoolExecutor pool : _scheduledPools)
			if (pool != null)
				total += pool.getQueue().size();
		return total;
	}
	
	private static int getQueue(ScheduledThreadPoolExecutor pool)
	{
		return pool == null ? 0 : pool.getQueue().size();
	}
	
	public static String getStats()
	{
		final StringBuilder sb = new StringBuilder(2048);
		sb.append("\n========== ThreadPool Metrics ==========");
		sb.append("\nScheduled tasks created: ").append(scheduledTasks.get());
		sb.append("\nPeriodic tasks created:  ").append(periodicTasks.get());
		sb.append("\nInstant tasks created:   ").append(instantTasks.get());
		sb.append("\nRejected tasks:          ").append(rejectedTasks.get());
		sb.append("\nTask errors:             ").append(taskErrors.get());
		
		long scheduledQueue = 0;
		for (int i = 0; i < _scheduledPools.length; i++)
		{
			final ScheduledThreadPoolExecutor pool = _scheduledPools[i];
			if (pool == null)
				continue;
			
			scheduledQueue += pool.getQueue().size();
			sb.append("\nScheduled #").append(i).append(": Active=").append(pool.getActiveCount()).append(", Queue=").append(pool.getQueue().size()).append(", Completed=").append(pool.getCompletedTaskCount());
		}
		
		sb.append("\nAI Pool:     Active=").append(_aiPool == null ? 0 : _aiPool.getActiveCount()).append(", Queue=").append(getQueue(_aiPool)).append(", Completed=").append(_aiPool == null ? 0 : _aiPool.getCompletedTaskCount());
		sb.append("\nEffect Pool: Active=").append(_effectPool == null ? 0 : _effectPool.getActiveCount()).append(", Queue=").append(getQueue(_effectPool)).append(", Completed=").append(_effectPool == null ? 0 : _effectPool.getCompletedTaskCount());
		sb.append("\nWorld Pool:  Active=").append(_worldPool == null ? 0 : _worldPool.getActiveCount()).append(", Queue=").append(getQueue(_worldPool)).append(", Completed=").append(_worldPool == null ? 0 : _worldPool.getCompletedTaskCount());
		sb.append("\nSave Pool:   Active=").append(_savePool == null ? 0 : _savePool.getActiveCount()).append(", Queue=").append(getQueue(_savePool)).append(", Completed=").append(_savePool == null ? 0 : _savePool.getCompletedTaskCount());
		
		long instantQueue = 0;
		for (int i = 0; i < _instantPools.length; i++)
		{
			final ThreadPoolExecutor pool = _instantPools[i];
			if (pool == null)
				continue;
			
			instantQueue += pool.getQueue().size();
			sb.append("\nInstant #").append(i).append(": Active=").append(pool.getActiveCount()).append(", Queue=").append(pool.getQueue().size()).append(", Completed=").append(pool.getCompletedTaskCount());
		}
		
		sb.append("\nScheduled totals: Active=N/A, Queue=").append(scheduledQueue + getQueue(_aiPool) + getQueue(_effectPool) + getQueue(_worldPool) + getQueue(_savePool));
		sb.append("\nInstant totals:   Queue=").append(instantQueue);
		
		sb.append("\n\nTop task origins:");
		final List<Map.Entry<String, TaskStat>> stats = new ArrayList<>(TASK_STATS.entrySet());
		stats.sort(Comparator.comparingLong((Map.Entry<String, TaskStat> e) -> e.getValue().created.get()).reversed());
		
		int count = 0;
		for (Map.Entry<String, TaskStat> e : stats)
		{
			if (++count > 25)
				break;
			
			final TaskStat s = e.getValue();
			final long executed = s.executed.get();
			final long avg = executed == 0 ? 0 : s.totalRuntimeMs.get() / executed;
			sb.append("\n").append(e.getKey()).append(": Created=").append(s.created.get()).append(", Executed=").append(executed).append(", Errors=").append(s.errors.get()).append(", AvgMs=").append(avg).append(", MaxMs=").append(s.maxRuntimeMs.get());
		}
		
		sb.append("\n========================================");
		return sb.toString();
	}
	
	private static boolean canAcceptScheduled(ScheduledThreadPoolExecutor pool, String name)
	{
		if (pool == null)
			return false;
		
		final int queue = pool.getQueue().size();
		
		if (queue >= SCHEDULED_QUEUE_HARD_LIMIT)
		{
			rejectedTasks.incrementAndGet();
			LOG.warning("ThreadPool: task scheduled descartada por fila excessiva. Task=" + safeName(name) + ", Queue=" + queue);
			return false;
		}
		
		if (queue == SCHEDULED_QUEUE_WARNING)
			LOG.warning("ThreadPool: fila scheduled muito alta. Queue=" + queue);
		
		return true;
	}
	
	private static ScheduledThreadPoolExecutor getScheduledPool()
	{
		final long index = POOL_BALANCER.getAndIncrement() & Long.MAX_VALUE;
		return _scheduledPools[(int) (index % _scheduledPools.length)];
	}
	
	private static ThreadPoolExecutor getInstantPool()
	{
		final long index = POOL_BALANCER.getAndIncrement() & Long.MAX_VALUE;
		return _instantPools[(int) (index % _instantPools.length)];
	}
	
	private static long validate(long delay)
	{
		return Math.max(0L, Math.min(MAX_DELAY, delay));
	}
	
	private static long validatePeriod(long period)
	{
		return Math.max(1L, Math.min(MAX_DELAY, period));
	}
	
	private static String safeName(String name)
	{
		return ((name == null) || name.isEmpty()) ? "UnnamedTask" : name;
	}
	
	public static synchronized void shutdown()
	{
		if (!INITIALIZED)
			return;
		
		SHUTTING_DOWN = true;
		LOG.info("ThreadPool: iniciando desligamento seguro...");
		
		try
		{
			for (ScheduledThreadPoolExecutor pool : _scheduledPools)
			{
				if (pool != null)
					pool.shutdownNow();
			}
			
			for (ThreadPoolExecutor pool : _instantPools)
			{
				if (pool != null)
					pool.shutdownNow();
			}
			
			if (_aiPool != null)
			    _aiPool.shutdownNow();
			if (_effectPool != null)
			    _effectPool.shutdownNow();
			if (_worldPool != null)
			    _worldPool.shutdownNow();
			if (_savePool != null)
			    _savePool.shutdownNow();

			
			
			for (ScheduledThreadPoolExecutor pool : _scheduledPools)
			{
				if (pool != null)
					pool.awaitTermination(5, TimeUnit.SECONDS);
			}
			
			for (ThreadPoolExecutor pool : _instantPools)
			{
				if (pool != null)
					pool.awaitTermination(5, TimeUnit.SECONDS);
			}
			
			LOG.info("ThreadPool: desligado com sucesso.");
		}
		catch (Throwable t)
		{
			LOG.log(Level.WARNING, "ThreadPool: erro ao desligar.", t);
		}
		finally
		{
			_scheduledPools = new ScheduledThreadPoolExecutor[0];
			_instantPools = new ThreadPoolExecutor[0];
			_aiPool = null;
			_effectPool = null;
			_worldPool = null;
			_savePool = null;

			INITIALIZED = false;
		}
	}
	
	public static boolean isShuttingDown()
	{
		return SHUTTING_DOWN;
	}
	
	public static void printStats()
	{
		LOG.info("========== ThreadPool Metrics ==========");
		LOG.info("Scheduled tasks created: " + scheduledTasks.get());
		LOG.info("Periodic tasks created:  " + periodicTasks.get());
		LOG.info("Instant tasks created:   " + instantTasks.get());
		LOG.info("Rejected tasks:          " + rejectedTasks.get());
		LOG.info("Task errors:             " + taskErrors.get());
		
		int scheduledQueueTotal = 0;
		int scheduledActiveTotal = 0;
		
		for (int i = 0; i < _scheduledPools.length; i++)
		{
			final ScheduledThreadPoolExecutor pool = _scheduledPools[i];
			if (pool != null)
			{
				scheduledQueueTotal += pool.getQueue().size();
				scheduledActiveTotal += pool.getActiveCount();
				LOG.info("Scheduled #" + i + ": Active=" + pool.getActiveCount() + ", Queue=" + pool.getQueue().size() + ", Completed=" + pool.getCompletedTaskCount());
			}
		}
		
		int instantQueueTotal = 0;
		int instantActiveTotal = 0;
		
		for (int i = 0; i < _instantPools.length; i++)
		{
			final ThreadPoolExecutor pool = _instantPools[i];
			if (pool != null)
			{
				instantQueueTotal += pool.getQueue().size();
				instantActiveTotal += pool.getActiveCount();
				LOG.info("Instant #" + i + ": Active=" + pool.getActiveCount() + ", Queue=" + pool.getQueue().size() + ", Completed=" + pool.getCompletedTaskCount());
			}
		}
		
		LOG.info("Scheduled totals: Active=" + scheduledActiveTotal + ", Queue=" + scheduledQueueTotal);
		LOG.info("Instant totals:   Active=" + instantActiveTotal + ", Queue=" + instantQueueTotal);
	}
	
	public static long getRejectedTasks()
	{
		return rejectedTasks.get();
	}
	
	public static long getTaskErrors()
	{
		return taskErrors.get();
	}
	
	private static final class SafeTask implements Runnable
	{
		private final String name;
		private final Runnable task;
		
		SafeTask(String name, Runnable task)
		{
			this.name = safeName(name);
			this.task = task;
		}
		
		@Override
		public void run()
		{
			 final long start = System.currentTimeMillis();
			if (SHUTTING_DOWN)
				return;
			
			try
			{
				task.run();
				 TASK_STATS.computeIfAbsent(name, k -> new TaskStat()).errors.incrementAndGet();
			}
			catch (Throwable t)
			{
				taskErrors.incrementAndGet();
				LOG.log(Level.WARNING, "ThreadPool Task Error [" + name + "]", t);
			}
			finally
			{
			    TASK_STATS.computeIfAbsent(name, k -> new TaskStat()).addRuntime(System.currentTimeMillis() - start);
			}

		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	private static final class NamedThreadFactory implements ThreadFactory
	{
		private final String prefix;
		private final AtomicLong counter = new AtomicLong(1);
		
		NamedThreadFactory(String prefix)
		{
			this.prefix = prefix;
		}
		
		@Override
		public Thread newThread(Runnable r)
		{
			final Thread t = new Thread(r);
			t.setName(prefix + "-" + counter.getAndIncrement());
			t.setDaemon(false);
			t.setPriority(Thread.NORM_PRIORITY);
			t.setUncaughtExceptionHandler((thread, throwable) -> LOG.log(Level.WARNING, "Uncaught exception in thread " + thread.getName(), throwable));
			return t;
		}
	}
}
