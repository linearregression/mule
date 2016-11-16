/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws;

import static javax.xml.ws.Endpoint.publish;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.mule.extension.ws.consumer.SimpleService;
import org.mule.extension.ws.internal.introspection.WsdlIntrospecter;
import org.mule.metadata.xml.XmlTypeLoader;
import org.mule.tck.junit4.rule.DynamicPort;

import javax.xml.ws.Endpoint;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public abstract class WscUnitTestCase {

  @ClassRule
  public static DynamicPort operationsPort = new DynamicPort("operationsPort");

  public static final String OPERATIONS_URL = "http://localhost:" + operationsPort.getValue() + "/test";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private static Endpoint service;
  protected WsdlIntrospecter introspecter;
  protected XmlTypeLoader loader;

  @BeforeClass
  public static void startService() {
    service = publish(OPERATIONS_URL, new SimpleService());
    assertThat(service.isPublished(), is(true));
  }

  @Before
  public void setup() {
    XMLUnit.getIgnoreWhitespace();
    introspecter = new WsdlIntrospecter(OPERATIONS_URL + "?wsdl", "TestService", "TestPort");
    loader = new XmlTypeLoader(introspecter.getSchemas());
  }

  @AfterClass
  public static void shutDownService() {
    service.stop();
  }
}
