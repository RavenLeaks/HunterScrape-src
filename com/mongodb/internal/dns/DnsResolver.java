/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.dns;

import java.util.List;

public interface DnsResolver {
    public List<String> resolveHostFromSrvRecords(String var1);

    public String resolveAdditionalQueryParametersFromTxtRecords(String var1);
}

