/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.management.jmx;

import com.mongodb.diagnostics.logging.Logger;
import com.mongodb.diagnostics.logging.Loggers;
import com.mongodb.management.MBeanServer;
import java.lang.management.ManagementFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

public class JMXMBeanServer
implements MBeanServer {
    private static final Logger LOGGER = Loggers.getLogger("management");
    private final javax.management.MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    @Override
    public void registerMBean(Object mBean, String mBeanName) {
        try {
            this.server.registerMBean(mBean, new ObjectName(mBeanName));
        }
        catch (Exception e) {
            LOGGER.warn("Unable to register MBean " + mBeanName, e);
        }
    }

    @Override
    public void unregisterMBean(String mBeanName) {
        try {
            ObjectName objectName = new ObjectName(mBeanName);
            if (this.server.isRegistered(objectName)) {
                this.server.unregisterMBean(objectName);
            }
        }
        catch (Exception e) {
            LOGGER.warn("Unable to unregister MBean " + mBeanName, e);
        }
    }
}

