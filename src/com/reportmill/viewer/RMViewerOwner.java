package com.reportmill.viewer;
import com.reportmill.base.RMClassUtils;
import com.reportmill.shape.*;
import com.reportmill.swing.shape.JComponentShape;
import java.util.concurrent.Callable;
import javax.swing.*;
import snap.swing.*;
import snap.util.*;
import snap.web.WebURL;

/**
 * A class to manage an RMViewer and .rpt file.
 */
public class RMViewerOwner extends UIOwner <SwingEvent, RMShape> {

    // Returns the viewer
    RMViewer          _viewer;

    // An object to manage window for SwingPanel
    SwingWindow       _swin;

/**
 * Returns the viewer.
 */
public RMViewer getViewer()  { return _viewer!=null? _viewer : (_viewer=createViewer()); }

/**
 * Creates the viewer.
 */
protected RMViewer createViewer()  { RMViewer viewer = new RMViewer(); viewer.setContent(getUI()); return viewer; }

/**
 * Returns the SwingPanelWindow to manage this SwingPanel's window.
 */
public SwingWindow getWindow()  { return _swin!=null? _swin : (_swin=createWindow()); }

/**
 * Returns the SwingPanelWindow to manage this SwingPanel's window.
 */
protected SwingWindow createWindow()
{
    SwingWindow swin = new SwingWindow();
    swin.setContentPaneCall(new Callable() { public JComponent call() { return getViewer(); }});
    return swin;
}

/**
 * Override to return UI as RMParentShape.
 */
public RMParentShape getUI()  { return (RMParentShape)super.getUI(); }

/**
 * Override to create document for class.
 */
protected RMShape createUI()
{
    WebURL url = WebURL.getURL(getClass(), getClass().getSimpleName() + ".rpt");
    return new RMArchiver().getParentShape(url);
}

/**
 * Returns a component if inside JComponentShape.
 */
public JComponent getComponent(String aName)
{
    RMShape shape = getNode(aName);
    if(shape instanceof JComponentShape)
        return ((JComponentShape)shape).getComponent();
    return null;
}

/**
 * Returns a component if inside JComponentShape.
 */
public <T> T getComponent(String aName, Class <T> aClass)
{
    return RMClassUtils.getInstance(getComponent(aName), aClass);
}

/**
 * Override.
 */
public UIHelper getNodeHelper(Object anObj)  { return RMViewerOwnerHpr.GetHelper(anObj); }

/**
 * Override.
 */
public void requestFocus(Object anObj)  { }

/**
 * Override.
 */
public void sendEvent(Object anObj)  { }

/**
 * Override.
 */
public void addKeyActionEvent(String aName, String aKey)  { }

/**
 * Override.
 */
public void runLater(Runnable aRunnable)  { SwingUtilities.invokeLater(aRunnable); }

/**
 * Returns whether current thread is event thread.
 */
protected boolean isEventThread()  { return SwingUtilities.isEventDispatchThread(); }

}