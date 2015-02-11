package com.reportmill.out;
import com.reportmill.base.*;
import com.reportmill.text.*;
import com.reportmill.shape.*;
import com.reportmill.graphics.*;
import java.io.*;
import java.util.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * This class is used to generate an Excel file from an RMDocument.
 *
 * You simply invoke this with: new RMExcelWriter().getBytes(aDoc);
 */
public class RMExcelWriter {

    // The workbook for this writer
    HSSFWorkbook          _workbook;
    
    // The list of workbook styles found in this workbook
    List <WorkbookStyle>  _styles = new ArrayList();

    // The list of fonts in this workbook
    List <WorkbookFont>   _fonts = new ArrayList();
    
    // The currently selected cell in the sheet
    HSSFCell              _activeCell;
    
    // Whether to show gridlines 
    boolean               _showsAllGridlines = false;
    
/**
 * Creates a basic excel writer.
 */
public RMExcelWriter() { }

/**
 * Returns a byte array of an Excel file (.xls) for the given RMDocument.
 */
public byte[] getBytes(RMDocument aDoc)
{
    // Create the workbook
    HSSFWorkbook book = getWorkbook(aDoc);
    
    // Write to ByteArrayOutputStream
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try { book.write(byteOut); } catch(Exception e) { e.printStackTrace(); }
    
    // Return bytes
    return byteOut.toByteArray();
}

/**
 * Returns an Excel workbook for the RMDocument.
 * Use this routine instead of getBytes() if you need to do any post-processing on the workbook.
 */
public HSSFWorkbook getWorkbook(RMDocument aDoc)
{
    // Create a new workbook
    _workbook = new HSSFWorkbook();
    
    // Validate and resolve page references in aDoc
    aDoc.layout();
    aDoc.resolvePageReferences();

    // Allocate the array of shapes that ultimately will become the spreadsheet
    List <RMShape> sheetShapes = new ArrayList();
    
    // Iterate through pages and have each one append XLS
    for(int i=0, iMax=aDoc.getPageCount(); i<iMax; i++) { RMPage page = aDoc.getPage(i);
        
        // Create a new sheet for each explicit page
        RMExcelSheet sheet = new RMExcelSheet(_workbook.createSheet());
        
        // If shape named ExcelHeader is present, remove and set sheet Header text
        RMShape excelHeader = page.getChildWithName("ExcelHeader"); if(excelHeader!=null) {
            page.removeChild(excelHeader);
            String text = excelHeader instanceof RMTextShape? ((RMTextShape)excelHeader).getText() : "";
            sheet.getSheet().getHeader().setCenter(text);
        }
       
        // If shape named ExcelFooter is present, remove and set sheet Footer text
        RMShape excelFooter = page.getChildWithName("ExcelFooter"); if(excelFooter!=null) {
            page.removeChild(excelFooter);
            String text = excelFooter instanceof RMTextShape? ((RMTextShape)excelFooter).getText() : "";
            sheet.getSheet().getFooter().setCenter(text);
        }
       
        // First, reorder the shapes so that shapes that define rows & columns will be processed first.
        sheetShapes.clear();
        getSheetShapes(page, sheetShapes);
        
        // Get the enclosing rect of all the shapes
        RMRect childrenRect = getBoundsOfExcelChildren(page);
        
        // Create a crosstab for the entire spreadsheet
        RMShapeTable tempCells = RMShapeTable.createTable(sheetShapes, page, childrenRect);
        
        // Set the sheet origin so the upper-leftmost child will be placed at the upper-left
        // of the sheet.  Note that this could be set to the RMPage's bounds origin instead, in which case
        // everything would appear exactly as it does in the template (as much as possible, anyway).
        sheet.setOrigin(childrenRect.getOrigin());
        
        // Create the Excel table
        if(tempCells != null)
            appendCrossTab(sheet, null, tempCells);

        // Now add the freeform shapes on top
        append(sheet, null, page);
        
        // Set per-sheet options
        sheet.getSheet().setDisplayGridlines(getShowsGridlines(i));
    }
    
    // Return workbook
    return _workbook;
}

/**
 * Returns whether gridlines will be shown in Excel sheets.
 */
public boolean getShowsAllGridlines()  { return _showsAllGridlines; }

/**
 * Sets whether gridlines will be shown in Excel sheets.
 */
public void setShowsAllGridlines(boolean aValue)  { _showsAllGridlines = aValue; }

/**
 *  Returns whether gridlines are shown for given page number. If you have multiple pages but only want
 *  gridlines for certain pages, subclass RMExcelWriter and override this method.
 */
public boolean getShowsGridlines(int aPage)  { return _showsAllGridlines; }

/**
 * Returns true if this shape should become a fixed spreadsheet cell.
 * Also returns true if all of the shape's children will be spreadsheet cells.
 */
public boolean isSheetShape(RMShape aShape)  { return aShape instanceof RMTableRow || aShape instanceof RMCrossTab; }

/**
 * Searches through the hierarchy for tables & cells which will define the 
 * row/column structure of the spreadsheet and adds them to the list.
 */
private void getSheetShapes(RMShape aShape, List aList)
{
    // Save away all RMTexts inside a sheetshape (note that RMCells are RMText subclasses)
    if(isSheetShape(aShape))
        ((RMParentShape)aShape).getChildrenWithClass(aList, RMTextShape.class);
    
    // Recurse for every child    
    else for(int i=0, n=aShape.getChildCount(); i<n; ++i)
        getSheetShapes(aShape.getChild(i), aList);
}

/** 
 * a lot like RMShape.getBoundsOfChildren(), but excludes RMTableRPGs
 */
private RMRect getBoundsOfExcelChildren(RMShape aShape)
{
    RMRect maxBounds = null;

    // Iterate over shape children
    for(int i=0, iMax=aShape.getChildCount(); i<iMax; i++) { RMShape child = aShape.getChild(i);
        
        // Declare variable for child bounds
        RMRect childBounds = null;
        
        // just get the frame for all regular children
        if(!(child instanceof RMTableRPG))
            childBounds = child.getFrame();
        
        // If it's a non-empty tableRPG, get only the bounds of it's children. This avoids having a huge
        // empty cell the size of the table for tables whose rows don't fill the available space.
        else if (child.getChildCount()>0) {
            childBounds = getBoundsOfExcelChildren(child);
            aShape.convertRectFromShape(childBounds, child);
        }

        // If child bounds, coalesce 
        if (childBounds != null) {
            if (maxBounds==null)
                maxBounds = new RMRect(childBounds);
            else maxBounds.unionEvenIfEmpty(childBounds);
        }
    }
    
    return maxBounds==null ? aShape.getBoundsInside() : maxBounds;
}


/**
 * Converts RM 72pt/inch units to Excel character width units.
 */
public static float getCharWidthFromPoints(double points)
{
    // Column widths in Excel are measured in multiples of a character width.
    // The character width is determined by the font that is set for the 'Normal' style.
    // Within the workbook the Normal style is the style at index 0.
    // Microsoft claims that the 'factory default' is Arial 10
    // 
    // It might be a good idea to force the issue by explicitly setting
    // the style at index 0 to be Arial 10, just in case.
    //
    // This calculation is based on the following:
    //  1.  72 pts = 96 pixels = 1 inch (according to MS)
    //  2.  The column width is specified in the file as a short representing 1/256s of the actual value.
    //  3.  With the default set to Arial 10, I set a column in Excel to 960 pixels and used BiffViewer to
    //      see what value Excel writes to the file. This value is -30428 (short) = 0x8924 = 35108 (int)
    //
    //      Therefore,
    //      960 pixels = 720 pts = 35108/256 chars = 137.140625 Arial 10 chars
    //      1 point = 137.140625/720 chars (& 1 char = 720/137.14 pts ~ 5.25 pts)
    // 
    return (float)points*137.140625f/720;
}

/**
 * Converts rm 72pts/inch units to excel column width units.
 * Column widths are in units of 1/256 of a char width.
 */
public static short getColumnWidthFromPoints(double points)  { return (short)(256*getCharWidthFromPoints(points)); }

/**
 * Recursively iterates over shape hierarchy and adds HSSFShapes for RMShapes.
 */
private void append(RMExcelSheet rmSheet, HSSFShapeContainer aParent, RMShape aShape)
{
    // If it's a sheet shape, it's already been processed (as have it's children)
    if(isSheetShape(aShape)) 
        return;
    
    // Declare variable for excel shape object that gets created for a freeform RMShape
    HSSFShape newShape = null;
    
    // Handle line segment
    if(aShape instanceof RMLineShape)
        newShape = rmSheet.addLine(aShape, aParent);

    // Handle RMImage (creates a new picture every time!!)
    else if(aShape instanceof RMImageShape) { RMImageShape imageShape = (RMImageShape)aShape;

        // Get image data (if not available or invalid, just return)
        RMImageData imageData = imageShape.getImageData();
        if(imageData==null || !imageData.isValid()) {
            System.err.println("Error retreiving image data"); return; }
        
        String type = imageData.getType();
        int poiType = 0;
        if (type.equalsIgnoreCase("png"))
            poiType = HSSFWorkbook.PICTURE_TYPE_PNG;
        else if (type.equalsIgnoreCase("jpg") || type.equalsIgnoreCase("jpeg")) 
            poiType = HSSFWorkbook.PICTURE_TYPE_JPEG;
        else {
            System.err.println("Image type \""+type+"\" not supported in Excel files.  Image should be either png or jpg format");
            return;
        }
        
        // Add picture
        int pindex = _workbook.addPicture(imageData.getBytes(), poiType);
        newShape = rmSheet.addNewShape(aShape, aParent);
        ((HSSFPicture)newShape).setPictureIndex(pindex);
    }
    
    // Handle text
    else if(aShape instanceof RMTextShape) { RMTextShape text = (RMTextShape)aShape;
        
        // POI does something weird with empty rich texts, so toss it
        if(text.length()>0) {
            newShape = rmSheet.addNewShape(text, aParent);
            ((HSSFTextbox)newShape).setString(createRichText(text.getXString()));
        }
        
        // Unless it had a fill or stroke, in which case just turn it into a rectangle
        else if(text.getFill()!= null || text.getStroke()!=null)
            newShape = rmSheet.addRect(aShape, aParent);
    }
    
    // Handle rectangle
    else if(aShape instanceof RMRectShape)
      newShape = rmSheet.addRect(aShape, aParent);
    
    // If shape isn't a table row, just recurse into children
    else for(int i=0, iMax=aShape.getChildCount(); i<iMax; i++)
        append(rmSheet, aParent, aShape.getChild(i));
    
    // If a shape was added, set it's fill & stroke colors
    if(newShape != null)
        setShapeFillAndStroke(newShape, aShape);
}

/**
 * Adds rows & columns from a crosstab.
 */
public void appendCrossTab(RMExcelSheet aSheet, HSSFShapeContainer aParent, RMShapeTable aCrossTab)
{
    // Get HSSF sheet
    HSSFSheet sheet = aSheet.getSheet();
    
    // First, set all the column widths (overrides existing values)
    for(int colIndex=0, colMax=aCrossTab.getColumnCount(); colIndex<colMax; ++colIndex) {
        RMShapeTable.STCol column = aCrossTab.getColumn(colIndex);
        short colWidth = getColumnWidthFromPoints(column.getWidth());
        sheet.setColumnWidth(colIndex, colWidth);
    }
    
    // Create the rows and set their heights
    for(int rowIndex=0, rowMax=aCrossTab.getRowCount(); rowIndex<rowMax; ++rowIndex) {
        HSSFRow row = sheet.createRow(rowIndex);
        row.setHeightInPoints((float)aCrossTab.getRow(rowIndex).getHeight());
        for(int colIndex=0, colMax=aCrossTab.getColumnCount(); colIndex<colMax; ++colIndex)
            row.createCell(colIndex);
    }
    
    // Now walk through all the cells and merge any that span multiple rows/cols
    for(int rowIndex=0, rowMax=aCrossTab.getRowCount(); rowIndex<rowMax; ++rowIndex) {
        
        // Get current row
        HSSFRow row = sheet.getRow(rowIndex);
        
        // Iterate over columns
        for(int colIndex=0, colMax=aCrossTab.getColumnCount(); colIndex<colMax; ++colIndex) {
            
            // Get column
            RMShapeTable.STCell rmcell = aCrossTab.getCell(rowIndex, colIndex);
            
            // If a cell spans multiple rows or columns, getCell(row,col) returns the same cell instance
            // for each row,col this cell covers. Only take the first one.
            if(rmcell.getRow()==rowIndex && rmcell.getColumn()==colIndex) {
                
                // Merge any cells that this cell spans
                int rowspan = rmcell.getRowSpan();
                int colspan = rmcell.getColumnSpan();
                if(rowspan>1 || colspan>1) {
                    CellRangeAddress cra = new CellRangeAddress(rowIndex, rowIndex+rowspan-1, colIndex, colIndex+colspan-1);
                    sheet.addMergedRegion(cra);
                }
                
                // Fill in the data and style
                if(rmcell.getCellShape() instanceof RMTextShape)
                    fillCell(row.getCell(colIndex), (RMTextShape)rmcell.getCellShape());
            }
        }
    }
}

/** 
 * Fill a spreadsheet cell with the contents & attributes of an RMText shape
 */
public void fillCell(HSSFCell aCell, RMTextShape aText)
{
    // Get plain string (replace any tabs with spaces (Excel doesn't like tabs)
    String string = aText.getText();
    string = RMStringUtils.replace(string, "\t", " ");

    // Get text format, if available
    RMFormat format = aText.getFormat();
    
    // Declare local variable for format string
    String formatString = null;
    
    // Handle numeric cells (number formatted)
    if(format instanceof RMNumberFormat) { RMNumberFormat numFormat = (RMNumberFormat)format;
        
        // Get format string
        formatString = numFormat.toPattern();
        
        // Get the cell value as number
        Number number = null;
        try { number = numFormat.parse(string); }
        catch(Exception e) { }
        
        // Set cell double value
        if(number != null)
           aCell.setCellValue(number.doubleValue());
        
        // If null number due to formatting exception, use original string instead.
        else {
            aCell.setCellValue(new HSSFRichTextString(string));
            formatString = null;
        }
    }
    
    // Handle date cells (date formatted)
    else if(format instanceof RMDateFormat) { RMDateFormat dateFormat = (RMDateFormat)format;
        
        // Get the format string
        formatString = dateFormat.toPattern();
        
        // Get the cell value as date
        Date date = null;
        try { date = dateFormat.parse(string); }
        catch(Exception e) { }
        
        // If date is non null, set date value and try to get equivalent excel format string
        if(date!=null) {
            aCell.setCellValue(date);
            formatString = getExcelDatePattern(formatString);
        }
        
        // If date was null, just set string and reset formatString
        else {
            aCell.setCellValue(new org.apache.poi.hssf.usermodel.HSSFRichTextString(string));
            formatString = null;
        }
    }
    
    // If text has no format, just add it as string
    else aCell.setCellValue(new org.apache.poi.hssf.usermodel.HSSFRichTextString(string));

    // Get HSSFCellStyle for cell font, alignment, format
    HSSFCellStyle style = getWorkbookStyle(aText, formatString);
    
    // Set cell style
    aCell.setCellStyle(style);
    
    // Activate the first non-empty cell
    if(_activeCell==null && aText.length()>0) {
        _activeCell = aCell;
        _activeCell.setAsActiveCell();
    }
}

/**
 * Sets the fill and stroke attributes of the Excel shape from the shape's attributes.
 */
public void setShapeFillAndStroke(HSSFShape hssfShape, RMShape aShape)
{
    // Set fill color (solid fill only). Shape has both index or arbitrary rgb colors
    if(aShape.getFill()!=null) {
        RMColor c = aShape.getFill().getColor();
        hssfShape.setFillColor(c.getRedInt(), c.getGreenInt(), c.getBlueInt());
    }
    else hssfShape.setNoFill(true);
    
    // Set stroke color, line-style
    if(aShape.getStroke()!=null) {
        RMColor c = aShape.getStroke().getColor();
        hssfShape.setLineStyle(HSSFShape.LINESTYLE_SOLID);
        hssfShape.setLineStyleColor(c.getRedInt(), c.getGreenInt(), c.getBlueInt());
    }
    else hssfShape.setLineStyle(HSSFShape.LINESTYLE_NONE);
}

/**
 * Returns a shared HSSFCellStyle for a given font, alignment and format string.
 */
private HSSFCellStyle getWorkbookStyle(RMTextShape aText, String aFormat)
{
    // Iterate over workbook styles and return first matching entry
    for(WorkbookStyle style : _styles)
        if(style.isMatch(aText, aFormat))
            return style.getHSSFCellStyle();
    
    // Otherwise, create and add style to list
    WorkbookStyle workBookStyle = new WorkbookStyle(aText, aFormat);
    _styles.add(workBookStyle);
    return workBookStyle.getHSSFCellStyle();
}

/**
 * Returns an HSSFont for a given RMFont.
 */
private HSSFFont getWorkbookFont(RMFont aFont, RMColor aColor)
{
    // Iterate over workbook fonts and return first matching entry
    for(WorkbookFont font : _fonts)
        if(font.isMatch(aFont, aColor))
            return font.getHSSFFont();
    
    // Otherwise, create and add style to list
    WorkbookFont workBookFont = new WorkbookFont(aFont, aColor);
    _fonts.add(workBookFont);
    return workBookFont.getHSSFFont();
}

/** 
 * Converts an XString, as much as possible, to an excel rich text string
 */
public HSSFRichTextString createRichText(RMXString anXString)
{
    // If null or empty xstring, just return empty poi rich text string)
    if(anXString==null || anXString.length()==0)
        return new HSSFRichTextString();
    
    // Create poi RichTextString
    HSSFRichTextString hstr = new HSSFRichTextString(anXString.getText());
    for(int i=0, n=anXString.getRunCount(); i<n; i++) {
        RMXStringRun run = anXString.getRun(i);
        HSSFFont hfont = getWorkbookFont(run.getFont(), run.getColor());
        hstr.applyFont(run.start(), run.end(), hfont);
    }

    // Return poi string
    return hstr;
}
        
/**
 * An inner class to map a cell font, alignment, fill, stroke, text color and format to a unique HSSFCellStyle.
 */
class WorkbookStyle {

