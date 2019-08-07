package com.aurora7795;

import com.aurora7795.EasyVRLibrary.DumpGrammarResult;
import com.aurora7795.Protocol.*;
import junit.framework.TestCase;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by Martin Bradford Gago on 10/02/2017.
 */
public class EasyVRLibraryTest extends TestCase {

    private String comPort = "/dev/tty.usbserial-fd1";
    // private String comPort = "COM3";
    private int baudRate = 9600;

    private ISerialPortWrapper testWrapper = new purejavacommWrapper(comPort, baudRate);

    public void testAddCommand_GroupOutOfRange_ThrowsException() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        assertThrows(IllegalArgumentException.class, () -> tempVr.AddCommand(17, 12));
    }


    public void testAddCommand_IndexOutOfRange_ThrowsException() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        assertThrows(IllegalArgumentException.class, () -> tempVr.AddCommand(17, 45));
    }


    public void testAddCommand_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        tempVr.ResetAll();

        //Act
        Boolean response = tempVr.AddCommand(0, 0);
        //Assert
        assertTrue(response);
    }


    public void testChangeBaudrate_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.ChangeBaudrate(Baudrate.B9600);
        //Assert
        assertTrue(response);
    }


    public void testEraseCommand_GroupOutOfRange_ThrowsException() {

        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        assertThrows(IllegalArgumentException.class, () -> tempVr.EraseCommand(17, 12));

    }


    public void testEraseCommand_IndexOutOfRange_ThrowsException() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        assertThrows(IllegalArgumentException.class, () -> tempVr.EraseCommand(2, 45));
    }


    public void testEraseCommand_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        tempVr.ResetAll();

        tempVr.AddCommand(1, 0);

        //Act
        Boolean response = tempVr.EraseCommand(1, 0);
        //Assert
        assertTrue(response);
    }


    public void testGetCommandCount_GroupOutOfRange_ThrowsException() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        assertThrows(IllegalArgumentException.class, () -> tempVr.GetCommandCount(17));
    }

    public void testGetCommandCount_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        int response = tempVr.GetCommandCount(3);
        //Assert
        assertTrue(response >= 0);
    }

    public void testGetGrammarsCount_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        int response = tempVr.GetGrammarsCount();
        //Assert
        assertTrue(response >= 0);
    }


    public void testPlayPhoneTone_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.PlayPhoneTone(1, 9);
        //Assert
        assertTrue(response);
    }


    public void testPlaySound_InvalidVolume_ThrowException() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        assertThrows(IllegalArgumentException.class, () -> tempVr.PlaySound(1, 345));
    }


    public void testPlaySound_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.PlaySound(1, 15);
        //Assert
        assertTrue(response);
    }


    public void testRealtimeLipsync_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.RealtimeLipsync(2, 100);
        //Assert
        assertTrue(response);
    }


    public void testRemoveCommand_GroupOutOfRange_ThrowsException() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        assertThrows(IllegalArgumentException.class, () -> tempVr.RemoveCommand(17, 12));
    }


    public void testRemoveCommand_IndexOutOfRange_ThrowsException() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        assertThrows(IllegalArgumentException.class, () -> tempVr.RemoveCommand(2, 45));
    }


    public void testRemoveCommand_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        tempVr.ResetAll();

        tempVr.AddCommand(1, 0);
        //Act
        Boolean response = tempVr.RemoveCommand(1, 0);
        //Assert
        assertTrue(response);
    }


    public void testResetAll_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.ResetAll();
        //Assert
        assertTrue(response);
    }


    public void testSetCommandLatency() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.SetCommandLatency(CommandLatency.MODE_NORMAL);
        //Assert
        assertTrue(response);
    }


    public void testSetDelay_OutsideBounds_ThrowsException() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        assertThrows(IllegalArgumentException.class, () -> tempVr.SetDelay(2000));
    }


    public void testSetDelay_Rounding10_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.SetDelay(23);
        //Assert
        assertTrue(response);
    }


    public void testSetDelay_Rounding100_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.SetDelay(93);
        //Assert
        assertTrue(response);
    }


    public void testSetDelay_Rounding1000_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.SetDelay(223);
        //Assert
        assertTrue(response);
    }


    public void testSetDelay_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.SetDelay(20);
        //Assert
        assertTrue(response);
    }


    public void testSetKnob_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.SetKnob(Protocol.Knob.LOOSE);
        //Assert
        assertTrue(response);
    }


    public void testSetLanguage_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.SetLanguage(Protocol.Language.ENGLISH);
        //Assert
        assertTrue(response);
    }


    public void testSetLevel_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.SetLevel(Protocol.Level.HARD);
        //Assert
        assertTrue(response);
    }


    public void testSetMicDistance_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.SetMicDistance(Protocol.Distance.FAR_MIC);
        //Assert
        assertTrue(response);
    }


    public void testSetTimeout_InvalidTimeout_ThrowsException() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        assertThrows(IllegalArgumentException.class, () -> tempVr.SetTimeout(60));

    }


    public void testSetTimeout_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.SetTimeout(1);
        //Assert
        assertTrue(response);
    }


    public void testSetTrailingSilence_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.SetTrailingSilence(Protocol.TrailingSilence.TRAILING_300MS);
        //Assert
        assertTrue(response);
    }


    public void testStop_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.Stop();
        //Assert
        assertTrue(response);
    }


    public void testGetId_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        ModuleId response = tempVr.GetId();
        //Assert
        assertTrue(response == ModuleId.EASYVR3_4);
    }


    public void testCheckMessages_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.CheckMessages();
        //Assert
        assertTrue(response);
    }


    public void testDetect_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        Boolean response = tempVr.Detect();
        //Assert
        assertTrue(response);
    }


    public void testDumpGrammar_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        //Act
        DumpGrammarResult response = tempVr.DumpGrammar(0);
        //Assert
        assertTrue(response != null);
        assertTrue(response.flags > 0);
        assertTrue(response.count > 0);
    }


    public void testDumpMessage_NoMessageAvailable_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        tempVr.ResetAll();
        //Act
        DumpMessageResult response = tempVr.DumpMessage(0);
        //Assert
        assertTrue(response != null);
        assertTrue(response.type == 0);
        assertTrue(response.length == 0);
    }


    public void testPlayMessageAsync_Success() {
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);

        tempVr.PlayMessageAsync(1, Protocol.MessageSpeed.SPEED_NORMAL, Protocol.MessageAttenuation.ATTEN_NONE);

    }


    public void testPlaySoundAsync_Success() {
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        tempVr.PlaySoundAsync(1, 15);
    }


    public void testRecordMessageAsync() {
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);

        tempVr.RecordMessageAsync(1, MessageType.MSG_EMPTY, 5);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tempVr.PlayMessageAsync(1, MessageSpeed.SPEED_NORMAL, MessageAttenuation.ATTEN_NONE);
    }


    public void testDumpCommand() {
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);


        DumpCommandResult response = tempVr.DumpCommand(1, 0);

        assertTrue(response != null);
        assertTrue(Objects.equals(response.name, "TESTING123"));
        assertTrue(response.training == 2);
    }


    public void testSetCommandLabel_Success() {
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        tempVr.ResetAll();
        Boolean response = tempVr.AddCommand(0, 0);

        assertTrue(response);

        response = tempVr.SetCommandLabel(0, 0, "testCom1");
        assertTrue(response);

        DumpCommandResult CommandResponse = tempVr.DumpCommand(0, 0);

        assertTrue(CommandResponse != null);
        assertTrue(Objects.equals(CommandResponse.name, "TESTCOM1"));
        assertTrue(CommandResponse.training == 0);
    }


    public void testDumpSoundTable_Success() {
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        tempVr.ResetAll();

        DumpSoundTableResult STresponse = tempVr.DumpSoundTable();

        assertTrue(STresponse != null);
        assertTrue(Objects.equals(STresponse.name, "SND_BEEP"));
        assertTrue(STresponse.count == 1);
    }


    public void testFixMessages_Success() {
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);
        tempVr.ResetAll();

        Boolean response = tempVr.FixMessages(true);

        assertTrue(response);
    }


    public void testGetNextWordLabel_Success() {
        //Arrange
        EasyVRLibrary tempVr = new EasyVRLibrary(testWrapper);

        DumpGrammarResult response = tempVr.DumpGrammar(0);
        assertTrue(response != null);

        //Act
        String name = tempVr.GetNextWordLabel();

        //Assert
        assertTrue(Objects.equals(name, "ROBOT"));

    }

}