package snap.util;
import java.math.*;
import java.util.*;

/**
 * Utility methods for common math operations.
 */
public class SnapMath {
    
    // Random
    static Random _random = new Random();   

/**
 * Returns whether two real numbers are equal within a small tolerance.
 */
public static boolean equals(double a, double b)  { return Math.abs(a - b) < 0.0001; }

/**
 * Returns whether a real number is practically zero.
 */
public static boolean equalsZero(double a)  { return equals(a, 0); }

/**
 * Returns whether a real number is practically greater than another.
 */
public static boolean gt(double a, double b)  { return (a>b) && !equals(a,b); }

/**
 * Returns whether a real number is practically greater than or equal another.
 */
public static boolean gte(double a, double b)  { return (a>b) || equals(a,b); }

/**
 * Returns whether a real number is practically less than another.
 */
public static boolean lt(double a, double b)  { return (a<b) && !equals(a,b); }

/**
 * Returns whether a real number is practically less than or equal another.
 */
public static boolean lte(double a, double b)  { return (a<b) || equals(a,b); }

/**
 * Returns whether a real number is between two other numbers.
 */
public static boolean between(double a, double x, double y)  { return ((a>=x) && (a<=y)); }

/**
 * Returns the sign of a given number (as -1 or 1).
 */
public static int sign(double f)  { return f<0? -1 : 1; }

/**
 * Returns the given number modulo the second number (mod for floats).
 */
public static double mod(double x, double y)  { return x - y*Math.floor(x/y); }

/**
 * Returns the given number rounded to the second number (rounding to arbitrary floating values).
 */
public static float round(float x, float y)  { return y*(int)((x + sign(x)*y/2)/y); }

/**
 * Returns the given number rounded to the second number (rounding to arbitrary double values).
 */
public static double round(double x, double y)  { return y*(int)((x + sign(x)*y/2)/y); }
  
/**
 * Truncate x down to the nearest y.
 */
public static double trunc(double x, double y)  { return y*Math.floor((x+.00001)/y); }

/**
 * Truncate x down to the nearest y.
 */
public static double floor(double x, double y)  { return y*Math.floor((x+.00001)/y); }

/**
 * Truncate x up to the nearest y.
 */
public static double ceil(double x, double y)  { return y*Math.ceil((x+.00001)/y); }

/**
 * Returns the given float clamped to 1/1000th precision.
 */
public static double clamp(double f)  { return (f>-1e-3) && (f<1e-3)? 0 : (f>1e5? 1e5f : (f<-1e5? -1e5f : f)); }

/**
 * Returns the given in clamped between the two values.
 */
public static int clamp(int i, int min, int max)  { return Math.min(Math.max(i, min), max); }

/**
 * Returns the given double clamped between the two values.
 */
public static float clamp(double f, double min, double max)  { return (float)Math.min(Math.max(f,min),max); }

/**
 * Returns the given double clamped between the two values (wraps around if out of range).
 */
public static int clamp_wrap(int a, int x, int y)  { return a<x? y - (x-a)%(y-x) : a>y? x + (a-y)%(y-x) : a; }

/**
 * Returns the given double clamped between the two values (wraps around if out of range).
 */
public static double clamp_wrap(double a, double x, double y)
{
    return a<x? y - Math.IEEEremainder(x-a,y-x) : a>y? x + Math.IEEEremainder(a-y,y-x) : a;
}

/**
 * Returns the given double clamped between the two values.
 */
public static double clamp_doubleback(double a, double x, double y)
{
    double newA = Math.abs(Math.IEEEremainder(a,2*(y-x)));
    if(newA>y) newA = 2*y-newA;
    return newA;
}

/**
 * Returns the negative of the given Number.
 */
public static Number negate(Number aNumber)
{
    // If BigDecimal, have it negate
    if(aNumber instanceof BigDecimal)
        return ((BigDecimal)aNumber).negate();
    
    // Return big decimal of negative double value
    return new BigDecimal(-SnapUtils.doubleValue(aNumber));
}

/**
 * Returns the sum of the two given Numbers.
 */
public static Number add(Number n1, Number n2)
{
    // Try subtracting as BigDecimal (can fail if either are NaN or neg/pos infinity), otherwise add as doubles
    try { return SnapUtils.getBigDecimal(n1).add(SnapUtils.getBigDecimal(n2)); }
    catch(Exception e) { return SnapUtils.doubleValue(n1) + SnapUtils.doubleValue(n2); }
}

/**
 * Returns the difference of the two given Numbers.
 */
public static Number subtract(Number n1, Number n2)
{
    // Try subtracting as BigDecimal (can fail if either are NaN or neg/pos infinity), otherwise subtract as doubles
    try { return SnapUtils.getBigDecimal(n1).subtract(SnapUtils.getBigDecimal(n2)); }
    catch(Exception e) { return SnapUtils.doubleValue(n1) - SnapUtils.doubleValue(n2); }
}

/**
 * Returns the product of the two given Numbers.
 */
public static Number multiply(Number n1, Number n2)
{
    // Try multiplying as BigDecimals (can fail if either are NaN or neg/pos infinity), otherwise, multiply as doubles
    try { return SnapUtils.getBigDecimal(n1).multiply(SnapUtils.getBigDecimal(n2)); }
    catch(Exception e) { return SnapUtils.doubleValue(n1)*SnapUtils.doubleValue(n2); }
}

/**
 * Returns the result of dividing n1 by n2.
 */
public static Number divide(Number n1, Number n2)
{
    // Try dividing as BigDecimals (can fail if either are NaN or neg/pos infinity)
    try { return SnapUtils.getBigDecimal(n1).divide(SnapUtils.getBigDecimal(n2), 16, BigDecimal.ROUND_HALF_DOWN); }
    
    // Otherwise, divide as doubles
    catch(Exception e) {
        double d1 = SnapUtils.doubleValue(n1);
        double d2 = SnapUtils.doubleValue(n2);
        if(d2==0)
            return d1>=0? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        return d1/d2;
    }
}

/**
 * Returns the sign of the given angle in degrees.
 */
public static double sin(double anAngle)  { return Math.sin(Math.toRadians(anAngle)); }

/**
 * Returns the cos of the given angle in degrees.
 */
public static double cos(double anAngle)  { return Math.cos(Math.toRadians(anAngle)); }

/**
 * Returns a random int.
 */
public static int randomInt()  { return _random.nextInt(); }

/**
 * Returns a random float up to given value.
 */
public static float randomFloat(float aVal)  { return Math.abs(_random.nextInt())/(float)Integer.MAX_VALUE*aVal; }

/**
 * A class used to specify an arbitrary mathematical function.
 */
public abstract static class RMFunc {

