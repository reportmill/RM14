package com.reportmill.graphics;

public class RMPolynomial {

    double  coefs[] = { };

    static double TOLERANCE = 1e-6;

    static int ACCURACY = 6;

public RMPolynomial(float COEFS[])
{
    //coefs = new double[COEFS.length];
    
    for(int i=COEFS.length-1; i>=0; i--)
        push(COEFS[i]);
}

/*Polynomial prototype.eval(x)
{
    var result=0;
    for(var i=this.coefs.length-1;i>=0;i--)
        result=result*x+this.coefs[i];
    return result;
}*/

/*Polynomial multiply(that)
{
    var result=new Polynomial();
    for(var i=0;i<=this.getDegree()+that.getDegree();i++)
        result.coefs.push(0);
    for(var i=0;i<=this.getDegree();i++)
        for(var j=0;j<=that.getDegree();j++)
            result.coefs[i+j]+=this.coefs[i]*that.coefs[j];
    return result;
}*/

//Polynomial.prototype.divide_scalar(scalar){for(var i=0;i<this.coefs.length;i++)this.coefs[i]/=scalar;};

public void simplify()
{
    for(int i=getDegree(); i>=0; i--) {
        if(Math.abs(coefs[i])<=TOLERANCE)
            pop();
        else break;
    }
}

//Polynomial.prototype.bisection(min,max){var minValue=this.eval(min);var maxValue=this.eval(max);var result;if(Math.abs(minValue)<=Polynomial.TOLERANCE)result=min;else if(Math.abs(maxValue)<=Polynomial.TOLERANCE)result=max;else if(minValue*maxValue<=0){var tmp1=Math.log(max-min);var tmp2=Math.log(10)*Polynomial.ACCURACY;var iters=Math.ceil((tmp1+tmp2)/Math.log(2));for(var i=0;i<iters;i++){result=0.5*(min+max);var value=this.eval(result);if(Math.abs(value)<=Polynomial.TOLERANCE){break;}if(value*minValue<0){max=result;maxValue=value;}else{min=result;minValue=value;}}}return result;};

//Polynomial.prototype.toString(){var coefs=new Array();var signs=new Array();for(var i=this.coefs.length-1;i>=0;i--){var value=this.coefs[i];if(value!=0){var sign=(value<0)?" - ":" + ";value=Math.abs(value);if(i>0)if(value==1)value="x";else value+="x";if(i>1)value+="^"+i;signs.push(sign);coefs.push(value);}}signs[0]=(signs[0]==" + ")?"":"-";var result="";for(var i=0;i<coefs.length;i++)result+=signs[i]+coefs[i];return result;};

public int getDegree()
{
    return coefs.length-1;
}

//Polynomial.prototype.getDerivative(){var derivative=new Polynomial();for(var i=1;i<this.coefs.length;i++){derivative.coefs.push(i*this.coefs[i]);}return derivative;};

public double[] getRoots()
{
    // Simplify
    simplify();
    
    // Solve
    switch(getDegree()) {
        case 0: return null;
        case 1: return getLinearRoot();
        case 2: return getQuadraticRoots();
        case 3: return getCubicRoots();
        //case 4: result = this.getQuarticRoots(); break;
        //default: result = new Array();
    }
    
    return null;
}

//public double[] getRootsInInterval=function(min,max){var roots=new Array();var root;if(this.getDegree()==1){root=this.bisection(min,max);if(root!=null)roots.push(root);}else{var deriv=this.getDerivative();var droots=deriv.getRootsInInterval(min,max);if(droots.length>0){root=this.bisection(min,droots[0]);if(root!=null)roots.push(root);for(i=0;i<=droots.length-2;i++){root=this.bisection(droots[i],droots[i+1]);if(root!=null)roots.push(root);}root=this.bisection(droots[droots.length-1],max);if(root!=null)roots.push(root);}else{root=this.bisection(min,max);if(root!=null)roots.push(root);}}return roots;};

public double[] getLinearRoot()
{
    double result[] = {};
    double a = coefs[1];
    if(a!=0)
        result = push(result, -this.coefs[0]/a);
    return result;
}

public double[] getQuadraticRoots()
{
    double results[] = {};
    if(getDegree()==2) {
        double a = coefs[2];
        double b = coefs[1]/a;
        double c = this.coefs[0]/a;
        double d=b*b-4*c;
        if(d>0) { 
            double e = Math.sqrt(d);
            results = push(results, 0.5*(-b+e));
            results = push(results, 0.5*(-b-e));
        }
        
        else if(d==0) {
            results = push(results, 0.5*-b);
        }
    }
    
    return results;
}

public double[] getCubicRoots()
{
    double results[] = { };
    
    if(getDegree()==3) {
        double c3 = coefs[3];
        double c2 = coefs[2]/c3;
        double c1 = coefs[1]/c3;
        double c0 = coefs[0]/c3;
        
        double a = (3*c1-c2*c2)/3;
        double b = (2*c2*c2*c2-9*c1*c2+27*c0)/27;
        double offset = c2/3;
        double discrim = b*b/4 + a*a*a/27;
        double halfB = b/2;
        
        if(Math.abs(discrim)<=TOLERANCE)
            discrim = 0;
        
        if(discrim>0) {
            double e = Math.sqrt(discrim);
            double root;
            double tmp = -halfB+e;
            if(tmp>=0)
                root=Math.pow(tmp,1/3);
            else root=-Math.pow(-tmp,1/3);
            tmp = -halfB-e;
            
            if(tmp>=0)
                root+=Math.pow(tmp,1/3);
            else root-=Math.pow(-tmp,1/3);
            
            results = push(results, root-offset);
        }
        
        else if(discrim<0) { 
            double distance = Math.sqrt(-a/3);
            double angle = Math.atan2(Math.sqrt(-discrim),-halfB)/3;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double sqrt3 = Math.sqrt(3);
            results = push(results, 2*distance*cos-offset);
            results = push(results, -distance*(cos+sqrt3*sin)-offset);
            results = push(results, -distance*(cos-sqrt3*sin)-offset);
        }
        
        else { 
            double tmp;
            if(halfB>=0)
                tmp=-Math.pow(halfB,1/3);
            else tmp=Math.pow(-halfB,1/3);
            results = push(results, 2*tmp-offset);
            results = push(results, -tmp-offset);
        }
    }
    
    return results;
}

//Polynomial.prototype.getQuarticRoots=function(){var results=new Array();if(this.getDegree()==4){var c4=this.coefs[4];var c3=this.coefs[3]/c4;var c2=this.coefs[2]/c4;var c1=this.coefs[1]/c4;var c0=this.coefs[0]/c4;var resolveRoots=new Polynomial(1,-c2,c3*c1-4*c0,-c3*c3*c0+4*c2*c0-c1*c1).getCubicRoots();var y=resolveRoots[0];var discrim=c3*c3/4-c2+y;if(Math.abs(discrim)<=Polynomial.TOLERANCE)discrim=0;if(discrim>0){var e=Math.sqrt(discrim);var t1=3*c3*c3/4-e*e-2*c2;var t2=(4*c3*c2-8*c1-c3*c3*c3)/(4*e);var plus=t1+t2;var minus=t1-t2;if(Math.abs(plus)<=Polynomial.TOLERANCE)plus=0;if(Math.abs(minus)<=Polynomial.TOLERANCE)minus=0;if(plus>=0){var f=Math.sqrt(plus);results.push(-c3/4 + (e+f)/2);results.push(-c3/4 + (e-f)/2);}if(minus>=0){var f=Math.sqrt(minus);results.push(-c3/4 + (f-e)/2);results.push(-c3/4 - (f+e)/2);}}else if(discrim<0){}else{var t2=y*y-4*c0;if(t2>=-Polynomial.TOLERANCE){if(t2<0)t2=0;t2=2*Math.sqrt(t2);t1=3*c3*c3/4-2*c2;if(t1+t2>=Polynomial.TOLERANCE){var d=Math.sqrt(t1+t2);results.push(-c3/4 + d/2);results.push(-c3/4 - d/2);}if(t1-t2>=Polynomial.TOLERANCE){var d=Math.sqrt(t1-t2);results.push(-c3/4 + d/2);results.push(-c3/4 - d/2);}}}}return results;};

public void push(double aValue)  { coefs = push(coefs, aValue); }

public double pop()
{
    int len = coefs.length;
    double value = coefs[len-1];
    double newArray[] = new double[len-1];
    for(int i=0; i<len-1; i++) newArray[i] = coefs[i];
    coefs = newArray;
    return value;
}

public static double[] push(double[] dArray, double aValue)
{
    int len = dArray.length;
    double newArray[] = new double[len+1];
    for(int i=0; i<len; i++) newArray[i] = dArray[i];
    newArray[len] = aValue;
    return newArray;
}

}