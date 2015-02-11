package snap.websites;
import java.io.*;
import java.util.*;
import snap.util.StringUtils;
import snap.web.*;

/**
 * A WebSite for HTTP sources.
 */
public class HTTPSite extends WebSite {

/**
 * Returns the string identifying the prefix for URLs in this data source.
 */
public String getURLScheme()  { return "http"; }

/**
 * Returns a data source file for given path (if file exists).
 */
protected WebFile getFileImpl(String aPath) throws IOException
{
    // Fetch URL
    String urls = getURLString() + aPath;
    HTTPRequest req = new HTTPRequest(urls);
    HTTPResponse resp  = req.getResponse();
    
    // Handle non-success response codes
    if(resp.getCode()==HTTPResponse.NOT_FOUND)
        return null; // throw new FileNotFoundException(aPath);
    if(resp.getCode()==HTTPResponse.UNAUTHORIZED)
        throw new AccessException();
    if(resp.getCode()!=HTTPResponse.OK)
        throw new IOException(resp.getMessage());
    
    // Create file, set bytes and return
    boolean isDir = StringUtils.getPathExtension(aPath).length()==0;
    if(isDir)  { String str = resp.getText(); isDir = str!=null && str.contains("Index of"); }
    WebFile file = createFile(aPath, isDir);
    file.setBytes(resp.getBytes());
    return file;
}

/**
 * Gets file bytes.
 */
public byte[] getFileBytesImpl(WebFile aFile) throws IOException
{
    String urls = getURLString() + aFile.getPath();
    HTTPRequest req = new HTTPRequest(urls);
    HTTPResponse resp  = req.getResponse();
    return resp.getBytes();
}

/**
 * Returns files at path.
 */
public List <WebFile> getFilesImpl(WebFile aFile) throws IOException
{
    // Create files list
    List <WebFile> files = getFilesFromHTML(aFile);
    if(files!=null)
        return files;
    
    // Gets files from html
    files = new ArrayList();
    
    // If ".index" file exists, load children
    String path = aFile.getPath();
    WebFile indexFile = getFile(StringUtils.getPathChild(path, ".index"));
    if(indexFile!=null) {
        String indexFileString = StringUtils.getISOLatinString(indexFile.getBytes());
        String fileEntries[] = indexFileString.split("\n");
        for(String fileEntry : fileEntries) {
            if(fileEntry.length()==0) continue;
            String fileInfo[] = fileEntry.split("\t");
            WebFile file = createFile(StringUtils.getPathChild(path, fileInfo[0]), false);
            files.add(file);
        }
    }
    
    // Return files
    return files;
}

/**
 * Returns files from HTML.
 */
List <WebFile> getFilesFromHTML(WebFile aFile) throws IOException
{
    byte bytes[] = getFileBytesImpl(aFile);
    String text = new String(bytes);
    int htag = text.indexOf("<HTML>"); if(htag<0) return null;
    List <WebFile> files = new ArrayList();
    
    for(int i=text.indexOf("HREF=\"", htag); i>0; i=text.indexOf("HREF=\"", i+8)) {
        int end = text.indexOf("\"", i+6); if(end<0) continue;
        String name = text.substring(i+6,end);
        if(name.length()<2 || !Character.isLetterOrDigit(name.charAt(0))) continue;
        boolean isDir = false; if(name.endsWith("/")) { isDir = true; name = name.substring(0, name.length()-1); }
        String path = StringUtils.getPathChild(aFile.getPath(), name);
        WebFile file = createFile(path, isDir);
        file.setModifiedTime(System.currentTimeMillis());
        files.add(file);
    }
    return files;
}

/** WebSite method. */
protected void saveFileImpl(WebFile aFile) throws Exception
{
    String urls = getURLString() + aFile.getPath();
    HTTPRequest req = new HTTPRequest(urls);
    req.setBytes(aFile.getBytes());
    req.getResponse();
}

/** WebSite method. */
protected void deleteFileImpl(WebFile aFile) throws Exception  { }

/** WebSite method. */
protected List getRowsImpl(Entity anEntity, Query aQuery)  { return null; }

/** WebSite method. */
protected void saveRowImpl(Row aRow)  { }

/** WebSite method. */
protected void deleteRowImpl(Row aRow)  { }

}