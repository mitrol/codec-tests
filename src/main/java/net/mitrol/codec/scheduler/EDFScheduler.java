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

/**
 * Non-preemptive EDF scheduler implementation.
 *
 * @author kulikov
 */
public class EDFScheduler {
    //system clock
    private Clock clock;
    //Task executors (CPUs)
    private CPU cpu[];

    /**
     * Creates new scheduler.
     *
     * @param cpuNum the number of available CPUs.
     */
    public EDFScheduler(int cpuNum) {
        cpu = new CPU[cpuNum];
        for (int i = 0; i < cpuNum; i++) {
            cpu[i] = new CPU(String.format("CPU[%d]", i));
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
    public boolean submit(Task task) {
        //first fit heuristic
        for (int i = 0; i < cpu.length; i++) {
            if (cpu[i].accept(task)) return true;
        }
        return false;
    }

    /**
     * Starts scheduler.
     */
    public void start() {
        for (int i = 0; i < cpu.length; i++) {
            cpu[i].isActive = true;
            cpu[i].start();
        }
    }

    /**
     * Stops scheduler.
     */
    public void stop() {
        for (int i = 0; i < cpu.length; i++) {
            cpu[i].isActive = false;
            cpu[i].interrupt();
        }
    }

    /**
     * Representation of the CPU
     */
    private class CPU extends Thread {
        //Activity flag
        protected volatile boolean isActive;
        //EDF task queue
        private DigitalTree queue = new DigitalTree();
        //Task been running
        private Task task;
        //Task execution stats
        private long startTime, duration;
        //CPU utilization
        private double U;

        /**
         * Creates new CPU.
         *
         * @param name the name of the CPU
         */
        public CPU(String name) {
            super(name);
        }

        /**
         * Queues task if this is possible.
         *
         * @param task the task for execution
         * @return true if task was accepted and false if task was rejected
         */
        public synchronized boolean accept(Task task) {
            if (isFeasible(task)) {
                queue.offer(task);

                //wake up if waiting
                notify();
                return true;
            }

            return false;
        }

        /**
         * Feasibility test.
         *
         * @param task the new task for checking scheduler feasibility
         * @return true if resulting task set will be feasible
         */
        private boolean isFeasible(Task task) {
            //TODO: make it sufficient
            //hack: allow to execute short task
            if (task.interarrivalTime == 0) {
                return task.duration < 1000000L;
            }

            //determine new utilization
            double util = U + ((double) task.duration / (double) task.interarrivalTime);

            //if utilization remains less then one allow to execute task
            if (util < 1) {
                U = util;
                return true;
            }

            return false;
        }

        @Override
        public void run() {
            while (this.isActive) {
                try {
                    perform();
                } catch (Exception e) {
                }
            }
        }

        private void perform() {
            //idle if no tasks in queue
            if (queue.isEmpty()) {
                idle(clock.getTime() + 1000000L);
                return;
            }

            //extract task with highest priority
            task = queue.poll();

            //idle CPU before allowed release time
            if (clock.getTime() < task.releaseTime) {
                //don't foget to return task back
                queue.offer(task);
                //start idleing
                idle(task.releaseTime);
                return;
            }

            //release time allowes execution
            //process task and measure execution time
            startTime = clock.getTime();
            task.perform();
            duration = clock.getTime() - startTime;

            //task finished, updating utilization factor
            U -= (double) task.duration / (double) task.interarrivalTime;

            //remember worst case execution time
            task.duration = (task.duration + duration) / 2;
        }

        /**
         * Idles CPUs till time specified.
         *
         * @param time the time to wake up in nanoseconds.
         */
        private synchronized void idle(long time) {
            //determine number of whole milliseconds
            long ms = (time - clock.getTime()) / 1000000;

            //idle CPU for milliseconds
            try {
                if (ms > 0) {
                    wait(ms, 0);
                }
            } catch (InterruptedException e) {
                return;
            }

            //spin for fraction of milliseconds
            while (clock.getTime() < time) {
            }
        }
    }
}
