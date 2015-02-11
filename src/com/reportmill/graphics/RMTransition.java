package com.reportmill.graphics;
import com.reportmill.base.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.Timer;

/**
 * An animation class that represents a change of an object property from one value to another.
 */
public class RMTransition implements Comparable<RMTransition> {

    // The target
    Object               _target;
    
    // The key
    String               _key;
    
    // The first value
    Object               _value1;
    
    // The second value
    Object               _value2;
    
    // The duration of the transition
    int                  _duration;
    
    // The delay of the transition
    int                  _delay;
    
    // Whether the transition does an auto-reverse
    boolean              _autoReverse;
    
    // The number of cycles the transition does
    int                  _cycleCount = 1;
    
    // The interpolator of the transition
    RMInterpolator       _interpolator = RMInterpolator.LINEAR;
    
    // The run status of the transition
    RunStatus            _runStatus;
    
    // The progress this transition has made in milliseconds
    int                  _progress;
    
    // The system timer
    static Timer         _timer;
    
    // The system timer start time
    static long          _timerStart;
    
    // The system timer current time
    static long          _timerTime;
    
    // The current transitions
    static List <RMTransition>  _transitions = new ArrayList();
    
    // Constants for run status of transition
    public enum RunStatus { Stopped, Running, Paused };

/**
 * Creates a new transition.
 */
public RMTransition(Object aTarget, String aKey, Object aValue, int aDuration, boolean isRelative)
{
    _target = aTarget; _key = aKey;
    _value2 = isRelative? add(getValue1(), aValue) : aValue;
    _duration = aDuration;
}

/**
 * Creates a new transition.
 */
public RMTransition(RMKeyValue aKeyVal1, RMKeyValue aKeyVal2)
{
    _target = aKeyVal1.getTarget(); _key = aKeyVal1.getKey();
    _value1 = aKeyVal1.getValue(); _value2 = aKeyVal2.getValue();
}

/**
 * Returns the target.
 */
public Object getTarget()  { return _target; }

/**
 * Returns the key.
 */
public String getKey()  { return _key; }

/**
 * Returns the first value.
 */
public Object getValue1()  { return _value1!=null? _value1 : (_value1 = RMKeyChain.getValue(getTarget(), getKey())); }

/**
 * Sets the first value.
 */
public void setValue1(Object aValue)  { _value1 = aValue; }

/**
 * Returns the second value.
 */
public Object getValue2()  { return _value2; }

/**
 * Sets the second value.
 */
public void setValue2(Object aValue)  { _value2 = aValue; }

/**
 * Returns the duration of the transition in milliseconds.
 */
public int getDuration()  { return _duration; }

/**
 * Sets the duration of the transition in milliseconds.
 */
public void setDuration(int aValue)  { _duration = aValue; }

/**
 * Returns the delay of the transition from the time it is told to play to when transition starts.
 */
public int getDelay()  { return _delay; }

/**
 * Sets the delay of the transition from the time it is told to play to when transition starts.
 */
public void setDelay(int aDelay)  { _delay = aDelay; }

/**
 * Returns whether the transition does an auto-reverse.
 */
public boolean isAutoReverse()  { return _autoReverse; }

/**
 * Sets whether the transition does an auto-reverse.
 */
public void setAutoReverse(boolean doAutoReverse)  { _autoReverse = doAutoReverse; }

/**
 * Returns the number of cycles in animation.
 */
public int getCycleCount()  { return _cycleCount; }

/**
 * Sets the number of cycles in animation.
 */
public void setCycleCount(int aCount)  { _cycleCount = aCount; }

/**
 * Returns the interpolator.
 */
public RMInterpolator getInterpolator()  { return _interpolator; }

/**
 * Sets the interpolator.
 */
public void setInterpolator(RMInterpolator anInterpolator)  { _interpolator = anInterpolator; }

/**
 * Returns the duration plus delay.
 */
public int getDelayedDuration()  { return getDuration() + getDelay(); }

/**
 * Returns the total duration of the animation (delayed duration times cycle count).
 */
public int getTotalDuration()  { return getDelayedDuration()*getCycleCount(); }

/**
 * Returns whether transition is running.
 */
public boolean isRunning()  { return getRunStatus()==RunStatus.Running; }

/**
 * Returns whether transition is paused.
 */
public boolean isPaused()  { return getRunStatus()==RunStatus.Paused; }

/**
 * Returns the run status of the transition.
 */
public RunStatus getRunStatus()  { return _runStatus; }

/**
 * Sets the run status of the transition.
 */
public void setRunStatus(RunStatus aStatus)  { _runStatus = aStatus; }

/**
 * Returns the progress this transition has made since it was started, in milliseconds.
 */
public int getProgress()  { return _progress; }

/**
 * Sets the progress this transition has made since it was started, in milliseconds.
 */
protected void setProgress(int aProgress)
{
    // Get progress ratio
    double progressRatio = getProgressRatio(aProgress);
    
    // Get value for progress and set value
    if(progressRatio!=getProgressRatio()) {
        double fraction = getInterpolator().getValue(progressRatio);
        Object value = getInterpolation(fraction, getValue1(), getValue2());
        RMKeyChain.setValue(getTarget(), getKey(), value);
    }
    
    // Set new progress
    _progress = aProgress;
}

/**
 * Returns the progress ratio for current progress.
 */
public double getProgressRatio()  { return getProgressRatio(getProgress()); }

/**
 * Returns the progress ratio for a progress.
 */
protected double getProgressRatio(int aProgress)
{
    // Get durations
    int delay = getDelay();
    int duration = getDuration();
    int delayedDuration = delay + duration;
    //int cycleDuration = isAutoReverse()? delayedDuration*2 : delayedDuration;
    int totalDuration = delayedDuration*getCycleCount();
    
    // Handle edge cases
    if(aProgress<=delay) return 0; if(aProgress>=totalDuration) return isAutoReverse()? getCycleCount()%2 : 1;
    
    // Get progress in cycle
    int cycle = aProgress/delayedDuration;
    int cycleProgress = aProgress % delayedDuration;
    int delayedProgress = isAutoReverse() && cycle%2==1? delayedDuration - cycleProgress : cycleProgress;
    int progress = delayedProgress - delay;

    // Return progress ratio
    return progress/(double)duration;
}

/**
 * Tells the transition to start playing.
 */
public void play()  { if(!isRunning()) playFrom(getProgress()); }

/**
 * Tells the transition to start playing from the beginning.
 */
public void playFromStart()  { playFrom(0); }

/**
 * Tells the transition to start playing at given time of transition duration (in milliseconds).
 */
public void playFrom(int aTime)
{
    // Get index of transition (remove existing one, if there)
    int index = Collections.binarySearch(_transitions, this);
    if(index>=0)
        _transitions.remove(index);
    else index = -index - 1;
    
    // Make sure timer is running
    if(_timer==null) _timer = new Timer(25, new ActionListener() { public void actionPerformed(ActionEvent e) {
        updateTransitions(); } });
    if(!_timer.isRunning()) {
        _timer.start(); _timerTime = System.currentTimeMillis(); }
    
    // Add transition
    _transitions.add(index, this);
    setRunStatus(RunStatus.Running);
    _progress = aTime;
}

/**
 * Pauses the animation.
 */
public void pause()  { if(isRunning()) setRunStatus(RunStatus.Paused); }

/**
 * Stops the transition.
 */
public void stop()
{
    // Get index of transition (remove existing one, if there)
    int index = Collections.binarySearch(_transitions, this);
    if(index>=0)
        _transitions.remove(index);
    
    // Update RunStatus
    setRunStatus(RunStatus.Stopped);
}

/**
 * Returns whether transition is running for given target and key.
 */
public static boolean isTransitioning(Object aTarget, String aKey)
{
    RMTransition t = getTransition(aTarget, aKey); return t!=null && t.isRunning();
}

/**
 * Returns the transition for given target and key.
 */
public static RMTransition getTransition(Object aTarget, String aKey)
{
    for(RMTransition trans : _transitions)
        if(trans.getTarget()==aTarget && trans.getKey().equals(aKey))
            return trans;
    return null;
}

/**
 * Updates transitions.
 */
protected static void updateTransitions()
{
    // Get current timer time
    long timerTime = System.currentTimeMillis();
    int elapsed = (int)(timerTime - _timerTime);
    
    // Iterate over transitions and update
    for(int i=_transitions.size()-1; i>=0; i--) { RMTransition trans = _transitions.get(i);
        
        // If transition is playing, update it
        if(trans.isRunning())
            trans.setProgress(trans.getProgress() + elapsed);
        
        // It transition is done, stop it
        if(trans.getProgress()>=trans.getTotalDuration())
            trans.stop();
    }
    
    // Set new timer time
    _timerTime = timerTime;
    
    // If no transitions left, stop timer
    if(_transitions.size()==0)
        _timer.stop();
}

/**
 * Adds a value.
 */
private Object add(Object aVal1, Object aVal2)
{
    if(aVal1 instanceof Integer) return ((Integer)aVal1) + RMUtils.intValue(aVal2);
    if(aVal1 instanceof Long) return ((Long)aVal1) + RMUtils.longValue(aVal2);
    if(aVal1 instanceof Float) return ((Float)aVal1) + RMUtils.floatValue(aVal2);
    if(aVal1 instanceof Double) return ((Double)aVal1) + RMUtils.doubleValue(aVal2);
    return aVal2;
}

/**
 * Standard compare implementation.
 */
public int compareTo(RMTransition aTrans)
{
    if(getTarget()!=aTrans.getTarget()) return System.identityHashCode(this)<System.identityHashCode(aTrans)? -1 : 1;
    return getKey().compareTo(aTrans.getKey());
}

/**
 * Returns an invocation by blending this invocation with given invocation using the given fraction of this invocation.
 */
public static Object getInterpolation(double aFraction, Object aValue1, Object aValue2)
{
    // Handle end-cases (0, 1) special - just return unblended value
    if(RMMath.equals(aFraction, 0)) return aValue1;
    else if(RMMath.equals(aFraction, 1)) return aValue2;
    
    // Get values
    Object value1 = aValue1;
    Object value2 = aValue2;
    
    // If either value is null, get zero version of other (bail if both are null)
    if(value1==null) { value1 = getZeroValue(value2); if(value2==null) return null; }
    if(value2==null) value2 = getZeroValue(value1);
    
    // Handle float, double, BigDecimal
    if(value1 instanceof Float || value1 instanceof Double || value1 instanceof BigDecimal) {
        double v1 = RMUtils.doubleValue(value1);
        double v2 = RMUtils.doubleValue(value2);
        return getInterpolation(aFraction, v1, v2);
    }
    
    // Handle int
    else if(value1 instanceof Integer) {
        double v1 = RMUtils.doubleValue(value1);
        double v2 = RMUtils.doubleValue(value2);
        return getInterpolationIntValue(aFraction, v1, v2);
    }
    
    // Handle Boolean (just return this since initial fraction check indicated we hadn't hit other invocation
    else if(value1 instanceof Boolean)
        return value1;
    
    // Handle RMColor
    else if(RMColor.class.isAssignableFrom(value1.getClass())) {
        RMColor c1 = (RMColor)value1;
        RMColor c2 = (RMColor)value2;
        return c1.blend(c2, aFraction);
    }
    
    // Handle Dimension
    else if(value1 instanceof Dimension) {
        Dimension d1 = (Dimension)value1;
        Dimension d2 = (Dimension)value2;
        int w = getInterpolationIntValue(aFraction, d1.getWidth(), d2.getWidth());
        int h = getInterpolationIntValue(aFraction, d1.getHeight(), d2.getHeight());
        return new Dimension(w, h);
    }
    
    // Handle Rectangle
    else if(value1 instanceof Rectangle2D) {
        Rectangle2D r1 = (Rectangle2D)value1;
        Rectangle2D r2 = (Rectangle2D)value2;
        double x = getInterpolation(aFraction, r1.getX(), r2.getX());
        double y = getInterpolation(aFraction, r1.getY(), r2.getY());
        double w = getInterpolation(aFraction, r1.getWidth(), r2.getWidth());
        double h = getInterpolation(aFraction, r1.getHeight(), r2.getHeight());
        Rectangle2D r3 = (Rectangle2D)r1.clone(); r3.setRect(x, y, w, h);
        return r3;
    }
    
    // Complain about anyone else
    else System.err.println("KeyTimer:blend: Unsupported arg type");

    // Return blended invocation
    return null;
}

/** Returns an interpolation for a given fraction (from 0-1) and two values. */
private static double getInterpolation(double aFraction, double aStart, double anEnd)
{ return aStart + (anEnd-aStart)*aFraction; }

/** Returns an interpolation for a given fraction (from 0-1) and two values. */
private static int getInterpolationIntValue(double aFraction, double aStart, double anEnd)
{ return (int)Math.round(getInterpolation(aFraction, aStart, anEnd)); }

/** Returns a "zero value" for various core types. */
private static Object getZeroValue(Object anObj)
{
    if(anObj instanceof Number) return 0;
    if(anObj instanceof Boolean) return Boolean.FALSE;
    if(anObj instanceof RMColor) return RMColor.clearWhite;
    return anObj;
}

/**
 * A transition for XY.
 */
public static class Translate extends RMTransition {

