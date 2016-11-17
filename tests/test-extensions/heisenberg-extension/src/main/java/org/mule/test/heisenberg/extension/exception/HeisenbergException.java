/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.heisenberg.extension.exception;

import org.mule.runtime.extension.api.annotation.error.ErrorType;

@ErrorType(value = "", parent = "")
public class HeisenbergException extends Exception {

  public HeisenbergException(String message) {
    super(message);
  }

  public HeisenbergException(String message, Throwable cause) {
    super(message, cause);
  }
}
