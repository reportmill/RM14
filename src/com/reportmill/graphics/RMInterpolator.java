package com.reportmill.graphics;

/**
 * This class provides functionality to interpolate between two numbers given a ratio between 0-1.
 */
public class RMInterpolator implements Cloneable {

    // The direction for the interpolation
    Direction         _direction = Direction.In;
    
    // Constants for direction
    public enum Direction { In, Out, Both };
    
/**
 * Creates a new interpolator.
 */
public RMInterpolator()  { }

/**
 * Creates a new interpolator with given direction.
 */
public RMInterpolator(Direction aDirection)  { _direction = aDirection; }

/**
 * Returns a shared interpolator for given name.
 */
public static RMInterpolator getInterpolator(String aName)
{
    // Handle no name
    if(aName==null)
        return getInterpolator(0);
    
    // Look for named interpolator
    for(int i=0, iMax=getInterpolatorCount(); i<iMax; i++)
        if(getInterpolator(i).getName().equalsIgnoreCase(aName))
            return getInterpolator(i);
    
    // Return Linear interpolator if interpolator not found
    return getInterpolator(0);
}

/**
 * Returns a new interpolator instance for a given name.
 */
public static RMInterpolator getNewInterpolator(String aName)
{
    RMInterpolator prototype = getInterpolator(aName);
    return prototype.clone();
}

/**
 * Returns the interpolator direction.
 */
public Direction getDirection()  { return _direction; }

/**
 * Returns an interpolated value for 0 & 1 given a ratio.
 */
public double getValue(double aRatio)  { return getValue(aRatio, 0, 1); }

/**
 * Returns a value given a ratio and start/end values.
 */
public double getValue(double aRatio, double aStart, double anEnd)
{
    switch(getDirection()) {
        case In: return interpolate(aRatio, aStart, anEnd);
        case Out: return interpolateOut(aRatio, aStart, anEnd);
        case Both: return interpolateBoth(aRatio, aStart, anEnd);
        default: System.err.println("Unsupported Direction " + getDirection()); return 0;
    }
}

/**
 * Returns a new ratio given normal ratio.
 */
public double getRatio(double aRatio)  { return aRatio; }

/**
 * Direction In interpolation.
 */
public double interpolate(double aRatio, double aStart, double aEnd) { return aStart + (aEnd-aStart)*getRatio(aRatio); }

/**
 * Direction Out interpolation.
 */
public double interpolateOut(double aRatio, double aStart, double aEnd)  { return interpolate(1-aRatio, aEnd, aStart); }

/**
 * Direction Both interpolation.
 */
public double interpolateBoth(double aRatio, double aStart, double anEnd)
{
    // Get mid point of values
    double midpoint = (aStart+anEnd)/2;
  
    // If beyond midpoint of ratio, interpolate in
    if(aRatio<=0.5)
        return interpolate(aRatio*2, aStart, midpoint);
  
    // Otherwise, interpolate out
    return interpolateOut((aRatio-.5)*2, midpoint, anEnd);
}

/**
 * Returns the name of this interpolator.
 */
public String getName()  { return "Linear"; }

/**
 * Returns a string representation of this interpolator.
 */
public String toString()  { return getName(); }

/**
 * Returns true if there's only a single shared instance of this interpolator.
 */
public boolean isShared()  { return true; }

/**
 * Standard clone implementation.
 */
public RMInterpolator clone()
{
    // Shared interpolators are, um, shared
    if(isShared())
        return this;
    
    // Do normal clone for others
    try { return (RMInterpolator)super.clone(); }
    catch(CloneNotSupportedException e) { return null; }
}

/**
 * Linear interpolator.
 */
public static RMInterpolator LINEAR = new RMInterpolator();

/**
 * Ease In interpolator.
 */
public static RMInterpolator EASE_IN = new RMInterpolator() {
    public double getRatio(double aRatio) { return Math.pow(aRatio, 2); }
    public String getName() { return "Ease In"; }
};

/**
 * Ease Out interpolator.
 */
public static RMInterpolator EASE_OUT = new RMInterpolator(Direction.Out) {
    public double getRatio(double aRatio) { return Math.pow(aRatio, 2); }
    public String getName() { return "Ease Out"; }
};

/**
 * Ease Both interpolator.
 */
public static RMInterpolator EASE_BOTH = new RMInterpolator(Direction.Both) {
    public double getRatio(double aRatio) { return Math.pow(aRatio, 2); }
    public String getName() { return "Ease Both"; }
};

/**
 * Ease In interpolator.
 */
public static RMInterpolator EASE_IN_CUBIC = new RMInterpolator() {
    public double getRatio(double aRatio) { return Math.pow(aRatio, 3); }
    public String getName() { return "Ease In Cubic"; }
};

/**
 * Ease Out interpolator.
 */
public static RMInterpolator EASE_OUT_CUBIC = new RMInterpolator(Direction.Out) {
    public double getRatio(double aRatio) { return Math.pow(aRatio, 3); }
    public String getName() { return "Ease Out Cubic"; }
};

/**
 * Ease Both interpolator.
 */
public static RMInterpolator EASE_BOTH_CUBIC = new RMInterpolator(Direction.Both) {
    public double getRatio(double aRatio) { return Math.pow(aRatio, 3); }
    public String getName() { return "Ease Both Cubic"; }
};

/**
 * Random interpolator.
 * Object will get a random value at every frame, bounded by the start & end values.
 */
public static RMInterpolator RANDOM = new RMInterpolator() {
    java.util.Random rand = new java.util.Random();
    public double getRatio(double aRatio) { return rand.nextDouble(); }
    public String getName() { return "Random"; }
};

/**
 * Bounce interpolator.
 */
public static RMInterpolator BOUNCE = new Bounce(4,.5);

/**
 * Parameterizable bounce interpolator class.
 */
public static class Bounce extends RMInterpolator {
    public int _numBounces;
    public double _elasticity;
    double _scale;
    
