package com.reportmill.apptools;
import com.reportmill.shape.RMCrossTabDivider;
import javax.swing.*;

/**
 * Provides ReportMill UI editing for CellDivider shape.
 */
public class RMCrossTabDividerTool <T extends RMCrossTabDivider> extends RMTool <T> {

/**
 * Override to return empty panel.
 */
protected JComponent createUI()
{
    JLabel label = new JLabel("CrossTab Divider"); label.setHorizontalAlignment(SwingConstants.CENTER);
    return label;
}

/**
 * Overrides tool method to indicate that cell dividers have no handles.
 */
public int getHandleCount(T aShape)  { return 0; }

}
