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
 * Implemnets immutable task executor
 *
 * @author kulikov
 */
public final class TaskExecutor {
    protected Task task;
    protected long priority;
    protected boolean isActive = true;
    private TaskQueue queue;

    protected TaskExecutor(TaskQueue queue) {
        this.queue = queue;
    }

    public void cancel() {
        synchronized (task) {
            this.isActive = false;
            //queue.remove(this);
        }
    }

    public void perform() {
        synchronized (task) {
            //check task 
            if (task != null && this.isActive) {
                try {
                    //execute task
                    System.out.println("Start");
                    task.perform();
                    System.out.println("Done");
                } catch (Throwable e) {
                    System.out.println("Error detected");
                    //handle error if error handler assigned
                    if (task.listener != null) {
                        task.listener.handlerError(null);
                    }
                }
            }
        }
    }

}
