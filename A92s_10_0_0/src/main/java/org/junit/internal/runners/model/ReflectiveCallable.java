package org.junit.internal.runners.model;

import java.lang.reflect.InvocationTargetException;

public abstract class ReflectiveCallable {
    /* access modifiers changed from: protected */
    public abstract Object runReflectiveCall() throws Throwable;

    public Object run() throws Throwable {
        try {
            return runReflectiveCall();
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
