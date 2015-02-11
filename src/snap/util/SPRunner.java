/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * A class for running operations in the background.
 */
public abstract class SPRunner <T> extends SnapObject {

    // The progress value
    double           _progress;
    
    // The name of this runner
    String           _name = "Snap Runner Thread";
    
    // A description of current activity
    String           _activityText = "Performing task(s)";
    
    // The runner status
    Status           _status = Status.Idle;

    // The runner thread
    Thread           _thread;
    
    // The runner start time (milliseconds)
    long             _startTime;
    
    // The runner end time (milliseconds)
    long             _endTime;
    
    // The result of the run method
    T                _result;
    
    // The exception thrown if run failed
    Exception        _exception;
    
    // Constants for status
    public enum Status { Idle, Running, Finished, Cancelled, Failed }
    
    // Constants for Runner PropertyChanges
    public static final String Progress_Prop = "Progress";
    public static final String ActivityText_Prop = "ActivityText";
    public static final String Status_Prop = "Status";
    
/**
 * Returns the progress value.
 */
public double getProgress()  { return _progress; }

/**
 * Sets the progress value.
 */
public void setProgress(double aValue)
{
    double value = aValue; if(value>1) value = 1;
    firePropertyChange(Progress_Prop, _progress, _progress = value, -1);
}

/**
 * Returns the name of runner (and thread).
 */
public String getName()  { return _name; }

/**
 * Sets the name of runner (and thread).
 */
public void setName(String aName)  { _name = aName; }

/**
 * Returns a description of the current activity beging performed.
 */
public String getActivityText()  { return _activityText; }

/**
 * Sets a description of the current activity beging performed.
 */
public void setActivityText(String aString)
{
    firePropertyChange(ActivityText_Prop, _activityText, _activityText = aString, -1);
}

/**
 * Returns the status.
 */
public Status getStatus()  { return _status; }

/**
 * Sets the status.
 */
protected void setStatus(Status aStatus)  { firePropertyChange(Status_Prop, _status, _status = aStatus, -1); }

/**
 * Returns the thread.
 */
public Thread getThread()  { return _thread; }

/**
 * Joins the runner.
 */
public SPRunner <T> join()  { try { _thread.join(); } catch(Exception e) { } return this; }

/**
 * Joins the runner.
 */
public SPRunner <T> join(int aTimeout)  { try { _thread.join(aTimeout); } catch(Exception e) { } return this; }

/**
 * Returns whether thread is still active.
 */
public boolean isActive()  { return _thread!=null && _thread.isAlive(); }

/**
 * Whether runner has been cancelled.
 */
public boolean isCancelled()  { return _status==Status.Cancelled; }

/**
 * Returns the start time.
 */
public long getStartTime()  { return _startTime; }

/**
 * Returns the end time.
 */
public long getEndTime()  { return _endTime; }

/**
 * Returns the elapsed time.
 */
public long getElapsedTime()  { return (isActive()? getSystemTime() : getEndTime()) - getStartTime(); }

/**
 * Returns the system time.
 */
protected long getSystemTime()  { return System.currentTimeMillis(); }

/**
 * Starts the runner.
 */
public SPRunner <T> start()
{
    // Create new thread to run this runner's run method then success/failure/finished method with result/exception
    _thread = new Thread(new Runnable() { public void run() {
        invokeRun();
        invokeFinished();
    }});
    
    // Start thread
    _thread.setName(getName());
    _thread.start();
    
    // Return this runner
    return this;
}

/**
 * Cancels the runner.
 */
public void cancel()
{
    // Set Status to Cancelled and interrupt
    setStatus(Status.Cancelled);
    getThread().interrupt();
}

/**
 * The method to run.
 */
public abstract T run() throws Exception;

/**
 * The method run on success.
 */
public void success(T aResult)  { }

/**
 * The method to run when cancelled.
 */
public void cancelled(Exception e)  { }

/**
 * The method to run on failure.
 */
public void failure(Exception e)  { }

/**
 * The method to run when finished (after success()/failure() call).
 */
public void finished()  { }

/**
 * Returns the result.
 */
public T getResult()  { return _result; }

/**
 * Returns the exception.
 */
public Throwable getExeption()  { return _exception; }

/**
 * Runs the run method.
 */
protected void invokeRun()
{
    // Set start time and run status
    _exception = null;
    _startTime = getSystemTime();
    setStatus(Status.Running);

    // Run run
    try { _result = run(); }
    catch(Exception e) { _exception = e; }
    catch(Throwable e) { _exception = new RuntimeException(e); }
    
    // Set end time
    _endTime = getSystemTime();
}

/**
 * Runs the success method.
 */
protected void invokeFinished()
{
    // Update status
    setStatus(_exception==null? Status.Finished : Status.Failed);
    
    // Call success/failure
    if(_exception==null)
        success(_result);
    else if(getStatus()==Status.Cancelled)
        cancelled(_exception);
    else failure(_exception);
    finished();
}

}