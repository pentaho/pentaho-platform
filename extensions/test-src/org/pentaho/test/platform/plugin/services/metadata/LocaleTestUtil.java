/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LocaleTestUtil {

  /**
   * Calls createFile(createFile(pathout.write("/"+filename)
   * 
   * @param String
   *          path
   * @param String
   *          filename
   * @return
   */
  protected File createFile( String path, String filename ) throws IOException {
    return createFile( path + "/" + filename );
  }

  /**
   * Creates a file. An empty string is written to the file to persist it. A new File() will not create the file on
   * disk.
   * 
   * @param absolutePath
   * @return
   */
  protected File createFile( String absolutePath ) throws IOException {
    File file = new File( absolutePath );
    BufferedWriter out = new BufferedWriter( new FileWriter( file ) );
    out.write( "" );
    out.close();
    return file;
  }

  /**
   * Creates a
   * 
   * @param locale
   * @return
   */
  protected File createPropertiesFile( String locale, String path, String filenamePrefix ) throws IOException {
    return createPropertiesFile( locale, path, filenamePrefix, false );
  }

  /**
   * Creates a
   * 
   * @param locale
   * @return
   */
  protected File createPropertiesFile( String locale, String path, String filenamePrefix, boolean addContents )
    throws IOException {

    String fullPathtoFile =
        path + "/" + filenamePrefix + ( filenamePrefix.endsWith( "_" ) ? "" : "_" ) + locale + ".properties";
    File file = createFile( fullPathtoFile );

    if ( addContents ) {
      BufferedWriter out = new BufferedWriter( new FileWriter( file ) );

      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[phone1].[description]" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[customer_region_id].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[address4].[name]=Address4" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[postal_code].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[lname].[description]=" );
      out.newLine();
      out.write( "[LogicalModel-BV_CUSTOMER].[name]=Model 1" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[member_card].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[account_num].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[num_cars_owned].[name]=Num cars owned" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[lname].[name]=Lname" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[address2].[name]=Address2" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[state_province].[name]=State province" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[customer_id].[name]=Customer id" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[phone1].[name]=Phone1" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[occupation].[name]=Occupation" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[customer_region_id].[name]=Customer region id" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[education].[description]=" );
      out.newLine();
      out.write( "[LogicalModel-BV_CUSTOMER].[Category-BC_CUSTOMER].[name]=Customer" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[account_num].[name]=Account num" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[num_cars_owned].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[education].[name]=Education" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[marital_status].[name]=Marital status" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[birthdate].[name]=Birthdate" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[gender].[name]=Gender" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[num_children_at_home].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[city].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[mi].[name]=Mi" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[fullname].[name]=Fullname" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[total_children].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[birthdate].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[city].[name]=City" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[address3].[name]=Address3" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[yearly_income].[name]=Yearly income" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[name]=Customer" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[date_accnt_opened].[name]=Date accnt opened" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[houseowner].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[phone2].[name]=Phone2" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[fullname].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[country].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[date_accnt_opened].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[state_province].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[marital_status].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[address1].[name]=Address1" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[yearly_income].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[fname].[description]=" );
      out.newLine();
      out.write( "[LogicalModel-BV_CUSTOMER].[LogicalTable-BT_CUSTOMER_CUSTOMER].[name]=Customer" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[address4].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[occupation].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[total_children].[name]=Total children" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[mi].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[address3].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[country].[name]=Country" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[address2].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[houseowner].[name]=Houseowner" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[postal_code].[name]=Postal code" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[num_children_at_home].[name]=Num children at home" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[gender].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[address1].[description]=" );
      out.newLine();
      out.write( "[LogicalModel-BV_CUSTOMER].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[member_card].[name]=Member card" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[fname].[name]=Fname" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[phone2].[description]=" );
      out.newLine();
      out.write( "[IPhysicalModel-Customer].[PT_CUSTOMER].[customer_id].[description]=" );
      out.newLine();
      out.close();
    }

    return file;
  }
}
