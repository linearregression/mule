/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.integration.routing.outbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.mule.functional.functional.FlowAssert;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.tck.testmodels.fruit.Apple;
import org.mule.tck.testmodels.fruit.Banana;
import org.mule.tck.testmodels.fruit.FruitBowl;
import org.mule.tck.testmodels.fruit.Orange;
import org.mule.test.AbstractIntegrationTestCase;

import java.util.List;

import org.junit.Test;

public class ExpressionSplitterSyncTestCase extends AbstractIntegrationTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/test/integration/routing/outbound/expression-splitter-sync-test-flow.xml";
  }

  @Test
  public void testRecipientList() throws Exception {
    FruitBowl fruitBowl = new FruitBowl(new Apple(), new Banana());
    fruitBowl.addFruit(new Orange());

    InternalMessage result = flowRunner("Distributor").withPayload(fruitBowl).run().getMessage();

    assertNotNull(result);
    assertTrue(result.getPayload().getValue() instanceof List);
    assertEquals(3, ((List) result.getPayload().getValue()).size());

    FlowAssert.verify();
  }
}
