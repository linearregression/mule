/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.spring;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Optional.empty;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.config.spring.dsl.model.extension.loader.ModuleExtensionStore;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads a mule configuration file into a {@link Document} object.
 *
 * @since 4.0
 */
public class XmlConfigurationDocumentLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(XmlConfigurationDocumentLoader.class);

  /**
   * Indicates that XSD validation should be used (found no "DOCTYPE" declaration).
   */
  private static final int VALIDATION_XSD = 3;

  /**
   * Creates a {@link Document} from an {@link InputStream} with the required configuration
   * of a mule configuration file parsing.
   *
   * @param filename //TODO(Fernandezlautaro) add javadoc
   * @param inputStream the input stream with the XML configuration content.
   * @return a new {@link Document} object with the provided content.
   */
  public Document loadDocument(String filename, InputStream inputStream) {
    return loadDocument(empty(), filename, inputStream);
  }

  //TODO(Fernandezlautaro) add javadoc
  public Document loadDocument(Optional<ModuleExtensionStore> moduleExtensionStore, String filename, InputStream inputStream) {
    final MuleLoggerErrorHandler errorHandler = new MuleLoggerErrorHandler(filename);
    Document document;
    try {
      document = new MuleDocumentLoader()
          .loadDocument(new InputSource(inputStream),
                        new ModuleDelegatingEntityResolver(moduleExtensionStore), errorHandler,
                        VALIDATION_XSD, true);
    } catch (Exception e) {
      throw new MuleRuntimeException(e);
    }
    if (errorHandler.hasErrors()) {
      throw new MuleRuntimeException(createStaticMessage(errorHandler.getErrors()));
    }
    return document;
  }

  /**
   * helper class to gather all errors while applying the found XSDs for the current input stream
   * //TODO(Fernandezlautaro) add javadoc
   */
  private static class MuleLoggerErrorHandler extends DefaultHandler {

    private final String filename;
    List<String> errors = new ArrayList<>();

    //TODO(Fernandezlautaro) add javadoc
    MuleLoggerErrorHandler(String filename) {
      this.filename = filename;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(format("Found a waring exception parsing document, message '%s'", e.toString()), e);
      }
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(format("Found a fatal error exception parsing document, message '%s'", e.toString()), e);
        if (hasErrors()) {
          LOGGER.debug(getErrors());
        }
      }
      throw e;
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
      final String errorMessage = e.toString();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(format("Found error exception parsing document, message '%s'", errorMessage), e);
      }
      errors.add(errorMessage);
    }

    //TODO(Fernandezlautaro) add javadoc
    private String getErrors() {
      final String subMessage = format(errors.size() == 1 ? "was '%s' error" : "were '%s' errors", errors.size());
      final StringBuilder sb =
          new StringBuilder(format("There %s while parsing the file '%s'.", subMessage, filename));
      sb.append(lineSeparator()).append("Full list:");
      errors.stream().forEach(error -> sb.append(lineSeparator()).append(error));
      sb.append(lineSeparator());
      return sb.toString();
    }

    //TODO(Fernandezlautaro) add javadoc
    private boolean hasErrors() {
      return !errors.isEmpty();
    }
  }
}
