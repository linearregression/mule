package org.mule.runtime.module.extension.internal.introspection.describer;

import org.mule.runtime.api.meta.model.ModelProperty;

import java.util.Set;

public class ImplementingExceptionModelProperty implements ModelProperty {

    Set<Class<? extends Exception>> exceptionClasses;

    public ImplementingExceptionModelProperty(Set<Class<? extends Exception>> exceptionClasses) {
        this.exceptionClasses = exceptionClasses;
    }

    @Override
    public String getName() {
        return "Exceppp";
    }

    @Override
    public boolean isExternalizable() {
        return false;
    }

    public Set<Class<? extends Exception>> getExceptionClasses() {
        return exceptionClasses;
    }
}