    // The y transition
    RMTransition    _y;
    
    /** Creates a new Translate transition. */
    public Translate(Object aTarget, double anX, double aY, int aDuration, boolean isRelative)
    {
        super(aTarget, "X", anX, aDuration, isRelative);
        _y = new RMTransition(aTarget, "Y", aY, aDuration, isRelative);
    }
    
    /** Override to send to both transitions. */
    public void setAutoReverse(boolean doAutoReverse)
    {
        super.setAutoReverse(doAutoReverse);
        _y.setAutoReverse(doAutoReverse);
    }
    
    /** Override to send to both transitions. */
    public void setCycleCount(int aCount)
    {
        super.setCycleCount(aCount);
        _y.setCycleCount(aCount);
    }
    
    /** Override to send to both transitions. */
    public void playFrom(int aTime)
    {
        super.playFrom(aTime);
        _y.playFrom(aTime);
    }
    
    /** Override to send to both transitions. */
    public void stop()
    {
        super.stop();
        _y.stop();
    }
}

/**
 * A transition for rotation.
 */
public static class Rotate extends RMTransition {

    /** Creates a new translation transition. */
    public Rotate(Object aTarget, double aRoll, int aDuration, boolean isRelative)
    {
        super(aTarget, "Roll", aRoll, aDuration, isRelative);
    }
}

}