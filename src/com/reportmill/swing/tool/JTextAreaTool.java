package com.reportmill.swing.tool;
import com.reportmill.swing.shape.JTextAreaShape;

/**
 * Provides UI editing for JTextAreaShape.
 */
public class JTextAreaTool extends JTextComponentTool {
    
/**
 * Returns the shape class that this tool is responsible for.
 */
public Class getShapeClass()  { return JTextAreaShape.class; }

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "TextArea Inspector"; }

/**
 * Overrides RMShape implementation to update text alignment.
 */
/*public void didBecomeSuperSelectedShapeInEditor(RMShape aShape, RMEditor anEditor)
{
    super.didBecomeSuperSelectedShapeInEditor(aShape, anEditor);
    JTextAreaShape textArea = (JTextAreaShape)aShape;
    textArea.getTextShape().setAlignmentX(com.reportmill.base.RMTypes.AlignX.Left);
    textArea.getTextShape().setAlignmentY(com.reportmill.base.RMTypes.AlignY.Top);
}*/

}