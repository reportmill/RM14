package snap.gfx;
import snap.util.*;

/**
 * This class represents an RGBA color, just like Java.awt.Color. It was originally needed when running without
 * AWT. It still has some convenience, but maybe one day should be replaced by, or just subclass, Java.awt.Color.
 */
public class SnapColor {
    
    // RGBA components
    double     _red, _green, _blue, _alpha = 1;
    
    // Common Colors
    public static SnapColor BLACK = new SnapColor(0);
    public static SnapColor BLUE = new SnapColor(0f, 0f, 1f);
    public static SnapColor CYAN = new SnapColor(1f, 0f, 0f, 0f, 1f);
    public static SnapColor DARKGRAY = new SnapColor(.333f);
    public static SnapColor GRAY = new SnapColor(.5f);
    public static SnapColor GREEN = new SnapColor(0f, 1f, 0f);
    public static SnapColor LIGHTGRAY = new SnapColor(.667f);
    public static SnapColor MAGENTA = new SnapColor(0f, 1f, 0f, 0f, 1f);
    public static SnapColor ORANGE = new SnapColor(1f, 200/255f, 0f);
    public static SnapColor PINK = new SnapColor(1f, 175/255f, 175/255f);
    public static SnapColor RED = new SnapColor(1f, 0f, 0f);
    public static SnapColor WHITE = new SnapColor(1);
    public static SnapColor YELLOW = new SnapColor(0f, 0f, 1f, 0f, 1f);
    public static SnapColor CLEAR = new SnapColor(0f, 0f, 0f, 0f);
    public static SnapColor LIGHTBLUE = new SnapColor(.333f, .333f, 1f);
    public static SnapColor CLEARWHITE = new SnapColor(1f, 1f, 1f, 0f);


/**
 * Creates a plain black opaque color.
 */
public SnapColor() { }

/**
 * Creates a color with the given gray value (0-1).
 */
public SnapColor(double g)  { _red = g; _green = g; _blue = g; }

/**
 * Creates a color with the given gray and alpha values (0-1).
 */
public SnapColor(double g, double a)  { _red = g; _green = g; _blue = g; _alpha = a; }

/**
 * Creates a color with the given red, green blue values (0-1).
 */
public SnapColor(double r, double g, double b)  { _red = r; _green = g; _blue = b; }

/**
 * Creates a color with the given red, green blue values (0-1).
 */
public SnapColor(int r, int g, int b)  { _red = r/255f; _green = g/255f; _blue = b/255f; }

/**
 * Creates a color with the given red, green, blue values (0-1).
 */
public SnapColor(double r, double g, double b, double a)  { _red = r; _green = g; _blue = b; _alpha = a; }

/**
 * Creates a color with the given cyan, magenta, yellow, black and alpha values (0-1). Bogus right now.
 */
public SnapColor(float c, float m, float y, float k, float a)  { _red = 1-c; _green = 1-m; _blue = 1-y; _alpha = a; }

/**
 * Creates a new color from the given hex string.
 */
public SnapColor(String aHexString)
{
    int start = aHexString.charAt(0)=='#'? 1 : 0;
    _red = Integer.decode("0x" + aHexString.substring(start, start + 2)).intValue()/255f;
    _green = Integer.decode("0x" + aHexString.substring(start + 2, start + 4)).intValue()/255f;
    _blue = Integer.decode("0x" + aHexString.substring(start + 4, start + 6)).intValue()/255f;
    if(aHexString.length() >= start + 8)
        _alpha = Integer.decode("0x" + aHexString.substring(start + 6, start + 8)).intValue()/255f;
}

/**
 * Returns the red component in the range 0-1.
 */
public double getRed2D()  { return _red; }

/**
 * Returns the green component in the range 0-1.
 */
public double getGreen2D()  { return _green; }

/**
 * Returns the blue component in the range 0-1.
 */
public double getBlue2D()  { return _blue; }

/**
 * Returns the alpha component in the range 0-1.
 */
public double getAlpha2D()  { return _alpha; }

/**
 * Returns the red component in the range 0-255.
 */
public int getRedInt()  { return (int)Math.round(_red*255); }

/**
 * Returns the green component in the range 0-255.
 */
public int getGreenInt()  { return (int)Math.round(_green*255); }

/**
 * Returns the blue component in the range 0-255.
 */
public int getBlueInt()  { return (int)Math.round(_blue*255); }

/**
 * Returns the alpha component in the range 0-255.
 */
public int getAlphaInt()  { return (int)Math.round(_alpha*255); }

/**
 * Returns the color as an int.
 */
public int getRGBA()
{
    int r = getRedInt(), g = getGreenInt(), b = getBlueInt(), a = getAlphaInt();
    return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
}

/**
 * Returns a color brighter than this color (blended with white).
 */
public SnapColor brighter()  { return blend(WHITE, .25); }

/**
 * Returns a color darker than this color (blended with black).
 */
public SnapColor darker()  { return blend(BLACK, .25); }

/**
 * Returns a color darker than this color (by this given fraction).
 */
public SnapColor blend(SnapColor aColor, double aFraction)
{
    // Return real objects if zero or 1
    if(aFraction==0) return this; else if(aFraction==1) return aColor;
    
    // Get blended components and return new color
    double r = _red + (aColor._red - _red)*aFraction;
    double g = _green + (aColor._green - _green)*aFraction;
    double b = _blue + (aColor._blue - _blue)*aFraction;
    double a = _alpha + (aColor._alpha - _alpha)*aFraction;
    return new SnapColor(r, g, b, a);
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, class and get other
    if(anObj==this) return true;
    if(!(anObj instanceof SnapColor)) return false;
    SnapColor c = (SnapColor)anObj;
    
    // Check components
    if(c._red!=_red) return false;
    if(c._green!=_green) return false;
    if(c._blue!=_blue) return false;
    if(c._alpha!=_alpha) return false;
    return true; // Return true since all checks passed
}

/**
 * Standard Hashcode implementation.
 */
public int hashCode()  { return getRGBA(); }

/**
 * Returns a string representation of this color.
 */
public String toString()  { return "{" + _red + " " + _green + " " + _blue + "}"; }

/**
 * Returns a hex string representation of this color.
 */
public String toHexString()
{
    // Allocate string buffer and get integer rgba components
    StringBuffer sb = new StringBuffer();
    int r = getRedInt(), g = getGreenInt(), b = getBlueInt(), a = getAlphaInt();
    
    // Add r, g, b components (and alpha, if not full) and return string
    if(r<16) sb.append('0'); sb.append(Integer.toHexString(r));
    if(g<16) sb.append('0'); sb.append(Integer.toHexString(g));
    if(b<16) sb.append('0'); sb.append(Integer.toHexString(b));
    if(a<255) { if(a<16) sb.append('0'); sb.append(Integer.toHexString(a)); }
    return sb.toString();
}

/**
 * Returns a color value for a given object.
 */
public static SnapColor colorValue(Object anObj)
{
    // Handle colors
    if(anObj instanceof SnapColor) return (SnapColor)anObj;
    
    // Handle string
    if(anObj instanceof String) { String cs = (String)anObj; cs.trim();
        
        // Try normal string constructor
        try { return new SnapColor(cs); }
        catch(Exception e) { }
        
        // make lookup case-insensitive by converting to lower case
        cs = cs.toLowerCase();
        
        // Try to invoke
        try { return (SnapColor)SnapColor.class.getField(cs).get(null); }
        catch(Exception e) { }
        
        // Didn't find it in the SnapColor statics, try one of the awt statics
        try { java.awt.Color c = (java.awt.Color)java.awt.Color.class.getField(cs).get(null);
            return new SnapColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()); } catch(Exception e) { }
        
        // Special case for names that would fail the above lookups because the fields are mixed upper & lower case
        if(cs.equals("lightgray") || cs.equals("light_gray")) return SnapColor.LIGHTGRAY;
        if(cs.equals("darkgray") || cs.equals("dark_gray")) return SnapColor.DARKGRAY;
        if(cs.equalsIgnoreCase("random")) return getRandom();
        
        // Look in colors list
        for(int i=0; i<_colors.length; i+=2) if(cs.equalsIgnoreCase(_colors[i])) return colorValue(_colors[i+1]);
        
        // Handle "light " or "dark " anything
        if(StringUtils.startsWithIC(cs, "light ")) { SnapColor c = colorValue(cs.substring(6));
            return c!=null? c.brighter() : null; }
        if(StringUtils.startsWithIC(cs, "dark ")) { SnapColor c = colorValue(cs.substring(5));
            return c!=null? c.darker() : null; }
    }
    
