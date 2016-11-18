/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.objectbuilder;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.module.extension.internal.introspection.ParameterGroupDescriptor;
import org.mule.runtime.module.extension.internal.runtime.ExecutionContextAdapter;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSetResult;
import org.mule.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;

public class ParameterGroupObjectBuilder<T> extends DefaultObjectBuilder<T> {

  private final ParameterGroupDescriptor groupDescriptor;

  public ParameterGroupObjectBuilder(ParameterGroupDescriptor groupDescriptor) {
    super(groupDescriptor.getType().getDeclaringClass());
    this.groupDescriptor = groupDescriptor;
  }

  public T build(ExecutionContextAdapter executionContext) throws MuleException {
    groupDescriptor.getType().getAnnotatedFields(Parameter.class).forEach(field -> {
      String fieldName = field.getAlias();
      if (executionContext.hasParameter(fieldName)) {
        addPropertyResolver(fieldName, new StaticValueResolver<>(executionContext.getParameter(fieldName)));
      }
    });

    return build(executionContext.getEvent());
  }

  public T build(ResolverSetResult result) throws MuleException {
    result.asMap().forEach((k, v) -> addPropertyResolver(k, new StaticValueResolver<>(v)));
    return build(Event.getCurrentEvent());
  }
}
