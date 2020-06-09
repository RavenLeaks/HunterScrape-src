/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.management;

import com.mongodb.management.MBeanServer;

@Deprecated
public class NullMBeanServer
implements MBeanServer {
    @Override
    public void unregisterMBean(String mBeanName) {
    }

    @Override
    public void registerMBean(Object mBean, String mBeanName) {
    }
}

