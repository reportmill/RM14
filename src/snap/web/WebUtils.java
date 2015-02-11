package snap.web;

/**
 * Utility methods for Web classes.
 */
public class WebUtils {

/**
 * Returns the common ancestor of this file and given file.
 */
public static WebFile getCommonAncestor(WebFile aFile1, WebFile aFile2)
{
    // Get this file directory and given file directory and return either if they are equal or root
    WebFile directory = aFile1.isDirectory()? aFile1 : aFile1.getParent();
    WebFile fileDirectory = directory.isRoot()? directory : aFile2.isDirectory()? aFile2 : aFile2.getParent();
    if(directory==fileDirectory)
        return directory;
    
    // Iterate up file's parents and return any equal to this file's directory
    for(WebFile file=fileDirectory.getParent(); !file.isRoot(); file=file.getParent())
        if(file==directory)
            return directory;
    
    // If not found, try again with directory parent
    return getCommonAncestor(directory.getParent(), fileDirectory);
}

}