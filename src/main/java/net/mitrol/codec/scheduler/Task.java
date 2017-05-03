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
 * Scheduling task.
 *
 * @author kulikov
 */
public abstract class Task {

    protected volatile long deadline;
    protected Scheduler scheduler;
    //error handler instance
    protected TaskListener listener;
    //reference to the chain
    protected TaskChain chain;
    //task duration
    protected long duration;
    //task period if task is periodic
    protected long interarrivalTime;
    //release time
    protected long releaseTime;
    Task left; //for DigitalTree
    Task right;
    private volatile boolean isActive = true;

    public Task(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Scheduler scheduler() {
        return this.scheduler;
    }

    /**
     * Modifies task listener.
     *
     * @param listener the handler instance.
     */
    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    /**
     * Current priority of this task.
     *
     * @return the value of priority
     */
    public abstract long getPriority();

    /**
     * Dead line on absolute clock when this task must be executed
     *
     * @return the value in nanoseconds.
     */
    public long getDeadLine() {
        return deadline;
    }

    /**
     * Modifies dead line of the task.
     *
     * @param d the new dead line value.
     */
    public void setDeadLine(long d) {
        this.deadline = d;
    }

    /**
     * Worst execution time of this task.
     *
     * @return duration expressed in nanoseconds.
     */
    public abstract long getDuration();

    /**
     * Executes task.
     *
     * @return dead line of next execution
     */
    public abstract long perform();

    /**
     * Cancels task execution
     */
    public synchronized void cancel() {
        this.isActive = false;
        scheduler.taskQueue.remove(this);
    }

    protected synchronized void run() {
        if (this.isActive) {
            try {
                perform();

                //notify listenet
                if (this.listener != null) {
                    this.listener.onTerminate();
                }
                //submit next partition
//                if (chain != null) chain.continueExecution();
            } catch (Exception e) {
                if (this.listener != null) listener.handlerError(e);
            }
        }
    }

    protected synchronized void activate() {
        this.isActive = true;
    }
}
