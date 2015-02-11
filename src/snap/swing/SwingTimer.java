package snap.swing;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import snap.util.UIEvent;

/**
 * A UITimer for Swing and SwingOwner.
 */
public class SwingTimer {

    // The SwingOwner
    SwingOwner      _owner;
    
    // The name
    String          _name;
    
    // The delay
    int             _period;
    
    // The timer start time
    long            _startTime;
    
    // The number of times the timer has fired
    int             _count;
    
    // The timer
    Timer           _timer = new Timer();
    
    // The timer task
    TimerTask       _task;

/**
 * Creates a new timer for name and period.
 */
public SwingTimer init(SwingOwner anOwner, String aName, int aPeriod)
{
    _owner = anOwner; _name = aName; _period = aPeriod; return this;
}

/**
 * Returns the owner.
 */
public SwingOwner getOwner()  { return _owner; }

/**
 * Returns the name.
 */
public String getName()  { return _name; }

/**
 * Returns the time in milliseconds between firings.
 */
public int getPeriod()  { return _period; }

/**
 * Sets the time in milliseconds between firings.
 */
public void setPeriod(int aPeriod)
{
    if(aPeriod==_period) return;
    _period = aPeriod;
    if(isRunning()) { stop(); start(); }
}

/**
 * Returns whether timer is running.
 */
public boolean isRunning()  { return _task!=null; }

/**
 * Returns the number of milliseconds timer has been active since start.
 */
public long getTime()  { return System.currentTimeMillis() - _startTime; }

/**
 * Returns the number of times the timer has fired.
 */
public int getCount()  { return _count; }

/**
 * Start timer.
 */
public void start()  { start(0); }

/**
 * Start timer.
 */
public synchronized void start(long aDelay)
{
    // If task already present, return
    if(_task!=null) return;
    
    // Create task and schedule
    _task = new TimerTask() { public void run() {
        sendEventInUIThread(); }};
    
    // Schedule task
    _timer.scheduleAtFixedRate(_task, aDelay, getPeriod());
    _startTime = System.currentTimeMillis();
}

/**
 * Stop timer.
 */
public synchronized void stop()
{
    if(_task!=null)
        _task.cancel();
    _task = null;
}

/**
 * Sends the event in UI Thread.
 */
protected void sendEventInUIThread()
{
    SwingUtilities.invokeLater(new Runnable() { public void run() { sendEvent(); }});
}

/**
 * Sends the event.
 */
protected void sendEvent()
{
    SwingOwner owner = getOwner(); JComponent ui = owner.getUI();
    SwingEvent.TimerEvent te = new SwingEvent.TimerEvent(ui, this);
    SwingEvent swge = new SwingEvent(te, ui, UIEvent.Type.Timer);
    swge.setName(getName());
    owner.sendEvent(swge);
    incrementSendCount();
}

/**
 * Increments the send count.
 */
protected void incrementSendCount()  { _count++; }

}