    // The prototype text
    RMTextShape    _text;
    
    // The excel format
    String         _format;
    
    // The HSSFCellStyle
    HSSFCellStyle  _style;

    /**
     * Creates a new workbook style.
     */
    public WorkbookStyle(RMTextShape aText, String aFormat)  { _text = aText; _format = aFormat; }
    
    /**
     * Returns whether this style is a match for given text and format string.
     */
    public boolean isMatch(RMTextShape aText, String aFormat)
    {
        if(!RMUtils.equals(aText.getFont(), _text.getFont())) return false;
        if(aText.getAlignmentX()!=_text.getAlignmentX()) return false;
        if(aText.getAlignmentY()!=_text.getAlignmentY()) return false;
        if(!RMUtils.equals(aText.getFill(), _text.getFill())) return false;
        if(!RMUtils.equals(aText.getStroke(), _text.getStroke())) return false;
        if(!RMUtils.equals(aText.getTextColor(), _text.getTextColor())) return false;
        if(!RMUtils.equals(aFormat, _format)) return false;
        return true;
    }

    /**
     * Returns a shared HSSFCellStyle for a given font, alignment and format string.
     */
    private HSSFCellStyle getHSSFCellStyle()
    {
        // If style already set, just return
        if(_style!=null)
            return _style;

        // Create a new HSSFCellStyle, configured with text wrapping by default
        _style = _workbook.createCellStyle();
        _style.setWrapText(true);
    
        // If font or text color is provided, set style font
        if(_text.getFont()!=null || _text.getTextColor()!=null)
            _style.setFont(getWorkbookFont(_text.getFont(), _text.getTextColor()));
        
        // Set style horizontal alignment
        switch(_text.getAlignmentX()) {
            case Left: _style.setAlignment(HSSFCellStyle.ALIGN_LEFT); break;
            case Center: _style.setAlignment(HSSFCellStyle.ALIGN_CENTER); break;
            case Right: _style.setAlignment(HSSFCellStyle.ALIGN_RIGHT); break;
        }
        
        // Set vertical alignment
        switch(_text.getAlignmentY()) {
            case Top: _style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP); break;
            case Bottom: _style.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM); break;
            case Middle: _style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER); break;
        }
        
        // If format is provided, set style format
        if(_format!=null) {
            
            // Get format index, and if valid, set it
            short formatIndex = _workbook.createDataFormat().getFormat(_format);
            if(formatIndex>=0)
                _style.setDataFormat(formatIndex);
        }
        
        // If there's a background fill and it's not solid white, set it
        if(_text.getFill() != null) {
            RMColor color = _text.getFill().getColor();
            if(!color.equals(RMColor.white)) {
                _style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                _style.setFillForegroundColor(getWorkbookColorIndex(color));
            }
        }
        
        // If text has stroke, configure stroke info
        if(_text.getStroke()!=null) {
            short color = getWorkbookColorIndex(_text.getStroke().getColor());
            _style.setTopBorderColor(color);
            _style.setLeftBorderColor(color);
            _style.setRightBorderColor(color);
            _style.setBottomBorderColor(color);
            _style.setBorderTop(HSSFCellStyle.BORDER_THIN);
            _style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            _style.setBorderRight(HSSFCellStyle.BORDER_THIN);
            _style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        }

        // Return the new HSSFCellStyle
        return _style;
    }
}

