package org.pentaho.platform.repository2.unified.webservices;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RepositoryFileTreeDto implements Serializable {
  RepositoryFileDto file;

  List<RepositoryFileTreeDto> children;

  public RepositoryFileTreeDto() {
  }

  public RepositoryFileDto getFile() {
    return file;
  }

  public void setFile(RepositoryFileDto file) {
    this.file = file;
  }

  @XmlElementWrapper()
  public List<RepositoryFileTreeDto> getChildren() {
    return children;
  }

  public void setChildren(List<RepositoryFileTreeDto> children) {
    this.children = children;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "RepositoryFileTreeDto [file=" + file + ", children=" + children + "]";
  }
}
