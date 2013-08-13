package org.pentaho.platform.repository2.unified.webservices;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExecutableFileTypeDto implements Serializable {

  private String description;
  
  private String extension;
  
  private String title;

  private boolean canEdit;

  private boolean canSchedule;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean isCanEdit() {
    return canEdit;
  }

  public void setCanEdit(boolean canEdit) {
    this.canEdit = canEdit;
  }

  public boolean isCanSchedule() {
    return canSchedule;
  }

  public void setCanSchedule(boolean canSchedule) {
    this.canSchedule = canSchedule;
  }
  
}
