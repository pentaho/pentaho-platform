/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.olap;

import javax.jcr.AccessDeniedException;

public class IOlapServiceException extends RuntimeException {

    private static final long serialVersionUID = 1852374894433624504L;

    public static enum Reason {
      GENERAL, ACCESS_DENIED, ALREADY_EXISTS, XMLA_SCHEMA_NAME_EXISTS;
      public static Reason convert(Throwable t) {
          if (t instanceof AccessDeniedException) {
              return Reason.ACCESS_DENIED;
          }
          return Reason.GENERAL;
      }
    };

    private Reason reason;

    public IOlapServiceException() {
      super();
    }

    public IOlapServiceException(final String msg, final Throwable throwable) {
      this(msg, throwable, Reason.GENERAL);
    }

    public IOlapServiceException(final String msg, final Throwable throwable, final Reason reason) {
      super(msg, throwable);
      this.reason = reason;
    }

    public IOlapServiceException(final String msg) {
      this(msg, Reason.GENERAL);
    }

    public IOlapServiceException(final String msg, final Reason reason) {
      super(msg);
      this.reason = reason;
    }

    public IOlapServiceException(final Throwable throwable) {
      this(throwable, Reason.GENERAL);
    }

    public IOlapServiceException(final Throwable throwable, final Reason reason) {
      super(throwable);
      this.reason = reason;
    }

    public Reason getReason() {
      return reason;
    }

  }