/**
 * An inner class to map an RMFont/RMColor pair to a unique HSSFont.
 */
class WorkbookFont {

    // base font
    RMFont    _font;
    
    // color
    RMColor   _color;
    
    // The HSSFFont
    HSSFFont  _hssfFont;
    
    /** Creates a new workbook font. */
    public WorkbookFont(RMFont aFont, RMColor aColor)  { _font = aFont; _color = aColor; }
    
    /** Standard equals implementation. */
    public boolean isMatch(RMFont aFont, RMColor aColor)
    {
        return RMUtils.equals(aFont, _font) && RMUtils.equals(aColor, _color);
    }
    
    /**
     * Returns the HSSFFont for this workbook font.
     */
    public HSSFFont getHSSFFont()
    {
        // If font already set, just return
        if(_hssfFont!=null)
            return _hssfFont;
        
        // Create a new workbook font
        _hssfFont = _workbook.createFont();
        
        // If font provided, set properties, otherwise use defaults.
        if(_font != null) {
            
            // Set font name
            _hssfFont.setFontName(_font.getFamilyEnglish());
        
            // Set whether is bold
            if(_font.isBold())
                _hssfFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        
            // Set whether is italic
            if(_font.isItalic())
                _hssfFont.setItalic(true);
        
            // Set font height
            _hssfFont.setFontHeightInPoints((short)Math.round(_font.getSize()));
        }
        
        // Set font color
        if(_color != null)
            _hssfFont.setColor(getWorkbookColorIndex(_color));
        
        // Return new HSSFFont
        return _hssfFont;
    }
}

