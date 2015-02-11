package snap.swing;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import snap.util.*;

/**
 * A class to manage dragging.
 */
public class SwingDragger implements DragSourceListener, DragSourceMotionListener {

    // The item being dragged
    Object                _item;

    // The thing to drag
    Transferable          _trans;

    // The DragGestureEvent
    DragGestureEvent      _dge;
    
    // The drag image
    Image                 _dragImage;
    
    // The point that the drag image should be dragged by
    Point                 _dragImagePoint;
    
    // An optional drag source listener
    DragSourceListener    _dsl;
    
    // A window that is optionally used to simulate image dragging.
    JWindow               _dragWindow;
    
    // The currently active dragger
    static SwingDragger   _dragger;

/**
 * Returns the item really being dragged.
 */
public Object getDragItem()  { return _item; }

/**
 * Sets the item really being dragged.
 */
public void setDragItem(Object anItem)  { _item = anItem; }

/**
 * Returns the drag item as given class.
 */
public <T> T getDragItem(Class<T> aClass)  { return ClassUtils.getInstance(_item, aClass); }

/**
 * Returns the drag item as string.
 */
public String getDragItemString()  { return _item instanceof String? (String)_item : null; }

/**
 * Returns the DragGestureEvent.
 */
public DragGestureEvent getDragGestureEvent()  { return _dge; }

/**
 * Sets the DragGestureEvent.
 */
public void setDragGestureEvent(DragGestureEvent anEvent)  { _dge = anEvent; }

/**
 * Returns the image to be dragged.
 */
public Image getDragImage()  { return _dragImage; }

/**
 * Sets the image to be dragged.
 */
public void setDragImage(Image anImage)
{
    _dragImage = anImage;
    if(getDragImagePoint()==null)
        setDragImagePoint(anImage.getWidth(null)/2, anImage.getHeight(null)/2);
}

/**
 * Sets the drag image from given string and font.
 */
public void setDragImageFromString(String aString, Font aFont)
{
    Font font = aFont!=null? aFont : getComponent().getFont();
    setDragImage(AWTUtils.getImageForString(aString, font));
}

/**
 * Returns the point that the drag image should be dragged by.
 */
public Point getDragImagePoint()  { return _dragImagePoint; }

/**
 * Returns the point that the drag image should be dragged by.
 */
public void setDragImagePoint(Point aPoint)  { _dragImagePoint = aPoint; }

/**
 * Returns the point that the drag image should be dragged by.
 */
public void setDragImagePoint(double anX, double aY)  { setDragImagePoint(new Point((int)anX, (int)aY)); }

/**
 * Returns the drag image offset point.
 */
protected Point getDragImageOffset()
{
    Point p = _dragImagePoint!=null? new Point(_dragImagePoint.x, _dragImagePoint.y) : null;
    if(p!=null && SnapUtils.isMac) { p.x = -p.x; p.y = -p.y; }
    return p;
}

/**
 * Sets the transferable.
 */
public Transferable getTransferable()  { return _trans!=null? _trans : (_trans=createTransferable()); }

/**
 * Sets the transferable.
 */
public void setTransferable(Transferable aTrans)  { _trans = aTrans; }

/**
 * Sets the transferable from string.
 */
public void setTransferable(String aString)  { setTransferable(new StringSelection(aString)); }

/**
 * Creates a transferable for DragItem.
 */
protected Transferable createTransferable()  { return createTransferable(getDragItem()); }

/**
 * Creates a transferable for given object.
 */
public static Transferable createTransferable(Object anItem)
{
    // Handle String
    if(anItem instanceof String)
        return new StringSelection((String)anItem);
        
    // Handle File
    if(anItem instanceof File)
        return new FileTransferable((File)anItem);

    // Throw Exception
    throw new RuntimeException("SwingDragger.createTransferable: Class not supported: " + anItem.getClass());
}

/**
 * Returns the optional DragSourceListener.
 */
public DragSourceListener getDragSourceListener()  { return _dsl; }

/**
 * Sets an optional DragSourceListener.
 */
public void setDragSourceListener(DragSourceListener aListener)  { _dsl = aListener; }

/**
 * Returns the component.
 */
protected JComponent getComponent()  { return (JComponent)_dge.getComponent(); }

/**
 * Returns the DragSource.
 */
public DragSource getDragSource()  { return _dge.getDragSource(); }

/**
 * Starts the drag.
 */
public void startDrag()
{
    // Get DragSource and start Listening to drag events drag source
    DragSource dragSource = getDragSource();
    dragSource.addDragSourceListener(this);
    dragSource.addDragSourceMotionListener(this);
    
    // Check to see if image drag is supported by system. If not (ie, Windows), simulate image dragging with a window.
    if(getDragImage()!=null && !DragSource.isDragImageSupported())
        createDragWindow();

    // Start drag
    _dragger = this;
    Transferable trans = getTransferable();
    dragSource.startDrag(_dge, DragSource.DefaultCopyDrop, getDragImage(), getDragImageOffset(), trans, _dsl);
}

/**
 * Returns the currently active dragger.
 */
public static SwingDragger getActiveDragger()  { return _dragger; }

/**
 * Creates new drag source listener.
 */
protected void createDragWindow()
{
    // Get drag image and the source window (if source is component)
    Image image = getDragImage();
    Window sourceWindow = SwingUtils.getWindow(getComponent());

    // Create window for drag image
    _dragWindow = new JWindow(sourceWindow);
    _dragWindow.setSize(image.getWidth(null), image.getHeight(null));
   
    // Create label for drag image and add to window
    _dragWindow.getContentPane().add(new JLabel(new ImageIcon(image)));
}

/**
 * DragSourceListener method.
 */
public void dragEnter(DragSourceDragEvent anEvent)  { }

/**
 * DragSourceMotionListener method.
 */
public void dragMouseMoved(DragSourceDragEvent anEvent) 
{
    // Make the window follow the cursor, if using window-based image dragging
    // Note that the offset of the window is 1 pixel down and to the right of the cursor.  This is different
    // from how it appears if the system can handle image dragging, in which case the image is centered under the
    // cursor. If the dragWindow were centered at the cursor position, the dragWindow would become the destination
    // of all the system drag events, and we would never get meaningful dragEntered, dragExited, etc. events.
    // Clients can use translateRectToDropDestination() to get the proper image location across systems.
    if(_dragWindow!=null) {
        _dragWindow.setLocation(anEvent.getX()+1, anEvent.getY()+1);
        if(!_dragWindow.isVisible())
            _dragWindow.setVisible(true);
    }
}

/**
 * DragSourceListener method.
 */
public void dragDropEnd(DragSourceDropEvent anEvent)
{
    // Get rid of the window and its resources
    if(_dragWindow!=null) {
        _dragWindow.setVisible(false);
        _dragWindow.dispose(); _dragWindow = null;
    }
    
    // Stop listening to events
    getDragSource().removeDragSourceListener(this);
    getDragSource().removeDragSourceMotionListener(this);
    _dragger = null;
}

/**
 * DragSourceListener method.
 */
public void dragOver(DragSourceDragEvent anEvent)  { }

/**
 * DragSourceListener method.
 */
public void dragExit(DragSourceEvent anEvent)  { }

/**
 * DragSourceListener method.
 */
public void dropActionChanged(DragSourceDragEvent anEvent)  { }

/**
 * A File Transferable.
 */
public static class FileTransferable implements Transferable {

    // The list of files
    java.util.List <File>  _files;

    /** Creates transferable. */
    public FileTransferable(File aFile)  { _files = Arrays.asList(aFile); }

    /** Creates transferable. */
    public FileTransferable(List <File> theFiles)  { _files = theFiles; }

    /** Transferable method. */
    public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[] { DataFlavor.javaFileListFlavor }; }
    
    /** Transferable method. */
    public boolean isDataFlavorSupported(DataFlavor aFlavor) { return aFlavor==DataFlavor.javaFileListFlavor; }
    
    /** Transferable method. */
    public Object getTransferData(DataFlavor aFlavor) { return _files; }
}

}