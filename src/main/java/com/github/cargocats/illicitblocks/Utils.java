package com.github.cargocats.illicitblocks;

import org.apache.commons.lang3.function.FailableSupplier;

public class Utils {
    public static <T> T rethrowing(FailableSupplier<T, Throwable> getter) {
        try {
            return getter.get();
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException rethrow(Throwable t) throws T {
        throw (T) t;
    }

    public static void debugLog(String format, Object... args) {
        if (!IllicitBlocks.DEBUG_LOGGING) return;
        IllicitBlocks.LOG.info(format, args);
    }
}