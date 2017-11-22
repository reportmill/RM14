package com.reportmill.shape;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.out.*;
import com.reportmill.text.RMFont;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import java.io.File;
import snap.util.*;
import snap.web.*;

/**
 * The RMDocument class represents a ReportMill document and is also an RMShape subclass, so it can be a real part of
 * the document/shape hierarchy. RMDocuments are also what ReportMill refers to as templates, and is commonly used like
 * this:
 * <p><blockquote><pre>
 *   RMDocument template = new RMDocument(aSource); // Load from path String, File, byte array, etc.
 *   RMDocument report = template.generateReport(aDataset); // Any Java dataset: EJBs, custom classes, collctns, etc.
 *   report.writePDF("MyReport.pdf");
 * </pre></blockquote><p>
 * On rare occasions, you may also want to create a document dynamically. Here's an example:
 * <p><blockquote><pre>
 *   RMDocument doc = new RMDocument(612, 792); // Standard US Letter size (8.5" x 11"), in points
 *   RMTable table = new RMTable(); // Create new table ...
 *   doc.getPage(0).addChild(table); // ... and add to first page
 *   table.setBounds(36, 36, 540, 680); // Position and size table
 *   table.getRow("Objects Details").getColumn(0).setText("Title: @getTitle@"); // Configure first text
 * </pre></blockquote><p>
 */
public class RMDocument extends RMParentShape {

    // The default document font
    RMFont            _font = RMFont.getDefaultFont();
    
    // The ReportMill version this document was created with
    float             _version = RMUtils.getVersion();
    
    // The currently selected page index
    int               _selIndex;
    
    // The page layout for the document (single, facing, continuous)
    PageLayout        _pageLayout = PageLayout.Single;
    
    // The native units of measure for this document
    Unit              _unit = Unit.Point;
    
    // Whether to show a grid
    boolean           _showGrid = false;
    
    // Whether to snap to grid
    boolean           _snapGrid = false;
    
    // Space between grid lines in points
    double            _gridSpacing = 9f;
    
    // Whether to show margin
    boolean           _showMargin = true;
    
    // Whether to snap to margin
    boolean           _snapMargin = true;
    
    // The margin rect
    RMRect            _margins = getMarginRectDefault();
    
    // Datasource
    RMDataSource      _dataSource;
    
    // Publish URL
    String            _publishUrl;
    
    // The string to be used when report encounters a null value
    String            _nullString = "<NA>";
    
    // Whether document should paginate or grow
    boolean           _paginate = true;
    
    // Whether output file formats should compress (PDF really)
    boolean           _compress = true;
    
    // The ReportOwner that created this document (if from RPG)
    ReportOwner       _reportOwner;
    
    // Locale
    public static Locale _locale = Locale.ENGLISH;  // Used by date/number formats    

    // Page Layout Enumerations
    public enum PageLayout { Single, Double, Quadruple, Facing, Continuous, ContinuousDouble };

