package top.dsbbs2.mp.util;

import javax.sound.midi.*;

import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.User;
//import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

//import top.dsbbs2.mp.MidiPlayer;

//import top.dsbbs2.mp.MidiPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
public class MidiUtil
{
    private static final byte[] instruments;
    private static ConcurrentHashMap<Object, Sequencer> playing;
    
    static {
        instruments = new byte[] { 0, 0, 0, 0, 0, 0, 0, 11, 6, 6, 6, 6, 9, 9, 15, 11, 10, 5, 5, 10, 10, 10, 10, 10, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 1, 1, 0, 10, 10, 1, 0, 0, 0, 4, 0, 0, 0, 0, 8, 8, 8, 12, 8, 14, 14, 14, 14, 14, 8, 8, 8, 8, 8, 14, 14, 8, 8, 8, 8, 8, 8, 8, 14, 8, 8, 8, 8, 14, 8, 8, 5, 8, 12, 1, 1, 0, 0, 8, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 12, 11, 11, 3, 3, 3, 14, 10, 6, 6, 3, 3, 2, 2, 2, 6, 5, 1, 1, 1, 13, 13, 2, 4, 7 };
        MidiUtil.playing = new ConcurrentHashMap<Object, Sequencer>();
    }
    
    private static byte byteInstrument(final int patch) {
        if (patch < 0 || patch >= MidiUtil.instruments.length) {
            return 0;
        }
        return MidiUtil.instruments[patch];
    }
    
    public static SoundType patchToInstrument(final int patch) {
        return Instrument.fromByte(byteInstrument(patch)).getSound();
    }
    
    public static void stop(final Object id) {
        if (MidiUtil.playing.containsKey(id)) {
            final Sequencer sequencer = MidiUtil.playing.get(id);
            try {
                sequencer.getReceiver().close();
            }
            catch (MidiUnavailableException ex) {}
            sequencer.stop();
            sequencer.close();
            MidiUtil.playing.remove(id);
        }
    }
    
    public static SoundType soundAttempt(final String attempt, final String fallback) {
        SoundType sound = null;
        try {
            sound = (SoundType)SoundTypes.class.getField(attempt).get(null);
            if (sound==null) {
				throw new NoSuchElementException();
			}
        }
        catch (Exception e) {
            try {
                sound = (SoundType)SoundTypes.class.getField(fallback).get(null);
            }
            catch (Exception ex) {}
        }
        /*if (sound == null) {
            sound = SoundTypes.ENTITY_PLAYER_LEVELUP;
        }*/
        return sound;
    }
    
