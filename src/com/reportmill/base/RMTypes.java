package com.reportmill.base;

/**
 * Common types.
 */
public interface RMTypes {

    // Constants for X/Y alignment
    public enum Align {
        TopLeft, TopCenter, TopRight,
        CenterLeft, Center, CenterRight,
        BottomLeft, BottomCenter, BottomRight
    }
    
    // Constants for horizontal alignment
    public enum AlignX { Left, Right, Center, Full; }

    // Constants for vertical alignment
    public enum AlignY { Top, Middle, Bottom; }
}