package snap.websites;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import snap.util.URLUtils;
import snap.web.*;

/**
 * A data source that pulls from the class path.
 */
public class ClassSite extends WebSite {

/**
 * Override to return name for ClassSite.
 */
public String getName()  { return "Class"; }

/**
 * Override to return Protocol for ClassSite.
 */
public String getURLScheme()  { return "class"; }

/**
 * Returns a WebFile for given path (if file exists).
 */
protected WebFile getFileImpl(String aPath)
{
    URL url = getClass().getResource(aPath); if(url==null) return null;
    WebFile file = createFile(aPath, aPath.equals("/"));
    return file;
}

protected List <WebFile> getFilesImpl(WebFile aFile)  { return Collections.emptyList(); }

protected byte[] getFileBytesImpl(WebFile aFile) throws IOException
{
    URL url = getClass().getResource(aFile.getPath()); if(url==null) return null;
    return URLUtils.getBytes(url);
}

protected void saveFileImpl(WebFile aFile) throws Exception  { }
protected void deleteFileImpl(WebFile aFile) throws Exception  { }

protected List<Row> getRowsImpl(Entity anEntity, Query aQuery)  { return null; }
protected void saveRowImpl(Row aRow)  { }
protected void deleteRowImpl(Row aRow)  { }

}