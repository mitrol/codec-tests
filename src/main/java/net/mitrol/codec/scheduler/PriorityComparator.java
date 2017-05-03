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

import java.util.Comparator;

/**
 * Compares priority of two tasks.
 *
 * @author kulikov
 */
public class PriorityComparator implements Comparator<Task> {

    /**
     * (Non Java-doc).
     *
     * @see Comparator#compare(Object, Object)
     */
    public int compare(Task t1, Task t2) {
        if (t1.getDeadLine() < t2.getDeadLine()) {
            return -1;
        } else if (t1.getDeadLine() == t2.getDeadLine()) {
            return 0;
        } else {
            return 1;
        }
    }
}
