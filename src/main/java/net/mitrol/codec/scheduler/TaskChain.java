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
 * Task set where each previous task after termination submits next task.
 *
 * @author kulikov
 */
public class TaskChain implements TaskListener {
    //the chain of tasks
    private Task[] task;

    //time ofset between tasks
    private long[] offset;

    //write index
    private int wi;

    //executor index
    private int i;

    //event listener
    private TaskChainListener listener;

    /**
     * Creates new chain.
     *
     * @param length the length of the chain.
     */
    public TaskChain(int length) {
        this.task = new Task[length];
        this.offset = new long[length];
    }

    /**
     * Modifies listener associated with this task chain.
     *
     * @param listener the listener instance.
     */
    public void setListener(TaskChainListener listener) {
        this.listener = listener;
    }

    /**
     * Adds task to the chain.
     *
     * @param task
     * @param offset
     */
    public synchronized void add(Task task, long offset) {
        //terminated task will be selected immediately before start
        task.chain = this;
        task.setListener(this);

        this.task[wi] = task;
        this.offset[wi] = offset;

        wi++;
    }

    /**
     * Starts the chain
     */
    protected void start() {
        //reset index
        i = 0;
        //submit first task
        task[0].setDeadLine(task[0].scheduler.getClock().getTime() + offset[0]);
        task[0].scheduler.submit(task[0]);
    }

    /**
     * Submits next task for the execution
     */
    private void continueExecution() {
        //increment task index
        i++;

        //submit next if the end of the chain not reached yet
        if (i < task.length && task[i] != null) {
            task[i].setDeadLine(task[i].scheduler.getClock().getTime() + offset[i]);
            task[i].scheduler.submit(task[i]);
        } else if (listener != null) {
            listener.onTermination();
        }
    }

    /**
     * (Non Java-doc.)
     *
     * @see org.mobicents.media.server.scheduler.TaskErrorHandler#handlerError(Exception)
     */
    public void handlerError(Exception e) {
        if (listener != null) {
            listener.onException(e);
        }
    }

    /**
     * Gets access to the subtasks.
     *
     * @return subtasks array.
     */
    protected Task[] getTasks() {
        return task;
    }

    /**
     * Clean all tasks
     */
    public void clean() {
        wi = 0;
    }

    public void onTerminate() {
        continueExecution();
    }
}
