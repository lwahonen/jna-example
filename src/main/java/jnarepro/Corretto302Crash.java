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
//        typedef UInt32                          FourCharCode;
//        typedef FourCharCode                    OSType;
//        #pragma pack(push, 4)
//        typedef struct AudioComponentDescription {
//            OSType              componentType;
//            OSType              componentSubType;
//            OSType              componentManufacturer;
//            UInt32              componentFlags;
//            UInt32              componentFlagsMask;
//        } AudioComponentDescription;
//        #pragma pack(pop)
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
    }


    public static int stringToInt(String toConvert) {
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

        System.out.println("Using struct "+desc);
        //Finds a component that meets the desc spec's
        Pointer comp = AudioUnitLibrary.INSTANCE.FindNextComponent(Pointer.NULL, desc);
        if (comp.equals(Pointer.NULL)) {
            throw new RuntimeException("Error in FindNextComponent");
        }
        System.out.println("Got component "+comp);

        //gains access to the services provided by the component
        PointerByReference mInputUnit = new PointerByReference();
        int err = AudioUnitLibrary.INSTANCE.OpenAComponent(comp, mInputUnit);
        if (err != 0) {
            throw new RuntimeException("Error in OpenAComponent");
        }
        System.out.println("For this component, got the audio unit "+mInputUnit.getValue());

        err = AudioUnitLibrary.INSTANCE.AudioUnitInitialize(mInputUnit.getValue());
        if (err != 0) {
            throw new RuntimeException("Error in AudioUnitInitialize");
        }
        System.out.println("Test passed without issues");
    }
}
