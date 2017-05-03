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
 * @author kulikov
 * @author gorbunov
 * @author kuzovikhin
 */
public class DSTTree {
    /**
     * Queues specified task using tasks dead line time.
     *
     * @param task the task to be queued.
     * @return TaskExecutor for the scheduled task.
     */
    public synchronized void accept(Task task) {

    }

    /**
     * Retrieves the task with earliest dead line and removes it from queue.
     *
     * @return task which has earliest dead line
     */
    public synchronized Task poll() {
        return null;
    }

    protected synchronized void remove(Task task) {
    }

}
