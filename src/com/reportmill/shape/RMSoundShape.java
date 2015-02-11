package com.reportmill.shape;
import com.reportmill.graphics.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.util.EventListener;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.SwingUtilities;
import snap.util.*;

/**
 * This class represents a sound in a document.
 */
public class RMSoundShape extends RMShape {
    
    // The actual sound data
    RMSoundData    _soundData;
    
    // A key to optionally try to get sound data from objects during RPG clone
    String         _key;
    
    // The delay before the sound starts
    float          _delay;
    
    // Loop count
    short          _loopCount = 1;
    
    // Whether to allow overlap with itself?
    boolean        _overlap;

    // Whether the sound is currently playing
    boolean        _playing = true;
    
    // The name of the sound (for tooltips)
    String         _name;
    
    // The sound clip
    Clip           _clip;
    
    // Get sound image
    static Image   _soundImage;
    
   /**
    * A listener interface that can be implemented by anyone that cares.
    */
    public interface RMSoundListener extends EventListener {
      void soundStopped(RMSoundShape source);
    }
    
/**
 * Creates an empty sound shape.
 */
public RMSoundShape()  { this(null); }

/**
 * Creates a sound shape from a given source (File, String path, InputStream, byte array, etc.).
 */
public RMSoundShape(Object aSource)
{
    // Set bogus width/height
    _width = _height = 40;
    
    // Load sound data from ource
    setSource(aSource);
}

/**
 * For the moment, this has to be here for binding.
 */
public Object getSource()  { return null; }

/**
 * Sets the source of the sound (File, String path, InputStream, byte array, etc.).
 */
public void setSource(Object aSource)
{
    boolean wasPlaying = getPlaying();
    
    // Load sound data from source
    if(aSource!=null) {
        // stop the current sound if it's playing
        if (wasPlaying) {
            setPlaying(false);
            // Is clip.stop() asynchronous?  If so then we should add a listener to restart the sound
        }
        setSoundData(RMSoundData.getSoundData(aSource));
        if(aSource instanceof String)
            _name = StringUtils.getPathFileName((String)aSource);
        // call setPlaying() so new sound will start if its supposed to be playing
        setPlaying(wasPlaying);
    }
    else _soundData = null;
}

/**
 * Returns the sound data for this sound shape.
 */
public RMSoundData getSoundData()  { return _soundData; }

/**
 * Sets the sound data for this sound shape.
 */
public void setSoundData(RMSoundData aSoundData)
{
    // If there's already a clip, close it if we're resetting the sound data.
    if ((_clip != null) && !SnapUtils.equals(_soundData, aSoundData)) {
        _clip.close();
        _clip = null;
    }
    _soundData = aSoundData;
}

/**
 * Returns the RPG key for this sound shape.
 */
public String getKey()  { return _key; }

/**
 * Sets the RPG key for this sound shape.
 */
public void setKey(String aKey)  { _key = aKey; }

/**
 * Returns the delay after which this sound is supposed to start to play.
 */
public float getDelay()  { return _delay; }

/**
 * Sets the delay after which this sound is supposed to start to play.
 */
public void setDelay(float aValue)  { _delay = aValue; }

/**
 * Returns the number of loops that this sound should play before stopping.
 */
public int getLoopCount()  { return _loopCount; }

/**
 * Returns the number of loops that this sound should play before stopping.
 */
public void setLoopCount(int aValue)  { _loopCount = (short)aValue; }

/**
 * Returns whether this sound should overlap other sounds.
 */
public boolean getOverlap()  { return _overlap; }

/**
 * Returns whether this sound should overlap other sounds.
 */
public void setOverlap(boolean aValue)  { _overlap = aValue; }

/**
 * Returns the name of the sound.
 */
public String getSoundName()  { return _name; }

/**
 * Returns whether this sound is currently playing.
 */
public boolean getPlaying()  { return _playing; }

/**
 * Sets whether or not this sound is currently playing.
 */
public void setPlaying(boolean aValue)
{
    // Set playing
    _playing = aValue;
    
    // If the sound is in an Editor, don't start it up
    if (!isViewing())
        return;
    
    // If we've been set to not playing, then we don't care if the clip is null
    if(!getPlaying() && (getClip()==null))
        return;
    
     // If clip is not available or already in requested state, just return
    if(getClip(true)==null || getClip().isRunning()==getPlaying())
        return;
    
    // If asked to start clip, start it
    if(getPlaying()) {
        getClip().setFramePosition(0);
        // Set loop count, which starts playback
        getClip().loop(getLoopCount()==0? 0 : getLoopCount()-1);

    }
    
    // If asked to stop clip, stop it
    else getClip().stop();
}

/**
 * Returns the clip.
 */
public Clip getClip()  { return getClip(false); }

/**
 * Returns the clip, creating it if requested.
 */
public Clip getClip(boolean create)
{
    // If clip is loaded or we're not asked to load, just return clip
    if(_clip!=null || !create)
        return _clip;
    
    if (getSoundData()==null)
        return null;
    
    // Create audio input stream
    AudioInputStream audioStream;
    try { audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(getSoundData().getBytes())); }
    catch(Exception e) { e.printStackTrace(); return null; }
    
