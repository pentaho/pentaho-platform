/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.osgi;

/**
 * This {@link org.pentaho.platform.api.engine.IPentahoSystemListener} is a facade for another {@link KarafBoot}.
 * It exists solely to prevent configuration change.
 *
 * @Deprecated This class will be removed in the next major version (6.0). Use KarafBoot instead.
 */
public class OSGIBoot extends KarafBoot {
}
