package com.gzshixiang.common.utils;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {


    private final static String TAG =ThreadPoolManager.class.getSimpleName();

    private static ThreadPoolManager instance = null;

    private ThreadPoolExecutor mThreadPoolExecutor;
    //线程数量,核心线程数
    private int corePoolSize = Runtime.getRuntime().availableProcessors();
    //池中允许的最大线程数
    private int maximumPoolSize = 10;
    //设置空闲线程的空闲时间，如果一共有6个线程，那么超出核心线程数的两个线程就会被记录时间，超过该时间就会被杀死，如果没有超过核心线程数，那么线程是不会被倒计时的。
    private long keepAliveTime = 10;
    //等待执行的容器容量大小
    private int capacity = 10;
    //拒绝后的执行任务容器--》凉快的地方
    private LinkedBlockingQueue taskQueue = new LinkedBlockingQueue();


    public synchronized static ThreadPoolManager getInstence() {
        if (instance == null) {
            synchronized (ThreadPoolManager.class) {
                if (instance == instance) {
                    instance = new ThreadPoolManager();

                }
            }
        }
        return instance;
    }

    /**
     * 构造方法里面就初始化线程池
     * ArrayBlockingQueue是一个执行任务的容量，当调用mThreadPoolExecutor的execute，容量加1，执行run完后，容量减1
     * ArrayBlockingQueue后面传入true就是以FIFO规则存储：先进先出
     */
    public ThreadPoolManager() {
        if (mThreadPoolExecutor == null) {
            mThreadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(capacity, true), handler);
        }
        //开启线程一直循环从等待队列里面取出可执行任务并执行
        mThreadPoolExecutor.execute(runnable);
    }

    /**
     * 往队列里面存入可执行任务
     *
     * @param runnable
     */
    public void putExecutableTasks(Runnable runnable) {
        try {
            taskQueue.put(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * ThreadPoolExecutor的run
     */
    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            //开启循环
            while (true) {
                //取出等待的执行任务
                Runnable taskQueueRunnable = null;
                try {
                    //FLog.d(TAG, "等待队列大小：" + taskQueue.size());
                    taskQueueRunnable = (Runnable) taskQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (runnable != null) {
                    mThreadPoolExecutor.execute(taskQueueRunnable);
                }
                //FLog.d(TAG, "线程池大小" + mThreadPoolExecutor.getPoolSize());
            }
        }
    };

    /**
     * 拒绝策略
     * 当ArrayBlockingQueue容量过大，就要执行拒绝策略，对来的执行任务说：放不下了，先到一边凉快去，那么就要有一个凉快的容器撞他们
     */
    private RejectedExecutionHandler handler = new RejectedExecutionHandler() {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                taskQueue.put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}
