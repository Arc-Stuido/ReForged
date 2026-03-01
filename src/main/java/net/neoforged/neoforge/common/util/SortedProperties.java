package net.neoforged.neoforge.common.util;

import java.util.Properties;
import java.util.Collections;
import java.util.Enumeration;
import java.util.TreeSet;

/**
 * A Properties subclass that stores entries in sorted key order.
 */
public class SortedProperties extends Properties {
    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<>(super.keySet()));
    }
}
