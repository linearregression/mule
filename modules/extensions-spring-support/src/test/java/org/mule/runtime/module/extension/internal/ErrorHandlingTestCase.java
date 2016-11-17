package org.mule.runtime.module.extension.internal;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mule.functional.junit4.ExtensionFunctionalTestCase;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.core.api.connector.ConnectionManager;
import org.mule.runtime.core.exception.MessagingException;
import org.mule.test.heisenberg.extension.HeisenbergExtension;

public class ErrorHandlingTestCase extends ExtensionFunctionalTestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Override
    protected Class<?>[] getAnnotatedExtensionClasses() {
        return new Class<?>[] {HeisenbergExtension.class};
    }


    @Override
    protected String[] getConfigFiles() {
        return new String[] {"heisenberg-operation-config.xml"};
    }

    @Test
    public void extensionWithExceptionEnricher() throws Throwable {
        MessagingException exception = flowRunner("callGus").runExpectingException();
        ErrorType errorType = exception.getEvent().getError().get().getErrorType();
    }

}
