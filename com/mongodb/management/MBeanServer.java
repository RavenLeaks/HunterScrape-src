/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.management;

@Deprecated
public interface MBeanServer {
    public void unregisterMBean(String var1);

    public void registerMBean(Object var1, String var2);
}

