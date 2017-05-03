/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package net.mitrol.codec.scheduler;

import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;

/**
 * Implements scheduler with multi-level priority queue.
 * <p>
 * This scheduler implementation follows to uniprocessor model with "super" thread.
 * The "super" thread includes IO bound thread and one or more CPU bound threads
 * with equal priorities.
 * <p>
 * The actual priority is assigned to task instead of process and can be
 * changed dynamically at runtime using the initial priority level, feedback
 * and other parameters.
 *
 * @author kulikov
 */
public class Scheduler {
    //priority queue
    protected TaskQueue taskQueue = new TaskQueue();
    //The clock for time measurement
    private Clock clock;
    //CPU bound threads
    private CpuThread[] cpuThread;

    //flag indicating state of the scheduler
    private boolean isActive;

    //locks start and waits when all threads will start.
    private CountDownLatch latch;

    /**
     * the amount of tasks missed their deadline
     */
    private volatile long missCount;

    /**
     * the number of total tasks executed
     */
    private volatile long taskCount;

    /**
     * The allowed time jitter
     */
    private long tolerance = 3000000L;

    //The most worst execution time detected
    private long wet;

    private Friction[] frictions;

    private Logger logger = Logger.getLogger(Scheduler.class);

    /**
     * Creates new instance of scheduler.
     */
    public Scheduler(int cpuNum) {
        Runtime runtime = Runtime.getRuntime();
        runtime.availableProcessors();

        latch = new CountDownLatch(cpuNum);

        cpuThread = new CpuThread[cpuNum];
        for (int i = 0; i < cpuThread.length; i++) {
            cpuThread[i] = new CpuThread(String.format("Scheduler[CPU-%s]", i));
        }

    }

    /**
     * Gets the clock used by this scheduler.
     *
     * @return the clock object.
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Sets clock.
     *
     * @param clock the clock used for time measurement.
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Queues task for execution according to its priority.
     *
     * @param task the task to be executed.
     */
    public void submit(Task task) {
        task.activate();
        taskQueue.accept(task);
    }

    /**
     * Queues chain of the tasks for execution.
     *
     * @param taskChanin the chain of the tasks
     */
    public void submit(TaskChain taskChain) {
        taskChain.start();
    }

    /**
     * Starts scheduler.
     */
    public void start() {
        if (clock == null) {
            throw new IllegalStateException("Clock is not set");
        }

        this.isActive = true;

        logger.info("Starting ");

        //prepare frictions first
//        frictions = new Friction[cpuThread.length];
//        for (int i = 0; i < cpuThread.length; i++) {
//            frictions[i] = new Friction(this);
//            frictions[i].setDeadLine(clock.getTime() + 1000000L * i);

//            taskQueue.accept(frictions[i]);
//        }

        //start threads now
        for (int i = 0; i < cpuThread.length; i++) {
            cpuThread[i].start();
        }

        //wait when threads start
        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        logger.info("Started ");
    }

    /**
     * Stops scheduler.
     */
    public void stop() {
        if (!this.isActive) {
            return;
        }

        latch = new CountDownLatch(cpuThread.length);
        this.isActive = false;

        for (int i = 0; i < cpuThread.length; i++) {
            cpuThread[i].shutdown();
        }

        //wait when threads stop
        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        taskQueue.clear();
    }

    /**
     * Shows the miss rate.
     *
     * @return the miss rate value;
     */
    public double getMissRate() {
        return taskCount > 0 ? (double) missCount / (double) taskCount : 0D;
    }

    public long getWorstExecutionTime() {
        return wet;
    }

    /**
     * Executor thread.
     */
    private class CpuThread extends Thread {
        private Task t;
        private volatile boolean active;

        public CpuThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            this.active = true;
            latch.countDown();
            while (active) {
/*                long next = taskQueue.getNextTaskDeadline();
                if (next == 0) {
                    synchronized(this) {
                        try {
                            wait(1,0);
                            continue;
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
                
                long idle = next - clock.getTime();
                if (idle > 0) {
                    synchronized(this) {
                        try {
                            wait(1,0);
                            continue;
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
  */
                //load task with highest priority and execute it.
                t = taskQueue.poll(clock.getTime());

                //if task has been canceled take another one
                if (t == null) {
                    synchronized (this) {
                        try {
                            wait(1, 0);
                        } catch (InterruptedException e) {
                        }
                    }
                    continue;
                }

                //System.out.println("This here spins couple of times per MILLISECOND " + System.currentTimeMillis());
                //to Vladimir:
                //see Spuri, M.: 1996b, "Holistic Analysis for Deadline Scheduled Real-Time Distributed
                //Systems". Technical Report RR-2873, INRIA, France

                try {
                    //update miss rate countor
                    long now = clock.getTime();

                    //increment task countor
                    taskCount++;

                    //check for missing dead line.
                    if (now - t.getDeadLine() > tolerance) {
                        missCount++;
                    }

                    //execute task
                    t.run();

                    //determine worst execution time
                    long duration = clock.getTime() - now;
                    if (duration > wet) {
                        wet = duration;
                    }
                } catch (Exception e) {
                }
            }

            latch.countDown();
        }

        /**
         * Terminates thread.
         */
        private void shutdown() {
            this.active = false;
        }
    }

}
