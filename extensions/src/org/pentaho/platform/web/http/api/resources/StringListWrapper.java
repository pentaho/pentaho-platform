package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StringListWrapper {
  List<String> strings = new ArrayList<String>();

  public StringListWrapper() {
  }
  
  public StringListWrapper(Collection<String> stringList) {
    this.strings.addAll(stringList);
  }
  
  public List<String> getStrings() {
    return strings;
  }

  public void setStrings(List<String> stringList) {
    if (stringList != this.strings) {
      this.strings.clear();
      this.strings.addAll(stringList);
    }
  }
}
