package com.reportmill.text;
import com.reportmill.base.*;
import java.util.*;
import snap.util.*;

/**
 * This class represents attributes of a paragraph in an RMXString (all of the characters up to and including each
 * newline in an RMXString make up a paragraph). Paragraphs can have their own alignment, indentation, min/max line
 * height, etc. You might use this class like this:
 * <p><blockquote><pre>
 *   RMParagraph pgraph = RMParagraph.defaultParagraph.deriveAligned(RMParagraph.ALIGN_RIGHT);
 *   RMXString xstring = new RMXString("Hello World", pgraph);
 */
public class RMParagraph implements Cloneable, RMTypes, XMLArchiver.Archivable {
    
    // Horizontal text alignment
    AlignX      _alignmentX = AlignX.Left;
    
    // Indention for whole paragraph
    double      _leftIndent = 0;
    
    // Indentation for first line of paragraph
    double      _leftIndentFirst = 0;
    
    // Indentation for right margin
    double      _rightIndent = 0;
    
    // Space between lines expressed as a factor of the current line height
    float       _lineSpacing = 1;
    
    // Space between lines expressed as a constant in points
    float       _lineGap = 0;
    
    // Min line height
    float       _lineHeightMin = 0;
    
    // Max line height
    float       _lineHeightMax = Float.MAX_VALUE;
    
    // Additional paragraph spacing
    float       _paragraphSpacing = 0;
    
    // Tab stops
    float       _tabs[] = _defaultTabs;
    
    // Tab stop types
    char        _tabTypes[] = _defaultTypes;
    
    // Default tab positions
    static float _defaultTabs[] = { 36f, 72f, 108f, 144f, 180f, 216f, 252f, 288f, 324f, 360f, 396f, 432f };
    
