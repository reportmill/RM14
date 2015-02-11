package com.reportmill.graphics;
import com.reportmill.base.*;
import java.util.*;

/**
 * Bezier curve fitting code adapted from 
 *   "An Algorithm for Automatically Fitting Digitized Curves", by Philip J. Schneider
 *   "Graphics Gems", 1990 - Andrew S. Glassner, ed.
 */
public class RMPathFitCurves {

    // Control points of fitted Bezier curve
    static BezierCurve bezCurve = new BezierCurve();

    // Type declarations for Beziers, an individual BezierCurve and a Vector
    static class Beziers {
        public RMPoint points[] = null;
        public int count = 0;
        
        // convert to a list of RMBeziers
        public List getBeziers() { 
            ArrayList l = new ArrayList(count);
            for(int i=0,p=0; i<count; ++i) {
                // copies points - really necessary?
                l.add(new RMBezier(new RMPoint(points[p]), 
                                   new RMPoint(points[p+1]),
                                   new RMPoint(points[p+2]),
                                   new RMPoint(points[p+3])));
                p+=3;
                }
            return l;
        }
    }
    
    // 
    static class BezierCurve {
        public RMPoint p[] = new RMPoint[4];
    }

/**
 * Takes a path with a bunch of line-to segments and replaces them with curves.
 */
public static void fitCurveFromPointIndex(RMPath path, int index)
{
    // Copy the points to be fit (remove duplicates that are adjacent or within one point)
    Vector points = new Vector(path.getPointCount()-index);
    for(int i=index, iMax=path.getPointCount(); i<iMax; i++) {

        // Get point and add it to points
        RMPoint point = path.getPoint(i);
        int j = points.size();
        
        // Remove last point if equal to point 2 points back, otherwise don't add if equal to last point
        if(j>1 && point.equals(points.elementAt(j-2)))
            points.remove(j-1);
        else if(j==0 || !point.equals(points.elementAt(j-1)))
            points.addElement(point);
    }

    // Fit curves to the discrete line segments added since mouse down
    if(points.size()>1) {
        RMPoint pointArray[] = (RMPoint[])points.toArray(new RMPoint[0]);
        Beziers beziers = FitCurve(pointArray, pointArray.length, 40);
        RMPoint curvePoints[] = beziers.points;

        // Remove the discrete line segments added since mouse down
        while(path.getPointCount() > index)
            path.removeLastElement();

        // Add new curves to self and register animation at each one
        for(int i=0; i<beziers.count; i++)
            path.curveTo(curvePoints[i*3+1], curvePoints[i*3+2], curvePoints[i*3+3]);
    }
}

/**
 * Fit a curve, preserving the start & end tangents.
 */
public static void getFitCurveSegments(RMPoint d[], RMSize start_tan, RMSize end_tan, List segs)
{
    RMPoint tHat1 = new RMPoint(start_tan.width, start_tan.height);
    // end_tan is the tangent to the offset curve at the endpoint, but
    // tHat2 is the vector from the last bezier control point to the third
    // control point, which points in the opposite direction.
    RMPoint tHat2 = new RMPoint(-end_tan.width, -end_tan.height);
    Beziers beziers = new Beziers();
    RMPoint curvePoints[];
    
    RMPathFitCurves.FitCubic(beziers, d, 0, d.length-1, tHat1, tHat2, 1); // ridiculously small error
    
    curvePoints=beziers.points;
    for(int i=0,p=0; i<beziers.count; ++i) {
        segs.add(new RMBezier(new RMPoint(curvePoints[p]), 
                              new RMPoint(curvePoints[p+1]),
                              new RMPoint(curvePoints[p+2]),
                              new RMPoint(curvePoints[p+3])));
        p+=3;
    }
}

// Fit a Bezier curve to a set of digitized points
private static Beziers FitCurve(RMPoint d[], int nPts, double error)
{
    // Approximate unit tangents at endpoints of digitized curve
    RMPoint tHat1 = V2Normalize(V2Sub(d[1],d[0])), tHat2 = V2Normalize(V2Sub(d[nPts-2],d[nPts-1]));
    Beziers beziers = new Beziers();

    // Call FitCubic on the whole range of points (it does its thing recursively) and return count(by reference) and points
    FitCubic(beziers, d, 0, nPts - 1, tHat1, tHat2, error);
    return beziers;
}

// Fit a Bezier curve to an arbitrary curve evaluated in a given range
public static List getFitCurveSegments(RMMath.RMFunc func, double start, double end, int nPts, double error)
{
    // scale to adjust output to range 0-1  (for no good reason)
    double yscale = func.f(end);
    
    // get exact unit tangents at endpoints
    RMPoint tHat1 = V2Normalize(new RMPoint(1, func.fprime(start,1)/yscale));
    RMPoint tHat2 = V2Normalize(new RMPoint(1, func.fprime(end,1)/yscale));
    
    // Generate nPoints points for curve fit
    RMPoint d[] = new RMPoint[nPts];
    for(int i=0; i<nPts; ++i) {
        double x = start + (end-start)*i/(nPts-1);
        double y = func.f(x)/yscale;
        d[i] = new RMPoint(x,y);
    }
    
    // Proceed as above
    Beziers beziers = new Beziers();
    FitCubic(beziers, d, 0, nPts - 1, tHat1, tHat2, error);
    
    // return as list of RMBezier objects
    return beziers.getBeziers();
}

// Fit a Bezier curve to a (sub)set of digitized points
private static void FitCubic(Beziers beziers, RMPoint d[], int first, int last, RMPoint tHat1, RMPoint tHat2, double error)
{
    double u[] = null;                         // Parameter values for point
    double maxError;                          // Maximum fitting error
    int splitPoint;                           // Point to split point set at
    int nPts = last - first + 1;              // Number of points in subset
    double iterationError = error*error;      // Error below which you try iterating
    int maxIterations = 4;                    // Max times to try iterating

    // Use heuristic if region only has two points in it
    if(nPts == 2) {
        double dist = V2DistanceBetween2Points(d[last], d[first])/3f;

        bezCurve.p[0] = d[first];
        bezCurve.p[3] = d[last];
        tHat1 = V2Scale(tHat1, dist);
        bezCurve.p[1] = V2Add(bezCurve.p[0], tHat1);
        tHat2 = V2Scale(tHat2, dist);
        bezCurve.p[2] = V2Add(bezCurve.p[3], tHat2);
        ConcatBezierCurve(beziers, bezCurve);
        return;
    }

    // Parameterize points, and attempt to fit curve
    u = ChordLengthParameterize(d, first, last);
    bezCurve = GenerateBezier(d, first, last, u, tHat1, tHat2);

    // Find max deviation of points to fitted curve
    double result[] = ComputeMaxError(d, first, last, bezCurve, u);
    maxError = result[0];
    splitPoint = (int)result[1];
    
    if(maxError < error) {
        ConcatBezierCurve(beziers, bezCurve);
        return;
    }

    // If error not too large, try some reparameterization and iteration
    if(maxError < iterationError) {
        for(int i=0; i<maxIterations; i++) {
            double u2[] = Reparameterize(d, first, last, u, bezCurve);
            bezCurve = GenerateBezier(d, first, last, u2, tHat1, tHat2);
            double result2[] = ComputeMaxError(d, first, last, bezCurve, u2);
            maxError = result2[0];
            splitPoint = (int)result2[1];
            
            if(maxError<error) {
                ConcatBezierCurve(beziers, bezCurve);
                return;
            }
            u = u2;
        }
    }

    // Fitting failed -- split at max error point and fit recursively
    RMPoint tHatCenter = ComputeCenterTangent(d, splitPoint); // Unit tangent vector at splitPoint
    FitCubic(beziers, d, first, splitPoint, tHat1, tHatCenter, error);
    tHatCenter = V2Negate(tHatCenter);
    FitCubic(beziers, d, splitPoint, last, tHatCenter, tHat2, error);
}

static void ConcatBezierCurve(Beziers beziers, BezierCurve bezCurve)
{
    int pointCount = beziers.count*3 + 1;
    beziers.count++;

    if(beziers.points!=null)
        beziers.points = RMArrayUtils.realloc(beziers.points, beziers.count*3+1);
    else beziers.points = new RMPoint[4];

    if(pointCount==1)
        beziers.points[0] = bezCurve.p[0];

    beziers.points[pointCount++] = bezCurve.p[1];
    beziers.points[pointCount++] = bezCurve.p[2];
    beziers.points[pointCount++] = bezCurve.p[3];
}

static BezierCurve bezCurve2 = new BezierCurve();          // RETURN bezier curve ctl pts

// Use least-squares method to find Bezier control points for region.
static BezierCurve GenerateBezier(RMPoint d[], int first, int last, double uPrime[], RMPoint tHat1, RMPoint tHat2)
{
    double C[][] = {{0,0},{0,0}}, X[] = {0,0};  // Matrix C & X
    double det_C0_C1, det_C0_X, det_X_C1;      // Determinants of matrices
    double alpha_l, alpha_r;                  // Alpha values, left and right
    int nPts = last - first + 1; // Number of pts in sub-curve
    RMPoint A[][] = new RMPoint[nPts][2];   // Precomputed rhs for eqn
 
    // Compute the A's
    for(int i=0; i<nPts; i++) {
        A[i][0] = V2Scale(tHat1, B1(uPrime[i]));
        A[i][1] = V2Scale(tHat2, B2(uPrime[i]));
    }

    for(int i=0; i<nPts; i++) {
        C[0][0] += V2Dot(A[i][0], A[i][0]);
        C[0][1] += V2Dot(A[i][0], A[i][1]);
        C[1][0] = C[0][1];
        C[1][1] += V2Dot(A[i][1], A[i][1]);

        RMPoint tmp = V2Sub(d[first + i], V2Add( V2Scale(d[first], B0(uPrime[i])), V2Add( V2Scale(d[first], B1(uPrime[i])),
            V2Add( V2Scale(d[last], B2(uPrime[i])), V2Scale(d[last], B3(uPrime[i]))))));

        X[0] += V2Dot(A[i][0], tmp); X[1] += V2Dot(A[i][1], tmp);
    }

    // Compute the determinants of C and X
    det_C0_C1 = C[0][0] * C[1][1] - C[1][0] * C[0][1];
    det_C0_X  = C[0][0] * X[1]    - C[0][1] * X[0];
    det_X_C1  = X[0]    * C[1][1] - X[1]    * C[0][1];

    // Finally, derive alpha values
    if(det_C0_C1 == 0.0)
        det_C0_C1 = (C[0][0] * C[1][1]) * 10e-12;
    alpha_l = det_X_C1 / det_C0_C1;
    alpha_r = det_C0_X / det_C0_C1;


    // If alpha negative, use the Wu/Barsky heuristic (see text)
    if(alpha_l < 0.0 || alpha_r < 0.0) {
        double dist = V2DistanceBetween2Points(d[last], d[first])/ 3.0;

        bezCurve2.p[0] = d[first];
        bezCurve2.p[3] = d[last];
        tHat1 = V2Scale(tHat1, dist);
        bezCurve2.p[1] = V2Add(bezCurve2.p[0], tHat1);
        tHat2 = V2Scale(tHat2, dist);
        bezCurve2.p[2] = V2Add(bezCurve2.p[3], tHat2);
        return bezCurve2;
    }

    // First and last control points of the Bezier curve are positioned exactly
    // at the first and last data points. Control points 1 and 2 are positioned
    // an alpha distance out on the tangent vectors, left & right, respectively
    bezCurve2.p[0] = d[first];
    bezCurve2.p[3] = d[last];
    tHat1 = V2Scale(tHat1, alpha_l);
    bezCurve2.p[1] = V2Add(bezCurve2.p[0], tHat1);
    tHat2 = V2Scale(tHat2, alpha_r);
    bezCurve2.p[2] = V2Add(bezCurve2.p[3], tHat2);

    return bezCurve2;
}

// Given set of points & their parameterization, find better parameterization
static double[] Reparameterize(RMPoint d[], int first, int last, double u[], BezierCurve bezCurve)
{
    int nPts = last-first+1;
    double uPrime[] = new double[nPts];                            // New parameter values
    for(int i=first; i<=last; i++)
        uPrime[i-first] = NewtonRaphsonRootFind(bezCurve, d[i], u[i-first]);
    return uPrime;
}

// Use Newton-Raphson iteration to find better root.
static double NewtonRaphsonRootFind(BezierCurve bc, RMPoint P, double u)
{
    RMPoint Q1[] = new RMPoint[3], Q2[] = new RMPoint[2];           // Q' and Q''
    
    // Compute Q(u) and generate control vertices for Q' & Q''
    RMPoint Q_u = Bezier(3, bc.p, u);
    for(int i=0; i<=2; i++)
        Q1[i] = new RMPoint((bc.p[i+1].x - bc.p[i].x)*3f, (bc.p[i+1].y - bc.p[i].y)*3f);

    for(int i=0; i<=1; i++)
        Q2[i] = new RMPoint((Q1[i+1].x - Q1[i].x)*2f, (Q1[i+1].y - Q1[i].y)*2f);
    
    // Compute Q'(u) and Q''(u)
    RMPoint Q1_u = Bezier(2, Q1, u);
    RMPoint Q2_u = Bezier(1, Q2, u);
    
    // Compute f(u)/f'(u)
    double numerator = (Q_u.x - P.x)*(Q1_u.x) + (Q_u.y - P.y)*(Q1_u.y);
    double denominator = (Q1_u.x)*(Q1_u.x) + (Q1_u.y)*(Q1_u.y) + (Q_u.x - P.x)*(Q2_u.x) + (Q_u.y - P.y)*(Q2_u.y);
    
    // u = u - f(u)/f'(u)   improved U
    return u - (numerator/denominator);
}

// Evaluate a Bezier curve at a particular parameter value
static RMPoint Bezier(int degree, RMPoint V[], double t)
{
    RMPoint Vtemp[] = new RMPoint[4];   // Point on curve at parameter t and local copy of control points

    // Copy array
    for(int i=0; i<=degree; i++)
        Vtemp[i] = new RMPoint(V[i]);

    // Triangle computation
    for(int i=1; i<=degree; i++)
        for(int j=0; j<=degree-i; j++) {
            Vtemp[j].x = (float)(1-t)*Vtemp[j].x + (float)t*Vtemp[j+1].x;
            Vtemp[j].y = (float)(1-t)*Vtemp[j].y + (float)t*Vtemp[j+1].y;
        }

    return Vtemp[0];
}

// Bezier multipliers
static double B0(double u) { return Math.pow(1-u,3); }
static double B1(double u) { return 3*u*Math.pow(1-u,2); }
static double B2(double u) { return 3*u*u*(1-u); }
static double B3(double u) { return u*u*u; }

// Approximate unit tangents at center of digitized curve
static RMPoint ComputeCenterTangent(RMPoint d[], int center)
{
    RMPoint V1 = V2Sub(d[center-1], d[center]);
    RMPoint V2 = V2Sub(d[center], d[center+1]);
    RMPoint tHatCenter = new RMPoint((V1.x + V2.x)/2f, (V1.y + V2.y)/2f);
    tHatCenter = V2Normalize(tHatCenter);
    return tHatCenter;
}

// Assign parameter values to points using relative dist between points
static double[] ChordLengthParameterize(RMPoint d[], int first, int last)
{
    double u[] = new double[(last-first+1)];
    u[0] = 0.0;
    for(int i=first+1; i<=last; i++)
        u[i-first] = u[i-first-1] + V2DistanceBetween2Points(d[i], d[i-1]);
    for(int i=first+1; i<=last; i++)
        u[i-first] /= u[last-first];
    return u;
}

// Find the maximum squared distance of digitized points to fitted curve.
static double[] ComputeMaxError(RMPoint d[], int first, int last, BezierCurve bezCurve, double u[])
{
    double maxDist = 0.0, dist; // Maximum & current error
    int splitPoint = (last - first + 1)/2;
    
    for(int i=first+1; i<last; i++) {

        // Get point on curve and vector from point to curve
        RMPoint p = Bezier(3, bezCurve.p, u[i-first]);
        RMPoint v = V2Sub(p, d[i]);
        dist = V2SquaredLength(v);
        if(dist >= maxDist) {
            maxDist = dist;
            splitPoint = i;
        }
    }
    
    return new double[] { maxDist, splitPoint };
}

// Return vector sum (c = a + b), vector difference (c = a - b), vector negative (b = -a), vector scale (b = a*s) and vector normalize
static RMPoint V2Add(RMPoint a, RMPoint b) { RMPoint c = new RMPoint(); c.x = a.x + b.x;  c.y = a.y + b.y; return c; }
static RMPoint V2Sub(RMPoint a, RMPoint b) { RMPoint c = new RMPoint(); c.x = a.x - b.x; c.y = a.y - b.y; return c; }
static RMPoint V2Negate(RMPoint v) { v = new RMPoint(v); v.x = -v.x;  v.y = -v.y; return v; }
static RMPoint V2Scale(RMPoint v, double s) {
    RMPoint result = new RMPoint();
    result.x = (float)(v.x*s);
    result.y = (float)(v.y*s);
    return result;
}
static RMPoint V2Normalize(RMPoint v) {
    v = new RMPoint(v);
    double l = V2Length(v);
    if(l!=0.0) {
        v.x /=l;
        v.y /=l;
    }
    return v;
}

// Returns length, squared length, normalized vector and vector dot product
static double V2Length(RMPoint a) { return Math.sqrt(V2SquaredLength(a)); }
static double V2SquaredLength(RMPoint a) { return (a.x*a.x)+(a.y*a.y); };
static double V2Dot(RMPoint a, RMPoint b) { return (a.x*b.x)+(a.y*b.y); }

// Return distance between 2 points
static double V2DistanceBetween2Points(RMPoint a, RMPoint b) {
    double dx = a.x - b.x, dy = a.y - b.y;
    return Math.sqrt((dx*dx)+(dy*dy));
}

}