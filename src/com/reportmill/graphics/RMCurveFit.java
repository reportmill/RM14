package com.reportmill.graphics;

import java.util.*;
import com.reportmill.base.*;

/** 
 * This class creates an approximating polygon for a given RMFunction.
 * The polygon is expressed as a series of sample points that 
 * can be interpolated between using Neville's method to quickly
 * obtain the value of any function.
 * 
 * This is used for path animation, to map the complex curve which
 * maps the arclength of the bezier path segment to the bezier 
 * parameter value.
 * 
 * The real function is the solution to an expression which would be
 * prohibitively expensive to try to evaluate inside an animation loop, 
 * so instead we create a polynomial approximation and use that instead. 
 * 
 * This code is distinct from RMPathFitCurves, which fits a set of 
 * bezier curves to a set of sample points.  
 */

public class RMCurveFit {

	// An individual polynomial which covers the curve in the range start-end.
	// The output of NevilleFit() routine is a list of these objects.
	public static class Piece {
		public double start;
		public double end;
		public double xsamples[];
		public double ysamples[];

		public Piece(double s, double e, double x[], double y[]) {
			start = s;
			end = e;
			xsamples = x;
			ysamples = y;
		}
	}

	/**
	 * A function whose value is determined by interpolating through a set
	 * of sample points using Neville's Method.
	 */
	public static class NevilleFunc extends RMMath.RMFunc
	{
		public double xsamples[];
		public double ysamples[];
		public double p[];

		public NevilleFunc() {
		}

		public void setSamples(double x[], double y[])
		{
			xsamples = x;
			ysamples = y;
			p = new double[xsamples.length];
		}

		public double f(double x) {
			int i,j;
			int samples = xsamples.length;

			for(i=0; i<samples; ++i)
				p[i]=ysamples[i];
			for(j=1; j<samples; ++j)
				for(i=0; i<samples-j; ++i) 
					p[i] = (p[i]*(xsamples[i+j]-x)+p[i+1]*(x-xsamples[i])) / (xsamples[i+j]-xsamples[i]);
			return p[0];
		}
	}

	/**
	 * A function whose value is the inverse of another function 
	 *  ie.  realFunc.f(x)=y ==> inverseFunc.f(y)=x 
	 */
	public static class InverseFunc extends RMMath.RMFunc {
		RMMath.RMFunc _realFunc;
		public InverseFunc( RMMath.RMFunc real ) { _realFunc = real; }
		public double f(double x) { return _realFunc.solve(x); }
		public double fprime(double x, int order) {
			return order==1 ? 1/_realFunc.fprime(x,1) : super.fprime(x,order);
		}
	}

	/**
	 * A function scaled such that f(1) == 1
	 *
	 */
	public static class ScaledFunc extends RMMath.RMFunc {
		RMMath.RMFunc _realFunc;
		double len;
		public ScaledFunc(RMMath.RMFunc real) {
			_realFunc = real;
			len = _realFunc.f(1);
		}
		public double f(double x) { return _realFunc.f(x)/len; }
		public double fprime(double x, int order) { return _realFunc.fprime(x, order)/len; }
	}

	// Returns Chebyshev nodes for interpolating polynomial of order n.
	// These are the 'best' values at which to sample the curve, in order
	// to minimaize the error.
	public static double[] cheby(int n) {
		double x[] = new double[n];
		// calculate scale factor so nodes cover entire interval [0-1]
		double c = Math.cos(Math.PI/(2*n));
		double b = (c+1)/(2*c);
		double a = (2-b*(c+1))/(1-c);

		// get nodes.  These are the x values of the (x,y) samples 
		// that minimize the error of the interpolation.
		for(int i=1; i<=n; ++i) {
			double node = (1+(b-a)*Math.cos((2*i-1)*Math.PI/(2*n)))/2;
			x[n-i] = node;
		}
		return x;
	}

/**
 * NevilleFit - 
 * This routine tries to fit an interpolating polygon to an arbitrary function.
 * Neville's method takes a set of points and calculates new points
 * by interpolating between the neighboring samples, and then interpolating
 * between the interpolations, etc, etc.
 * This method tries to create a set of points such that, when plugged into
 * Neville's method, will approximate the curve with minimal error.
 * 
 * It first tries to create a linear->5th degree polynomial (corresponding to two
 * to six sample points), and then calculates the maximum error of that 
 * polynomial to the real curve.  If the error is too great, it subdivides 
 * at the maximum error point and recurses.
 * 
 * The final result is a piecewise list of polynomials, expressed as sample points.
 */
public static List nevilleFit(RMMath.RMFunc func, double start, double end, List pieceList) 
{
	int i,samples = 2;
	int outsamples=64;
	double maxerr=0;
	double maxerrpt=-1;
	double x[];
	double y[];
	NevilleFunc interp = new NevilleFunc();

	double error_limit = 0.0004;

	if (end<=start)
		return pieceList;

	if (pieceList == null) 
		pieceList = new ArrayList();

	// try 2-6 nodes (linear to quintic polygon)
	do {
		// Get the nodes (interpolation points) for the new polygon
		x = cheby(samples);
		y= new double[samples];
		// calculate y value at each node
		for(i=0; i<samples; ++i) {
			// map Chebychev nodes to the domain (cheby() routine maps them to 0-1)
			x[i] = start + (end-start)*x[i];
			// get the sample for the given node
			y[i] = func.f(x[i]);
		}

		// initialize Neville interpolation function with the samples
		interp.setSamples(x,y);

		double xx = 0;
		double yy;
		maxerr = 0;

		// Now that we have a polygon, see how good a fit it is
		for(int outi=0; outi<outsamples; ++outi) {
			// x value in range
			xx = start + (end-start)*outi/(outsamples-1);
			// do the interpolation 
			yy = interp.f(xx);

			// get the actual value
			double actual = func.f(xx);

			// calculate squared error and keep track of worst fit point
			double esq = (yy-actual)*(yy-actual);
			if (esq>maxerr) {
				maxerr=esq;
				maxerrpt = xx;
			}
		}

		// try again with next higher order curve, up to 5th degree
		++samples;
	}
	while(maxerr>error_limit && samples<=5);

	// if we didn't succeed, subdivide and try again
	if (maxerr>error_limit) {
		pieceList = nevilleFit(func, start, maxerrpt, pieceList);
		return nevilleFit(func, maxerrpt, end, pieceList);
	}
	else {
		// If the fit is good, save the samples
		pieceList.add(new Piece(start, end, x, y));
		return pieceList;
	}
}

public static void main(String args[]) {
    double xs[] = {0,.293,.707,1};
    double ys[] = {0,.117,.386,1};
    NevilleFunc interp = new NevilleFunc();
    interp.setSamples(xs, ys);
    for(double x=0; x<=1; x+=0.05) {
        System.out.println(x+", "+interp.f(x));
    }
}

}