    // the function
    public abstract double f(double x);
  
    /**
     *  nth derivative of function at x.
     *  Base class calculates it numerically, but you could override this if you know the exact form.
     *  order=1 for first derivative, 2 for second derivative, etc.
     */
    public double fprime(double x, int order) {
        if (order<=0) return f(x);
        double epsilon = 1e-10;
        return (fprime(x+epsilon,order-1)-fprime(x,order-1))/epsilon;
    }
    
    /**
     * Numerical integration of a function in the interval [start, end].
     * Uses composite Simpson's method.
     * (override if you know the exact form)
     */
    public double integrate(double start, double end, int npts)
    {
        if (end<start)
            return -integrate(end, start, npts);
        
        // make sure n is even
        int nintervals = npts+(npts%2==1?1:0);
        // get size of each interval
        double h = (end-start)/nintervals;
        // Simpson's method: 
        //   I = h/3 * (f(start) + 4*Sum[f,start+h,end-h,2h] + 2*Sum[f,start+2h,end-2h,2h] + f(end))
        
        // start with the endpoints
        double integral = f(start)+f(end);
        // calculate sums of even & odd terms
        double point = start+h;
        double odds=f(point);
        double evens=0;
        while(nintervals>2) {
            point += h;
            evens += f(point);
            point += h;
            odds += f(point);
            nintervals -= 2;
        }
        integral += 4*odds + 2*evens;
        return h*integral/3;
    }
    
    /**
     * Uses Newton's method to find numerical solution to f(x)=a.
     * (override if you know the exact solution)
     */
    public double solve(double a)
    {
        // use a as initial guess
        double newX=a;
        double guess;
        double limit = 1e-10;
        int maxiters=1000;
        
        // Newton's method:  newx = oldx - f(oldx)/f'(oldx)
        do {
            guess = newX;
            newX -= (f(guess)-a)/fprime(guess,1);
            } 
        // loop until guess has settled on an answer within limit
        while(Math.abs(guess-newX) > limit && --maxiters>0);
        
        // if we seem to have gotten stuck, accept whatever we're at if it's reasonable
        // otherwise report an error
        if ((maxiters==0) && (Math.abs(f(guess)-a) > limit/2))
            // perhaps a little severe
            throw new RuntimeException("Can't converge on solution.");
        
        //testing - average seems to converge in about 3 or 4 iterations
        //System.err.println("DELETEME: solved in "+(1001-maxiters)+" iterations");
                
        return guess;
    }
}

}