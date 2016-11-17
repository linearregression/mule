/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.internal.support;

import static java.lang.String.format;
import static javax.jms.DeliveryMode.NON_PERSISTENT;
import static javax.jms.DeliveryMode.PERSISTENT;
import static org.mule.extensions.jms.api.connection.JmsSpecification.JMS_1_1;
import org.mule.extensions.jms.api.connection.JmsSpecification;
import org.mule.extensions.jms.api.connection.LookupJndiDestination;
import org.mule.extensions.jms.api.destination.ConsumerType;
import org.mule.extensions.jms.api.destination.TopicConsumer;

import java.util.Optional;
import java.util.function.Function;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Jms11Support</code> is a template class to provide an abstraction to to
 * the JMS 1.1 API specification.
 *
 * @since 4.0
 */
public class Jms11Support extends Jms20Support {

  private Logger logger = LoggerFactory.getLogger(Jms11Support.class);

  public Jms11Support() {
    super();
  }

  public Jms11Support(LookupJndiDestination lookupJndiDestination, Function<String, Optional<Destination>> jndiObjectSupplier) {
    super(lookupJndiDestination, jndiObjectSupplier);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JmsSpecification getSpecification() {
    return JMS_1_1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageConsumer createConsumer(Session session, Destination destination, String messageSelector, ConsumerType type)
      throws JMSException {

    if (!type.isTopic()) {
      return session.createConsumer(destination, messageSelector);
    }

    TopicConsumer topicConsumer = (TopicConsumer) type;
    if (topicConsumer.isDurable()) {
      return session.createDurableSubscriber((Topic) destination, topicConsumer.getSubscriptionName(), messageSelector,
                                             topicConsumer.isNoLocal());
    }

    return session.createConsumer(destination, messageSelector, topicConsumer.isNoLocal());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void send(MessageProducer producer, Message message, Destination dest, boolean persistent, int priority,
                   long ttl, boolean topic)
      throws JMSException {
    if (logger.isDebugEnabled()) {
      logger.debug(format("Sending message to [%s], persistent:[%s], with priority:[%s] and ttl:[%s]",
                          dest instanceof Queue ? ((Queue) dest).getQueueName() : ((Topic) dest).getTopicName(),
                          persistent, priority, ttl));
    }
    producer.send(dest, message, persistent ? PERSISTENT : NON_PERSISTENT, priority, ttl);
  }

}
