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


package org.pentaho.platform.api.scheduler2;

/**
 * An {@link IScheduleSubject} represents an entity that is associated in some way with the scheduling system. A subject
 * might impose restrictions on scheduling availability. You can think of subjects as principles in a scheduling system
 * ACL.
 * 
 * @author aphillips
 */
public interface IScheduleSubject {
  public enum SubjectType {
    SYSTEM, USER, ROLE, FILE
  };

  /**
   * Unique identifier to a subject
   * 
   * @return the subject id
   */
  public String getSubjectId();

  /**
   * The subject type
   * 
   * @return the subject type
   */
  public SubjectType getSubjectType();
}
