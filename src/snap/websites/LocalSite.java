package snap.websites;
import java.io.File;
import java.util.*;

/**
 * A FileSite subclass that stores files in a named directory relative to SnapCode home directory.
 */
public class LocalSite extends FileSite {

    // The file path to this data source
    String                 _filePath;
    
/**
 * Returns the string identifying the prefix for URLs in this data source.
 */
public String getURLScheme()  { return "local"; }

/**
 * RMJSONArchiver GetKeys method.
 */
public List <String> getJSONKeys()  { return Arrays.asList("Name"); }

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
        File jdir = snap.util.ClientUtils.getHomeDir(true);
        _filePath = new File(jdir, getPath()).getAbsolutePath();
    }
    
    // Return file path
    return _filePath;
}

}