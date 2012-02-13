package org.pentaho.platform.repository2.unified.webservices;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;

/**
 * Converts {@code RepositoryFileTree} into JAXB-safe object and vice-versa.
 * 
 * @author mlowery
 */
public class RepositoryFileTreeAdapter extends XmlAdapter<RepositoryFileTreeDto, RepositoryFileTree> {

  @Override
  public RepositoryFileTreeDto marshal(final RepositoryFileTree v) {
    RepositoryFileTreeDto treeDto = new RepositoryFileTreeDto();
    treeDto.file = RepositoryFileAdapter.toFileDto(v.getFile());

    List<RepositoryFileTreeDto> children = null;
    if (v.getChildren() != null) {
      children = new ArrayList<RepositoryFileTreeDto>();
      for (RepositoryFileTree child : v.getChildren()) {
        children.add(marshal(child));
      }
    }

    treeDto.children = children;

    return treeDto;
  }

  @Override
  public RepositoryFileTree unmarshal(final RepositoryFileTreeDto v) {
    List<RepositoryFileTree> children = null;
    if (v.children != null) {
      children = new ArrayList<RepositoryFileTree>();
      for (RepositoryFileTreeDto child : v.children) {
        children.add(unmarshal(child));
      }
    }

    return new RepositoryFileTree(RepositoryFileAdapter.toFile(v.file), children);
  }
}
