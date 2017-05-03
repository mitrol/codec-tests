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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements queue of tasks.
 *
 * @author kulikov
 */
public class ConcurrentTaskQueue extends TaskQueue {
    long time;
    int pollIndex;
    int N = 2000000;
    int CUT = 1000;
    Task[] tasks1 = new Task[N];
    AtomicInteger size = new AtomicInteger();
    //inner holder for tasks
    int u = 0;

    public ConcurrentTaskQueue() {
    }

    /**
     * Shows if this queue is empty.
     *
     * @return true if queue is currently empty
     */
    public synchronized boolean isEmpty() {
        return size.get() == 0;
    }

    /**
     * Queues specified task using tasks dead line time.
     *
     * @param task the task to be queued.
     * @return TaskExecutor for the scheduled task.
     */
    public void accept(Task task) {
        //u++;
        ////(u%100==0) {
        //	System.out.println(System.currentTimeMillis() + " " + task);
        //}
        long dead = (System.nanoTime() - task.getDeadLine()) / CUT;
        if (dead > N) {
            //System.out.println(task.toString() + " " + dead);
        }//System.out.println(task.toString() + " " + dead);
        //the smaller priority value means that task will reside
        int index = (int) ((task.getDeadLine() / CUT) % N);
        boolean spotFound = false;
        int step = 100;
        //System.out.println("\n\nAccpeting " + index + " by " + task);
        while (!spotFound && step-- > 0 && index >= 0) {

            Task t = tasks1[(int) index];
            if (t != null) {
                //System.out.println("Spot taken " + index + " by " + t);
                if (t.getDeadLine() > task.getDeadLine()) {
                    //System.out.println("Swapping " + t + " out of " + index + " with " + task);
                    tasks1[index] = task;
                    task = t;
                    continue;
                }
                if (t.getPriority() > task.getPriority()) {
                    index--;
                    if (index < 0) index = N + index;
                    //System.out.println("Adjusting index " + index + " by " + t);
                } else {
                    index++;
                    //System.out.println("Adjusting index " + index + " by " + t);
                }
            } else {
                spotFound = true;
                //System.out.println("Seated " + index + " by " + task);
            }
        }
        if (!spotFound) {
            System.out.println("NO SPOTS FOUND!!!!! Adjust the settings.");
        }
        size.addAndGet(1);
        tasks1[(int) index] = task;

    }


    /**
     * Retrieves the task with earliest dead line and removes it from queue.
     *
     * @return task which has earliest dead line
     */
    public synchronized Task poll() {
        size.addAndGet(-1);
        //int index = (int) ((System.nanoTime()/CUT)%N);
        pollIndex -= 1000;
        if (pollIndex < 0) pollIndex = N + pollIndex;
        //System.out.println("Polling at time " + index + " pollIndex " + pollIndex);
        // if(pollIndex>index) {
        //System.out.println("1 Scan from " + pollIndex + " to " + N);
        for (int q = pollIndex; q < N; q++) {
            Task t = tasks1[q];
            if (t != null) {
                pollIndex = q + 1;
                tasks1[q] = null;
                return t;
            }
        }
        //System.out.println("2 Scan from " + 0 + " to " + index);
        for (int q = 0; q <= pollIndex; q++) {
            Task t = tasks1[q];
            if (t != null) {
                pollIndex = q + 1;
                tasks1[q] = null;
                return t;
            }
        }/*
        } else {
        	System.out.println("3 Scan from " + pollIndex + " to " + index);
        	for(int q=pollIndex;q<N;q++) {
        		Task t = tasks1[q];
        		if(t != null) {
        			pollIndex = q+1;
        			tasks1[q] = null;
        			return t;
        		}
        	}
        	System.out.println("4 Scan from " + 0 + " to " + pollIndex);
        	for(int q=0;q<pollIndex;q++) {
        		Task t = tasks1[q];
        		if(t != null) {
        			pollIndex = q+1;
        			tasks1[q] = null;
        			return t;
        		}
        	}
        }*/
        return null;
    }


    /**
     * Clean the queue.
     */
    public synchronized void clear() {
        for (int q = 0; q < N; q++) tasks1[q] = null;
    }

    /**
     * Gets the size of this queue.
     *
     * @return the size of the queue.
     */
    public int size() {
        return size.get();
    }

    public synchronized int size2() {
        int count = 0;
        for (int q = 0; q < N; q++) if (tasks1[q] != null) count++;
        return count;
    }

    protected synchronized void remove(Task task) {
        for (int q = 0; q < N; q++) if (tasks1[q] == task) tasks1[q] = null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Queue[");
        /*
        int len = Math.min(30, list.size());
        for (int i = 0; i < len -1; i++) {
            //sb.append(list.get(i).getPriority());
            sb.append(",");
        }*/

        //sb.append(list.get(len - 1).getPriority());
        sb.append("]");
        return sb.toString();
    }
}
