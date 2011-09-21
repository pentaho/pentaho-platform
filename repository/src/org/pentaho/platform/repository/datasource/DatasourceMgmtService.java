/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * The purpose of this class is to maintain a list of versions of each hibernated
 * class (the object definition, not the contents of any one object) for the purposes
 * of initiating an automatic schema update.
 *
 * 
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 07, 2008 
 * @author rmansoor
 */
package org.pentaho.platform.repository.datasource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;

public class DatasourceMgmtService implements IDatasourceMgmtService {

  private static final Log logger = LogFactory.getLog(DatasourceMgmtService.class);
  public Log getLogger() {
    return DatasourceMgmtService.logger;
  }
  public DatasourceMgmtService() {
  }
  
  public void createDatasource(IDatasource newDatasource) throws DuplicateDatasourceException, DatasourceMgmtServiceException {
    Session session = HibernateUtil.getSession();
    if(newDatasource != null) {
      if (getDatasource(newDatasource.getName()) == null) {
        try {
          session.setCacheMode(CacheMode.REFRESH);
          IPasswordService passwordService = PentahoSystem.getObjectFactory().get(IPasswordService.class, null);
          newDatasource.setPassword(passwordService.encrypt(newDatasource.getPassword()));
          session.save(newDatasource);
        } catch(ObjectFactoryException objface) {
          throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
            "DatasourceMgmtService.ERROR_0009_UNABLE_TO_INIT_PASSWORD_SERVICE")); //$NON-NLS-1$
        } catch(PasswordServiceException pse) {
            session.evict(newDatasource);
          throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
              "DatasourceMgmtService.ERROR_0007_UNABLE_TO_ENCRYPT_PASSWORD"), pse );//$NON-NLS-1$
        } catch (HibernateException ex) {
          session.evict(newDatasource);
          throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
              "DatasourceMgmtService.ERROR_0001_UNABLE_TO_CREATE_DATASOURCE",newDatasource.getName()), ex );//$NON-NLS-1$
        } finally {
          session.setCacheMode(CacheMode.NORMAL);
        }
      } else {
        throw new DuplicateDatasourceException(Messages.getInstance().getErrorString(
        "DatasourceMgmtService.ERROR_0005_DATASOURCE_ALREADY_EXIST",newDatasource.getName()));//$NON-NLS-1$
      }
    } else {
      throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
          "DatasourceMgmtService.ERROR_0010_NULL_DATASOURCE_OBJECT"));//$NON-NLS-1$
    }
    session.setCacheMode(CacheMode.NORMAL);
    HibernateUtil.flushSession();
   }
  public void deleteDatasource(String jndiName) throws NonExistingDatasourceException, DatasourceMgmtServiceException {
    IDatasource datasource = getDatasource(jndiName);
    if (datasource != null) {
      deleteDatasource(datasource);
    } else {
      throw new NonExistingDatasourceException(Messages.getInstance().getErrorString(
        "DatasourceMgmtService.ERROR_0006_DATASOURCE_DOES_NOT_EXIST",jndiName));//$NON-NLS-1$
    }
  }  
  public void deleteDatasource(IDatasource datasource) throws NonExistingDatasourceException, DatasourceMgmtServiceException {
    Session session = HibernateUtil.getSession();
      if (datasource != null) {
        try {
          session.setCacheMode(CacheMode.REFRESH);
          session.delete(session.merge(datasource));
        } catch (HibernateException ex) {
          throw new DatasourceMgmtServiceException( ex.getMessage(), ex );
        } finally {
          session.setCacheMode(CacheMode.NORMAL);
        }
      } else {
        throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
        "DatasourceMgmtService.ERROR_0010_NULL_DATASOURCE_OBJECT"));//$NON-NLS-1$
      }

    HibernateUtil.flushSession();
  }

  public IDatasource getDatasource(String jndiName) throws DatasourceMgmtServiceException {
    Session session = HibernateUtil.getSession();
    IDatasource datasource = null;
    try {
      session.setCacheMode(CacheMode.REFRESH);
      IDatasource pentahoDatasource = (IDatasource) session.get(Datasource.class, jndiName);
      if(pentahoDatasource != null) {
        datasource = clone(pentahoDatasource);
        IPasswordService passwordService = PentahoSystem.getObjectFactory().get(IPasswordService.class, null);
        datasource.setPassword(passwordService.decrypt(datasource.getPassword()));
      }
      return datasource;
    } catch(ObjectFactoryException objface) {
      throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
        "DatasourceMgmtService.ERROR_0009_UNABLE_TO_INIT_PASSWORD_SERVICE"), objface);//$NON-NLS-1$
    } catch(PasswordServiceException pse) {
      throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
        "DatasourceMgmtService.ERROR_0008_UNABLE_TO_DECRYPT_PASSWORD"), pse );//$NON-NLS-1$
    } catch (HibernateException ex) {
      throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
        "DatasourceMgmtService.ERROR_0004_UNABLE_TO_RETRIEVE_DATASOURCE"), ex);//$NON-NLS-1$
    } finally {
      session.setCacheMode(CacheMode.NORMAL);
    }
  }

  public List<IDatasource> getDatasources() throws DatasourceMgmtServiceException {
    Session session = HibernateUtil.getSession();
    try {
      session.setCacheMode(CacheMode.REFRESH);
      String nameQuery = "org.pentaho.platform.repository.datasource.Datasource.findAllDatasources"; //$NON-NLS-1$
      Query qry = session.getNamedQuery(nameQuery).setCacheable(true);
      List<IDatasource> pentahoDatasourceList = qry.list();
      List<IDatasource> datasourceList = new ArrayList<IDatasource>();
      for(IDatasource pentahoDatasource: pentahoDatasourceList) {
        IDatasource datasource = clone(pentahoDatasource);
        IPasswordService passwordService = PentahoSystem.getObjectFactory().get(IPasswordService.class, null);
        datasource.setPassword(passwordService.decrypt(datasource.getPassword()));
        datasourceList.add(datasource);        
      }
      return datasourceList;
    } catch(PasswordServiceException pse) {
      throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
        "DatasourceMgmtService.ERROR_0007_UNABLE_TO_ENCRYPT_PASSWORD"), pse );//$NON-NLS-1$
    } catch(ObjectFactoryException objface) {
      throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
        "DatasourceMgmtService.ERROR_0009_UNABLE_TO_INIT_PASSWORD_SERVICE"), objface);//$NON-NLS-1$
    } catch (HibernateException ex) {
      throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
        "DatasourceMgmtService.ERROR_0004_UNABLE_TO_RETRIEVE_DATASOURCE", ""), ex );//$NON-NLS-1$ //$NON-NLS-2$
    } finally {
      session.setCacheMode(CacheMode.NORMAL);
    }
  }

  public void updateDatasource(IDatasource datasource) throws NonExistingDatasourceException, DatasourceMgmtServiceException {
    Session session = HibernateUtil.getSession();
    if(datasource != null) {
      IDatasource tmpDatasource = getDatasource(datasource.getName());
      if (tmpDatasource != null) {
        try {
          session.setCacheMode(CacheMode.REFRESH);
          IPasswordService passwordService = PentahoSystem.getObjectFactory().get(IPasswordService.class, null); 
          // Store the new encrypted password in the datasource object
          datasource.setPassword(passwordService.encrypt(datasource.getPassword()));

          // BISERVER-5677 - clear the old datasource from the datasource service cache so updates will be available
          // without having to restart the server
          IDatasourceService datasourceService =  PentahoSystem.getObjectFactory().get(IDatasourceService.class, null);
          datasourceService.clearDataSource(datasource.getName());

          session.update(session.merge(datasource));
        } catch(ObjectFactoryException objface) {
          throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
            "DatasourceMgmtService.ERROR_0009_UNABLE_TO_INIT_PASSWORD_SERVICE"), objface);//$NON-NLS-1$
        } catch(PasswordServiceException pse) {
            throw new DatasourceMgmtServiceException( Messages.getInstance().getErrorString(
              "DatasourceMgmtService.ERROR_0007_UNABLE_TO_ENCRYPT_PASSWORD"), pse );//$NON-NLS-1$
        } catch (HibernateException ex) {
          throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
            "DatasourceMgmtService.ERROR_0004_UNABLE_TO_RETRIEVE_DATASOURCE", datasource.getName()), ex );//$NON-NLS-1$
        } finally {
          session.setCacheMode(CacheMode.NORMAL);
        }
      } else {
        throw new NonExistingDatasourceException(Messages.getInstance().getErrorString(
          "DatasourceMgmtService.ERROR_0006_DATASOURCE_DOES_NOT_EXIST", datasource.getName()) );//$NON-NLS-1$
      }
    } else {
      throw new DatasourceMgmtServiceException(Messages.getInstance().getErrorString(
          "DatasourceMgmtService.ERROR_0010_NULL_DATASOURCE_OBJECT"));//$NON-NLS-1$
    }
  }
  
  public void init(final IPentahoSession session) {
    HibernateUtil.beginTransaction();
  }
  
  private IDatasource clone (IDatasource datasource) throws ObjectFactoryException {
      IDatasource returnDatasource = PentahoSystem.getObjectFactory().get(IDatasource.class, null);
      returnDatasource.setDriverClass(datasource.getDriverClass());
      returnDatasource.setIdleConn(datasource.getIdleConn());
      returnDatasource.setMaxActConn(datasource.getMaxActConn());
      returnDatasource.setName(datasource.getName());
      returnDatasource.setPassword(datasource.getPassword());
      returnDatasource.setQuery(datasource.getQuery());
      returnDatasource.setUrl(datasource.getUrl());
      returnDatasource.setUserName(datasource.getUserName());
      returnDatasource.setWait(datasource.getWait());
      return returnDatasource;
  }
  
}