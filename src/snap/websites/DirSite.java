package snap.websites;
import java.util.ArrayList;
import java.util.List;
import snap.web.*;

/**
 * A data source implementation that draws from a directory WebFile.
 */
public class DirSite extends WebSite {

    // The directory WebFile
    WebFile          _dir;

/**
 * Returns the directory.
 */
public WebFile getDir()  { return getURL().getFile(); }

/**
 * Returns the directory file for a path.
 */
protected WebFile getDirFile(String aPath)
{
    WebFile dir = getDir(); if(dir==null || !dir.isDirectory()) return null;
    WebSite ds = dir.getSite();
    String path = dir.getPath() + aPath;
    return ds.getFile(path);
}

/**
 * Returns the directory file for a path.
 */
protected WebFile createDirFile(String aPath, boolean isDir)
{
    WebFile dir = getDir(); if(dir==null || !dir.isDirectory()) return null;
    WebSite ds = dir.getSite();
    String path = dir.getPath() + aPath;
    return ds.createFile(path, isDir);
}

/**
 * Get file from directory.
 */
protected WebFile getFileImpl(String aPath) throws Exception
{
    WebFile dfile = getDirFile(aPath); if(dfile==null) return null;
    WebFile file = createFile(aPath, dfile.isDirectory());
    file.setModifiedTime(dfile.getModifiedTime());
    return file;
}

/**
 * Get file from directory.
 */
protected List <WebFile> getFilesImpl(WebFile aFile) throws Exception
{
    WebFile dfile = getDirFile(aFile.getPath()); if(dfile==null) return null;
    List <WebFile> dfiles = dfile.getFiles(), files = new ArrayList(dfiles.size());
    for(WebFile df : dfiles) {
        WebFile f = createFile(aFile.getDirPath() + df.getName(), df.isDirectory());
        f.setModifiedTime(df.getModifiedTime());
        files.add(f);
    }
    return files;
}

/**
 * Return file bytes.
 */
protected byte[] getFileBytesImpl(WebFile aFile) throws Exception
{
    WebFile dfile = getDirFile(aFile.getPath());
    return dfile!=null? dfile.getBytes() : null;
}

/**
 * Save file.
 */
protected void saveFileImpl(WebFile aFile) throws Exception
{
    WebFile dfile = createDirFile(aFile.getPath(), aFile.isDirectory());
    if(!aFile.isDirectory()) dfile.setBytes(aFile.getBytes());
    dfile.save();
    aFile.setModifiedTime(dfile.getModifiedTime());
}

/**
 * Delete file.
 */
protected void deleteFileImpl(WebFile aFile) throws Exception
{
    WebFile dfile = getDirFile(aFile.getPath());
    if(dfile!=null) dfile.delete();
}

/** WebSite methods. */
protected List getRowsImpl(Entity anEntity, Query aQuery)  { return null; }
protected void saveRowImpl(Row aRow)  { }
protected void deleteRowImpl(Row aRow)  { }

}