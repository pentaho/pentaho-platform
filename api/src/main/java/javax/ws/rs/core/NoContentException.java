/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package jakarta.ws.rs.core;

import java.io.IOException;

/**
 * An I/O exception thrown by {@link jakarta.ws.rs.ext.MessageBodyReader} implementations
 * when reading a zero-length message content to indicate that the message body reader
 * is not able to produce an instance representing an zero-length message content.
 * <p>
 * This exception, when thrown while reading a server request entity, is automatically
 * translated by JAX-RS server runtime into a {@link jakarta.ws.rs.BadRequestException}
 * wrapping the original {@code NoContentException} and rethrown for a standard processing by
 * the registered {@link jakarta.ws.rs.ext.ExceptionMapper exception mappers}.
 * </p>
 */
public class NoContentException extends IOException {
    private static final long serialVersionUID = -3082577759787473245L;

    /**
     * Construct a new {@code NoContentException} instance.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     */
    public NoContentException(String message) {
        super(message);
    }

    /**
     * Construct a new {@code NoContentException} instance.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the underlying cause of the exception.
     */
    public NoContentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a new {@code NoContentException} instance.
     *
     * @param cause the underlying cause of the exception.
     */
    public NoContentException(Throwable cause) {
        super(cause);
    }
}
