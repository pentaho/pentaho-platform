package org.pentaho.platform.repository2.unified;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;

/**
 * <a href="http://code.google.com/p/hamcrest/">Hamcrest</a> matchers for {@code IUnifiedRepository}-related classes.
 * 
 * <p>
 * Hamcrest matchers can be used by JUnit's {@code assertThat} and mock frameworks like Mockito.
 * </p>
 * 
 * @author mlowery
 */
@SuppressWarnings({ "nls" })
public class UnifiedRepositoryMatchers {

  private static class NodeRepositoryFileDataMatcher extends TypeSafeMatcher<NodeRepositoryFileData> {

    private static final String shortName = "hasData";
    
    private PathPropertyPair[] pairs;
    
    public NodeRepositoryFileDataMatcher(PathPropertyPair... pairs) {
      for (PathPropertyPair pair : pairs) {
        checkPath(pair.getPath());
      }
      this.pairs = pairs;
    }
    
    @Override
    public boolean matchesSafely(final NodeRepositoryFileData data) {  
      for (PathPropertyPair pair : pairs) {
        DataProperty expectedProperty = pair.getProperty();
        String[] pathSegments = pair.getPath().substring(1).split("/");
        DataNode currentNode = data.getNode();
        if (!currentNode.getName().equals(pathSegments[0])) {
          return false;
        }
        for (int i = 1; i < pathSegments.length-1; i++) {
          currentNode = currentNode.getNode(pathSegments[i]);
        }
        DataProperty actualProperty = currentNode.getProperty(pathSegments[pathSegments.length-1]);
        if (!expectedProperty.equals(actualProperty)) {
          return false;
        }
      }
      return true;  
    }

    @Override
    public void describeTo(final Description description) {
      description.appendText(shortName);
      description.appendText("(");
      description.appendText("pathPropertyPairs=");
      description.appendText(Arrays.toString(pairs));
      description.appendText(")");
    }
    
  }
  
