package jnarepro;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class Corretto302Crash {

    public interface AudioUnitLibrary extends Library {
        AudioUnitLibrary INSTANCE = Native.loadLibrary("AudioUnit", AudioUnitLibrary.class);

        Pointer FindNextComponent(Pointer Component, ComponentDescription desc);

        int AudioUnitInitialize(Pointer inputUnit);

        int OpenAComponent(Pointer comp, PointerByReference mInputUnit);
    }


    public static class ComponentDescription extends Structure {
        public int componentType;          /* A unique 4-byte code indentifying the command set */
        public int componentSubType;       /* Particular flavor of this instance */
        public int componentManufacturer;  /* Vendor indentification */
        public int componentFlags;         /* 8 each for Component,Type,SubType,Manuf/revision */
        public int componentFlagsMask;     /* Mask for specifying which flags to consider in search, zero during registration */

        public ComponentDescription() {
            super();

        }

        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"componentType", "componentSubType", "componentManufacturer", "componentFlags", "componentFlagsMask"});
        }


        public ComponentDescription(Pointer memory) {
            super(memory);

            read();
        }
    }


    public static int stringToInt(String toConvert) {
        if (toConvert.equals("0"))
            return 0;
        if (toConvert.equals("1"))
            return 1;
        ByteBuffer conv = null;
        try {
            conv = ByteBuffer.wrap(toConvert.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            return 0;
        }
        return conv.getInt();
    }

    public static final String kAudioUnitType_Output = "auou";
    public static final String kAudioUnitSubType_HALOutput = "ahal";
    public static final String kAudioUnitManufacturer_Apple = "appl";

    public static void main(String[] args) {
        ComponentDescription desc = new ComponentDescription();
        desc.componentType = stringToInt(kAudioUnitType_Output);
        desc.componentSubType = stringToInt(kAudioUnitSubType_HALOutput);
        desc.componentManufacturer = stringToInt(kAudioUnitManufacturer_Apple);
        desc.componentFlags = 0;
        desc.componentFlagsMask = 0;

        //Finds a component that meets the desc spec's
        Pointer comp = AudioUnitLibrary.INSTANCE.FindNextComponent(Pointer.NULL, desc);
        if (comp.equals(Pointer.NULL)) {
            throw new RuntimeException("Error in FindNextComponent");
        }

        //gains access to the services provided by the component
        PointerByReference mInputUnit = new PointerByReference();
        int err = AudioUnitLibrary.INSTANCE.OpenAComponent(comp, mInputUnit);
        if (err != 0) {
            throw new RuntimeException("Error in OpenAComponent");
        }

        err = AudioUnitLibrary.INSTANCE.AudioUnitInitialize(mInputUnit.getValue());
        if (err != 0) {
            throw new RuntimeException("Error in AudioUnitInitialize");
        }
        System.out.println("Test passed without issues");
    }
}