    public static void playMidi(final Sequence sequence, final float tempo, final Object ID, final PlaySoundAble... listeners) {
        try {
            final Sequencer sequencer = MidiSystem.getSequencer(false);
            sequencer.setSequence(sequence);
            sequencer.open();
            sequencer.setTempoFactor(tempo);
            final NoteBlockReceiver reciever = new NoteBlockReceiver(ID, listeners);
            sequencer.getTransmitter().setReceiver(reciever);
            sequencer.start();
            MidiUtil.playing.put(ID, sequencer);
        }
        catch (InvalidMidiDataException | MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void playMidi(final InputStream stream, final float tempo, final Object ID, final PlaySoundAble... listeners) {
    	//Task.builder().execute(() -> {
			try {
				playMidi(MidiSystem.getSequence(stream), tempo, ID, listeners);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		//}).submit(MidiPlayer.getInstance().getPlugin());
    }
    
    public static void stopAll() {
        MidiUtil.playing.forEach((id, s) -> s.stop());
    }
    
    public static boolean isPlaying(final Object key) {
        return MidiUtil.playing.containsKey(key);
    }
    
    public enum Instrument
    {
        PIANO("PIANO", 0, 0, "BLOCK_NOTE_BLOCK_HARP", "BLOCK_NOTE_HARP", "NOTE_PIANO"), 
        BASS("BASS", 1, 1, "BLOCK_NOTE_BLOCK_BASS", "BLOCK_NOTE_BASS", "NOTE_BASS"), 
        SNARE_DRUM("SNARE_DRUM", 2, 2, "BLOCK_NOTE_BLOCK_SNARE", "BLOCK_NOTE_SNARE", "NOTE_SNARE_DRUM"), 
        STICKS("STICKS", 3, 3, "BLOCK_NOTE_BLOCK_HAT", "BLOCK_NOTE_HAT", "NOTE_STICKS"), 
        BASE_DRUM("BASE_DRUM", 4, 4, "BLOCK_NOTE_BLOCK_BASEDRUM", "BLOCK_NOTE_BASEDRUM", "NOTE_BASS_DRUM"), 
        GUITAR("GUITAR", 5, 5, "BLOCK_NOTE_BLOCK_GUITAR", "BLOCK_NOTE_GUITAR", "NOTE_BASS_GUITAR"), 
        BELL("BELL", 6, 6, "BLOCK_NOTE_BLOCK_BELL", "BLOCK_NOTE_BELL", "NOTE_PIANO"), 
        CHIME("CHIME", 7, 7, "BLOCK_NOTE_BLOCK_CHIME", "BLOCK_NOTE_CHIME", "NOTE_PIANO"), 
        FLUTE("FLUTE", 8, 8, "BLOCK_NOTE_BLOCK_FLUTE", "BLOCK_NOTE_FLUTE", "NOTE_PIANO"), 
        XYLOPHONE("XYLOPHONE", 9, 9, "BLOCK_NOTE_BLOCK_XYLOPHONE", "BLOCK_NOTE_XYLOPHONE", "NOTE_STICKS"), 
        PLING("PLING", 10, 10, "BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_PLING", "NOTE_PIANO"), 
        BANJO("BANJO", 11, 11, "BLOCK_NOTE_BLOCK_BANJO", "BLOCK_NOTE_BLOCK_GUITAR", "BLOCK_NOTE_BASS_GUITAR"), 
        BIT("BIT", 12, 12, "BLOCK_NOTE_BLOCK_BIT", "BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_PIANO"), 
        COW_BELL("COW_BELL", 13, 13, "BLOCK_NOTE_BLOCK_COW_BELL", "BLOCK_NOTE_BLOCK_BELL", "BLOCK_NOTE_BELL"), 
        DIDGERIDOO("DIDGERIDOO", 14, 14, "BLOCK_NOTE_BLOCK_DIDGERIDOO", "BLOCK_NOTE_BLOCK_BASS", "BLOCK_NOTE_BASS"), 
        IRON_XYLOPHONE("IRON_XYLOPHONE", 15, 15, "BLOCK_NOTE_BLOCK_IRON_XYLOPHONE", "BLOCK_NOTE_BLOCK_XYLOPHONE", "BLOCK_NOTE_XYLOPHONE");
        
        private final int pitch;
        private SoundType sound;
        
        private Instrument(final String s, final int n, final int pitch, final String sound, final String fallback, final String old) {
            this.sound = MidiUtil.soundAttempt(sound, fallback);
            if (sound == null) {
                this.sound = MidiUtil.soundAttempt(old, fallback);
            }
            if (sound == null) {
            	this.sound = SoundTypes.ENTITY_PLAYER_LEVELUP;
            }
            this.pitch = pitch;
        }
        
        public static Instrument fromByte(final byte instrument) {
            switch (instrument) {
                case 1: {
                    return Instrument.BASS;
                }
                case 2: {
                    return Instrument.SNARE_DRUM;
                }
                case 3: {
                    return Instrument.STICKS;
                }
                case 4: {
                    return Instrument.BASE_DRUM;
                }
                case 5: {
                    return Instrument.GUITAR;
                }
                case 6: {
                    return Instrument.BELL;
                }
                case 7: {
                    return Instrument.CHIME;
                }
                case 8: {
                    return Instrument.FLUTE;
                }
                case 9: {
                    return Instrument.XYLOPHONE;
                }
                case 10: {
                    return Instrument.PLING;
                }
                case 11: {
                    return Instrument.BANJO;
                }
                case 12: {
                    return Instrument.BIT;
                }
                case 13: {
                    return Instrument.COW_BELL;
                }
                case 14: {
                    return Instrument.DIDGERIDOO;
                }
                case 15: {
                    return Instrument.IRON_XYLOPHONE;
                }
                default: {
                    return Instrument.PIANO;
                }
            }
        }
        
        public SoundType getSound() {
            return this.sound;
        }
        
        public byte getByte() {
            return (byte)this.pitch;
        }
    }
    
    public interface PlaySoundAble
    {
        void playSound(final SoundType p0, final float p1, final float p2);
        
        static PlaySoundAble newPlaySoundAble(final User player) {
            return new PlaySoundAble() {
                @Override
                public void playSound(final SoundType var1, final float var2, final float var3) {
                    if (player.isOnline()) {
                        player.getPlayer().ifPresent(i->i.playSound(var1,SoundCategories.MASTER,player.getPosition(), var2, var3));
                    }
                }
            };
        }
        
        static PlaySoundAble newPlaySoundAble(final Location<World> loc) {
            return new PlaySoundAble() {
                @Override
                public void playSound(final SoundType var1, final float var2, final float var3) {
                    loc.getExtent().playSound(var1,SoundCategories.MASTER,loc.getPosition(), var2, var3);
                }
            };
        }
    }
    
    public static class NoteBlockReceiver implements Receiver
    {
        private final Map<Integer, Integer> patches;
        private final PlaySoundAble[] listeners;
        private final Object ID;
        
        public NoteBlockReceiver(final Object ID, final PlaySoundAble... listeners) {
            this.patches = new HashMap<Integer, Integer>();
            this.listeners = listeners;
            this.ID = ID;
        }
        
        @Override
        public void send(final MidiMessage midi, final long time) {
            if (midi instanceof ShortMessage) {
                final ShortMessage message = (ShortMessage)midi;
                final int channel = message.getChannel();
                switch (message.getCommand()) {
                    case 192: {
                        final int patch = message.getData1();
                        this.patches.put(channel, patch);
                        break;
                    }
                    case 144: {
                        final float volume = 10.0f * (message.getData2() / 127.0f);
                        final float pitch = this.getNote(this.toMCNote(message.getData1()));
                        SoundType instrument = Instrument.PIANO.getSound();
                        final Optional<Integer> optional = Optional.ofNullable(this.patches.get(message.getChannel()));
                        if (optional.isPresent()) {
                            instrument = ((channel == 9) ? MidiUtil.patchToInstrument(optional.get()) : MidiUtil.patchToInstrument(optional.get()));
                        }
                        PlaySoundAble[] listeners;
                        for (int length = (listeners = this.listeners).length, i = 0; i < length; ++i) {
                            final PlaySoundAble player = listeners[i];
                            player.playSound(instrument, volume, pitch);
                        }
                        break;
                    }
                }
            }
        }
        
        public float getNote(final byte note) {
            return (float)Math.pow(2.0, (note - 12) / 12.0);
        }
        
        private byte toMCNote(final int n) {
            if (n < 54) {
                return (byte)((n - 6) % 12);
            }
            if (n > 78) {
                return (byte)((n - 6) % 12 + 12);
            }
            return (byte)(n - 54);
        }
        
        @Override
        public void close() {
            MidiUtil.stop(this.ID);
            this.patches.clear();
        }
    }
}
