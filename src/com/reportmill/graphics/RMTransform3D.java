package com.reportmill.graphics;

/**
 * This class represents a 3D transform. 
 */
public class RMTransform3D implements Cloneable {
    
    // All of the transform components
    public double[][] m = new double[][] { { 1, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } };
    
/** Creates a transform3d with the identity matrix. */
public RMTransform3D() { }

/** Multiplies receiver by given transform. */
public RMTransform3D multiply(RMTransform3D aTransform)
{
    // Get this float array, given float array and new float array
    double m1[][] = m;
    double m2[][] = aTransform.m;
    double m3[][] = new double[4][4];
    
    // Perform multiplication
    for(int i=0; i<4; i++)
        for(int j=0; j<4; j++)
            for(int k=0; k<4; k++)
                m3[i][j] += m1[i][k]*m2[k][j];
    
    // Return this (loaded from m3)
    return fromArray(m3);
}

/** Translates by given x, y & z. */
public RMTransform3D translate(double x, double y, double z)
{
    //m[3][0] += x; m[3][1] += y; m[3][2] += z;
    RMTransform3D rm = new RMTransform3D();
    rm.m[3][0] = x; rm.m[3][1] = y; rm.m[3][2] = z;
    return multiply(rm);
}

/** Rotate x axis by given degrees. */
public RMTransform3D rotateX(double anAngle)
{
    RMTransform3D rm = new RMTransform3D();
    double angle = Math.toRadians(anAngle);
    double c = Math.cos(angle), s = Math.sin(angle);
    rm.m[1][1] = c;
    rm.m[2][2] = c;
    rm.m[1][2] = s;
    rm.m[2][1] = -s;
    return multiply(rm);
}

/** Rotate y axis by given degrees. */
public RMTransform3D rotateY(double anAngle)
{
    RMTransform3D rm = new RMTransform3D();
    double angle = Math.toRadians(anAngle);
    double c = Math.cos(angle), s = Math.sin(angle);
    rm.m[0][0] = c;
    rm.m[2][2] = c;
    rm.m[0][2] = -s;
    rm.m[2][0] = s;
    return multiply(rm);
}

/** Rotate z axis by given degrees. */
public RMTransform3D rotateZ(double anAngle)
{
    RMTransform3D rm = new RMTransform3D();
    double angle = Math.toRadians(anAngle);
    double c = Math.cos(angle), s = Math.sin(angle);
    rm.m[0][0] = c;
    rm.m[1][1] = c;
    rm.m[0][1] = s;
    rm.m[1][0] = -s;
    return multiply(rm);
}

/** Rotate about arbitrary axis. */
public RMTransform3D rotate(RMVector3D anAxis, double anAngle)
{
    RMTransform3D rm = new RMTransform3D();
    double angle = Math.toRadians(anAngle);
    double c = Math.cos(angle), s = Math.sin(angle), t = 1 - c;
    rm.m[0][0] = t*anAxis.x*anAxis.x + c;
    rm.m[0][1] = t*anAxis.x*anAxis.y + s*anAxis.z;
    rm.m[0][2] = t*anAxis.x*anAxis.z - s*anAxis.y;
    rm.m[1][0] = t*anAxis.x*anAxis.y - s*anAxis.z;
    rm.m[1][1] = t*anAxis.y*anAxis.y + c;
    rm.m[1][2] = t*anAxis.y*anAxis.z + s*anAxis.x;
    rm.m[2][0] = t*anAxis.x*anAxis.y + s*anAxis.y;
    rm.m[2][1] = t*anAxis.y*anAxis.z - s*anAxis.x;
    rm.m[2][2] = t*anAxis.z*anAxis.z + c;
    return multiply(rm);
}

/** Rotate x,y,z with three Euler angles (same as rotateX(rx).rotateY(ry).rotateZ(rz)) */
public RMTransform3D rotate(double rx, double ry, double rz)
{
    RMTransform3D rm = new RMTransform3D();
    double ax = Math.toRadians(rx);
    double ay = Math.toRadians(ry);
    double az = Math.toRadians(rz);
    double a = Math.cos(ax);
    double b = Math.sin(ax);
    double c = Math.cos(ay);
    double d = Math.sin(ay);
    double e = Math.cos(az);
    double f = Math.sin(az);
    double ad = a*d;
    double bd = b*d;
    
    rm.m[0][0] = c*e;
    rm.m[1][0] = -c*f;
    rm.m[2][0] = d;
    rm.m[0][1] = bd*e+a*f;
    rm.m[1][1] = -bd*f+a*e;
    rm.m[2][1] = -b*c;
    rm.m[0][2] = -ad*e+b*f;
    rm.m[1][2] = ad*f+b*e;
    rm.m[2][2] = a*c;
    return multiply(rm);
}

/** Returns a matrix whose axes are aligned with the world (screen) coordinate system.
 * All rotations & skews are removed, and perspective is replaced by uniform scaling.
 */
public RMTransform3D worldAlign(RMPoint3D originPt)
{
   RMPoint3D tp = transform((RMPoint3D)originPt.clone());
   double w = m[2][3]*originPt.z+m[3][3];
   
   for(int i=0; i<4; ++i)
       for(int j=0; j<4; ++j)
           m[i][j]=i==j?(i<2?1f/w:1):0;
   m[3][0]=tp.x-originPt.x/w;
   m[3][1]=tp.y-originPt.y/w;
   m[3][2]=tp.z-originPt.z/w;
   return this;
}

/** Skew by the given degrees. */
public RMTransform3D skew(double skx, double sky)
{
    RMTransform3D rm = new RMTransform3D();
    rm.m[2][0] = skx; //Math.toRadians(skx);
    rm.m[2][1] = sky; //Math.toRadians(sky);
    return multiply(rm);
}

/** Apply perspective transform. */
public RMTransform3D perspective(double d)
{
    RMTransform3D p = new RMTransform3D(); p.m[2][3] = 1/d; //p.m[3][3] = 0;
    return multiply(p);
}

public RMTransform3D invert()
{
    double t[][] = toArray(), minv[][] = new RMTransform3D().toArray();
    double determinant = 1, factor;

    // Forward elimination
    for(int i=0; i<3; i++) {
        
        // Get pivot and pivotsize
        int pivot = i;
        double pivotsize = Math.abs(t[i][i]);
        
        // Iterate
        for(int j=i+1; j<4; j++)
            if(pivotsize < Math.abs(t[j][i])) {
                pivot = j;
                pivotsize = Math.abs(t[j][i]);
            }
        
        // Test pivotsize
        if(pivotsize==0)
            return fromArray(minv);
            
        // Do something else
        if(pivot!=i) {
            for(int j=0; j<4; j++) {
                double tmp = t[i][j];
                t[i][j] = t[pivot][j];
                t[pivot][j] = tmp;
                tmp = minv[i][j];
                minv[i][j] = minv[pivot][j];
                minv[pivot][j] = tmp;
            }
            determinant = -determinant;
        }
        
        // Something else
        for(int j=i+1; j<4; j++){
            factor = t[j][i]/t[i][i];
            for(int k=0; k!=4; k++) {
                t[j][k] -= factor*t[i][k];
                minv[j][k] -= factor*minv[i][k];
            }
        }
    }

    // Backward substitution
    for(int i=3; i>=0; --i){
        if((factor = t[i][i])==0.0)
            return fromArray(minv);
        for(int j=0; j!=4; j++) {
            t[i][j] /= factor;
            minv[i][j] /= factor;
        }
        determinant *= factor;
        for(int j=0; j!=i; j++) {
            factor = t[j][i];
            for(int k=0; k!=4; k++) {
                t[j][k] -= factor*t[i][k];
                minv[j][k] -= factor*minv[i][k];
            }
        }
    }
    
    return fromArray(minv);
}

/** Transforms a given point (and returns it as a convenience). */
public RMPoint3D transform(RMPoint3D aPoint)
{
    double x2 = m[0][0]*aPoint.x + m[1][0]*aPoint.y + m[2][0]*aPoint.z + m[3][0];
    double y2 = m[0][1]*aPoint.x + m[1][1]*aPoint.y + m[2][1]*aPoint.z + m[3][1];
    double z2 = m[0][2]*aPoint.x + m[1][2]*aPoint.y + m[2][2]*aPoint.z + m[3][2];
    double w =  m[0][3]*aPoint.x + m[1][3]*aPoint.y + m[2][3]*aPoint.z + m[3][3];
    aPoint.x = x2/w; aPoint.y = y2/w; aPoint.z = z2/w;
    return aPoint;
}

/** Transforms a given vector (and returns it as a convenience). */
public RMVector3D transform(RMVector3D aVector)
{
    RMPoint3D p1 = transform(new RMPoint3D(0, 0, 0));
    RMPoint3D p2 = transform(new RMPoint3D(aVector.x, aVector.y, aVector.z));
    aVector.x = p2.x - p1.x; aVector.y = p2.y - p1.y; aVector.z = p2.z - p1.z;
    return aVector;
}

/** Returns a float array for the transform. */
public double[][] toArray()
{
    double m2[][] = new double[4][4];
    for(int i=0; i<4; i++) for(int j=0; j<4; j++) m2[i][j] = m[i][j];
    return m2;
}

/** Loads the transform flom a float array. */
public RMTransform3D fromArray(double m2[][])
{
    for(int i=0; i<4; i++) for(int j=0; j<4; j++) m[i][j] = m2[i][j];
    return this;
}

public Object clone()
{
    RMTransform3D copy = new RMTransform3D();
    return copy.fromArray(toArray());
}

}