package snap.swing;
import java.awt.datatransfer.*;
import java.io.File;
import java.util.List;

/**
 * Clipboard utilities.
 */
public class ClipboardUtils {

/**
 * Returns a string from given transferable.
 */
public static String getString(Transferable aTrans)
{
    // Handle StringFlavor
    if(aTrans.isDataFlavorSupported(DataFlavor.stringFlavor))
        try { return (String)aTrans.getTransferData(DataFlavor.stringFlavor); }
        catch(Exception e) { e.printStackTrace(); return null; }
    
    // Handle FileList
    List <File> files = getFiles(aTrans);
    if(files!=null && files.size()>0)
        return files.get(0).getAbsolutePath();
    
    // Otherwise return null
    return null;
}

/**
 * Returns a list of files from a given transferable.
 */
public static List <File> getFiles(Transferable aTrans)
{
    // Handle JavaFileListFlavor
    if(aTrans.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        try { return (List)aTrans.getTransferData(DataFlavor.javaFileListFlavor); }
        catch(Exception e) { System.err.println(e); return null; }
    
    // Otherwise return null
    return null;
}

}