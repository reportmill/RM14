package com.reportmill.apptools;
import com.reportmill.app.RMEditorPane;
import com.reportmill.base.RMClassUtils;
import com.reportmill.shape.*;
import java.awt.event.MouseEvent;

/**
 * A tool implementation for RMSubreport.
 */
public class RMSubreportTool <T extends RMSubreport> extends RMTool <T> {

/** Override to declare window title. */
public String getWindowTitle()  { return "Subreport"; }

/** Override to declare shape class. */
public Class getShapeClass()  { return RMSubreport.class; }

/**
 * Event handling from select tool for super selected shapes.
 */
public void mousePressed(T aSubreport, MouseEvent anEvent)
{
    // Open doc
    if(anEvent.getClickCount()>1 && aSubreport.getSubreportName()!=null) {
        RMDocument document = aSubreport.getDocument().getSubreport(aSubreport.getSubreportName());
        if(document!=null) {
            RMEditorPane editorPane = RMClassUtils.newInstance(getEditorPane().getClass()).open(document);
            editorPane.setWindowVisible(true);
        }
    }
}


}