    // Unit Enumerations
    public enum Unit { Inch, Point, CM, MM, Pica }

// Set headless flag
static { RMUtils.setHeadless(); }

/**
 * Creates a plain empty document. It's really only used by the archiver.
 */
public RMDocument() { addPage(); }

/**
 * Creates a document with the given width and height (in printer points).
 */
public RMDocument(double aWidth, double aHeight)
{
    addPage();
    setPageSize(aWidth, aHeight);
}

/**
 * Creates a new document from the given source.
 */
public RMDocument(Object aSource)  { new RMArchiver().getDoc(aSource, this); }

/**
 * Creates a new document from aSource using RMArchiver.
 */
public static RMDocument getDoc(Object aSource)  { return new RMArchiver().getDoc(aSource, null); }

/**
 * Returns the filename associated with this document, if available.
 */
public String getFilename()  { return getSourceURL()!=null? getSourceURL().getPath() : null; }

/**
 * Returns the document's default font.
 */
public RMFont getFont()  { return _font; }

/**
 * Sets the document default font.
 */
public void setFont(RMFont aFont)
{
    if(RMUtils.equals(aFont, getFont()) || aFont==null) return; // If value already set or null, just return
    firePropertyChange("Font", _font, _font = aFont, -1); // Set value and fire PropertyChange
    super.setFont(aFont); // Do normal version
}

/**
 * Returns the version this document was loaded as.
 */
public float getVersion()  { return _version; }

/**
 * Returns the number of pages in this document.
 */
public int getPageCount()  { return getChildCount(); }

/**
 * Returns the page at the given index.
 */
public RMPage getPage(int anIndex)  { return (RMPage)getChild(anIndex); }

/**
 * Returns the last page (convenience).
 */
public RMPage getPageLast()  { return getPage(getPageCount()-1); }

/**
 * Returns the list of pages associated with this document.
 */
public List <RMPage> getPages()  { return (List)_children; }

/**
 * Adds a new page to this document.
 */
public RMPage addPage()  { addPage(createPage()); return getPageLast(); }

/**
 * Adds a given page to this document.
 */
public void addPage(RMPage aPage)  { addPage(aPage, getPageCount()); }

/**
 * Adds a given page to this document at the given index.
 */
public void addPage(RMPage aPage, int anIndex)  { addChild(aPage, anIndex); }

/**
 * Removes a page from this document.
 */
public RMPage removePage(int anIndex)  { return (RMPage)removeChild(anIndex); }

/**
 * Removes the given page.
 */
public int removePage(RMPage aPage)  { return removeChild(aPage); }

/**
 * Creates a new page.
 */
public RMPage createPage()  { RMPage page = new RMPage(); page.setSize(getPageSize()); return page; }

/**
 * Override to make sure document has size.
 */
public void addChild(RMShape aChild, int anIndex)
{
    super.addChild(aChild, anIndex);
    if(getWidth()==0) setBestSize();
}

/**
 * Add the pages in the given document to this document (at end) and clears the pages list in the given document.
 */
public void addPages(RMDocument aDoc)
{
    // Add pages from given document
    for(RMShape page : aDoc.getChildArray())
        addPage((RMPage)page);
    
    // Add page reference shapes from given document and clear from old document
    if(_reportOwner!=null && aDoc._reportOwner!=null) {
        _reportOwner.getPageReferenceShapes().addAll(aDoc._reportOwner.getPageReferenceShapes());
        aDoc._reportOwner.getPageReferenceShapes().clear();
    }
}

/**
 * Returns the current page index of this document.
 */
public int getSelectedIndex()  { return _selIndex; }

/**
 * Selects the currently selected page by index.
 */
public void setSelectedIndex(int anIndex)
{
    int index = Math.min(anIndex, getPageCount()-1);
    if(index==_selIndex) return; // If value already set, just return
    firePropertyChange("SelectedPage", _selIndex, _selIndex = index, -1); // Set value and fire PropertyChange
    relayout(); // Rebuild
}

/**
 * Returns the currently selected page of this document.
 */
public RMPage getSelectedPage()  { return _selIndex>=0 && _selIndex<getPageCount()? getPage(_selIndex) : null; }

/**
 * Selects the given page.
 */
public void setSelectedPage(RMPage aPage)  { setSelectedIndex(aPage.indexOf()); }

/**
 * Returns the page layout for the document.
 */
public PageLayout getPageLayout()  { return _pageLayout; }

/**
 * Sets the page layout for the document.
 */
public void setPageLayout(PageLayout aValue)
{
    if(aValue==_pageLayout) return; // If value already set, just return
    firePropertyChange("PageLayout", _pageLayout, _pageLayout = aValue, -1); // Set value & fire PropertyChange
    if(getPageCount()>0) setSize(getPrefWidth(), getPrefHeight()); // Reset size
}

/**
 * Set page layout from string.
 */
public void setPageLayout(String aValue)
{
    try { setPageLayout(PageLayout.valueOf(RMStringUtils.firstCharUpperCase(aValue))); }
    catch(Exception e) { System.err.println("Unsupported Document.PageLayout: " + aValue); }
}

/**
 * Returns the units used to express sizes in the current document (POINTS, INCHES, CENTIMETERS).
 */
public Unit getUnit()  { return _unit; }

/**
 * Sets the units used to express sizes in the current document (POINTS, INCHES, CENTIMETERS).
 */
public void setUnit(Unit aValue)
{
    if(aValue==_unit) return; // If value already set, just return
    firePropertyChange("Unit", _unit, _unit = aValue, -1); // Set value and fire PropertyChange
}

/**
 * Sets the units used to express sizes in the current document with one of the strings: point, inch or cm.
 */
public void setUnit(String aString)
{
    try { setUnit(RMEnumUtils.valueOfIC(Unit.class, aString)); }
    catch(Exception e) { System.err.println("Unsupported Document.Unit: " + aString); }
}

/**
 * Converts given value from document units to printer points (1/72 of an inch).
 */
public double getPointsFromUnits(double aValue)  { return aValue*getUnitsMultiplier(); }

/**
 * Converts given value to document units from printer points (1/72 of an inch).
 */
public double getUnitsFromPoints(double aValue)  { return aValue/getUnitsMultiplier(); }

/**
 * Returns the multiplier used to convert printer points to document units.
 */
public float getUnitsMultiplier()
{
    switch(getUnit()) {
        case Inch: return 72;
        case CM: return 28.34646f;
        case MM: return 2.834646f;
        case Pica: return 12;
        default: return 1;
    }
}

/**
 * Returns whether the document should show an alignment grid.
 */
public boolean getShowGrid()  { return _showGrid; }

/**
 * Sets whether the document should show an alignment grid.
 */
public void setShowGrid(boolean aValue)  { repaint(); _showGrid = aValue; }

/**
 * Returns whether the document should snap to an alignment grid.
 */
public boolean getSnapGrid()  { return _snapGrid; }

/**
 * Sets whether the document should snap to an alignment grid.
 */
public void setSnapGrid(boolean aValue)  { _snapGrid = aValue; }

/**
 * Returns the grid spacing for the document's grid.
 */
public double getGridSpacing()  { return _gridSpacing; }

/**
 * Sets the grid spacing for the document's grid.
 */
public void setGridSpacing(double aValue)  { repaint(); if(aValue>0) _gridSpacing = aValue; }

/**
 * Returns whether the document should show a margin rect.
 */
public boolean getShowMargin()  { return _showMargin; }

/**
 * Sets whether the document should show a margin rect.
 */
public void setShowMargin(boolean aValue)  { repaint(); _showMargin = aValue; }

/**
 * Returns whether the document should snap to a margin rect.
 */
public boolean getSnapMargin()  { return _snapMargin; }

/**
 * Sets whether the document should snap to a margin rect.
 */
public void setSnapMargin(boolean aValue)  { _snapMargin = aValue; }

/**
 * Returns the margin rect for this document.
 */
public RMRect getMarginRect()
{
    double marginWidth = getSelectedPage().getWidth() - getMarginLeft() - getMarginRight();
    double marginHeight = getSelectedPage().getHeight() - getMarginTop() - getMarginBottom();
    return new RMRect(getMarginLeft(), getMarginTop(), marginWidth, marginHeight);
}

/**
 * Sets the margin rect for this document.
 */
public void setMarginRect(RMRect aRect)  { repaint(); _margins = aRect; }

/**
 * Returns the default margin rect.
 */
public RMRect getMarginRectDefault()  { return new RMRect(36, 36, 36, 36); }

/**
 * Sets the margin rect for this document.
 */
public void setMargins(double left, double right, double top, double bottom)
{
    setMarginRect(new RMRect(left, top, right, bottom));
}

/** Returns the margin rects left value. */
public double getMarginLeft()  { return _margins.x; }

/** Returns the margin rects right value. */
public double getMarginRight()  { return _margins.width; }

/** Returns the margin rects top value. */
public double getMarginTop()  { return _margins.y; }

/** Returns the margin rects bottom value. */
public double getMarginBottom()  { return _margins.height; }

/**
 * Returns the size of a document page.
 */
public RMSize getPageSize()  { return getPageCount()>0? getSelectedPage().getSize() : getPageSizeDefault(); }

/**
 * Sets the size of the document (and all of its pages).
 */
public void setPageSize(double aWidth, double aHeight)
{
    // Cache old value
    Object oldValue = getPageSize();
    
    // Set size of all doc pages
    for(int i=0, iMax=getPageCount(); i<iMax; i++)
        getPage(i).setSize(aWidth, aHeight);
    
    // Fire property change
    firePropertyChange("PageSize", oldValue, new RMSize(aWidth, aHeight), -1);
    
    // Set page size and revalidate
    setSize(getPrefWidth(), getPrefHeight());
}

/**
 * Returns the default page size.
 */
public RMSize getPageSizeDefault()  { return new RMSize(612,792); }

/**
 * Returns the autosizing default.
 */
public String getAutosizingDefault()  { return "~-~,~-~"; }

/**
 * Returns the RMDataSource associated with this document.
 */
public RMDataSource getDataSource()  { return _dataSource; }

/**
 * Sets the RMDataSource associated with this document.
 */
public void setDataSource(RMDataSource aDataSource)  { _dataSource = aDataSource; }

/**
 * Returns the schema for the RMDataSource associated with this document (convenience).
 */
public Schema getDataSourceSchema()  { return _dataSource==null? null : _dataSource.getSchema(); }

/**
 * Returns the entity this shape should show in keys browser.
 */
public Entity getDatasetEntity()  { return getDataSource()!=null? getDataSourceSchema().getRootEntity() : null; }

/** Returns the URL this document should be uploaded to. */
public String getPublishUrl()  { return _publishUrl; }

/** Sets the URL this document should be uploaded to. */
public void setPublishUrl(String aValue)  { _publishUrl = aValue; }

/** Returns the string used to replace any occurrances of null values in a generated report. */
public String getNullString()  { return _nullString; }

/** Sets the string used to replace any occurrances of null values in a generated report. */
public void setNullString(String aValue)  { _nullString = aValue; }

/** Returns whether the document should paginate generated reports by default. */
public boolean isPaginate()  { return _paginate; }

/** Sets whether the document should paginate generated reports by default. */
public void setPaginate(boolean aValue)  { _paginate = aValue; }

/** Returns whether the document should compress images in generated file formats like PDF. */
public boolean getCompress()  { return _compress; }

/** Sets whether the document should compress images in generated file formats like PDF. */
public void setCompress(boolean aValue)  { _compress = aValue; }

/**
 * Returns the document as an XML byte array.
 */
public byte[] getBytes()  { return toXML().getBytes(); }

/**
 * Returns the document as a byte array of a PDF file.
 */
public byte[] getBytesPDF()  { return new com.reportmill.pdf.writer.RMPDFWriter().getBytes(this); }

/**
 * Returns the document as a byte array of an HTML file.
 */
public byte[] getBytesHTML()  { return new RMHtmlFile(this).getBytes(); }

/**
 * Returns the document as a byte array of a CSV file.
 */
public byte[] getBytesCSV()  { return getBytesDelimitedAscii(",", "\n", true); }

/**
 * Returns the document as a byte array of a delimited ASCII file (using given field, record separator strings).
 */
public byte[] getBytesDelimitedAscii(String fieldDelimiter, String recordDelimiter, boolean quoteFields)
{
    return RMStringWriter.delimitedAsciiBytes(this, fieldDelimiter, recordDelimiter, quoteFields);
}

/**
 * Returns the document as byte array of an Excel file.
 */
public byte[] getBytesExcel()  { return new RMExcelWriter().getBytes(this); }

/**
 * Returns the document as byte array of an Excel file.
 */
public byte[] getBytesRTF()  { return new RMRTFWriter().getBytes(this); }

/**
 * Returns the document as byte array of a JPEG file.
 */
public byte[] getBytesJPEG()
{
    BufferedImage image = new RMShapeImager().setColor(Color.white).createImage(getPage(0));
    return RMAWTUtils.getBytesJPEG(image);
}

/**
 * Returns the document as byte array of PNG file.
 */
public byte[] getBytesPNG()
{
    BufferedImage image = new RMShapeImager().createImage(getPage(0));
    return RMAWTUtils.getBytesPNG(image);
}

/**
 * Returns the document as a string of a CSV file.
 */
public String getStringCSV()  { return getStringDelimitedText(",", "\n", true); }

/**
 * Returns the document as a string of a delimited text file.
 */
public String getStringDelimitedText(String fieldDelimiter, String recordDelimiter, boolean quoteFields)
{
    return RMStringWriter.delimitedString(this, fieldDelimiter, recordDelimiter, quoteFields);
}

/**
 * Writes the document out to the given path String (it extracts type from path extension).
 */
public void write(String aPath)
{
    String path = aPath.toLowerCase();
    if(path.endsWith(".pdf")) writePDF(aPath);
    if(path.endsWith(".html")) new RMHtmlFile(this).write(aPath);
    if(path.endsWith(".csv")) RMUtils.writeBytes(getBytesCSV(), aPath);
    if(path.endsWith(".jpg")) RMUtils.writeBytes(getBytesJPEG(), aPath);
    if(path.endsWith(".png")) RMUtils.writeBytes(getBytesPNG(), aPath);
    if(path.endsWith(".xls")) RMUtils.writeBytes(getBytesExcel(), aPath);
    if(path.endsWith(".rtf")) RMUtils.writeBytes(getBytesRTF(), aPath);
    if(path.endsWith(".rpt") || path.endsWith(".rib") || path.endsWith(".xml"))
        RMUtils.writeBytes(toXML().getBytes(), aPath);
}

/**
 * Writes the document to the given File object
 */
public void write(File aFile)  { write(aFile.getAbsolutePath()); }

/**
 * Writes the document to the given path String as PDF.
 */
public void writePDF(String aPath)  { RMUtils.writeBytes(getBytesPDF(), aPath); }

/**
 * Returns the total time needed to animate this document (total of all page animators).
 */
public float getMaxTime()
{
    float duration = 0;
    for(int i=0, iMax=getPageCount(); i<iMax; i++) {
        RMAnimator animator = getPage(i).getChildAnimator();
        duration += animator==null? 0 : animator.getMaxTime();
    }
    return duration;
}

/**
 * Returns the document itself (over-ridden from RMShape).
 */
public RMDocument getDocument()  { return this; }

/**
 * Returns the animator for a specific page.
 */
public RMAnimator getAnimator(int anIndex)  { return getPage(anIndex).getChildAnimator(); }

/**
 * Returns the animator for the last page.
 */
public RMAnimator getAnimatorLast()  { return getPageLast().getChildAnimator(); }

/**
 * Returns whether the last animator loops.
 */
public boolean getLoops()  { return getAnimatorLast()==null? false : getAnimatorLast().getLoops(); }

/**
 * Returns a subreport document for given name (override to improve).
 */
public RMDocument getSubreport(String aName)
{
    // If there is a filename, see if sister document exists
    if(getFilename()!=null) {
        
        // If name doesn't end in rpt, add it
        if(!RMStringUtils.endsWithIC(aName, ".rpt")) aName += ".rpt";
        
        // Get directory, subreport filename and subreport document
        String directory = RMStringUtils.getPathParent(getFilename());
        String subreportFilename = RMStringUtils.getPathChild(directory, aName);
        try { return RMDocument.getDoc(subreportFilename); }
        catch(Exception e) { }
        
        // Otherwise, just try full name in case it's a path
        try { return RMDocument.getDoc(aName); }
        catch(Exception e) { }
    }
    
    // Return null since not found
    return null;
}

/**
 * Overrides paint shape, because document should never really paint itself.
 */
public void paintShape(RMShapePainter aPntr) { }

/**
 * Returns a generated report from this template evaluated against the given object.
 */
public RMDocument generateReport()
{
    return generateReport(getDataSource()!=null? getDataSource().getDataset() : null, null, true);
}

/**
 * Returns a generated report from this template evaluated against the given object.
 */
public RMDocument generateReport(Object theObjects)  { return generateReport(theObjects, null, true); }

/**
 * Returns a generated report from this template evaluated against the given object and userInfo.
 */
public RMDocument generateReport(Object objects, Object userInfo)  { return generateReport(objects, userInfo, true); }

/**
 * Returns a generated report from this template evaluated against the given object with an option to paginate.
 */
public RMDocument generateReport(Object objects, boolean paginate)  { return generateReport(objects, null, paginate); }

/**
 * Returns generated report from this template evaluated against given object/userInfo (with option to paginate).
 */
public RMDocument generateReport(Object theObjects, Object theUserInfo, boolean aPaginateFlag)
{
    // Create and configure reportmill with objects, userinfo, pagination and null-string
    ReportOwner ro = new ReportOwner(); ro.setTemplate(this);
    if(theObjects!=null) ro.addModelObject(theObjects);
    if(theUserInfo!=null) ro.addModelObject(theUserInfo);
    ro.setPaginate(aPaginateFlag && isPaginate());
    ro.setNullString(getNullString());
    return ro.generateReport();
}

/**
 * Override to handle ShapeLists special.
 */
protected RMShape rpgChildren(ReportOwner anRptOwner, RMParentShape aParent)
{
    // Declare local variable for whether table of contents page was encountered
    RMPage tableOfContentsPage = null; int tocPageIndex = 0;

    RMDocument doc = (RMDocument)aParent;
    for(int i=0, iMax=getChildCount(); i<iMax; i++) { RMPage page = getPage(i);
    
        // Check for table of contents table
        if(RMTableOfContents.checkForTableOfContents(page)) {
            tableOfContentsPage = page; tocPageIndex = aParent.getChildCount(); continue; }

        // Generate report and add results
        RMParentShape crpg = (RMParentShape)anRptOwner.rpg(page, doc);
        if(crpg instanceof ReportOwner.ShapeList) {
            for(RMShape pg : crpg.getChildArray()) doc.addPage((RMPage)pg); }
        else doc.addPage((RMPage)crpg);
    }
    
    // Do RPG for TableOfContentsPage
    if(tableOfContentsPage!=null) RMTableOfContents.rpgPage(anRptOwner, doc, tableOfContentsPage, tocPageIndex);

    // Report report
    return aParent;
}

/**
 * Performs page substitutions on any text fields that were identified as containing @Page@ keys.
 */
public void resolvePageReferences()  { if(_reportOwner!=null) _reportOwner.resolvePageReferences(); }

/**
 * Rebuilds the document according to the selected page and page layout.
 */
protected void layoutChildren()
{
    // Get document
    int selectedIndex = getSelectedIndex();
    double offscreen = getWidth() + 5000;
    
    // If no pages or selected page, return
    if(getPageCount()==0 || getSelectedPage()==null) return;
    
    // Handle PageLayout Single: Iterate over pages, set location to zero and set current page to visible
    if(getPageLayout()==RMDocument.PageLayout.Single) {
        for(int i=0, iMax=getChildCount(); i<iMax; i++) { RMPage page = getPage(i);
            boolean showing = i==getSelectedIndex();
            page.setXY(showing? 0 : offscreen, 0);
        }
    }
    
    // Handle PageLayout Double: Iterate over pages, set location of alternating pages to zero/page-width
    else if(getPageLayout()==RMDocument.PageLayout.Double) {
        for(int i=0, iMax=getChildCount(); i<iMax; i+=2) {
            RMPage page1 = getPage(i), page2 = i+1<iMax? getPage(i+1) : null;
            boolean showing = i==selectedIndex || i+1==selectedIndex;
            page1.setXY(showing? 0 : offscreen, 0);
            if(page2!=null)
                page2.setXY(showing? page1.getWidth() : offscreen, 0);
        }
    }
    
    // Handle PageLayout Facing
    else if(getPageLayout()==RMDocument.PageLayout.Facing) {
        
        // Set location of page 1
        RMPage page = getPage(0);
        page.setXY(selectedIndex==0? getPageSize().width : offscreen, 0);
        
        // Iterate over pages, set location of alternating pages to zero/page-width, set current pages to visible
        for(int i=1, iMax=getChildCount(); i<iMax; i+=2) {
            RMPage page1 = getPage(i), page2 = i+1<iMax? getPage(i+1) : null;
            boolean showing = i==selectedIndex || i+1==selectedIndex;
            page1.setXY(showing? 0 : offscreen, 0);
            if(page2!=null)
                page2.setXY(showing? page1.getWidth() : offscreen, 0);
        }
    }
    
    // Handle PageLayout Continuous: Add all pages and set appropriate xy
    else if(getPageLayout()==RMDocument.PageLayout.Continuous) {
        float y = 0;
        for(int i=0, iMax=getChildCount(); i<iMax; i++) { RMPage page = getPage(i);
            page.setXY(0, y); y += page.getHeight() + 10; }
    }
}

/**
 * Override to return double page width for PageLayout.Double & Facing.
 */
protected double computePrefWidth(double aHeight)
{
    double width = getPageSize().width;
    switch(getPageLayout()) { case Double: case Facing: width *= 2; break; }
    return width;
}

/**
 * Override to return height*PageCount (plus spacing) for Continuous.
 */
protected double computePrefHeight(double aWidth)
{
    double height = getPageSize().height;
    if(getPageLayout()==PageLayout.Continuous && getPageCount()>0) {
        height *= getPageCount(); height += (getPageCount()-1)*10; }
    return height;
}

/**
 * Returns RXElement for document.
 */
public XMLElement toXML()
{
    layout();
    resolvePageReferences();
    return new RMArchiver().writeObject(this);
}

/**
 * Copies basic document attributes (shallow copy only - no children or pages).
 */
public RMDocument clone()
{
    RMDocument clone = (RMDocument)super.clone(); clone._reportOwner = null; return clone;
}

/**
 * XML archival.
 */
protected XMLElement toXMLShape(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXMLShape(anArchiver); e.setName("document");
    
    // Remove questionable document/shape attributes
    e.removeAttribute("x"); e.removeAttribute("y");
    e.removeAttribute("width"); e.removeAttribute("height");
    
    // Archive Version
    e.add("version", RMUtils.getVersion());
    
    // Archive DataSource, Font
    XMLElement dxml = _dataSource!=null? anArchiver.toXML(_dataSource, this) : null;
    if(dxml!=null && dxml.getAttributeCount()>0) e.add(dxml);
    if(!RMUtils.equals(getFont(), RMFont.getDefaultFont())) e.add(anArchiver.toXML(getFont(), this));
    
    // Archive PageLayout, Unit
    if(getPageLayout()!=PageLayout.Single) e.add("page-layout", getPageLayout().name());
    if(getUnit()!=Unit.Point) e.add("unit", getUnit().name());
        
    // Archive ShowMargin, SnapMargin, MarginRect
    if(_showMargin) e.add("show-margin", true);
    if(_snapMargin) e.add("snap-margin", true);
    if((_showMargin || _snapMargin) && !getMarginRect().equals(getMarginRectDefault()))
        e.add("margin", _margins.toXMLString());
        
    // Archive ShowGrid, SnapGrid, GridSpacing
    if(_showGrid) e.add("show-grid", true);
    if(_snapGrid) e.add("snap-grid", true);
    if((_showGrid || _snapGrid) && _gridSpacing!=9) e.add("grid", _gridSpacing);
        
    // Archive NullString, Paginate, Compress, PublishURL
    if(_nullString!=null && _nullString.length()>0) e.add("null-string", _nullString);
    if(!_paginate) e.add("paginate", _paginate);
    if(!_compress) e.add("compress", false);
    if(_publishUrl!=null && _publishUrl.length()>0) e.add("publish", _publishUrl);
        
    // Return element
    return e;
}

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive pages
    for(int i=0, iMax=getPageCount(); i<iMax; i++)
        anElement.add(anArchiver.toXML(getPage(i), this));
}

