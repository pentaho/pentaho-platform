package org.pentaho.platform.plugin.services.importer;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Repository implementation of IPlatformImportBundle. Instances of this class are contructed using the supplied Builder
 * {@link RepositoryFileImportBundle.Builder}
 *
 * User: nbaker
 * Date: 6/13/12
 */
public class RepositoryFileImportBundle implements IPlatformImportBundle {
  
  private InputStream inputStream;
  private String path = "/";
  private String name;
  private String title;
  private String charSet;
  private String mimeType;
  private String comment;
  private boolean overwriteInRepository;//file or folder overwrite
  private boolean hidden;
  private RepositoryFile file;
  //BIServer 8158 - ACL import handling properties
  private RepositoryFileAcl acl;
  private boolean applyAclSettings;
  private boolean overwriteAclSettings;
  private boolean retainOwnership;  
  // end 8158
  private Map<String, Object> properties = new HashMap<String, Object>();
  private List<IPlatformImportBundle> children = new ArrayList<IPlatformImportBundle>();

  private RepositoryFileImportBundle(){
  }

  /**
   * When set this ACL will be applied to the importing content if that content is to be managed by the
   * repository.
   *
   * NOTE: this may be better placed in a sub-interface
   * @return Repository ACL
   */
  public RepositoryFileAcl getAcl() {
    return this.acl;
  }

  public void setAcl(RepositoryFileAcl acl){
    this.acl = acl;
  }

  @Override
  public List<IPlatformImportBundle> getChildBundles() {
    return children;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return this.inputStream;
  }

  public void setInputStream(InputStream inStr){
    this.inputStream = inStr;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
	this.title = title;
  }

  @Override
  public String getCharset() {
    return charSet;
  }

  public void setCharSet(String charSet) {
    this.charSet = charSet;
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public boolean overwriteInRepossitory() {
    return overwriteInRepository;
  }

  public void setOverwriteInRepository(boolean overwrite) {
    this.overwriteInRepository = overwrite;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }
  
  public boolean isFolder() {
	return this.mimeType != null && this.mimeType.equals("text/directory");
  }
  
  public RepositoryFile getFile() {
	  return this.file;
  }
  
  public void setFile(RepositoryFile file) {
	  this.file = file;
  }
  
  public boolean isOverwriteInRepository() {
    return overwriteInRepository;
  }


  public boolean isApplyAclSettings() {
    return applyAclSettings;
  }

  public void setApplyAclSettings(boolean applyAclSettings) {
    this.applyAclSettings = applyAclSettings;
  }

  public boolean isOverwriteAclSettings() {
    return overwriteAclSettings;
  }

  public void setOverwriteAclSettings(boolean overwriteAclSettings) {
    this.overwriteAclSettings = overwriteAclSettings;
  }

  public boolean isRetainOwnership() {
    return retainOwnership;
  }

  public void setRetainOwnership(boolean retainOwnership) {
    this.retainOwnership = retainOwnership;
  }

  public String getCharSet() {
    return charSet;
  }

  private boolean validate(){
	if(this.mimeType != null && this.mimeType.equals("text/directory")) {
		return this.inputStream == null;
	} else {
		return this.inputStream != null;
	}  
  }

  private void addProperty(String prop, Object val){
    properties.put(prop, val);
  }

  public Object getProperty(String prop){
    return properties.get(prop);
  }

  public static class Builder {
    RepositoryFileImportBundle bundle;

    public Builder(){
      bundle = new RepositoryFileImportBundle();
    }

    public RepositoryFileImportBundle build(){
      if(bundle.validate()){
        return bundle;
      }
      throw new IllegalStateException("Bundle is not valid, check your inputs");
    }

    public Builder overwriteFile(boolean overwrite){
      bundle.setOverwriteInRepository(overwrite);
      return this;
    }

    public Builder comment(String comment){
      bundle.setComment(comment);
      return this;
    }

    public Builder acl(RepositoryFileAcl acl){
      bundle.setAcl(acl);
      return this;
    }

    public Builder input(InputStream in){
      bundle.setInputStream(in);
      return this;
    }
    
    public Builder file(RepositoryFile file){
        bundle.setFile(file);
        return this;
    }

    public Builder hidden(boolean hidden){
      bundle.setHidden(hidden);
      return this;
    }
    
    public Builder charSet(String charSet){
      bundle.setCharSet(charSet);
      return this;
    }

    public Builder name(String name){
      bundle.setName(name);
      return this;
    }

    public Builder title(String title){
        bundle.setTitle(title);
        return this;
    }
    
    public Builder path(String path){
      bundle.setPath(path);
      return this;
    }

    public Builder withParam(String param, String value) {
      bundle.addProperty(param, value);
      return this;
    }

    public Builder mime(String mime){
      bundle.setMimeType(mime);
      return this;
    }    
    
    /**
     * @param applyAclSettings
     * @see org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle#setApplyAclSettings(boolean)
     */
    public void applyAclSettings(boolean applyAclSettings) {
      bundle.setApplyAclSettings(applyAclSettings);
    }

    /**
     * @param overwriteAclSettings
     * @see org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle#setOverwriteAclSettings(boolean)
     */
    public void overwriteAclSettings(boolean overwriteAclSettings) {
      bundle.setOverwriteAclSettings(overwriteAclSettings);
    }

    /**
     * @param retainOwnership
     * @see org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle#setRetainOwnership(boolean)
     */
    public void retainOwnership(boolean retainOwnership) {
      bundle.setRetainOwnership(retainOwnership);
    }

    public Builder addChildBundle(IPlatformImportBundle childBundle) {
      bundle.children.add(childBundle);
      return this;
    }
  }

  @Override
  public boolean overwriteInRepository() { 
    return overwriteInRepository;
  }
}
