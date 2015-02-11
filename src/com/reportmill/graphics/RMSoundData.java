package com.reportmill.graphics;
import com.reportmill.base.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

/**
 * Manages sound data from sound file bytes.
 */
public class RMSoundData {

    // Where the sound data came from
    Object          _source;
    
    // The raw sound file bytes
    byte            _bytes[];

    // Bits per sample
    int             _bitsPerSample;    // 8 bit, 16 bit
    
    // Samples per second
    int             _samplesPerSecond; // Usually 5.5 khz, 11 khz, 22 khz or 44 khz
    
    // Channel count
    int             _channelCount;     // 1 mono, 2 stereo
    
    // Sample count
    int             _sampleCount;      // Number of samples in sound
    
    // Uncompressed samples
    byte            _sampleBytes[];

    // Constants for bit rate
    public static final int  RMBitRate5k = 0;
    public static final int  RMBitRate11k = 1;
    public static final int  RMBitRate22k = 2;
    public static final int  RMBitRate44k = 3;
    public static final int  RMBitRateUndefined = 999;
    
    public static Map _cache = new Hashtable();

/**
 * Clears out the cache
 */
public static void emptyCache()  { _cache.clear(); }

/**
 * Returns whether sound data can read given extension.
 */
public static boolean canRead(String anExt)
{
    // Get supported formats
    AudioFileFormat.Type types[] = AudioSystem.getAudioFileTypes();
    for(int i=0; i<types.length; i++) {
        if(types[i].getExtension().equalsIgnoreCase(anExt))
            return true;
    }
    
    // Bogus
    return "ogg".equalsIgnoreCase(anExt);
}

/**
 * Returns a sound data for a given source.
 */
public static RMSoundData getSoundData(Object aSource)
{
    if (aSource==null) 
        return null;
    
    // See if soundData is in _cache, if so, just return it
    RMSoundData soundData = (RMSoundData)_cache.get(aSource);
    if(soundData!=null)
        return soundData;
    
    // Create new sound data for source
    soundData = new RMSoundData(aSource);
    if(soundData.getBytes()==null)
        return null;
    
    // Iterate over sound datas, if equal to any in cache, return cache version
    for(Iterator <RMSoundData> i=_cache.values().iterator(); i.hasNext();) {
        RMSoundData sd = i.next();
        if(soundData.getBytes().equals(sd.getBytes()))
            return sd;
    }

    // Add sound data to cache
    _cache.put(aSource, soundData);
    
    // Return sound data
    return soundData;
}

/**
 * Creates a new sound data for given source.
 */
public RMSoundData(Object aSource)
{
    // Set source
    _source = aSource;
    
    // Set bytes
    _bytes = RMUtils.getBytes(aSource);
}

/**
 * Returns the name for this sound.
 */
public String getName()  { return "" + System.identityHashCode(this); }

/**
 * Returns the sound data bytes.
 */
public byte[] getBytes()  { return _bytes; }

/**
 * Returns the bits per sample.
 */
public int getBitsPerSample()  { readData(); return _bitsPerSample; }

/**
 * Returns the samples per second.
 */
public int getSamplesPerSecond()  { readData(); return _samplesPerSecond; }

/**
 * Returns the channel count.
 */
public int getChannelCount()  { readData(); return _channelCount; }

/**
 * Returns the sample count.
 */
public int getSampleCount()  { readData(); return _sampleCount; }

/**
 * Returns the sample bytes.
 */
public byte[] getSampleBytes()  { readData(); return _sampleBytes; }

/**
 * Returns the bit rate.
 */
public int bitRate()
{
    readData();
    switch(_samplesPerSecond/5000) {
        case 1: return RMBitRate5k;
        case 2: return RMBitRate11k;
        case 4: return RMBitRate22k;
        case 8: return RMBitRate44k;
    }

    return RMBitRateUndefined;
}

/**
 * Reads sound format info from sounds data bytes.
 */
public void readData()
{
    if(_sampleBytes!=null) return;
    
    // Create audio input stream
    AudioInputStream audioStream;
    try { audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(getBytes())); }
    catch(Exception e) { throw new Error(e); }
    
    // Get audio format
    AudioFormat audioFormat = audioStream.getFormat();
    
    // Create line info
    DataLine.Info lineInfo = new DataLine.Info(Clip.class, audioFormat);
    
    // If not supported, try to get deoded format
    if(!AudioSystem.isLineSupported(lineInfo)) {
        
        // Get decoded format
        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, audioFormat.getSampleRate(), 16,
            audioFormat.getChannels(), audioFormat.getChannels()*2, audioFormat.getSampleRate(), false);
        
        // Get decoded stream
        audioStream = AudioSystem.getAudioInputStream(audioFormat, audioStream);
    }
    
    _bitsPerSample = audioFormat.getSampleSizeInBits();
    
    _samplesPerSecond = (int)audioFormat.getSampleRate();
    
    _channelCount = audioFormat.getChannels();
    
    _sampleBytes = RMUtils.getBytes(audioStream);

    _sampleCount = _sampleBytes.length/audioFormat.getFrameSize();
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity
    if(anObj==this) return true;
    
    // Check class
    if(!(anObj instanceof RMSoundData)) return false;
    
    // Get other sound data
    RMSoundData other = (RMSoundData)anObj;
    
    // Check bytes
    if(!RMUtils.equals(other._bytes, _bytes)) return false;
    
    // Return true since all checks passed
    return true;
}

}