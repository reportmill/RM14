package snap.websites;
import snap.util.FileUtils;
import java.io.File;

/**
 * A WebSite subclass to manage files in ~/Library/SnapSandboxes.
 */
public class SandboxSite extends FileSite {

    // The file path to this data source
    String                 _filePath;
    
/**
 * Returns the string identifying the prefix for URLs in this data source.
 */
public String getURLScheme()  { return "sandbox"; }

/**
 * Returns the Java file for RMFile.
 */
protected File getStandardFile(String aPath)  { return new File(getPathInFileSystem() + aPath); }

/**
 * Returns the path of this data source in file system.
 */
protected String getPathInFileSystem()
{
    // If not set or if name has changed, reset cached FilePath
    if(_filePath==null) {
        File jsdir = FileUtils.getAppDataDir("SnapSandboxes", true);
        String path = getPath(); if(File.separatorChar!='/') path = path.replace("/", File.separator);
        _filePath = new File(jsdir, path).getAbsolutePath();
    }
    
    // Return file path
    return _filePath;
}

}