    /**
     * Create a new Bounce interpolator.
     */
    public Bounce(int nBounces, double elasticity)  { setBounceParameters(nBounces, elasticity); }
        
    /**
     * Sets the interpolator parameters
     *   numBounces is the number of bounces taken before stopping at the destination.
     *   elasticity is a percentage which represents how much energy is lost on each bounce.
     *     0 means the bounce is inelastic and won't bounce at all
     *     1 means the bounce is perfectly elastic, so each bounce will go back to the starting point
     */
    public void setBounceParameters(int numBounces, double elasticity)
    {
        _numBounces = numBounces<0 ? 0 : numBounces;
        _elasticity = elasticity;
        _scale=1;
        double e=2*_elasticity;
        for(int i=0;i<_numBounces;++i) {
            _scale += e;
            e*=_elasticity;
        }
    }

    public double getRatio(double aRatio)
    {
        aRatio *= _scale;
        if (aRatio <= 1) return aRatio*aRatio;
        aRatio-=1;
        double e = 2*_elasticity;
        while(aRatio>0) {
            if (aRatio<=e)
                return 1+aRatio*(aRatio-e);
            aRatio -=e;
            e*=_elasticity;
        }
        return 1;
    }
    public String getName()  { return "Bounce"; }
}

/**
 * One Shot interpolator.
 */
public static RMInterpolator ONE_SHOT = new RMInterpolator() {
    public double getRatio(double aRatio) { return aRatio<1? 0 : 1; }
    public String getName() { return "One Shot"; }
};

/**
 * An Interpolator subclass that can be used for periodic (ie looping) motion.
 * The different type values are (where n is the frequency):
 * LOOP : start -> end, n times, BACKANDFORTH - start -> end -> start, n times
 * SINUSOIDAL - start -> end -> start, n times along a smooth sin wave
 */
public static class Periodic extends RMInterpolator {

    // Ivars
    RMInterpolator     _parent;  // any other interpolator
    PeriodType         _type = PeriodType.LOOP;  // the type of loop
    float              _frequency = 2;  // the number of times to repeat
    float              _phase;  // the phase of the wave

    // Constants of PeriodType
    public enum PeriodType { LOOP, BACKANDFORTH, SINUSOIDAL }

    /** Override to return Periodic. */
    public String getName()  { return "Periodic"; }
    
    /** Returns false, since periodic interpolators have parameters. */
    public boolean isShared()  { return false; }
    
    /** The main entry point. */
    public double getValue(double aRatio, double aStart, double anEnd)
    {
        // In the time it takes to go from 0-1, this interpolator goes 'frequency' times
        double time = aRatio;
        time *= _frequency;
        time += _phase; // adjust for the phase
     
        switch(_type) {
            // when new time gets to 1, set back to 0 ((time*frequency+phase) mod 1) Notice actual endpoint never reached.
            case LOOP: time -= Math.floor(time); break;
            // when the time gets to 1/2, start going backwards
            case BACKANDFORTH: time -= Math.floor(time); time *= 2; if(time>1) time = 2-time; break;
            // result is like back and forth, but smooth instead of abrupt
            case SINUSOIDAL: time = (1-Math.cos(time*2*Math.PI))/2; break;
        }
         
        if (_parent == null) _parent = RMInterpolator.getInterpolator(null);
        return _parent.getValue(time,aStart,anEnd);
    }
    
    // Attributes
    public float getFrequency()  { return _frequency; }
    public void setFrequency(float aFrequency)  { _frequency = aFrequency; }
    public RMInterpolator getParent()  { return _parent!=null? _parent : (_parent = RMInterpolator.getInterpolator(null)); }
    public void setParent(RMInterpolator aParent)  { _parent = aParent; }
    public float getPhase()  { return _phase; }
    public void setPhase(float aPhase)  { _phase = aPhase; }
    public PeriodType getType()  { return _type; }
    public void setType(PeriodType aType)  { _type = aType; }
    public Periodic clone()
    { Periodic clone = (Periodic)super.clone(); clone.setParent(getParent().clone()); return clone; }
}

// Some common named interpolators
static RMInterpolator _interpolators[] = {
    LINEAR, EASE_IN, EASE_OUT, EASE_BOTH, EASE_IN_CUBIC, EASE_OUT_CUBIC, EASE_BOTH_CUBIC,
    BOUNCE, ONE_SHOT, RANDOM,
    new Periodic()
};

/**
 * Returns number of shared common interpolators.
 */
public static int getInterpolatorCount()  { return _interpolators.length; }

/**
 * Returns the individual common interpolator at given index.
 */
public static RMInterpolator getInterpolator(int anIndex)  { return _interpolators[anIndex]; }

}