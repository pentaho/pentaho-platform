package org.pentaho.platform.repository2.unified;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;

/**
 * <a href="http://code.google.com/p/hamcrest/">Hamcrest</a> matchers {@code IUnifiedRepository}.
 * 
 * <p>
 * Hamcrest matchers can be used by JUnit's {@code assertThat}, JMock, EasyMock, and Mockito.
 * </p>
 * 
 * @author mlowery
 */
@SuppressWarnings({ "nls" })
public class UnifiedRepositoryMatchers {

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

}