/**
 * XML unarchival.
 */
protected void fromXMLShape(XMLArchiver anArchiver, XMLElement anElement)
{
    // Remove default page and unarchive basic shape attributes
    while(getPageCount()>0) removePage(0);
    super.fromXMLShape(anArchiver, anElement);
    setSourceURL(anArchiver.getSourceURL());
    
    // Unarchive Version
    _version = anElement.getAttributeFloatValue("version", 8.0f);
    anArchiver.setVersion(_version);
    
    // Unarchive Datasource, Font
    XMLElement dxml = anElement.get("datasource");
    if(dxml!=null) setDataSource(anArchiver.fromXML(dxml, RMDataSource.class, this));
    if(anElement.getElement("font")!=null) _font = (RMFont)anArchiver.fromXML(anElement.getElement("font"), null);
    
    // Unarchive PageLayout, Unit
    if(anElement.hasAttribute("page-layout")) setPageLayout(anElement.getAttributeValue("page-layout"));
    if(anElement.hasAttribute("unit")) setUnit(anElement.getAttributeValue("unit"));
    
    // Unarchive ShowMargin, SnapMargin, MarginRect
    setShowMargin(anElement.getAttributeBoolValue("show-margin"));
    setSnapMargin(anElement.getAttributeBoolValue("snap-margin"));
    if(anElement.getAttributeValue("margin")!=null)
        setMarginRect(RMRect.fromXMLString(anElement.getAttributeValue("margin")));
        
    // Unarchive ShowGrid, SnapGrid, GridSpacing
    setShowGrid(anElement.getAttributeBoolValue("show-grid"));
    setSnapGrid(anElement.getAttributeBoolValue("snap-grid"));
    setGridSpacing(anElement.getAttributeFloatValue("grid", 9));
    
    // Unarchive NullString, Paginate, Compress, PublishURL
    setNullString(anElement.getAttributeValue("null-string", ""));
    setPaginate(anElement.getAttributeBoolValue("paginate", true));
    setCompress(anElement.getAttributeBoolValue("compress", true));
    setPublishUrl(anElement.getAttributeValue("publish"));
}

/** Editor method indicates that document is super selectable. */
public boolean superSelectable()  { return true; }

/** Editor method indicates that pages super select immediately. */
public boolean childrenSuperSelectImmediately()  { return true; }

/** Editor method indicates that document accepts children (should probably be false). */
public boolean acceptsChildren()  { return true; }

/** Obsolete method for old pdfBytes() method. */
public byte[] pdfBytes()  { return getBytesPDF(); }

}