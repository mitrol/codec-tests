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
package net.mitrol.voip.codecs.scheduler;

/**
 *
 * @author Dmitriy Gorbunov
 */

package net.mitrol.codec.scheduler;

class DigitalTree {
    private Task root;

    public DigitalTree() {
        root = null;
    }

    public synchronized boolean isEmpty() {
        if (root != null)
            return false;
        else
            return true;
    }

    public synchronized void offer(Task task) {
        if (root == null) {
            root = task;
            task.left = null;
            task.right = null;
        } else {
            long priority = task.getDeadLine();
            long k = 1; //start with the second bit
            long bit = 0;
            Task pos = root; //current node
            Task prev = null; //previous node

            while (pos != null) {
                bit = (priority << k) >>> 63;
                prev = pos;

                if (bit == 0)
                    pos = pos.left;
                else
                    pos = pos.right;

                k++;
            }

            if (bit == 0)
                prev.left = task;
            else
                prev.right = task;

            task.left = null;
            task.right = null;
        }
    }

    public synchronized Task poll() {
        if (isEmpty()) {
            return null;
        }

        Task min = root;
        Task pos = root;
        Task prevMin = null;
        Task prev = null;

        //search task with the lowest priority
        while (true) {
            if (min.getDeadLine() > pos.getDeadLine()) {
                min = pos;
                prevMin = prev;
            }

            if (pos.left != null) {
                prev = pos;
                pos = pos.left;
            } else if (pos.right != null) {
                prev = pos;
                pos = pos.right;
            } else {
                break;
            }
        }

        //remove task with the lowest priority
        if (min == root) {
            //remove the root
            if (pos == root) {
                root = null;
            } else {
                if (prev.left == pos) {
                    prev.left = null;
                } else {
                    prev.right = null;
                }

                pos.left = root.left;
                pos.right = root.right;
                root = pos;
            }

        } else if (min == pos) {
            //remove the center
            if (prev.left == pos) {
                prev.left = null;
            } else {
                prev.right = null;
            }
        } else {
            //remove the end
            if (prev.left == pos) {
                prev.left = null;
            } else {
                prev.right = null;
            }

            if (prevMin.left == min) {
                prevMin.left = pos;
            } else {
                prevMin.right = pos;
            }

            pos.left = min.left;
            pos.right = min.right;
        }

        return min;
    }
}
