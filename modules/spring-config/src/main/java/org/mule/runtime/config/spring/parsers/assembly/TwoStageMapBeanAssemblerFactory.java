/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.spring.parsers.assembly;

import org.mule.runtime.config.spring.parsers.assembly.configuration.PropertyConfiguration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public class TwoStageMapBeanAssemblerFactory implements BeanAssemblerFactory {

  private BeanAssemblerStore store;

  public TwoStageMapBeanAssemblerFactory(BeanAssemblerStore store) {
    this.store = store;
  }

  public BeanAssembler newBeanAssembler(PropertyConfiguration beanConfig, BeanDefinitionBuilder bean,
                                        PropertyConfiguration targetConfig, BeanDefinition target) {
    return new TwoStageMapBeanAssembler(store, beanConfig, bean, targetConfig, target);
  }

  public interface BeanAssemblerStore {

    public void saveBeanAssembler(BeanAssembler beanAssembler);

  }


}
