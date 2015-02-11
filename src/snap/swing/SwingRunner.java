/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.swing;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import snap.util.SPRunner;

/**
 * A runner than invokes success/failure methods in Swing EventDispatch thread.
 */
public abstract class SwingRunner <T> extends SPRunner <T> implements PropertyChangeListener {

    // The parent component
    JComponent       _component;
    
    // The runner panel title
    String           _title = "Background Process Panel";
    
    // The progress monitor
    ProgressMonitor  _progressMonitor;
    
    // The runner estimated time (milliseconds - implies timer active)
    int              _estimatedTime = -1;
    
    // The timer to update progress if estimated time is set
    Timer            _timer;

/**
 * Creates a SwingRunner.
 */
public SwingRunner()  { }

/**
 * Creates a SwingRunner for component and with title - will show a panel if activity takes too long.
 */
public SwingRunner(JComponent aComponent, String aTitle)
{
    _component = aComponent; _title = aTitle; _estimatedTime = 0;
    addPropertyChangeListener(this);
}

/**
 * Returns the parent component.
 */
public JComponent getComponent() { return _component; }

/**
 * Returns the title.
 */
public String getTitle()  { return _title; }

/**
 * Override to start timer/panel.
 */
public SwingRunner<T> start()
{
    // Do normal version
    super.start();
    if(_estimatedTime<0) return this;
    
    // Create timer
    if(getEstimatedTime()>0) {
        _timer = new Timer(200, new ActionListener() { public void actionPerformed(ActionEvent e) {
            double progress = getElapsedTime()/(double)getEstimatedTime();
            setProgress(progress);
        }});
        _timer.start();
    }
    
    // Create panel
    _progressMonitor = new ProgressMonitor(getComponent(), getTitle(), getActivityText(), 0, 1000) {
        public void close() {
            super.close();
            if(isCanceled())
                cancel();
        }
    };
    
    // Return this
    return this;
}

/**
 * Returns the estimated time.
 */
public int getEstimatedTime()  { return _estimatedTime; }

/**
 * Sets the estimated time.
 */
public SwingRunner<T> setEstimatedTime(int aValue)  { _estimatedTime = aValue; return this; }

/**
 * Watch runner property changes to update monitor.
 */
public void propertyChange(final PropertyChangeEvent anEvent)
{
    // If not Swing thread, re-invoke on Swing thread and return
    if(!SwingUtilities.isEventDispatchThread()) {
        SwingUtilities.invokeLater(new Runnable() { public void run() { propertyChange(anEvent); }}); return; }
    
    // Handle Progress
    String pname = anEvent.getPropertyName();
    if(pname.equals(Progress_Prop) && _progressMonitor!=null)
        _progressMonitor.setProgress((int)Math.round(getProgress()*1000));
    
    // Handle ActivityText
    else if(pname.equals(ActivityText_Prop) && _progressMonitor!=null)
        _progressMonitor.setNote(getActivityText());
        
    // Handle Status
    else if(pname.equals(Status_Prop) && getStatus()!=Status.Running) {
        
        // Close progress monitor
        if(_progressMonitor!=null) {
            _progressMonitor.close(); _progressMonitor = null; }
        
        // Stop/clear timer
        if(_timer!=null) {
            _timer.stop(); _timer = null; }
    }
}

/**
 * Runs the success method.
 */
protected void invokeFinished()
{
    SwingUtilities.invokeLater(new Runnable() { public void run() {
        SwingRunner.super.invokeFinished(); }});
}

}