    // Get audio format
    AudioFormat audioFormat = audioStream.getFormat();
    
    // Create line info
    DataLine.Info lineInfo = new DataLine.Info(Clip.class, audioFormat);
    
    // If not supported, try to get deoded format
    if(!AudioSystem.isLineSupported(lineInfo)) {
        
        // Complain
        System.out.println("Converting from " + audioFormat.getEncoding() + " to " + AudioFormat.Encoding.PCM_SIGNED);
        
        // Get decoded format
        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
            audioFormat.getChannels(), audioFormat.getChannels()*2, audioFormat.getSampleRate(), false);
        
        // Get decoded stream
        audioStream = AudioSystem.getAudioInputStream(audioFormat, audioStream);
        
        // Get decoded line info
        lineInfo = new DataLine.Info(Clip.class, audioFormat);
    }
    
    // Create clip
    try { _clip = (Clip)AudioSystem.getLine(lineInfo); }
    catch(Exception e) { e.printStackTrace(); return null; }
    
    // Add a line listener to send events to somebody when the sound finishes
    _clip.addLineListener(new LineListener() {
        public void update(LineEvent event)
        {
            // this could probably be ==
            if (event.getType().equals(LineEvent.Type.STOP)) {
                // notify rm listeners on the main thread
                if (getListenerCount(RMSoundListener.class)>0) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            for(int i=0, iMax=getListenerCount(RMSoundListener.class); i<iMax; i++) {
                                RMSoundListener listener = getListener(RMSoundListener.class, i);
                                listener.soundStopped(RMSoundShape.this);
                            }
                        }
                    });
                }
                // flush the clip's resources
                _clip.flush();
            }
        }
    });
    
    // Have clip open stream
    try { _clip.open(audioStream); }
    catch(Exception e) { e.printStackTrace(); return null; }
        
    // Return clip
    return _clip;
}

/**
 * Overrides shape implementation to start sound if it should be playing.
 */
protected void shapeShown()  { setPlaying(getPlaying()); }

/**
 * Overrides shape implementation to stop sound.
 */
protected void shapeHidden()
{
    if(getClip()!=null && getClip().isRunning())
        getClip().stop();
}

/**
 * Handles painting a sound shape.
 */
public void paintShape(RMShapePainter aPntr)
{
    // If not editing, just return
    if(!aPntr.isEditing()) return;
    
    // Do normal shape drawing
    super.paintShape(aPntr);
    
    // Get sound image
    if(_soundImage==null)
        try { _soundImage = ImageIO.read(getClass().getResource("/com/reportmill/apptools/RMSoundShape.png")); }
        catch(Exception e) { e.printStackTrace(); }

    // Draw sound image
    if(_soundImage!=null)
        aPntr.drawImage(_soundImage, 0, 0, (int)getWidth(), (int)getHeight());
}

/**
 * Adds the property names for this shape.
 */
protected void addPropNames() { addPropNames("Source", "Playing"); super.addPropNames(); }

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic shape attributes and reset element name
    XMLElement e = super.toXML(anArchiver); e.setName("sound-shape");
    
    // Archive Key, Delay, LoopCount
    if(_key!=null && _key.length()>0) e.add("key", _key);
    if(_delay!=0) e.add("delay", _delay);
    if(_loopCount!=1) e.add("loop", _loopCount);
        
    // Archive SoundData
    if(_soundData!=null && _soundData.getBytes()!=null) {
        e.add("resource", _soundData.getName());
        anArchiver.addResource(_soundData.getBytes(), _soundData.getName());
    }
    
    // Archive SoundName and Playing
    if(getSoundName() != null && getSoundName().length()>0) e.add("sound-name", getSoundName());
    e.add("playing", getPlaying());
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive Key, Delay, LoopCount
    setKey(anElement.getAttributeValue("key"));
    setDelay(anElement.getAttributeFloatValue("delay"));
    setLoopCount(anElement.getAttributeIntValue("loop", 1));
    
    // Unarchive SoundData
    String resourceName = anElement.getAttributeValue("resource");
    if(resourceName!=null) {
        byte bytes[] = anArchiver.getResource(resourceName);
        _soundData = RMSoundData.getSoundData(bytes);
    }

    // Unarchive SoundName, Playing
    _name = anElement.getAttributeValue("sound-name");
    _playing = anElement.getAttributeBoolValue("playing");
    
    // Return this sound shape
    return this;
}

}