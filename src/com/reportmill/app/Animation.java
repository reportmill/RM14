package com.reportmill.app;
import com.reportmill.base.*;
import com.reportmill.graphics.*;
import com.reportmill.shape.*;
import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import snap.swing.*;

/**
 * This class provides Swing UI editing for shape animation.
 */
public class Animation extends SwingOwner implements RMAnimator.Listener {
    
    // The key frames JList
    JList             _keyFramesList;
    
    // The changes JList
    JList             _changesList;
    
    // Format for frames list
    DecimalFormat     _timeFormat = new DecimalFormat("0.000");
    
    // Whether to allow update to time slider/text
    boolean           _update = true;
    
    // The list of key frames
    List <Integer>    _keyFrames;
    
    // The list of key frames for selected shapes
    List <Integer>    _selectedShapesKeyFrames;
    
    // The list of changes (keys) for key frame and selected shape
    List <String>     _changes = new Vector();
    
    // The freeze frame button
    JButton           _freezeFrameButton;
    
    // A button to bring up special ui for interpolators
    JButton           _interpolatorUIButton;

/**
 * Initialize UI for this inspector.
 */
protected void initUI()
{
    // Get KeyFrameList and customize
    _keyFramesList = getNode("KeyFrameList", JList.class);
    _keyFramesList.setCellRenderer(new AnimCellRenderer());
    _keyFramesList.setModel(new KeyFrameListModel());
    _keyFramesList.setLayout(null);
    _keyFramesList.setFixedCellHeight(15);
    
    // Get ChangesList and customize
    _changesList = getNode("ChangesList", JList.class); 
    _changesList.setModel(new ChangesListModel());
    _changesList.setFixedCellHeight(15);
    
    // Customize FreezeFrame Button
    _freezeFrameButton = new JButton("Freeze");
    _freezeFrameButton.setFont(RMAWTUtils.Helvetica10);
    _freezeFrameButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    _freezeFrameButton.setHorizontalAlignment(SwingConstants.LEFT);
    IconUtils.setButtonIconAsLabelIcon(_freezeFrameButton);
    initUI(_freezeFrameButton);
    
    // Get InterpolatorUIButton
    _interpolatorUIButton = getNode("InterpolatorUIButton", JButton.class);
}

/**
 * Populates the combobox with all the interpolator names if necessary.
 */
public void updateInterpolatorCombobox()
{
    JComboBox box = getNode("InterpolationComboBox", JComboBox.class);
    int nInterpolators = RMInterpolator.getInterpolatorCount();
    
    if (nInterpolators != box.getItemCount()) {
        // Get interpolation names
        String interpolations[] = new String[nInterpolators];
        for(int i=0; i<nInterpolators; i++)
            interpolations[i] = RMInterpolator.getInterpolator(i).getName();
        
        // Set in InterpolationComboBox
        box.setModel(new DefaultComboBoxModel(interpolations));
    }
}

/**
 * Updates the UI panel controls from the current selection.
 */
public void resetUI()
{
    // Get the main editor
    RMEditor editor = RMEditor.getMainEditor();
    
    // Get the current animator
    RMAnimator animator = getAnimator(false);
    
    // If animator is null, replace with default instance
    if(animator==null) {
        animator = new RMAnimator();
        animator.setOwner(editor.getSuperSelectedParentShape());
    }
    
    // If animator is running, just return
    if(animator.isRunning()) return;
    
    // Get the currently selected shape and shapes
    RMShape shape = editor.getSelectedOrSuperSelectedShape();
    List shapes = editor.getSelectedOrSuperSelectedShapes();
    
    // Update TimeText, TimeSlider and TimeSlider Maximum
    setNodeValue("TimeText", _timeFormat.format(animator.getTimeSeconds()));
    setNodeValue("TimeSlider", Math.round(animator.getTimeSeconds()*animator.getFrameRate()));
    getNode("TimeSlider", JSlider.class).setMaximum(Math.round(animator.getMaxTimeSeconds()*animator.getFrameRate()));
    
    // Update LoopCheckBox
    setNodeValue("LoopCheckBox", animator.getLoops());
    
    // Update FrameRateText
    setNodeValue("FrameRateText", animator.getFrameRate());
    
    // Update MaxTimeText
    setNodeValue("MaxTimeText", animator.getMaxTimeSeconds());
    
    // If there wasn't really an animator, just return
    //if(getAnimator(false)==null) return;
    
    // Add this inspector as listener
    animator.addAnimatorListener(this);
    
    // Get animator key frames
    _keyFrames = animator.getKeyFrameTimes();
    
    // Get selected shapes key frames
    _selectedShapesKeyFrames = shape.isRoot()? null : animator.getKeyFrameTimes(shapes, true);
    
    // Reset model to update key frame list
    _keyFramesList.setModel(new KeyFrameListModel());

    // Get animator selected frame indices (start and end)
    int frameStartIndex = _keyFrames.indexOf(animator.getScopeTime());
    int frameEndIndex = _keyFrames.indexOf(animator.getTime());
    
    // If animator selected frames are adjacent, just select animator time
    if(frameEndIndex==frameStartIndex+1)
        frameStartIndex++;
        
    // If _timesBrowser and animator still don't match, reset keyFrameList
    _keyFramesList.setSelectionInterval(frameStartIndex, frameEndIndex);

    // If can freeze frame, configure and add freezeFrameButton
    if(animator.canFreezeFrame()) {
        int time = (Integer)_keyFramesList.getSelectedValue();
        String text = _timeFormat.format(time/1000f);
        _freezeFrameButton.setText(text);
        _freezeFrameButton.setIcon(null);
        IconUtils.setButtonIconAsLabelIcon(_freezeFrameButton);
        if(_freezeFrameButton.getParent()==null)
            _keyFramesList.add(_freezeFrameButton);
        _freezeFrameButton.setBounds(0, _keyFramesList.getSelectedIndex()*15, _keyFramesList.getWidth(), 15);
    }
    
    // If can't freeze frame, remove freeze frame button
    else _keyFramesList.remove(_freezeFrameButton);
    
    // Clear list of changes
    _changes.clear();
    
    // Get currently selected shape timeline and key frame
    RMTimeline timeline = shape.getTimeline();
    RMKeyFrame keyFrame = timeline!=null? timeline.getKeyFrameAt(animator.getTime()) : null;
    
    // If frame is empty and can be frozen, set changes list to say "Hit frame button to freeze"
    if(animator.canFreezeFrame())
        _changes.add("Hit Frame Button to Freeze Frame");
    
    // If frame isn't empty, set changes to attributes at time
    else if(keyFrame!=null)
        for(RMKeyValue kval : keyFrame.getKeyValues())
            _changes.add(kval.getKey());
    
    // Update ChangeList model
    ((ChangesListModel)_changesList.getModel()).didChange();
        
    // Get selected change
    String change = getNodeStringValue(_changesList);
    
    // Get key/value for change
    RMKeyValue keyValue = keyFrame!=null && change!=null? keyFrame.getKeyValue(shape, change) : null;
    
    // Get interpolator for change
    RMInterpolator interpolator = keyValue!=null? keyValue.getInterpolator() : null;
    
    // Update InterpolationComboBox (and enabled status)
    updateInterpolatorCombobox();
    setNodeEnabled("InterpolationComboBox", keyValue!=null);
    setNodeValue("InterpolationComboBox", interpolator!=null? interpolator.getName() : "Linear");
    
    // Update InterpolatorUIButton Visible property
    getNode("InterpolatorUIButton", JComponent.class).setVisible(interpolator!=null && !interpolator.isShared());

    // Update HelpText - one frame selected
    if(frameEndIndex-frameStartIndex>1) {
        String ts = getKeyFrameFormatted(frameStartIndex);
        setNodeValue("HelpText", "All changes are made relative to start of selected range (" + ts + ").");
    }
    
    // Update HelpText - multiple frames selected
    else if(frameStartIndex>0 && frameStartIndex<=getKeyFrameCount()) {
        String ts = getKeyFrameFormatted(frameStartIndex - 1);
        setNodeValue("HelpText", "Select multiple key frames to make changes across a range.\n" +
            "All changes are made relative to previous key frame (" + ts + ").");
    }
    
    // Update HelpText - all frames selected
    else setNodeValue("HelpText", "Select multiple key frames to make changes across a range.\n" +
        "All changes are made relative to previous key frame.");
}

/**
 * Responds to changes from UI controls.
 */
public void respondUI(SwingEvent anEvent)
{
    // Get the current animator (just return if null) - if running, stop it
    RMAnimator animator = getAnimator(true); if(animator==null) return;
    if(animator.isRunning())
        animator.stop();
    
    // Handle TimeSlider or TimeTextField
    if(anEvent.equals("TimeSlider"))
        setTimeSeconds(anEvent.getFloatValue()/animator.getFrameRate());

    // Handle TimeTextField
    if(anEvent.equals("TimeText"))
        setTimeSeconds(anEvent.getFloatValue());

    // Handle PlayButton
    if(anEvent.equals("PlayButton")) {
        animator.setResetTimeOnStop(true);
        animator.play();
    }
    
    // Handle StopButton
    if(anEvent.equals("StopButton"))
        animator.stop();
    
    // Handle StepButton
    if(anEvent.equals("StepButton"))
        setTime(animator.getTime() + animator.getInterval());
    
    // Handle BackButton
    if(anEvent.equals("BackButton"))
        setTime(animator.getTime() - animator.getInterval());
    
    // Handle LoopCheckBox
    if(anEvent.equals("LoopCheckBox"))
        animator.setLoops(anEvent.getBoolValue());

    // Handle FrameRateText
    if(anEvent.equals("FrameRateText"))
        animator.setFrameRate(anEvent.getFloatValue());

    // Handle MaxTimeText
    if(anEvent.equals("MaxTimeText"))
        animator.setMaxTimeSeconds(anEvent.getFloatValue());

    // Handle KeyFrameList
    if(anEvent.getTarget()==_keyFramesList) {
        int index = _keyFramesList.getMaxSelectionIndex();
        if(index>=0 && index<getKeyFrameCount()) {
            if(index!=_keyFramesList.getMinSelectionIndex())
                setTimeForScopedKeyFrame(getKeyFrame(index), getKeyFrame(_keyFramesList.getMinSelectionIndex()));
            else setTime(getKeyFrame(index));
        }
    }

    // Handle freezeFrameButton
    if(anEvent.getTarget()==_freezeFrameButton)
        animator.addFreezeFrame();

    // Handle ShiftKeyFramesMenuItem
    if(anEvent.equals("ShiftFramesMenuItem")) {
        
        // Run option panel
        int time = animator.getTime();
        String msg = "Shift key frames from time " + time/1000f + " to end by time:";
        DialogBox dbox = new DialogBox("Shift Key Frames"); dbox.setQuestionMessage(msg);
        String shiftString = dbox.showInputDialog(getUI(), "0.0");
        int shift = shiftString==null? 0 : Math.round(RMStringUtils.floatValue(shiftString)*1000);

        // Shift frames
        if(shift!=0)
            animator.shiftFrames(time, shift);
    }

    // Handle ScaleFramesMenuItem
    if(anEvent.equals("ScaleFramesMenuItem")) {
        
        // Run option panel
        int maxTime = animator.getMaxTime();
        String msg = "Scale key frames from current frame to new max time";
        DialogBox dbox = new DialogBox("Scale Key Frames"); dbox.setQuestionMessage(msg);
        String newMaxTimeString = dbox.showInputDialog(getUI(), Float.toString(maxTime/1000f));
        int newMaxTime = newMaxTimeString==null? maxTime : Math.round(RMStringUtils.floatValue(newMaxTimeString)*1000);

        // Scale frames
        if(newMaxTime!=maxTime)
            animator.scaleFrames(animator.getTime(), newMaxTime);
    }
    
    // Handle DeleteButton
    if(anEvent.equals("DeleteButton"))
        delete();
    
    // Handle interpolationCombo
    if(anEvent.equals("InterpolationComboBox")) {
        Object changes[] = getSelectedValues(_changesList);
        RMShape shape = RMEditor.getMainEditor().getSelectedOrSuperSelectedShape();
        RMTimeline timeline = shape.getTimeline();
        String interpName = anEvent.getStringValue();
        for(Object chng : changes) { String change = (String)chng;
            RMKeyFrame keyFrame = timeline!=null? timeline.getKeyFrameAt(animator.getTime()) : null;
            RMKeyValue keyValue = keyFrame!=null? keyFrame.getKeyValue(shape, change) : null;
            if(keyValue!=null) {
                RMInterpolator interpolator = RMInterpolator.getNewInterpolator(interpName);
                keyValue.setInterpolator(interpolator); // Should derive instead?
            }
        }
    }
    
    // Handle InterpolatorUIButton - selected changes may have different interpolators, in which case this
    // button should really be disabled (or removed) For now, just grab the first one.  This is a BUG
    /*if(anEvent.equals("InterpolatorUIButton")) {
        Object changes[] = getSelectedValues(_changesList);
        RMShape shape = RMEditor.getMainEditor().getSelectedOrSuperSelectedShape();
        RMTimeline timeline = shape.getTimeline();
        String change = (String)changes[0];
        RMKeyFrame keyFrame = timeline!=null? timeline.getKeyFrameAt(animator.getTime()) : null;
        RMKeyValue keyValue = keyFrame!=null? keyFrame.getKeyValue(shape, change) : null;
        if(keyValue!=null) {
            RMInterpolator interpolation = keyValue.getInterpolator();
            //SwingPanel interpUI = interpolation.getUI();
            //if(interpUI != null) interpUI.setWindowVisible(true);
        }
    }*/
}

/**
 * Returns the selected values for a JList.
 */
private Object[] getSelectedValues(JList aList)
{
    List values = aList.getSelectedValuesList();
    return values.toArray();
}

/**
 * Returns the current animator from main editor super selected shape.
 */
private RMAnimator getAnimator(boolean create)
{
    RMEditor editor = RMEditor.getMainEditor();
    return editor!=null? editor.getSuperSelectedShape().getChildAnimator(create) : null;
}

/**
 * Returns the number of key frames for the current animator.
 */
private int getKeyFrameCount()  { return _keyFrames==null? 0 : _keyFrames.size(); }

/**
 * Returns the float time value of the key frame at the given index.
 */
private Integer getKeyFrame(int anIndex)  { return anIndex>=0? _keyFrames.get(anIndex) : null; }

/**
 * Returns the float time value of the key frame at the given index as a formatted string.
 */
private String getKeyFrameFormatted(int anIndex)  { return _timeFormat.format(getKeyFrame(anIndex)); }

/**
 * Sets the time of the current animator to the given time.
 */
public void setTime(int aTime)  { setTimeForScopedKeyFrame(aTime, null); }

/**
 * Sets the time of the current animator to the given time.
 */
public void setTimeForScopedKeyFrame(int aTime, Integer aScope)
{
    RMAnimator animator = getAnimator(true);

    RMEditor.getMainEditor().undoerSetUndoTitle("Time Change");

    // Perform time change
    _update = false;
    animator.setScopeTime(aScope);
    animator.setTime(aTime);
    _update = true;
    
    if(aScope==null) {
        setNodeSelectedIndex(_keyFramesList, -1);
        setNodeValue(_keyFramesList, animator.getTime());
        setNodeSelectedIndex(_changesList, -1);
    }
}

/**
 * Sets the time of the current animator to the given time.
 */
public void setTimeSeconds(float aTime)  { setTime(Math.round(aTime*1000)); }

/**
 * Handles delete of key frame(s) or change(s).
 */
public void delete()
{
    // Get editor
    RMEditor editor = RMEditor.getMainEditor();
    
    // Get animator (just return if null)
    RMAnimator animator = getAnimator(false); if(animator==null) return;
    
    // Get list, helper and selected range
    int keyFrameStart = _keyFramesList.getMinSelectionIndex();
    int keyFrameEnd = _keyFramesList.getMaxSelectionIndex();
    int time = getKeyFrame(keyFrameStart);
    int time2 = getKeyFrame(keyFrameEnd);
    
    // If trying to delete frame 0, just beep and return
    if(keyFrameEnd==0) { Toolkit.getDefaultToolkit().beep(); return; }
    
    // If changes are selected, just delete them
    if(_changesList.getSelectedIndex()>=0) {
        
        // Get selected items from changes list
        Object changes[] = getSelectedValues(_changesList);

        // Iterate over selected shapes
        for(int i=0, iMax=editor.getSelectedOrSuperSelectedShapeCount(); i<iMax; i++) {
            
            // Get current loop selected shape
            RMShape shape = editor.getSelectedOrSuperSelectedShape(i);
            
            // Get selected shape's timeline (just continue if null)
            RMTimeline timeline = shape.getTimeline(); if(timeline==null) continue;
            
            // Iterate over changes list and remove from timeline for time range
            for(int j=0; j<changes.length; j++) { String change = (String)changes[j]; if(change==null) continue;
                timeline.removeKeyFrameKeyValues(shape, change, time, time2, true); }
            
            // Reset time
            shape.getUndoer().disable();
            int t = timeline.getTime(); timeline.setTime(0); timeline.setTime(t);
            shape.getUndoer().enable();
        }
    }

    // If no changes are selected, prompt user to delete all changes for key frame
    else {
        
        // Run panel for whether to delete changes
        String msg = "Do you really want to delete all changes\n" + "associated with this key frame(s)?";
        DialogBox dbox = new DialogBox("Delete Key Frame"); dbox.setWarningMessage(msg);
        dbox.setOptions("Delete", "Cancel");
        int response = dbox.showOptionDialog(getUI(), "Delete");
        
        // If approved, remove frames and reset main editor pane
        if(response==0)
            animator.removeFramesBetweenTimes(time, time2, true);
    }
}

/**
 * Inner class to manage keyFrameList.
 */
private class KeyFrameListModel extends AbstractListModel {
    public Object getElementAt(int index)  { return getKeyFrame(index); }
    public int getSize()  { return getKeyFrameCount(); }
}

/**
 * Inner class to manage changesList.
 */
private class ChangesListModel extends AbstractListModel {
    public Object getElementAt(int index)  { return RMListUtils.get(_changes, index); }
    public int getSize()  { return _changes.size(); }
    public void didChange()  { fireContentsChanged(this, 0, 999); }
}

/**
 * Inner class to format frame values and de-emphasize keyFrames for unselected objects (make gray).
 */
private class AnimCellRenderer extends JLabel implements ListCellRenderer {
    public AnimCellRenderer() { setOpaque(true); }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc)
    {
        int time = ((Integer)value); String string = _timeFormat.format(time/1000f);
        setText(string); setFont(list.getFont()); setEnabled(list.isEnabled());
        setForeground(isSel? list.getSelectionForeground() : list.getForeground());
        setBackground(isSel? list.getSelectionBackground() : list.getBackground());

        // If not relevant to selected shape make brighter
        if(!isSel && !RMListUtils.contains(_selectedShapesKeyFrames, value))
            setForeground(Color.gray);
            
        return this;
    }
}

/** Animator Listener method. */
public void animatorStarted(RMAnimator anAnimator)  { }
public void animatorStopped(RMAnimator anAnimator)  { }

/**
 * Animator Listener method : updates time slider and time text when animator has been updated.
 */
public void animatorUpdated(RMAnimator anAnimator)
{
    if(_update && anAnimator==getAnimator(true)) {
        setNodeValue("TimeSlider", Math.round(anAnimator.getTimeSeconds()*anAnimator.getFrameRate()));
        setNodeValue("TimeText", _timeFormat.format(anAnimator.getTimeSeconds()));
    }
}

/**
 * Returns the name for this inspector.
 */
public String getWindowTitle()  { return "Animation"; }

}