/**
 * The custom palette crap doesn't work (http://issues.apache.org/bugzilla/show_bug.cgi?id=24519)
 * So this does a brute-force lookup through the fixed palette.  It just finds the closest color in Lab space.
 */
public short getWorkbookColorIndex(RMColor c)
{
    Map cols = HSSFColor.getIndexHash();
    float minDistance = Float.MAX_VALUE;
    float target_lab[] = c.toLab();
    short cindex = 0;

    // Iterate over HSSFColors
    for(Map.Entry ent : (Set <Map.Entry>)cols.entrySet()) {
        HSSFColor xc = (HSSFColor)ent.getValue();
        short rgb[] = xc.getTriplet();
        float lab[] = RMColor.rgbToLab(((float)rgb[0])/255, ((float)rgb[1])/255, ((float)rgb[2])/255);
         
        // Get squared distance
        float squaredDistance = (target_lab[0]-lab[0])*(target_lab[0]-lab[0]) +
           (target_lab[1]-lab[1])*(target_lab[1]-lab[1]) + (target_lab[2]-lab[2])*(target_lab[2]-lab[2]);
        
        // If less than min distance, get color index and reset min distance
        if(squaredDistance < minDistance) {
            minDistance = squaredDistance;
            cindex = xc.getIndex();
        }
    }
    
    // Return 
    return cindex;
}

