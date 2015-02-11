package com.reportmill.text;
import java.awt.datatransfer.*;
import java.io.*;
import snap.util.XMLArchiver;

/**
 * Text editor utils.
 */
public class RMTextEditorUtils {

    // RMTextEditorClipboard to Copy/Paste attributed strings.
    static DataFlavor RMTextFlavor = new DataFlavor("text/rm-xstring", "ReportMill Text Data");

/**
 * Transferable implementation for text editor and xstrings.
 */
public static class RMXStringTransferable implements Transferable {
    
    // The string being written to the clipboard
    RMXString _string;
    
    /** Creates a new editor clipboard for given xstring. */
    public RMXStringTransferable(RMXString aString) { _string = aString; }
    
    /** Returns the supported flavors: RMTextFlavor and stringFlavor. */
    public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[] { RMTextFlavor, DataFlavor.stringFlavor }; }
    
    /** Returns whether given flavor is supported. */
    public boolean isDataFlavorSupported(DataFlavor f)
    {
        return f.equals(RMTextFlavor) || f.equals(DataFlavor.stringFlavor);
    }
    
    /** Returns an inputstream with clipboard data for requested flavor. */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
    {
        // If RMTextFlavor, archive bytes for xstring
        if(flavor.equals(RMTextFlavor)) {
            byte bytes[] = new XMLArchiver().toXML(_string).getBytes();
            return new ByteArrayInputStream(bytes);
        }
        
        // If string flavor, just return string
        else if(flavor.equals(DataFlavor.stringFlavor))
            return _string.getText();
        
        // Otherwise, complain
        else throw new UnsupportedFlavorException(flavor);
    }
}

/**
 * This method returns the range of the @-sign delinated key closest to the current selection (or null if not found).
 */
public static RMTextSel smartFindFormatRange(RMTextEditor aTextEditor)
{
    int selectionStart = aTextEditor.getSelStart();
    int selectionEnd = aTextEditor.getSelEnd();
    int previousAtSignIndex = -1;
    int nextAtSignIndex = -1;
    String string = aTextEditor.getXString().getText();

    // See if selection contains an '@'
    if(selectionEnd>selectionStart)
        previousAtSignIndex = string.indexOf("@", selectionStart);
    if(previousAtSignIndex>=selectionEnd)
        previousAtSignIndex = -1;

    // If there wasn't an '@' in selection, see if there is one before the selected range
    if(previousAtSignIndex<0)
        previousAtSignIndex = string.lastIndexOf("@", selectionStart-1);

    // If there wasn't an '@' in or before selection, see if there is one after the selected range
    if(previousAtSignIndex<0)
        previousAtSignIndex = string.indexOf("@", selectionEnd);

    // If there is a '@' in, before or after selection, see if there is another after it
    if(previousAtSignIndex>=0)
        nextAtSignIndex = string.indexOf("@", previousAtSignIndex + 1);

    // If there is a '@' in, before or after selection, but not one after it, see if there is one before that
    if(previousAtSignIndex>=0 && nextAtSignIndex<0)
        nextAtSignIndex = string.lastIndexOf("@", previousAtSignIndex-1);

    // If both a previous and next '@', select the chars inbetween
    if(previousAtSignIndex>=0 && nextAtSignIndex>=0 && previousAtSignIndex!=nextAtSignIndex) {
        int start = Math.min(previousAtSignIndex, nextAtSignIndex);
        int end = Math.max(previousAtSignIndex, nextAtSignIndex);
        return new RMTextSel(aTextEditor, start, end + 1);
    }

    // Return null since range not found
    return null;
}

}