  /**
   * Throws {@code IllegalArgumentException} if path does not meet certain criteria.
   * @param path path to check
   */
  private static void checkPath(final String path) {
    if (path == null) {
      throw new IllegalArgumentException("path cannot be null");
    }
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("paths must start with a slash");
    }
    if (path.endsWith("/")) {
      throw new IllegalArgumentException("paths must not end with a slash");
    }
    if (path.trim().equals("/")) {
      throw new IllegalArgumentException("path must be path to property");
    }
  }
  
  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair(final String path, final String value) {
    checkPath(path);
    String[] pathSegments = path.split("/");
    return new PathPropertyPair(path, new DataProperty(pathSegments[pathSegments.length-1], value, DataPropertyType.STRING));
  }
  
  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair(final String path, final boolean value) {
    checkPath(path);
    String[] pathSegments = path.split("/");
    return new PathPropertyPair(path, new DataProperty(pathSegments[pathSegments.length-1], value, DataPropertyType.BOOLEAN));
  }
  
  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair(final String path, final long value) {
    checkPath(path);
    String[] pathSegments = path.split("/");
    return new PathPropertyPair(path, new DataProperty(pathSegments[pathSegments.length-1], value, DataPropertyType.LONG));
  }
  
  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair(final String path, final double value) {
    checkPath(path);
    String[] pathSegments = path.split("/");
    return new PathPropertyPair(path, new DataProperty(pathSegments[pathSegments.length-1], value, DataPropertyType.DOUBLE));
  }
  
  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair(final String path, final Date value) {
    checkPath(path);
    String[] pathSegments = path.split("/");
    return new PathPropertyPair(path, new DataProperty(pathSegments[pathSegments.length-1], value, DataPropertyType.DATE));
  }
  
  /**
   * Factory for {@link PathPropertyPair} instances.
   */
  public static PathPropertyPair pathPropertyPair(final String path, final Serializable value) {
    checkPath(path);
    String[] pathSegments = path.split("/");
    return new PathPropertyPair(path, new DataProperty(pathSegments[pathSegments.length-1], value, DataPropertyType.REF));
  }
  
  /**
   * A path and property pair.
   */
  public static class PathPropertyPair {

    private String path;
    
    private DataProperty property;
    
    public PathPropertyPair(final String path, final DataProperty property) {
      this.path = path;
      this.property = property;
    }

    private String getPath() {
      return path;
    }

    private DataProperty getProperty() {
      return property;
    }
    
  }
  
  /**
   * Matcher for {@link SimpleRepositoryFileData}.
   */
  private static class SimpleRepositoryFileDataMatcher extends TypeSafeMatcher<SimpleRepositoryFileData> {

    private static final String shortName = "hasData";
    
    private String expectedMimeType;

    private String expectedEncoding;

    private byte[] expectedBytes;

    public SimpleRepositoryFileDataMatcher(final byte[] expectedBytes, final String expectedEncoding,
        final String expectedMimeType) {
      this.expectedBytes = expectedBytes;
      this.expectedEncoding = expectedEncoding;
      this.expectedMimeType = expectedMimeType;
    }

    @Override
    public boolean matchesSafely(final SimpleRepositoryFileData data) {
      return streamMatches(data.getStream()) && ObjectUtils.equals(expectedMimeType, data.getMimeType())
          && ObjectUtils.equals(expectedEncoding, data.getEncoding());
    }

    private boolean streamMatches(final InputStream stream) {
      if (stream == null) {
        return expectedBytes.length == 0;
      }
      if (!stream.markSupported()) {
        throw new RuntimeException("cannot test for match on stream that cannot be reset");
      }
      stream.mark(Integer.MAX_VALUE);
      byte[] actualBytes = null;
      try {
        actualBytes = IOUtils.toByteArray(stream);
        stream.reset(); // leave it like we found it
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return Arrays.equals(expectedBytes, actualBytes);
    }

    public void describeTo(final Description description) {
      final int MAX_EXCERPT_LENGTH = 10;
      
      description.appendText(shortName);
      description.appendText("(");
      
      if (StringUtils.isNotBlank(expectedEncoding)) {
        description.appendText("text=");
      
        String text = null;
        try {
          text = new String(expectedBytes, expectedEncoding);
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
        description.appendText(head(text, MAX_EXCERPT_LENGTH));
        description.appendText(",");
        description.appendText("encoding=");
        description.appendText(expectedEncoding);
      } else {
        description.appendText("bytes=");
        description.appendText(head(expectedBytes, MAX_EXCERPT_LENGTH));
      }
      description.appendText(",");
      description.appendText("mimeType=");
      description.appendText(expectedMimeType);
      description.appendText(")");
    }
    
    /**
     * Returns at most {@code count} characters from {@code str}.
     */
    private String head(final String str, final int count) {
      if (str.length() > count) {
        return str.substring(0, count) + "...";
      } else {
        return str;
      }
    }

    /**
     * Returns {@code String} representation of array consisting of at most {@code count} bytes from {@code bytes}.
     */
    private String head(final byte[] bytes, final int count) {
      if (bytes.length > count) {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        for (int i = 0; i < count; i++) {
          if (i > 0) {
            buf.append(", ");
          }
          buf.append(bytes[i]);
        }
        buf.append("...");
        buf.append("]");
        return buf.toString();
      } else {
        return Arrays.toString(bytes);
      }
    }

  }
  
  /**
   * Matcher for {@link RepositoryFile}.  Will only attempt to match non-null properties.
   */
  private static class SelectiveRepositoryFileMatcher extends TypeSafeMatcher<RepositoryFile> {

    private static final String shortName = "isLikeFile";
    
    private RepositoryFile expectedFile;

    public SelectiveRepositoryFileMatcher(final RepositoryFile expectedFile) {
      this.expectedFile = expectedFile;
    }
    
    /*
     * If you add comparisons here, add them in describeTo as well.
     */
    @Override
    public boolean matchesSafely(final RepositoryFile file) {
      return (expectedFile.getId() != null ? expectedFile.getId().equals(file.getId()) : true) &&
          (expectedFile.getName() != null ? expectedFile.getName().equals(file.getName()) : true) &&
          (expectedFile.getTitle() != null ? expectedFile.getTitle().equals(file.getTitle()) : true) &&
          (expectedFile.getPath() != null ? expectedFile.getPath().equals(file.getPath()) : true) &&
          (expectedFile.getCreatedDate() != null ? expectedFile.getCreatedDate().equals(file.getCreatedDate()) : true) &&
          (expectedFile.getLastModifiedDate() != null ? expectedFile.getLastModifiedDate().equals(file.getLastModifiedDate()) : true) &&
          (expectedFile.getVersionId() != null ? expectedFile.getVersionId().equals(file.getVersionId()) : true) &&
          (expectedFile.getDeletedDate() != null ? expectedFile.getDeletedDate().equals(file.getDeletedDate()) : true);
    }
    
    @Override
    public void describeTo(final Description description) {
      boolean appended = false;
      description.appendText(shortName);
      description.appendText("(");
      if (expectedFile.getId() != null) {
        description.appendText(appended ? "," : "");
        description.appendText("id=");
        description.appendText(expectedFile.getId().toString());
        appended = true;
      }
      if (expectedFile.getName() != null) {
        description.appendText(appended ? "," : "");
        description.appendText("name=");
        description.appendText(expectedFile.getName());
        appended = true;
      }
      if (expectedFile.getTitle() != null) {
        description.appendText(appended ? "," : "");
        description.appendText("title=");
        description.appendText(expectedFile.getTitle());
        appended = true;
      }
      if (expectedFile.getPath() != null) {
        description.appendText(appended ? "," : "");
        description.appendText("path=");
        description.appendText(expectedFile.getPath());
        appended = true;
      }
      if (expectedFile.getCreatedDate() != null) {
        description.appendText(appended ? "," : "");
        description.appendText("createdDate=");
        description.appendText(expectedFile.getCreatedDate().toString());
        appended = true;
      }
      if (expectedFile.getLastModifiedDate() != null) {
        description.appendText(appended ? "," : "");
        description.appendText("lastModifiedDate=");
        description.appendText(expectedFile.getLastModifiedDate().toString());
        appended = true;
      }
      if (expectedFile.getVersionId() != null) {
        description.appendText(appended ? "," : "");
        description.appendText("versionId=");
        description.appendText(expectedFile.getVersionId().toString());
        appended = true;
      }
      if (expectedFile.getDeletedDate() != null) {
        description.appendText(appended ? "," : "");
        description.appendText("deletedDate=");
        description.appendText(expectedFile.getDeletedDate().toString());
        appended = true;
      }
      description.appendText(")");
    }

  }

  /**
   * Matcher for binary {@code SimpleRepositoryFileData}.
   * 
   * <p>Example:</p>
   * <pre>
   * assertThat(simpleRepositoryFileData, hasData(byteArray, "application/pdf"));
   * </pre>
   * 
   * @param expectedBytes expected bytes
   * @param expectedMimeType expected MIME type
   * @return matcher
   */
  public static <T> Matcher<SimpleRepositoryFileData> hasData(final byte[] expectedBytes, final String expectedMimeType) {
    return new SimpleRepositoryFileDataMatcher(expectedBytes, null, expectedMimeType);
  }

  /**
   * Matcher for textual {@code SimpleRepositoryFileData}.
   * 
   * <p>Example:</p>
   * <pre>
   * assertThat(simpleRepositoryFileData, hasData("test123", "UTF-8", "text/plain"));
   * </pre>
   * 
   * @param expectedText expected text
   * @param encoding expected encoding
   * @param expectedMimeType expected MIME type
   * @return matcher
   */
  public static <T> Matcher<SimpleRepositoryFileData> hasData(final String expectedText, final String encoding,
      final String expectedMimeType) {
    byte[] expectedBytes = null;
    try {
      expectedBytes = expectedText.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    return new SimpleRepositoryFileDataMatcher(expectedBytes, encoding, expectedMimeType);
  }
  
  /**
   * Matcher for {@code RepositoryFile}. Only attempts to match on non-null properties.
   * 
   * <p>Example:</p>
   * <pre>
   * assertThat(repositoryFile, isLikeFile(new RepositoryFile.Builder("123", "test.txt").build()));
   * </pre>
   * 
   * @param expectedFile expected file
   * @return matcher
   */
  public static <T> Matcher<RepositoryFile> isLikeFile(final RepositoryFile expectedFile) {
    return new SelectiveRepositoryFileMatcher(expectedFile);
  }
  
  /**
   * Factory for {@code NodeRepositoryFileData} matcher. Only attempts to match pairs given.
   * 
   * <p>Example:</p>
   * <pre>
   * assertThat(nodeRepositoryFileData, hasData(pathPropertyPair("/databaseMeta/HOST_NAME", "localhost")));
   * </pre>
   * 
   * @param pairs path property pairs
   * @return matcher
   */
  public static <T> Matcher<NodeRepositoryFileData> hasData(final PathPropertyPair... pairs) {
    return new NodeRepositoryFileDataMatcher(pairs);
  }

}
