package com.reportmill.app;
import com.reportmill.shape.RMArchiver;
import com.reportmill.shape.RMParentShape;
import javax.swing.Icon;
import snap.swing.Swing;

/**
 * A gallery panel for ribs Swing components.
 */
public class SwingGallery extends Gallery {

    // Array of documents
    Object          _docs[];
     
    // Array of document icons
    Icon            _docIcons[];
     
/**
 * Creates a new SwingGallery.
 */
public SwingGallery()
{
    // Create and initialize docs/icons arrays
    _docs = new Object[5]; _docIcons = new Icon[5];
    _docs[0] = "SwingGallery.ribs/Gallery0.rib";
    _docIcons[0] = Swing.getIcon("Gallery0.png", getClass());
    _docs[1] = "SwingGallery.ribs/Gallery1.rib";
    _docIcons[1] = Swing.getIcon("Gallery1.png", getClass());
    _docs[2] = "SwingGallery.ribs/Gallery2.rib";
    _docIcons[2] = Swing.getIcon("/com/reportmill/swing/tool/JTable.png", getClass());
    _docs[3] = "SwingGallery.ribs/Gallery3.rib";
    _docIcons[3] = Swing.getIcon("/com/reportmill/swing/tool/JTabbedPane.png", getClass());
    _docs[4] = "SwingGallery.ribs/Gallery4.rib";
    _docIcons[4] = Swing.getIcon("Gallery4.png", getClass());
}

/**
 * Returns the number of document shown by the gallery.
 */
public int getDocumentCount()  { return 5; }

/**
 * Returns the specific document at the given index.
 */
public RMParentShape getDocument(int anIndex)
{
    Object doc = _docs[anIndex];
    if(doc instanceof String)
        doc = _docs[anIndex] = new RMArchiver().getParentShape(getClass().getResourceAsStream((String)doc));
    return (RMParentShape)doc;
}

/**
 * Returns the icon for the specific document at the given index.
 */
public Icon getDocumentIcon(int anIndex)  { return _docIcons[anIndex]; }

}