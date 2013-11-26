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

package org.pentaho.test.platform.plugin.services.security.userrole.ldap;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.DefaultRoleComparator;
import org.pentaho.platform.engine.security.DefaultUsernameComparator;
import org.pentaho.platform.plugin.services.security.userrole.ldap.DefaultLdapUserRoleListService;
import org.pentaho.platform.plugin.services.security.userrole.ldap.NoOpLdapAuthoritiesPopulator;
import org.pentaho.platform.plugin.services.security.userrole.ldap.RolePreprocessingMapper;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.GenericLdapSearch;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearch;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearchParamsFactory;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearchParamsFactoryImpl;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.UnionizingLdapSearch;
import org.pentaho.platform.plugin.services.security.userrole.ldap.transform.GrantedAuthorityToString;
import org.pentaho.platform.plugin.services.security.userrole.ldap.transform.SearchResultToAttrValueList;
import org.pentaho.platform.plugin.services.security.userrole.ldap.transform.StringToGrantedAuthority;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ldap.LdapUserSearch;
import org.springframework.security.ldap.populator.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.ldap.LdapUserDetailsService;

import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

//import org.pentaho.platform.engine.core.audit.NullAuditEntry;

/**
 * Tests for the <code>DefaultLdapUserRoleListService</code> class. The ways in which an LDAP schema can be layed out
 * are numerous. See the comment for each method to get an idea of how the schema is layed out in each example.
 * 
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public class DefaultLdapUserRoleListServiceTests extends AbstractPentahoLdapIntegrationTests {

  private static final Log logger = LogFactory.getLog( DefaultLdapUserRoleListServiceTests.class );

  private LdapUserSearch getUserSearch( final String searchBase, final String searchFilter ) {
    return new FilterBasedLdapUserSearch( searchBase, searchFilter, getContextSource() );
  }

  /**
   * Get the roles of user <code>suzy</code> by extracting the <code>cn</code> token from the <code>uniqueMember</code>
   * attribute of the object that matches base of <code>ou=users</code> and filter of <code>(uid={0})</code>.
   * 
   * <p>
   * Note that the UserDetailsService used by Spring Security is re-used here.
   * </p>
   * 
   * @throws Exception
   */
  @Test
  public void testGetAuthoritiesForUser1() throws Exception {
    LdapUserSearch userSearch = getUserSearch( "ou=users", "(uid={0})" ); //$NON-NLS-1$//$NON-NLS-2$

    LdapUserDetailsService service = new LdapUserDetailsService( userSearch, new NoOpLdapAuthoritiesPopulator() );

    RolePreprocessingMapper mapper = new RolePreprocessingMapper();
    mapper.setRoleAttributes( new String[] { "uniqueMember" } ); //$NON-NLS-1$
    mapper.setTokenName( "cn" ); //$NON-NLS-1$
    service.setUserDetailsMapper( mapper );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setUserDetailsService( service );

    List res = userRoleListService.getRolesForUser( null, "suzy" ); //$NON-NLS-1$

    assertTrue( res.contains( "ROLE_A" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAuthoritiesForUser1(): " + res ); //$NON-NLS-1$
    }

  }

  @Test
  public void testGetAuthoritiesForUser1ForTenant() throws Exception {
    ITenant defaultTenant = new Tenant( "/pentaho/tenant0", true );
    login( "suzy", defaultTenant );

    LdapUserSearch userSearch = getUserSearch( "ou=users", "(uid={0})" ); //$NON-NLS-1$//$NON-NLS-2$

    LdapUserDetailsService service = new LdapUserDetailsService( userSearch, new NoOpLdapAuthoritiesPopulator() );

    RolePreprocessingMapper mapper = new RolePreprocessingMapper();
    mapper.setRoleAttributes( new String[] { "uniqueMember" } ); //$NON-NLS-1$
    mapper.setTokenName( "cn" ); //$NON-NLS-1$
    service.setUserDetailsMapper( mapper );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setUserDetailsService( service );

    List res = userRoleListService.getRolesForUser( defaultTenant, "suzy" ); //$NON-NLS-1$

    assertTrue( res.contains( "ROLE_A" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAuthoritiesForUser1(): " + res ); //$NON-NLS-1$
    }

    try {
      userRoleListService.getRolesForUser( new Tenant( "/pentaho", true ), "suzy" );
    } catch ( UnsupportedOperationException uoe ) {
      assertNotNull( uoe );
    }

  }

  /**
   * Get the roles of user <code>suzy</code> by returning the <code>cn</code> attribute of each object that matches base
   * of <code>ou=roles</code> and filter of <code>(roleOccupant={0})</code>.
   * 
   * <p>
   * Note that the UserDetailsService used by Spring Security is re-used here.
   * </p>
   */
  @Test
  public void testGetAuthoritiesForUser2() {
    DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator( getContextSource(), "ou=roles" ); //$NON-NLS-1$
    populator.setGroupRoleAttribute( "cn" ); //$NON-NLS-1$
    populator.setGroupSearchFilter( "(roleOccupant={0})" ); //$NON-NLS-1$

    LdapUserSearch userSearch = getUserSearch( "ou=users", "(uid={0})" ); //$NON-NLS-1$//$NON-NLS-2$

    LdapUserDetailsService service = new LdapUserDetailsService( userSearch, populator );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setUserDetailsService( service );

    List res = userRoleListService.getRolesForUser( null, "suzy" ); //$NON-NLS-1$
    assertTrue( res.contains( "ROLE_IS" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAuthoritiesForUser2(): " + res ); //$NON-NLS-1$
    }

  }

  /**
   * Same as above except sorted.
   */
  @Test
  public void testGetAuthoritiesForUser2Sorted() {
    DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator( getContextSource(), "ou=roles" ); //$NON-NLS-1$
    populator.setGroupRoleAttribute( "cn" ); //$NON-NLS-1$
    populator.setGroupSearchFilter( "(roleOccupant={0})" ); //$NON-NLS-1$

    LdapUserSearch userSearch = getUserSearch( "ou=users", "(uid={0})" ); //$NON-NLS-1$//$NON-NLS-2$

    LdapUserDetailsService service = new LdapUserDetailsService( userSearch, populator );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setUserDetailsService( service );
    userRoleListService.setRoleComparator( new DefaultRoleComparator() );

    List res = userRoleListService.getRolesForUser( null, "suzy" ); //$NON-NLS-1$
    assertTrue( res.contains( "ROLE_POWER_USER" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAuthoritiesForUser2Sorted(): " + res ); //$NON-NLS-1$
    }

  }

  /**
   * Search for all users starting at <code>ou=groups</code>, looking for objects with
   * <code>objectClass=groupOfUniqueNames</code>, and extracting the <code>uid</code> token of the
   * <code>uniqueMember</code> attribute.
   */
  @Test
  public void testGetAllUserNames1() throws Exception {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "uniqueMember" } ); //$NON-NLS-1$

    LdapSearchParamsFactoryImpl paramFactory =
        new LdapSearchParamsFactoryImpl( "ou=groups", "(objectClass=groupOfUniqueNames)", con1 ); //$NON-NLS-1$//$NON-NLS-2$
    paramFactory.afterPropertiesSet();

    Transformer transformer1 = new SearchResultToAttrValueList( "uniqueMember", "uid" ); //$NON-NLS-1$ //$NON-NLS-2$

    GenericLdapSearch allUsernamesSearch = new GenericLdapSearch( getContextSource(), paramFactory, transformer1 );
    allUsernamesSearch.afterPropertiesSet();

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setAllUsernamesSearch( allUsernamesSearch );

    List res = userRoleListService.getAllUsers();

    assertTrue( res.contains( "pat" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "admin" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAllUserNames1(): " + res ); //$NON-NLS-1$
    }
  }

  @Test
  public void testGetAllUserNames1ForTenant() throws Exception {
    ITenant defaultTenant = new Tenant( "/pentaho/tenant0", true );
    login( "suzy", defaultTenant );

    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "uniqueMember" } ); //$NON-NLS-1$

    LdapSearchParamsFactoryImpl paramFactory =
        new LdapSearchParamsFactoryImpl( "ou=groups", "(objectClass=groupOfUniqueNames)", con1 ); //$NON-NLS-1$//$NON-NLS-2$
    paramFactory.afterPropertiesSet();

    Transformer transformer1 = new SearchResultToAttrValueList( "uniqueMember", "uid" ); //$NON-NLS-1$ //$NON-NLS-2$

    GenericLdapSearch allUsernamesSearch = new GenericLdapSearch( getContextSource(), paramFactory, transformer1 );
    allUsernamesSearch.afterPropertiesSet();

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setAllUsernamesSearch( allUsernamesSearch );

    List res = userRoleListService.getAllUsers( defaultTenant );

    assertTrue( res.contains( "pat" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "admin" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAllUserNames1(): " + res ); //$NON-NLS-1$
    }

    try {
      userRoleListService.getAllUsers( new Tenant( "/pentaho", true ) );
    } catch ( UnsupportedOperationException uoe ) {
      assertNotNull( uoe );
    }

  }

  /**
   * Same as above except sorted.
   */
  @Test
  public void testGetAllUserNames1Sorted() throws Exception {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "uniqueMember" } ); //$NON-NLS-1$

    LdapSearchParamsFactoryImpl paramFactory =
        new LdapSearchParamsFactoryImpl( "ou=groups", "(objectClass=groupOfUniqueNames)", con1 ); //$NON-NLS-1$//$NON-NLS-2$
    paramFactory.afterPropertiesSet();

    Transformer transformer1 = new SearchResultToAttrValueList( "uniqueMember", "uid" ); //$NON-NLS-1$ //$NON-NLS-2$

    GenericLdapSearch allUsernamesSearch = new GenericLdapSearch( getContextSource(), paramFactory, transformer1 );
    allUsernamesSearch.afterPropertiesSet();

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setAllUsernamesSearch( allUsernamesSearch );
    userRoleListService.setUsernameComparator( new DefaultUsernameComparator() );

    List res = userRoleListService.getAllUsers();

    assertTrue( res.indexOf( "pat" ) < res.indexOf( "tiffany" ) );

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAllUserNames1Sorted(): " + res ); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=users</code>, looking for objects with <code>objectClass=person</code>,
   * and returning the <code>uniqueMember</code> attribute.
   */
  @Test
  public void testGetAllUserNames2() {
    SearchControls con2 = new SearchControls();
    con2.setReturningAttributes( new String[] { "uid" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory = new LdapSearchParamsFactoryImpl( "ou=users", "(objectClass=person)", con2 ); //$NON-NLS-1$ //$NON-NLS-2$

    Transformer transformer2 = new SearchResultToAttrValueList( "uid" ); //$NON-NLS-1$

    LdapSearch allUsernamesSearch = new GenericLdapSearch( getContextSource(), paramsFactory, transformer2 );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setAllUsernamesSearch( allUsernamesSearch );

    List res = userRoleListService.getAllUsers();

    assertTrue( res.contains( "pat" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "admin" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAllUserNames2(): " + res ); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=roles</code>, looking for objects with
   * <code>objectClass=organizationalRole</code>, and extracting the <code>uid</code> token of the
   * <code>roleOccupant</code> attribute.
   */
  @Test
  public void testGetAllUserNames3() {
    SearchControls con3 = new SearchControls();
    con3.setReturningAttributes( new String[] { "roleOccupant" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory =
        new LdapSearchParamsFactoryImpl( "ou=roles", "(objectClass=organizationalRole)", con3 ); //$NON-NLS-1$ //$NON-NLS-2$

    Transformer transformer3 = new SearchResultToAttrValueList( "roleOccupant", "uid" ); //$NON-NLS-1$ //$NON-NLS-2$

    LdapSearch allUsernamesSearch = new GenericLdapSearch( getContextSource(), paramsFactory, transformer3 );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setAllUsernamesSearch( allUsernamesSearch );

    List res = userRoleListService.getAllUsers();

    assertTrue( res.contains( "pat" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "tiffany" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "admin" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAllUserNames3(): " + res ); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=users</code>, looking for objects with
   * <code>businessCategory=cn={0}*</code>, and returning the <code>uid</code> attribute. This search implies that the
   * schema is setup such that a user's roles come from one of the user's attributes.
   */
  @Test
  public void testGetUsernamesInRole1() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "uid" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory =
        new LdapSearchParamsFactoryImpl( "ou=users", "(businessCategory=cn={0}*)", con1 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer1 = new SearchResultToAttrValueList( "uid" ); //$NON-NLS-1$

    GrantedAuthorityToString transformer2 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch =
        new GenericLdapSearch( getContextSource(), paramFactory, transformer1, transformer2 );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setUsernamesInRoleSearch( usernamesInRoleSearch );

    List<String> res = userRoleListService.getUsersInRole( null, "DEV" ); //$NON-NLS-1$

    assertTrue( res.contains( "pat" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "tiffany" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getUsernamesInRole1(): " + res ); //$NON-NLS-1$
    }
  }

  @Test
  public void testGetUsernamesInRole1ForTenant() {
    ITenant defaultTenant = new Tenant( "/pentaho/tenant0", true );
    login( "suzy", defaultTenant );

    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "uid" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory =
        new LdapSearchParamsFactoryImpl( "ou=users", "(businessCategory=cn={0}*)", con1 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer1 = new SearchResultToAttrValueList( "uid" ); //$NON-NLS-1$

    GrantedAuthorityToString transformer2 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch =
        new GenericLdapSearch( getContextSource(), paramFactory, transformer1, transformer2 );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setUsernamesInRoleSearch( usernamesInRoleSearch );

    List<String> res = userRoleListService.getUsersInRole( defaultTenant, "DEV" ); //$NON-NLS-1$

    assertTrue( res.contains( "pat" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "tiffany" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getUsernamesInRole1(): " + res ); //$NON-NLS-1$
    }

    try {
      userRoleListService.getUsersInRole( new Tenant( "/pentaho", true ), "DEV" );
    } catch ( UnsupportedOperationException uoe ) {
      assertNotNull( uoe );
    }
  }

  /**
   * Same as above except sorted.
   */
  @Test
  public void testGetUsernamesInRole1Sorted() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "uid" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory =
        new LdapSearchParamsFactoryImpl( "ou=users", "(businessCategory=cn={0}*)", con1 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer1 = new SearchResultToAttrValueList( "uid" ); //$NON-NLS-1$

    GrantedAuthorityToString transformer2 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch =
        new GenericLdapSearch( getContextSource(), paramFactory, transformer1, transformer2 );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setUsernamesInRoleSearch( usernamesInRoleSearch );
    userRoleListService.setUsernameComparator( new DefaultUsernameComparator() );

    List<String> res = userRoleListService.getUsersInRole( null, "DEV" ); //$NON-NLS-1$

    assertTrue( res.contains( "pat" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "tiffany" ) ); //$NON-NLS-1$

    assertTrue( res.indexOf( "pat" ) < res.indexOf( "tiffany" ) );

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getUsernamesInRole1Sorted(): " + res ); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=roles</code>, looking for objects with
   * <code>(&(objectClass=organizationalRole)(cn={0}))</code>, and extracting the <code>uid</code> token of the
   * <code>roleOccupant</code> attribute. This search implies that the schema is setup such that a user's roles come
   * from that user's DN being present in the <code>roleOccupant</code> attribute of a child object under the
   * <code>ou=roles</code> object.
   */
  @Test
  public void testGetUsernamesInRole2() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "roleOccupant" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory =
        new LdapSearchParamsFactoryImpl( "ou=roles", "(&(objectClass=organizationalRole)(cn={0}))", con1 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer1 = new SearchResultToAttrValueList( "roleOccupant", "uid" ); //$NON-NLS-1$ //$NON-NLS-2$

    GrantedAuthorityToString transformer2 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch =
        new GenericLdapSearch( getContextSource(), paramFactory, transformer1, transformer2 );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setUsernamesInRoleSearch( usernamesInRoleSearch );

    List<String> res = userRoleListService.getUsersInRole( null, "DEV" ); //$NON-NLS-1$

    assertTrue( res.contains( "pat" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "tiffany" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getUsernamesInRole2(): " + res ); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=groups</code>, looking for objects with
   * <code>(&(objectClass=groupOfUniqueNames)(cn={0}))</code>, and extracting the <code>uid</code> token of the
   * <code>uniqueMember</code> attribute. This search implies that the schema is setup such that a user's roles come
   * from that user's DN being present in the <code>uniqueMember</code> attribute of a child object under the
   * <code>ou=groups</code> object.
   */
  @Test
  public void testGetUsernamesInRole3() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "uniqueMember" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory =
        new LdapSearchParamsFactoryImpl( "ou=groups", "(&(objectClass=groupOfUniqueNames)(cn={0}))", con1 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer1 = new SearchResultToAttrValueList( "uniqueMember", "uid" ); //$NON-NLS-1$ //$NON-NLS-2$

    GrantedAuthorityToString transformer2 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch =
        new GenericLdapSearch( getContextSource(), paramFactory, transformer1, transformer2 );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setUsernamesInRoleSearch( usernamesInRoleSearch );

    List<String> res = userRoleListService.getUsersInRole( null, "DEVELOPMENT" ); //$NON-NLS-1$

    assertTrue( res.contains( "pat" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "tiffany" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getUsernamesInRole3(): " + res ); //$NON-NLS-1$
    }
  }

  /**
   * Search for all users starting at <code>ou=groups</code>, looking for objects with
   * <code>(&(objectClass=groupOfUniqueNames)(cn={0}))</code>, and extracting the <code>uid</code> token of the
   * <code>uniqueMember</code> attribute. This search implies that the schema is setup such that a user's roles come
   * from that user's DN being present in the <code>uniqueMember</code> attribute of a child object under the
   * <code>ou=groups</code> object.
   * 
   * @throws Exception
   */
  @Test
  public void testGetUsernamesInRole4() throws Exception {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "uniqueMember" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory =
        new LdapSearchParamsFactoryImpl( "ou=groups", "(&(objectClass=groupOfUniqueNames)(cn={0}))", con1 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer1 = new SearchResultToAttrValueList( "uniqueMember", "uid" ); //$NON-NLS-1$ //$NON-NLS-2$

    GrantedAuthorityToString transformer2 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch =
        new GenericLdapSearch( getContextSource(), paramFactory, transformer1, transformer2 );

    SearchControls con2 = new SearchControls();
    con2.setReturningAttributes( new String[] { "uid" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramFactory2 =
        new LdapSearchParamsFactoryImpl( "ou=users", "(businessCategory=cn={0}*)", con2 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer transformer3 = new SearchResultToAttrValueList( "uid" ); //$NON-NLS-1$

    GrantedAuthorityToString transformer4 = new GrantedAuthorityToString();

    LdapSearch usernamesInRoleSearch2 =
        new GenericLdapSearch( getContextSource(), paramFactory2, transformer3, transformer4 );

    Set searches = new HashSet();
    searches.add( usernamesInRoleSearch );
    searches.add( usernamesInRoleSearch2 );
    UnionizingLdapSearch unionSearch = new UnionizingLdapSearch( searches );
    unionSearch.afterPropertiesSet();

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setUsernamesInRoleSearch( unionSearch );

    List<String> res = userRoleListService.getUsersInRole( null, "DEV" ); //$NON-NLS-1$

    assertTrue( res.contains( "pat" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "tiffany" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getUsernamesInRole4() with role=ROLE_DEV: " + res ); //$NON-NLS-1$
    }

    res = userRoleListService.getUsersInRole( null, "DEVELOPMENT" ); //$NON-NLS-1$

    assertTrue( res.contains( "pat" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "tiffany" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getUsernamesInRole4() with role=DEVELOPMENT: " + res ); //$NON-NLS-1$
    }

  }

  /**
   * Search for all roles (aka authorities) starting at <code>ou=roles</code>, looking for objects with
   * <code>objectClass=organizationalRole</code>, and returning the <code>cn</code> attribute.
   */
  @Test
  public void testGetAllAuthorities1() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "cn" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory =
        new LdapSearchParamsFactoryImpl( "ou=roles", "(objectClass=organizationalRole)", con1 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer one = new SearchResultToAttrValueList( "cn" ); //$NON-NLS-1$
    Transformer two = new StringToGrantedAuthority();
    Transformer[] transformers = { one, two };
    Transformer transformer = new ChainedTransformer( transformers );

    LdapSearch rolesSearch = new GenericLdapSearch( getContextSource(), paramsFactory, transformer );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setAllAuthoritiesSearch( rolesSearch );

    List res = userRoleListService.getAllRoles();

    assertTrue( res.contains( "ROLE_CTO" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "ROLE_CEO" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAllAuthorities1(): " + res ); //$NON-NLS-1$
    }
  }

  @Test
  public void testGetAllAuthorities1ForTenant() {
    ITenant defaultTenant = new Tenant( "/pentaho/tenant0", true );
    login( "suzy", defaultTenant );
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "cn" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory =
        new LdapSearchParamsFactoryImpl( "ou=roles", "(objectClass=organizationalRole)", con1 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer one = new SearchResultToAttrValueList( "cn" ); //$NON-NLS-1$
    Transformer two = new StringToGrantedAuthority();
    Transformer[] transformers = { one, two };
    Transformer transformer = new ChainedTransformer( transformers );

    LdapSearch rolesSearch = new GenericLdapSearch( getContextSource(), paramsFactory, transformer );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setAllAuthoritiesSearch( rolesSearch );

    List res = userRoleListService.getAllRoles( defaultTenant );

    assertTrue( res.contains( "ROLE_CTO" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "ROLE_CEO" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAllAuthorities1(): " + res ); //$NON-NLS-1$
    }

    try {
      userRoleListService.getAllRoles( new Tenant( "/pentaho", true ) );
    } catch ( UnsupportedOperationException uoe ) {
      assertNotNull( uoe );
    }
  }

  /**
   * Same as above except sorted.
   */
  @Test
  public void testGetAllAuthorities1Sorted() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "cn" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory =
        new LdapSearchParamsFactoryImpl( "ou=roles", "(objectClass=organizationalRole)", con1 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer one = new SearchResultToAttrValueList( "cn" ); //$NON-NLS-1$
    Transformer two = new StringToGrantedAuthority();
    Transformer[] transformers = { one, two };
    Transformer transformer = new ChainedTransformer( transformers );

    LdapSearch rolesSearch = new GenericLdapSearch( getContextSource(), paramsFactory, transformer );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setAllAuthoritiesSearch( rolesSearch );
    userRoleListService.setRoleComparator( new DefaultRoleComparator() );

    List res = userRoleListService.getAllRoles();

    assertTrue( res.contains( "ROLE_CTO" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "ROLE_CEO" ) ); //$NON-NLS-1$

    assertTrue( res.indexOf( "ROLE_ADMINISTRATOR" ) < res.indexOf( "ROLE_DEV" ) );

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAllAuthorities1Sorted(): " + res ); //$NON-NLS-1$
    }
  }

  /**
   * Search for all roles (aka authorities) starting at <code>ou=groups</code>, looking for objects with
   * <code>objectClass=groupOfUniqueNames</code>, and returning the <code>cn</code> attribute.
   */
  @Test
  public void testGetAllAuthorities2() {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "cn" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory =
        new LdapSearchParamsFactoryImpl( "ou=groups", "(objectClass=groupOfUniqueNames)", con1 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer one = new SearchResultToAttrValueList( "cn" ); //$NON-NLS-1$
    Transformer two = new StringToGrantedAuthority();
    Transformer[] transformers = { one, two };
    Transformer transformer = new ChainedTransformer( transformers );

    LdapSearch rolesSearch = new GenericLdapSearch( getContextSource(), paramsFactory, transformer );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setAllAuthoritiesSearch( rolesSearch );

    List res = userRoleListService.getAllRoles();

    assertTrue( res.contains( "ROLE_SALES" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "ROLE_MARKETING" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAllAuthorities2(): " + res ); //$NON-NLS-1$
    }
  }

  /**
   * Union the results of two different searches.
   * <ul>
   * <li>Search 1: Search for all roles (aka authorities) starting at <code>ou=groups</code>, looking for objects with
   * <code>objectClass=groupOfUniqueNames</code>, and returning the <code>cn</code> attribute.</li>
   * <li>Search 2: Search for all roles (aka authorities) starting at <code>ou=roles</code>, looking for objects with
   * <code>objectClass=organizationalRole</code>, and returning the <code>cn</code> attribute.</li>
   * </ul>
   */
  @Test
  public void testGetAllAuthorities3() throws Exception {
    SearchControls con1 = new SearchControls();
    con1.setReturningAttributes( new String[] { "cn" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory =
        new LdapSearchParamsFactoryImpl( "ou=roles", "(objectClass=organizationalRole)", con1 ); //$NON-NLS-1$ //$NON-NLS-2$

    Transformer one = new SearchResultToAttrValueList( "cn" ); //$NON-NLS-1$
    Transformer two = new StringToGrantedAuthority();
    Transformer[] transformers = { one, two };
    Transformer transformer = new ChainedTransformer( transformers );

    LdapSearch rolesSearch = new GenericLdapSearch( getContextSource(), paramsFactory, transformer );

    SearchControls con2 = new SearchControls();
    con1.setReturningAttributes( new String[] { "cn" } ); //$NON-NLS-1$

    LdapSearchParamsFactory paramsFactory2 =
        new LdapSearchParamsFactoryImpl( "ou=groups", "(objectClass=groupOfUniqueNames)", con2 ); //$NON-NLS-1$//$NON-NLS-2$

    Transformer oneB = new SearchResultToAttrValueList( "cn" ); //$NON-NLS-1$
    Transformer twoB = new StringToGrantedAuthority();
    Transformer[] transformers2 = { oneB, twoB };
    Transformer transformer2 = new ChainedTransformer( transformers2 );

    LdapSearch rolesSearch2 = new GenericLdapSearch( getContextSource(), paramsFactory2, transformer2 );

    Set searches = new HashSet();
    searches.add( rolesSearch );
    searches.add( rolesSearch2 );
    UnionizingLdapSearch unionSearch = new UnionizingLdapSearch( searches );

    DefaultLdapUserRoleListService userRoleListService = new DefaultLdapUserRoleListService();

    userRoleListService.setAllAuthoritiesSearch( unionSearch );

    List res = userRoleListService.getAllRoles();

    assertTrue( res.contains( "ROLE_DEVMGR" ) ); //$NON-NLS-1$
    assertTrue( res.contains( "ROLE_DEVELOPMENT" ) ); //$NON-NLS-1$

    if ( logger.isDebugEnabled() ) {
      logger.debug( "results of getAllAuthorities3(): " + res ); //$NON-NLS-1$
    }

  }

  /**
   * Logs in with given username.
   * 
   * @param username
   *          username of user
   * @param tenantId
   *          tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login( final String username, final ITenant tenant ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( username );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
    authList.add( new GrantedAuthorityImpl( "TenantAdmin" ) );
    authList.add( new GrantedAuthorityImpl( "Authenticated" ) );
    GrantedAuthority[] authorities = authList.toArray( new GrantedAuthority[0] );
    UserDetails userDetails = new User( username, password, true, true, true, true, authorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
  }
}
