/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.spring;

import static java.lang.String.format;
import static java.util.Optional.empty;
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
   * @param inputStream the input stream with the XML configuration content.
   * @return a new {@link Document} object with the provided content.
   */
  public Document loadDocument(InputStream inputStream) {
    return loadDocument(empty(), inputStream);
  }

  public Document loadDocument(Optional<ModuleExtensionStore> moduleExtensionStore, InputStream inputStream) {
    try {
      final MuleLoggerErrorHandler errorHandler = new MuleLoggerErrorHandler();
      Document document = new MuleDocumentLoader()
          .loadDocument(new InputSource(inputStream),
                        new ModuleDelegatingEntityResolver(moduleExtensionStore), errorHandler,
                        VALIDATION_XSD, true);
      errorHandler.throwExceptionIfErrorsWereFound();
      return document;
    } catch (Exception e) {
      throw new MuleRuntimeException(e);
    }
  }

  /**
   * helper class to gather all errors while applying the found XSDs for the current input stream
   */
  private static class MuleLoggerErrorHandler extends DefaultHandler {

    List<String> errors = new ArrayList<>();

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(format("Found a fatal exception parsing document, message '%s'", e.toString()), e);
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
        LOGGER.debug(format("Found exception parsing document, message '%s'", errorMessage), e);
      }
      errors.add(errorMessage);
    }

    public boolean hasErrors() {
      return !errors.isEmpty();
    }

    public String getErrors() {
      final StringBuilder sb =
          new StringBuilder(format("There are '%s' errors while parsing the file, full list:", errors.size()));
      errors.stream().forEach(error -> sb.append(error).append("\n"));
      return sb.toString();
    }

    public void throwExceptionIfErrorsWereFound() {
      if (hasErrors()) {
        throw new IllegalArgumentException(getErrors());
      }
    }

  }
}
