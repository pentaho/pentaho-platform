/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package javax.ws.rs.core;

import java.io.IOException;

/**
 * An I/O exception thrown by {@link javax.ws.rs.ext.MessageBodyReader} implementations
 * when reading a zero-length message content to indicate that the message body reader
 * is not able to produce an instance representing an zero-length message content.
 * <p>
 * This exception, when thrown while reading a server request entity, is automatically
 * translated by JAX-RS server runtime into a {@link javax.ws.rs.BadRequestException}
 * wrapping the original {@code NoContentException} and rethrown for a standard processing by
 * the registered {@link javax.ws.rs.ext.ExceptionMapper exception mappers}.
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
