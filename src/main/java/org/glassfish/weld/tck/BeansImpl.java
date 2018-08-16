/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.weld.tck;

import org.jboss.cdi.tck.spi.Beans;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BeansImpl implements Beans {

    private final static AtomicLong count = new AtomicLong();

    private final ConcurrentHashMap objects = new ConcurrentHashMap();

    @Override
    public boolean isProxy(Object instance) {
        return instance.getClass().getName().indexOf("_$$_Weld") > 0;
    }

    @Override
    public synchronized byte[] passivate(Object instance) throws IOException {
        // We are not really testing the implementation of passivate and activation.
        //  Just save the object and return it later in activate.
        long curCount = count.getAndIncrement();
        byte[] bytes = ("" + curCount ).getBytes();
        objects.put( bytes, instance );
        return bytes;
    }


    @Override
    public synchronized Object activate(byte[] bytes) throws IOException, ClassNotFoundException {
        // We are not really testing the implementation of passivate and activation.
        //  Just return the object that was saved by passivate.
        return objects.get( bytes );
    }

}