    // Default tab types
    static char  _defaultTypes[] = { 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L' };
    
    // Default paragraph
    public static final RMParagraph DEFAULT = new RMParagraph();
    public static final RMParagraph CENTERED = DEFAULT.deriveAligned(RMTypes.AlignX.Center);

    // Constants for tab types
    public static final char TAB_LEFT = 'L';
    public static final char TAB_RIGHT = 'R';
    public static final char TAB_CENTER = 'C';
    public static final char TAB_DECIMAL = 'D';
    
/**
 * Creates a new paragraph object initialized to defaultParagraph. You should probably use RMParagraph.defaultParagraph
 * instead.
 */
public RMParagraph() { }

/**
 * Creates a new paragraph with the given alignment and indentation.
 */
public RMParagraph(AlignX anAlign, float leftIndent, float leftIndentFirst, float rightIndent)
{
    _alignmentX = anAlign; _leftIndent = leftIndent; _leftIndentFirst = leftIndentFirst;
}

/**
 * Returns the alignment associated with this paragraph.
 */
public AlignX getAlignmentX()  { return _alignmentX; }

/**
 * Returns the left side indentation of this paragraph.
 */
public double getLeftIndent() { return _leftIndent; }

/**
 * Returns left side indentation of first line in this paragraph (this can be set different than successive lines).
 */
public double getLeftIndentFirst() { return _leftIndentFirst; }

/**
 * Returns the right side indentation of this paragraph.
 */
public double getRightIndent() { return _rightIndent; }

/**
 * Returns the spacing of lines expressed as a factor of a given line's height.
 */
public float getLineSpacing() { return _lineSpacing; }

/**
 * Returns additional line spacing expressed as a constant amount in points.
 */
public float getLineGap() { return _lineGap; }

/**
 * Returns the minimum line height in printer points associated with this paragraph.
 */
public float getLineHeightMin() { return _lineHeightMin; }

/**
 * Returns the maximum line height in printer points associated with this paragraph.
 */
public float getLineHeightMax() { return _lineHeightMax; }

/**
 * Returns the spacing between paragraphs in printer points associated with this paragraph.
 */
public float getParagraphSpacing() { return _paragraphSpacing; }

/**
 * Returns the number of tabs associated with this paragraph.
 */
public int getTabCount() { return _tabs.length; }

/**
 * Returns the specific tab value for the given index in printer points.
 */
public float getTab(int anIndex) { return _tabs[anIndex]; }

/**
 * Returns the type of tab at the given index.
 */
public char getTabType(int anIndex) { return _tabTypes[anIndex]; }

/**
 * Returns the raw tab array
 */
public float[] getTabs() { return _tabs; }

/**
 * Returns the raw tab type array
 */
public char[] getTabTypes() { return _tabTypes; }

/**
 * Returns the tab index for the given location.
 */
public int getTabIndex(float aLocation)
{
    // Iterate over tabs until we find one greater than given location
    for(int i=0, iMax=getTabCount(); i<iMax; i++)
        if(getTab(i)>aLocation)
            return i;
    return -1;  // If location was greater than all tab stops, return -1
}

/**
 * Returns the values of all the tabs associated with this paragraph as a comma separated string.
 */
public String getTabsString()
{
    // Iterate over tabs and build string
    StringBuffer sb = new StringBuffer();
    for(int i=0, iMax=_tabs.length; i<iMax; i++) {
        if(_tabs[i]==(int)_tabs[i]) sb.append((int)_tabs[i]);  // If tab is really int, append value as int
        else sb.append(_tabs[i]);  // Otherwise append value as float
        if(_tabTypes[i]!=TAB_LEFT) sb.append(_tabTypes[i]);  // If tab is not left tab, append type
        if(i+1<iMax) sb.append(',');  // If not end of tabs, append comma
    }
    
    // Return tabs string
    return sb.toString();
}

/**
 * Sets the value of tabs from the given tabs string.
 */
private void setTabsString(String aString)
{
    // Get individual tab strings
    String tabs[] = aString.split("\\s*\\,\\s*");
    if(tabs.length==1 && tabs[0].length()==0)
        tabs = new String[0];
    
    // Create tabs and types arrays
    _tabs = new float[tabs.length];
    _tabTypes = new char[tabs.length];
    
    // Iterate over tabs and set individual floats
    for(int i=0, iMax=tabs.length; i<iMax; i++) {
        _tabs[i] = RMUtils.floatValue(tabs[i]);  // Get tab location
        char type = tabs[i].charAt(tabs[i].length()-1);  // Get tab type
        _tabTypes[i] = Character.isLetter(type)? type : TAB_LEFT;
    }
}

/**
 * Returns a paragraph identical to the receiver, but with the given alignment.
 */
public RMParagraph deriveAligned(AlignX anAlign)
{
    RMParagraph ps = clone(); ps._alignmentX = anAlign; return ps;
}

/**
 * Returns a paragraph identical to the receiver, but with the given indentation values.
 */
public RMParagraph deriveIndent(double leftIndent, double leftIndentFirst, double rightIndent)
{
    RMParagraph ps = clone();
    ps._leftIndent = leftIndent; ps._leftIndentFirst = leftIndentFirst; ps._rightIndent = rightIndent;
    return ps;
}

/**
 * Returns a paragraph identical to the receiver, but with the given line spacing.
 */
public RMParagraph deriveLineSpacing(float aHeight)
{
    RMParagraph ps = clone(); ps._lineSpacing = aHeight; return ps;
}

/**
 * Returns a paragraph identical to the receiver, but with the given line gap.
 */
public RMParagraph deriveLineGap(float aHeight)  { RMParagraph ps = clone(); ps._lineGap = aHeight; return ps; }

/**
 * Returns a paragraph identical to the receiver, but with the given min line height.
 */
public RMParagraph deriveLineHeightMin(float aHeight)
{
    RMParagraph ps = clone(); ps._lineHeightMin = aHeight; return ps;
}

/**
 * Returns a paragraph identical to the receiver, but with the given max line height.
 */
public RMParagraph deriveLineHeightMax(float aHeight)
{
    RMParagraph ps = clone(); ps._lineHeightMax = aHeight; return ps;
}

/**
 * Sets the tab stops.
 */
public void setTabs(float newTabs[], char newTypes[])  { _tabs = newTabs; _tabTypes = newTypes; }

/**
 * Returns a paragraph identical to the receiver, but with the given tabs.
 */
public RMParagraph deriveTabs(float newTabs[], char newTypes[])
{
    RMParagraph ps = clone();
    setTabs(newTabs, newTypes);
    return ps;
}

/**
 * Returns a paragraph identical to the receiver, but with new value for tab at given index.
 */
public RMParagraph deriveTab(int anIndex, float tabValue, char tabType)
{
    // Clone this paragraph
    RMParagraph ps = clone();
    
    // If index is in bounds, start by removing existing tab stop
    if(anIndex>=0 && anIndex<_tabs.length) {
        
        // Create new tab arrays
        ps._tabs = new float[getTabCount()-1];
        ps._tabTypes = new char[getTabCount()-1];
        
        // Iterate over this paragraph's tabs, copying over all tabs but the given index
        for(int i=0, j=0; i<_tabs.length; i++) {
            if(i!=anIndex) {
                ps._tabs[j] = _tabs[i]; ps._tabTypes[j++] = _tabTypes[i]; }
        }
    }
    
    // If tab value is greater than zero, add tab back
    if(tabValue>=0) {
        
        // Cache tab arrays
        float tabs[] = ps._tabs;
        char tabTypes[] = ps._tabTypes;
        
        // Create new tab arrays
        ps._tabs = new float[tabs.length+1];
        ps._tabTypes = new char[tabs.length+1];
        
        // Iterate over tabs, inserting given tab before any of greater or equal value
        for(int i=0, j=0; i<tabs.length+1; i++) {
            
            // If last loop iteration and value hasn't been inserted, insert it
            if(i==tabs.length) {
                if(i==j) { ps._tabs[i] = tabValue; ps._tabTypes[i] = tabType; }
                continue; }
            
            // If new tab not inserted yet, but it is less than or equal this loop tab, insert value
            if(i==j && tabValue<=tabs[i]) {
                ps._tabs[j] = tabValue; ps._tabTypes[j++] = tabType; } // Insert given tab
            
            // Copy over next loop tab
            ps._tabs[j] = tabs[i]; ps._tabTypes[j++] = tabTypes[i];
        }
    }
    
    // Return the paragraph
    return ps;
}

/**
 * Standard clone of this object.
 */
public RMParagraph clone()
{
    try { return (RMParagraph)super.clone(); }
    catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    if(this==anObj) return true;
    if(!(anObj instanceof RMParagraph)) return false;
    RMParagraph other = (RMParagraph)anObj;
    if(other._alignmentX!=_alignmentX) return false;
    if(other._leftIndent!=_leftIndent) return false;
    if(other._leftIndentFirst!=_leftIndentFirst) return false;
    if(other._rightIndent!=_rightIndent) return false;
    if(other._lineSpacing!=_lineSpacing) return false;
    if(other._lineGap!=_lineGap) return false;
    if(other._lineHeightMin!=_lineHeightMin) return false;
    if(other._lineHeightMax!=_lineHeightMax) return false;
    if(other._paragraphSpacing!=_paragraphSpacing) return false;
    if(!Arrays.equals(other._tabs, _tabs)) return false;
    if(!Arrays.equals(other._tabTypes, _tabTypes)) return false;
    return true;
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Get new element named pgraph
    XMLElement e = new XMLElement("pgraph");
    
    // Archive AlignmentX, LeftIndent, LeftIndentFirst, RightIndent
    if(_alignmentX!=AlignX.Left) e.add("align", getAlignmentX().toString().toLowerCase());
    if(_leftIndent!=0) e.add("left-indent", _leftIndent);
    if(_leftIndentFirst!=_leftIndent) e.add("left-indent-0", _leftIndentFirst);
    if(_rightIndent!=0) e.add("right-indent", _rightIndent);
        
    // Archive LineSpacing, LineGap, LineHeightMin, LineHeightMax, ParagraphSpacing
    if(_lineSpacing!=1) e.add("line-space", _lineSpacing);
    if(_lineGap!=0) e.add("line-gap", _lineGap);
    if(_lineHeightMin!=0) e.add("min-line-ht", _lineHeightMin);
    if(_lineHeightMax!=Float.MAX_VALUE) e.add("max-line-ht", _lineHeightMax);
    if(_paragraphSpacing!=0) e.add("pgraph-space", _paragraphSpacing);
        
    // Archive Tabs
    if(!Arrays.equals(_tabs, _defaultTabs) || !Arrays.equals(_tabTypes, _defaultTypes))
        e.add("tabs", getTabsString());

    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive AlignmentX, LeftIndent, LeftIndentFirst, RightIndent
    _alignmentX = RMEnumUtils.valueOfIC(AlignX.class, anElement.getAttributeValue("align", "left"));
    _leftIndent = anElement.getAttributeFloatValue("left-indent");
    _leftIndentFirst = anElement.getAttributeFloatValue("left-indent-0", (float)_leftIndent);
    _rightIndent = anElement.getAttributeFloatValue("right-indent");
    
    // Archive LineSpacing, LineGap, LineHeightMin, LineHeightMax, ParagraphSpacing
    _lineSpacing = anElement.getAttributeFloatValue("line-space", 1);
    _lineGap = anElement.getAttributeFloatValue("line-gap");
    _lineHeightMin = anElement.getAttributeFloatValue("min-line-ht");
    _lineHeightMax = anElement.getAttributeFloatValue("max-line-ht", Float.MAX_VALUE);
    _paragraphSpacing = anElement.getAttributeFloatValue("pgraph-space");
    
    // Unarchive Tabs
    if(anElement.hasAttribute("tabs"))
        setTabsString(anElement.getAttributeValue("tabs"));
    
    // Return paragraph
    return this;
}

}