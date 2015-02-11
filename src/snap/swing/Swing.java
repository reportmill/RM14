package snap.swing;
import com.reportmill.shape.RMArchiver;
import com.reportmill.swing.shape.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import snap.util.*;
import snap.web.WebURL;

/**
 * This class helps to load and manage panels of UI components in the form of a UI file, which is a simple
 * XML description of UI controls.
 */
public class Swing {
    
    // Cache for images loaded by UIs
    static Map <String, Image>  _images = new HashMap();
    
    // Cache for icons loaded by UIs
    static Map <String, Icon>   _icons = new HashMap();
    
/** Causes ribs to be initialized when Swing class is first touched. */
static { init(); }

/**
 * Initializes Snap Swing functionality (installs an event queue and configures helper map).
 * This is called automatically when Ribs class is first touched.
 */
public static void init()  { RibsEventQueue.Shared.install(); }

/**
 * This method loads a UI file from a byte source.
 */
public static JComponent createUI(Object aSource)
{
    // Get Source URL and/or bytes
    WebURL url = null; try { url = WebURL.getURL(aSource); } catch(Exception e) { }
    byte bytes[] = url!=null && url.getFile()!=null? url.getFile().getBytes() : SnapUtils.getBytes(aSource);

    // Create archiver and read component shape
    JComponentShape jcs = (JComponentShape)new RMArchiver().getParentShape(url!=null? url : bytes);
    
    // Create component and return
    JBuilder builder = new JBuilder(); builder.setUseRealClasses(true);
    JComponent comp = builder.createComponentDeep(jcs);
    if(comp instanceof SpringsPane) comp.setPreferredSize(comp.getSize());
    return comp;
}

/**
 * Returns the named image - tries to load from given owner class resource directory or current directory.
 */
public static Image getImage(String aName, Class aClass)
{
    Image image = _images.get(aName);
    if(image==null) _images.put(aName, image = createImage(aName, aClass));
    return image;
}

/**
 * Creates the named image - tries to load from given owner class resource directory or current directory.
 */
public static Image createImage(String aName, Class aClass)
{
    InputStream istream = getInputStream(aName, aClass);
    try { if(istream!=null) return ImageIO.read(istream); }
    catch(IOException e)  { System.err.println(e); }
    catch(SecurityException e) {
        if(ImageIO.getUseCache()) {
            System.out.println("ImageIO Security Exception - turning off image cache");
            ImageIO.setUseCache(false);
            return createImage(aName, aClass);
        }
    }
    System.err.println("Swing.createImage: Couldn't load " + aName + " for " + aClass); return null;
}

/**
 * Returns the named icon - tries to load from given owner class resource directory or current directory.
 */
public static Icon getIcon(String aName, Class aClass)
{
    Icon icon = _icons.get(aName);
    if(icon==null) _icons.put(aName, icon = createIcon(aName, aClass));
    return icon;
}

/**
 * Creates the named icon - tries to load from given owner class resource directory or current directory.
 */
public static Icon createIcon(String aName, Class aClass)
{
    // Get bytes for given name and owner (just return if null)
    InputStream inputStream = getInputStream(aName, aClass);
    byte bytes[] = inputStream!=null? SnapUtils.getBytes(inputStream) : null;
    if(bytes==null)
        return UIManager.getIcon(aName); // What the heck - let Swing try to resolve the name
    
    // Create icon from bytes and return
    if(StringUtils.endsWithIC(aName, ".rpt"))
        return new RMShapeIcon(bytes, 0, 0);
    return new ImageIcon(bytes);
}

/**
 * Returns an input stream for the given name - tries to load from given owner class resource dir or current dir.
 */
public static InputStream getInputStream(String aName, Class aClass)
{
    // Try referencing aName relative to source-class
    InputStream is1 = aClass.getResourceAsStream(aName);
    if(is1!=null)
        return is1;
    
    // Try referencing source-class.ribs/aName
    String path2 = aClass.getSimpleName() + ".ribs/" + aName;
    InputStream is2 = aClass.getResourceAsStream(path2);
    if(is2!=null)
        return is2;
    
    // Try referencing pkg.images/aName
    String path3 = "pkg.images/" + aName;
    InputStream is3 = aClass.getResourceAsStream(path3);
    if(is3!=null)
        return is3;
    
    // Return null since not found
    return null;
}

/**
 * Returns whether the alt key is down for the current input event.
 */
public static boolean isAltDown()  { return RibsEventQueue.Shared._isAltDown; }

/**
 * Returns whether the meta key is down for the current input event.
 */
public static boolean isMetaDown()  { return RibsEventQueue.Shared._isMetaDown; }

/**
 * Returns whether mouse is pressed in mouse drag loop.
 */
public static boolean isMouseDown()  { return RibsEventQueue.Shared._mousePressedEvent!=null; }

/**
 * Sends Event for the given EventObject.
 */
public static void sendEvent(EventObject anEvent)  { sendEvent(anEvent, null); }

/**
 * Sends Event for the given EventObject and component.
 */
public static void sendEvent(EventObject anEvent, JComponent aComp)
{
    JComponent comp = aComp!=null? aComp : (JComponent)anEvent.getSource(); if(comp==null) return;
    SwingEvent event = new SwingEvent(anEvent, comp);
    SwingHelper helper = SwingHelper.getSwingHelper(comp);
    UIOwner owner = helper.getOwner(comp); if(owner==null) return;
    owner.sendEvent(event);
}

/** Legacy. */
public static SwingHelper getHelper(Object anObj)  { return SwingHelper.getSwingHelper(anObj); }

/** Legacy. */
public static SwingHelpers.JComponentHpr getHelper(JComponent aComp)  { return SwingHelper.getSwingHelper(aComp); }

/**
 * Runs runnable later in Swing if thread not swing.
 */
public static boolean runSwing(Runnable aRunnable)
{
    if(SwingUtilities.isEventDispatchThread()) return false;
    SwingUtilities.invokeLater(aRunnable); return true;
}

/**
 * Runs runnable later in Swing if thread not swing.
 */
public static boolean callSwing(Runnable aRunnable)
{
    if(SwingUtilities.isEventDispatchThread()) return false;
    try { SwingUtilities.invokeAndWait(aRunnable); return true; }
    catch(Exception e) { throw new RuntimeException(e); }
}

}