    // Treat numbers as 32bit RGBA ints
    if(anObj instanceof Number) { Number number = (Number)anObj; int rgba = number.intValue();
        float comps[] = new float[4]; for(int i=0; i<4; ++i) { comps[i] = (rgba & 0xff) / 255f; rgba >>= 8; }
        return new SnapColor(comps[3], comps[2], comps[1], comps[0]);
    }
    
    // Return null
    return null;
}

/**
 * Returns a random color.
 */
public static SnapColor getRandom()
{
    return new SnapColor(SnapMath.randomFloat(1), SnapMath.randomFloat(1), SnapMath.randomFloat(1));
}
    
// Some colors:
static String _colors[] = {
    "BEIGE", "#F5F5DC", "BROWN", "#A52A2A", "CRIMSON", "#DC143C", "FUCHSIA", "#FF00FF", "GOLD", "#FFD700",
    "GOLDENROD", "#DAA520", "HOTPINK", "#FF69B4", "INDIGO", "#4B0082", "IVORY", "#FFFFF0",
    "KHAKI", "#F0E68C", "LAVENDER", "#E6E6FA", "LIME", "#00FF00", "MAROON", "#800000", "NAVY", "#000080",
    "OLIVE", "#808000", "PLUM", "#DDA0DD", "POWDERBLUE", "#B0E0E6", "PURPLE", "#800080",
    "SALMON", "#FA8072", "SILVER", "#C0C0C0", "SKYBLUE", "#87CEEB", "TAN", "#D2B48C", "TEAL", "#008080",
    "VIOLET", "#EE82EE"
};

/**
 * Returns an AWT version of this color.
 */
public java.awt.Color awt() { return _awt!=null? _awt:(_awt=new java.awt.Color(getRGBA(),true)); } java.awt.Color _awt;

}