/**
 * Returns an Excel date format string for the given SimpleDateFormat string.
 */
private String getExcelDatePattern(String javaPattern)
{
    // Rather than try to translate all possible format patterns, this is just a list of the ones in the
    // RMStudio formatter panel and one or two others.
    String rmtoexcelmap[][] = {
            {"EEEE, MMMM d, yyyy", "dddd\", \"mmmm\" \"d\", \"yyyy"},
            {"MMMM d, yyyy", "mmmm\" \"d\", \"yyyy"},
            {"d MMMM yyyy", "d\\ mmmm\\ yyyy"},
            {"MM/dd/yy", "mm\\/dd\\/yy"},
            {"MM/dd/yyyy", "mm\\/dd\\/yyyy"},
            {"MMM dd, yyyy", "mmm\\ dd\\,\\ yyyy"},
            {"dd MMM", "dd\\ mmm"},
            {"dd-MMM", "dd\\-mmm"},
            {"dd MMM yyyy", "dd\\ mmm\\ yyyy"},
            {"dd-MMM-yyyy", "dd\\-mmm\\-yyyy"},
            {"HH:mm:ss a zzzz", "hh\\:mm\\:ss\\ am/pm"}, // skipping timezone
            {"hh:mm a", "hh\\:mm\\ am/pm"},
            {"EE", "ddd"}
    };
    
    // Note that toLowerCase() might do a half-assed job. Plain characters (spaces, commas, etc) would have
    // to be escaped: E->d, and EE->ddd
    // There's a conflict over mm used as both month & minutes no timezones: a ->  am/pm
    for(int i=0; i<rmtoexcelmap.length; ++i) 
        if(javaPattern.equals(rmtoexcelmap[i][0]))
            return rmtoexcelmap[i][1];

    // Since format not found, see if we should print warning (only first time)
    if(!_foundErrors.contains(javaPattern)) {
        _foundErrors.add(javaPattern);
        System.err.println("RMExcel: Can't convert date format " + javaPattern + " to Excel");
    }
    
    // Since conversion failed, return basic month/day/year
    return "mm\\/dd\\/yyyy";
}

// A map to hold encountered errors
private static Set  _foundErrors = new HashSet();

}