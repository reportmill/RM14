package snap.swing;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * An Ribs event queue subclass to capture isAltDown(), isMetaDown() and send Ribs.EventListener notifications.
 */
public class RibsEventQueue extends EventQueue {

    // Whether last input event had alt or menu accelerator down
    boolean                _isAltDown, _isMetaDown;
    
    // The last mouse down event if in dragging loop
    MouseEvent             _mousePressedEvent;
    
    // Whether to suppress current/next mouse loop
    boolean                _suppressMouseLoop;
    
    // A shared event queue
    public static RibsEventQueue  Shared = new RibsEventQueue();
    
/**
 * Installs event queue.
 */
public void install()
{
    EventQueue equeue = Toolkit.getDefaultToolkit().getSystemEventQueue();
    if(equeue!=this)
        equeue.push(this);
}

/**
 * An implementation of dispatch event to do useful Ribs things.
 */
protected void dispatchEvent(AWTEvent anEvent)
{
    // If event is InputEvent, capture IsAltDown and IsMetaDown
    if(anEvent instanceof InputEvent) { InputEvent inputEvent = (InputEvent)anEvent;
        _isAltDown = inputEvent.isAltDown();
        _isMetaDown = inputEvent.isMetaDown();
    }
    
    // If MouseEvent, track MouseDownSee if we should stifle focus transfer
    if(anEvent instanceof MouseEvent) { MouseEvent mouseEvent = (MouseEvent)anEvent;
        
        // Check for suppress on each mouse pressed
        if(anEvent.getID()==MouseEvent.MOUSE_PRESSED) {
            
            // Set MouseDownEvent and reset SuppressMouseLoop
            _mousePressedEvent = mouseEvent; _suppressMouseLoop = false;

            // Get component
            Component component = (Component)anEvent.getSource();
    
            // If component isn't focused, but claims to want focus, check for input verifier
            if(!component.hasFocus() && component.isFocusable()) {
    
                // Get current focus owner
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    
                // If focus owner is JComponent, check for input verifier
                if(focusOwner instanceof JComponent) {
                    
                    // Get jcomponent and  InputVerifier
                    JComponent jcomponent = (JComponent)focusOwner;
                    InputVerifier inputVerifier = jcomponent.getInputVerifier();
    
                    // If input verifier doesn't want to yield focus, set suppress mouse loop and beep
                    if(inputVerifier!=null && !inputVerifier.shouldYieldFocus(jcomponent)) {
                        _suppressMouseLoop = true;
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        }
        
        // If MOUSE_RELEASED, clear MouseDownEvent
        else if(mouseEvent.getID()==MouseEvent.MOUSE_RELEASED)
            _mousePressedEvent = null;
        
        // If suppress, just return
        if(_suppressMouseLoop) return;
    }

    // Do normal dispatch event with exception handler
    super.dispatchEvent(anEvent);
}

}