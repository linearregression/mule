/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.policy;

import org.mule.runtime.core.api.Event;

import java.io.Serializable;

/**
 * A {@code Policy} is responsible to handle the state of a policy applied to a particular execution.
 *
 * @since 4.0
 */
public interface Policy extends Serializable {

  /**
   * Process the policy chain of processors. The provided {@code nextOperation} function has the behaviour
   * to be executed by the next-operation of the chain.
   *
   * @param event the event with the data to execute the policy
   * @param nextOperation the next-operation processor implementation
   * @return the result of processing the {@code event} through the policy chain.
   * @throws Exception
   */
  Event process(Event event, NextOperation nextOperation) throws Exception;

  /**
   * This method provides access to the modified event by the policy chain before the next-operation
   * execution.
   *
   * This event is required for routing the event to another pipeline in the case of a failure and keep
   * the policy pipeline variables in context.
   *
   * @return the event prior to the next-operation execution.
   */
  Event getNextOperationEvent();

}
