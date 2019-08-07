package com.aurora7795;

import java.io.IOException;

import static com.aurora7795.Protocol.*;

/**
 * Hello world!
 */
public class EasyVRLibrary {

    public static int EASYVR_RX_TIMEOUT = 500;
    public static int EASYVR_STORAGE_TIMEOUT = 500;
    public static int DEF_TIMEOUT = EASYVR_RX_TIMEOUT;
    public static int STORAGE_TIMEOUT = EASYVR_STORAGE_TIMEOUT;
    private static ISerialPortWrapper _serialPort;
    public int EASYVR_WAKE_TIMEOUT = 200;
    public int EASYVR_PLAY_TIMEOUT = 5000;
    public int EASYVR_TOKEN_TIMEOUT = 1500;
    public int NO_TIMEOUT = 0;
    protected byte Group;
    protected byte Id;
    private int Value;
    private Status _status = new Status();

    public EasyVRLibrary(ISerialPortWrapper serialPortLibrary) {
        if (_serialPort != null) return;
        // Create the serial port with basic settings
        _serialPort = serialPortLibrary;

        Value = -1;
        Group = -1;
        Id = -1;
        _status.V = 0;
    }

    private static void SendCommand(char command) {
        try {
            _serialPort.Write(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Integer ReceiveArgumentAsInt() {
        Integer response;
        SendCommand((char) Protocol.ARG_ACK);
        response = ArgumentEncoding.ConvertArgumentCode(GetResponse(DEF_TIMEOUT));
        return response;
    }

    private static Character ReceiveArgumentAsChar() {
        char response;
        SendCommand((char) ARG_ACK);

        response = GetResponse(DEF_TIMEOUT);
        return response;

    }

    private static void SendArgument(int argument) {
        try {
            _serialPort.Write(ArgumentEncoding.IntToArgumentChar(argument));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void SendCharacter(char argument) {
        try {
            _serialPort.Write(argument);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static Character GetResponse(int timeout) {

        _serialPort.SetTimeout(timeout);
        Character temp = null;
        try {
            temp = _serialPort.Read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("read off buffer: %s%n", temp);
        return temp;
    }

    /**
     * Adds a new custom command to a group.
     *
     * @param group (0-16) is the target group, or one of the values in #Groups
     * @param index (0-31) is the index of the command within the selected group
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean AddCommand(int group, int index) {
        if (group < 0 || group > 16) throw new IllegalArgumentException(Integer.toString(group));
        if (index < 0 || index > 31) throw new IllegalArgumentException(Integer.toString(index));

        SendCommand(CMD_GROUP_SD);
        SendArgument(group);
        SendArgument(index);

        int rx;
        rx = GetResponse(STORAGE_TIMEOUT);
        if (rx == STS_SUCCESS)
            return true;

        _status.V = 0;

        if (rx == STS_OUT_OF_MEM)
            _status.Memfull = true;

        return false;
    }

    /**
     * Sets the new communication speed. You need to modify the baudrate of the
     * underlying Stream object accordingly, after the function returns successfully.
     *
     * @param baudRate one of values in #Baudrate
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean ChangeBaudrate(Baudrate baudRate) {
        SendCommand(CMD_BAUDRATE);
        SendArgument(baudRate.getValue());

        return GetResponse(DEF_TIMEOUT) == STS_SUCCESS;
    }

    /**
     * Performs a memory check for consistency.
     * <p>
     * If a memory write or erase operation does not complete due to unexpecte conditions, like power losses, the
     * memory contents may be corrupted.
     * When the check fails #getError() returns #ERR_CUSTOM_INVALID.
     *
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean CheckMessages() {
        SendCommand(CMD_VERIFY_RP);
        SendArgument(-1);
        SendArgument(0);

        char rx;
        rx = GetResponse(STORAGE_TIMEOUT);
        ReadStatus(rx);
        return (_status.V == 0);
    }

    /**
     * Detects an EasyVR module, waking it from sleep mode and checking it responds correctly.
     *
     * @return true if a compatible module has been found
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean Detect() {
        int i;
        for (i = 0; i < 5; ++i) {
            SendCommand(CMD_BREAK);

            if (GetResponse(DEF_TIMEOUT) == STS_SUCCESS)
                return true;
        }
        return false;
    }

    /**
     * Starts listening for a SonicNet token. Manually check for completion with #hasFinished().
     * <p>
     * The module is busy until token detection completes and it cannot accept other commands.You can interrupt
     * listening with #stop().
     *
     * @param bits      (4 or 8) specifies the length of received tokens
     * @param rejection rejection (0-2) specifies the noise rejection level, it can be one of the values in
     *                  #RejectionLevel
     * @param timeout   (1-28090) is the maximum time in milliseconds to keep listening for a valid token or(0)
     *                  to listen without time limits.
     */
    @SuppressWarnings("WeakerAccess")
    public void DetectToken(BitNumber bits, RejectionLevel rejection, int timeout) {
        SendCommand(CMD_RECV_SN);
        SendArgument(bits.getValue());
        SendArgument(rejection.getValue());

        if (timeout > 0)
            timeout = (timeout * 2 + 53) / 55; // approx / 27.46 - err < 0.15%
        SendArgument((timeout >> 5) & 0x1F);
        SendArgument(timeout & 0x1F);
    }

    /**
     * Retrieves the contents of a built-in or a custom grammar.
     * Command labels contained in the grammar can be obtained by calling #getNextWordLabel()
     *
     * @param grammar (0-31) is the target grammar, or one of the values in #Wordset
     * @return DumpGrammarResult is successful, containing:
     * Flags - a variable that holds some grammar flags when the function returns. See #GrammarFlag
     * Count - count is a variable that holds the number of words in the grammar when the function returns.
     * Null if failed
     */
    @SuppressWarnings("WeakerAccess")
    public DumpGrammarResult DumpGrammar(int grammar) {
        if (grammar < 0 || grammar > 31) throw new IllegalArgumentException(Integer.toString(grammar));

        DumpGrammarResult response = new DumpGrammarResult();

        SendCommand(CMD_DUMP_SI);
        SendArgument(grammar);

        if (GetResponse(DEF_TIMEOUT) != STS_GRAMMAR) {
            return null;
        }

        char rx;
        rx = ReceiveArgumentAsChar();

        response.flags = (byte) ((rx == -1) ? 32 : rx);

        rx = ReceiveArgumentAsChar();

        response.count = (byte) rx;
        return response;
    }

    /**
     * Retrieves the type and length of a recorded message
     * <p>
     * The specified message may have errors. Use #getError() when the function fails, to know the reason of the
     * failure.
     *
     * @param index (0-31) is the index of the target message slot
     * @return DumpMessageResult which contains:
     * Type: (0,8) is a variable that holds the message format when the function returns(see #MessageType)
     * Length: A variable that holds the message length in bytes when the function returns
     */
    @SuppressWarnings("WeakerAccess")
    public DumpMessageResult DumpMessage(int index) {

        DumpMessageResult response = new DumpMessageResult();

        SendCommand(CMD_DUMP_RP);
        SendArgument(-1);
        SendArgument(index);

        char sts;
        sts = GetResponse(STORAGE_TIMEOUT);
        if (sts != STS_MESSAGE) {
            ReadStatus(sts);
            return null;
        }

        // if communication should fail
        _status.V = 0;
        _status.Error = true;

        response.type = ReceiveArgumentAsInt();

        response.length = 0;
        if (response.type == 0)
            return response;

        int[] tempArray = new int[7];

        for (int i = 0; i < 6; ++i) {
            char rx;

            rx = ReceiveArgumentAsChar();


            tempArray[i] |= rx & 0x0F;

            rx = ReceiveArgumentAsChar();
            tempArray[i] |= (rx << 4) & 0xF0;
        }

        _status.V = 0;

        return response;
    }

    /**
     * Retrieves the name of the sound table and the number of sounds it contains
     *
     * @return DumpSoundResult, which contains:
     * Name: points to an array of at least 32 characters that holds the sound table label when the function
     * returns
     * Count: A variable that holds the number of sounds when the function returns
     * NULL if failed
     */
    @SuppressWarnings("WeakerAccess")
    public DumpSoundTableResult DumpSoundTable() {
        DumpSoundTableResult response = new DumpSoundTableResult();

        SendCommand(CMD_DUMP_SX);

        if (GetResponse() != STS_TABLE_SX) {
            return null;
        }

        Integer rx;
        rx = ReceiveArgumentAsInt();
        if (rx == null) {
            return null;
        }
        response.count = rx << 5;
        rx = ReceiveArgumentAsInt();
        if (rx == null) {
            return null;
        }
        response.count |= rx;

        rx = ReceiveArgumentAsInt();
        if (rx == null) {
            return null;
        }
        int length = rx;

        StringBuilder tempString = new StringBuilder();

        for (int i = 0; i < length; ++i) {
            Character rxChar;
            rxChar = ReceiveArgumentAsChar();
            if (rx == null) {
                return null;
            }
            if (rx == '^') {
                rxChar = ReceiveArgumentAsChar();
                if (rxChar == null) {
                    return null;
                }
                tempString.append(ArgumentEncoding.ConvertArgumentCode(rxChar));
                --length;
            } else {
                tempString.append(rxChar);
            }

        }
        response.name = tempString.toString();
        return response;
    }

    /**
     * Schedules playback of a SonicNet token after the next sound starts playing.
     * <p>
     * The scheduled token remains valid for one operation only, so you have to call #playSound() or
     * #playSoundAsync() immediately after this function.
     *
     * @param bits  bits (4 or 8) specifies the length of transmitted token
     * @param token token is the index of the SonicNet token to play (0-255 for 8-bit tokens or 0-15 for 4-bit tokens)
     * @param delay delay (1-28090) is the time in milliseconds at which to send the token, since the beginning of the
     *              next sound playback
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean EmbedToken(int bits, int token, int delay) {
        SendCommand(CMD_SEND_SN);
        SendArgument(bits);
        SendArgument((token >> 5) & 0x1F);
        SendArgument(token & 0x1F);
        delay = (delay * 2 + 27) / 55; // approx / 27.46 - err < 0.15%
        if (delay == 0) // must be > 0 to embed in some audio
            delay = 1;
        SendArgument((delay >> 5) & 0x1F);
        SendArgument(delay & 0x1F);

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Sets the name of a custom command.
     *
     * @param group (0-16) is the target group, or one of the values in #Groups
     * @param index (0-31) is the index of the command within the selected group
     * @param name  name is a string containing the label to be assigned to the specified command
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean SetCommandLabel(int group, int index, String name) {
        SendCommand(CMD_NAME_SD);
        SendArgument(group);
        SendArgument(index);

        // numeric characters in the label string must be prefixed with a '^' - this increases the overall length of the
        // name and needs to be taken into account when determining how many characters will be sent to the Easy VR module

        int escapedCharsNeeded = 0;
        for (char c : name.toCharArray()) {
            if (Character.isDigit(c)) {
                escapedCharsNeeded++;
            }
        }

        SendArgument(name.length() + escapedCharsNeeded);

        for (char c : name.toCharArray()) {

            if (Character.isDigit(c)) {
                SendCharacter('^');
                SendArgument(c - '0');
            } else if (Character.isLetter(c)) {
                SendCharacter((char) (c & ~0x20)); // to uppercase
            } else {
                SendCharacter('_');
            }
        }

        return GetResponse(STORAGE_TIMEOUT) == STS_SUCCESS;
    }

    /**
     * Erases the training data of a custom command.
     *
     * @param group (0-16) is the target group, or one of the values in #Groups
     * @param index (0-31) is the index of the command within the selected group
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean EraseCommand(int group, int index) {
        if (group < 0 || group > 16) throw new IllegalArgumentException(Integer.toString(group));
        if (index < 0 || index > 31) throw new IllegalArgumentException(Integer.toString(index));

        SendCommand(CMD_ERASE_SD);
        SendArgument(group);
        SendArgument(index);

        return GetResponse() == STS_SUCCESS;
    }

    /// <summary>
    ///     Retrieves all internal data associated to a custom command.
    /// </summary>
    /// <param name="group">(0-16) is the target group, or one of the values in #Groups</param>
    /// <param name="index">(0-31) is the index of the command within the selected group</param>
    /// <param name="data">points to an array of at least 258 bytes that holds the command raw data</param>
    /// <returns>true if the operation is successful</returns>


    /**
     * Retrieves all internal data associated to a custom command.
     *
     * @param group (0-16) is the target group, or one of the values in #Groups
     * @param index (0-31) is the index of the command within the selected group
     * @return an ExportCommandResult object which contains:
     * data: points to an array of at least 258 bytes that holds the command raw data
     * null if method fails.
     */
    @SuppressWarnings("WeakerAccess")
    public ExportCommandResult ExportCommand(byte group, byte index) {
        ExportCommandResult response = new ExportCommandResult();

        SendCommand(CMD_SERVICE);
        SendArgument(SVC_EXPORT_SD - ARG_ZERO);
        SendArgument(group);
        SendArgument(index);

        if (GetResponse(STORAGE_TIMEOUT) != STS_SERVICE)
            return null;

        Character rx;
        rx = ReceiveArgumentAsChar();
        if (rx != SVC_DUMP_SD - ARG_ZERO)
            return null;

        for (int i = 0; i < 258; ++i) {
            rx = ReceiveArgumentAsChar();
            if (rx == null)
                return null;
            response.data[i] = (rx << 4) & 0xF0;
            if (rx == null)
                return null;
            response.data[i] |= rx & 0x0F;
        }
        return response;
    }

    /// <summary>
    ///     Retrieves the current mouth position during lip-sync.
    /// </summary>
    /// <param name="value">(0-31) is filled in with the current mouth opening position</param>
    /// <returns>true if the operation is successful, false if lip-sync has finished</returns>
    @SuppressWarnings("WeakerAccess")
    public Integer FetchMouthPosition() {
        int value;
        SendCharacter(' ');
        char rx = GetResponse();
        if (rx >= ARG_MIN && rx <= ARG_MAX) {
            value = rx;
            return value;
        }
        // check if finished
        if (rx >= 0)
            ReadStatus(rx);
        return null;
    }

    /**
     * Performs a memory check and attempt recovery if necessary. Incomplete data wil be erased.Custom commands/groups are
     * not affected.
     * <p>
     * It will take some time for the whole process to complete (several seconds) and it cannot be interrupted.
     * During this time the module cannot accept any other command. The sound table and custom grammars data is not
     * affected.
     *
     * @param wait specifies whether to wait until the operation is complete (or times out)
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean FixMessages(Boolean wait) {
        SendCommand(CMD_VERIFY_RP);
        SendArgument(-1);
        SendArgument(1);

        if (!wait) {
            return true;
        }

        try {
            Thread.sleep(25000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Gets the recognised command index if any.
     *
     * @return (0 to 31) is the command index if recognition is successful, (-1) if no command has been recognized or an error
     * occurred
     */
    @SuppressWarnings("WeakerAccess")
    public int GetCommand() {
        if (_status.Command) return ArgumentEncoding.ConvertArgumentCode((char) Value);
        return -1;
    }

    /**
     * Gets the number of commands in the specified group.
     *
     * @param group (0-16) is the target group, or one of the values in #Groups
     * @return integer is the count of commands (negative in case of errors)
     */
    @SuppressWarnings("WeakerAccess")
    public int GetCommandCount(int group) {
        if (group < 0 || group > 16) throw new IllegalArgumentException(Integer.toString(group));

        SendCommand(CMD_COUNT_SD);
        SendArgument(group);

        if (GetResponse() != STS_COUNT) return -1;
        Character rx = ReceiveArgumentAsChar();
        if (rx != null)
            return ArgumentEncoding.ConvertArgumentCode(rx);
        return -1;
    }

    /**
     * Gets the last error code if any.
     *
     * @return (0 to 255) is the error code, (-1) if no error occurred
     */
    @SuppressWarnings("WeakerAccess")
    public short GetError() {
        if (_status.Error) return (short) Value;
        return -1;
    }

    /**
     * Retrieves the name and training data of a custom command.
     *
     * @param group (0-16) is the target group, or one of the values in #Groups
     * @param index (0-31) is the index of the command within the selected group
     * @return DumpCommandResult, which contains:
     * <p>
     * Name: points to an array of at least 32 characters that holds the command
     * label when the function returns
     * <p>
     * training: training is a variable that holds the training count when the function returns.
     * Additional information about training is available through the functions #isConflict()
     * and #getWord() or #getCommand()
     * <p>
     * null if failed.
     */
    @SuppressWarnings("WeakerAccess")
    public DumpCommandResult DumpCommand(int group, int index) {
        DumpCommandResult response = new DumpCommandResult();

        SendCommand(CMD_DUMP_SD);
        SendArgument(group);
        SendArgument(index);

        if (GetResponse() != STS_DATA)
            return null;

        Integer rx = ReceiveArgumentAsInt();
        if (rx == null)
            return null;

        response.training = rx & 0x07;
        if (rx == -1 || response.training == 7)
            response.training = 0;

        _status.V = 0;
        _status.Conflict = (rx & 0x18) != 0;
        _status.Command = (rx & 0x08) != 0;
        _status.Builtin = (rx & 0x10) != 0;

        rx = ReceiveArgumentAsInt();
        if (rx == null) {
            return null;
        }

        Value = rx;

        rx = ReceiveArgumentAsInt();
        if (rx == null) {
            return null;
        }

        StringBuilder tempString = new StringBuilder();

        for (int length = rx; length > 0; length--) {
            Character rxChar = ReceiveArgumentAsChar();
            if (rxChar == null) {
                return null;
            }

            if (rxChar == '^') {
                rxChar = ReceiveArgumentAsChar();
                if (rxChar == null) {
                    return null;
                }
                tempString.append(ArgumentEncoding.ConvertArgumentCode(rxChar));
                --length;
            } else {
                tempString.append(rxChar);
            }
        }

        response.name = tempString.toString();
        return response;
    }

    /**
     * Gets the total number of grammars available, including built-in and custom.
     *
     * @return integer is the count of grammars (negative in case of errors)
     */
    @SuppressWarnings("WeakerAccess")
    public int GetGrammarsCount() {
        SendCommand(CMD_DUMP_SI);
        SendArgument(-1);

        if (GetResponse() != STS_COUNT) return -1;
        Character rx = ReceiveArgumentAsChar();
        if (rx != null)
            return ArgumentEncoding.ConvertArgumentCode(rx);
        return -1;
    }

    /**
     * Gets the module identification number (firmware version).
     *
     * @return Module ID for the easy VR module
     */
    @SuppressWarnings("WeakerAccess")
    public ModuleId GetId() {
        SendCommand(STS_ID);

        char response = GetResponse(DEF_TIMEOUT);
        if (response != STS_ID)
            try {
                throw new Exception("Invalid response: " + response);
            } catch (Exception e) {
                e.printStackTrace();
            }

        response = ReceiveArgumentAsChar();

        int decodedValue = ArgumentEncoding.ConvertArgumentCode(response);

        return ModuleId.fromInt(decodedValue);
    }

    /**
     * Gets the index of the received SonicNet token if any.
     *
     * @return integer is the index of the received SonicNet token (0-255 for 8-bit tokens or 0-15 for 4-bit tokens)
     * if detection was successful, (-1) if no token has been received or an error occurred
     */
    @SuppressWarnings("WeakerAccess")
    public int GetToken() {
        if (_status.Token) return ArgumentEncoding.ConvertArgumentCode((char) Value);
        return -1;
    }

    /**
     * Gets the recognised word index if any, from built-in sets or custom grammars.
     *
     * @return (0 to 31) is the command index if recognition is successful, (-1) if no built-in word has been recognized or an
     * error occurred
     */
    @SuppressWarnings("WeakerAccess")
    public int GetWord() {
        if (_status.Builtin) {
            int value = ArgumentEncoding.ConvertArgumentCode((char) Value);
            System.out.printf("Word recognized with index: %d%n", value);
            return value;
        }
        return -1;
    }

    /**
     * Polls the status of on-going recognition, training or asynchronous playback tasks.
     *
     * @return true if the operation has completed
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean HasFinished() {
        char rx = GetResponse(NO_TIMEOUT);
        if (rx < 0)
            return false;

        ReadStatus(rx);
        return true;
    }

    /**
     * Overwrites all internal data associated to a custom command. When commands are imported this way, their training
     * should be tested again with #verifyCommand()
     *
     * @param group (0-16) is the target group, or one of the values in #Groups
     * @param index (0-31) is the index of the command within the selected group
     * @param data  points to an array of at least 258 bytes that holds the command raw data
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean ImportCommand(int group, int index, byte[] data) {
        if (group < 0 || group > 16) throw new IllegalArgumentException(Integer.toString(group));
        if (index < 0 || index > 31) throw new IllegalArgumentException(Integer.toString(index));
        SendCommand(CMD_SERVICE);
        SendArgument(SVC_IMPORT_SD - ARG_ZERO);
        SendArgument(group);
        SendArgument(index);

        for (int i = 0; i < 258; ++i) {
            int tx = (data[i] >> 4) & 0x0F;
            SendArgument(tx);
            tx = data[i] & 0x0F;
            SendArgument(tx);
        }
        return GetResponse(STORAGE_TIMEOUT) == STS_SUCCESS;
    }

    /**
     * Retrieves the wake-up indicator (only valid after #hasFinished() has been called).
     *
     * @return true if the module has been awakened from sleep mode
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean IsAwakened() {
        return _status.Awakened;
    }

    /**
     * Retrieves the conflict indicator.
     *
     * @return true is a conflict occurred during training. To know what caused the conflict, use #getCommand() and
     * #getWord() (only valid for triggers)
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean IsConflict() {
        return _status.Conflict;
    }

    /**
     * Retrieves the invalid protocol indicator.
     *
     * @return true if an invalid sequence has been detected in the communication protocol
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean IsInvalid() {
        return _status.Invalid;
    }

    /**
     * Retrieves the memory full indicator (only valid after #addCommand() returned false).
     *
     * @return true if a command could not be added because of memory size constaints(up to 32 custom commands can be
     * created)
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean IsMemoryFull() {
        return _status.Memfull;
    }

    /**
     * Retrieves the timeout indicator.
     *
     * @return true if a timeout occurred
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean IsTimeout() {
        return _status.Timeout;
    }

    /**
     * Starts playback of a recorded message. Manually check for completion with #hasFinished().
     * <p>
     * The module is busy until playback completes and it cannot accept other commands.You can interrupt playback
     * with #stop().
     *
     * @param index       (0-31) is the index of the target message slot
     * @param speed       (0-1) may be one of the values in #MessageSpeed
     * @param attenuation (0-3) may be one of the values in #MessageAttenuation
     */
    @SuppressWarnings("WeakerAccess")
    public void PlayMessageAsync(int index, MessageSpeed speed, MessageAttenuation attenuation) {
        SendCommand(CMD_PLAY_RP);
        SendArgument(-1);
        SendArgument(index);
        SendArgument((speed.getValue() << 2) | (attenuation.getValue() & 3));
    }


    /**
     * Plays a phone tone and waits for completion
     *
     * @param tone     is the index of the tone (0-9 for digits, 10 for '*' key, 11 for '#' key and 12-15 for extra keys
     *                 'A' to 'D', -1 for the dial tone)
     * @param duration (1-32) is the tone duration in 40 milliseconds units, or  in seconds for the dial tone
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean PlayPhoneTone(int tone, int duration) {
        if (tone < -1 || tone > 15) throw new IllegalArgumentException(Integer.toString(tone));
        if (duration < 1 || duration > 32) throw new IllegalArgumentException(Integer.toString(duration));

        SendCommand(CMD_PLAY_DTMF);
        SendArgument(-1);
        SendArgument(tone);
        SendArgument(duration);

        char response = GetResponse(5000);
        return response == STS_SUCCESS;
    }

    /**
     * Plays a sound from the sound table and waits for completion
     * <p>
     * To alter the maximum time for the wait, define the EASYVR_PLAY_TIMEOUT macro before including the EasyVR
     * library.
     *
     * @param index  index is the index of the target sound in the sound table
     * @param volume volume (0-31) may be one of the values in #SoundVolume
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean PlaySound(int index, int volume) {
        if (volume < 0 || volume > 31) throw new IllegalArgumentException(Integer.toString(volume));

        SendCommand(CMD_PLAY_SX);
        SendArgument((byte) ((index >> 5) & 0x1F));
        SendArgument((byte) (index & 0x1F));
        SendArgument(volume);

        return GetResponse(EASYVR_PLAY_TIMEOUT) == STS_SUCCESS;
    }

    /**
     * Starts playback of a sound from the sound table. Manually check for completion with #hasFinished().
     * <p>
     * The module is busy until playback completes and it cannot accept other commands.You can interrupt playback
     * with #stop().
     *
     * @param index  index is the index of the target sound in the sound table
     * @param volume (0-31) may be one of the values in #SoundVolume
     */
    @SuppressWarnings("WeakerAccess")
    public void PlaySoundAsync(int index, int volume) {
        if (volume < 0 || volume > 31) throw new IllegalArgumentException(Integer.toString(volume));

        SendCommand(CMD_PLAY_SX);
        SendArgument((index >> 5) & 0x1F);
        SendArgument(index & 0x1F);
        SendArgument(volume);
    }

    /**
     * Starts real-time lip-sync on the input voice signal. Retrieve output values with #fetchMouthPosition() or abort
     * with #stop().
     *
     * @param threshold (0-1023) is a measure of the strength of the input signal below which the mouth is considered to be closed(see
     *                  #LipsyncThreshold, adjust based on microphone settings, distance and background noise)
     * @param timeout   (0-255) is the maximum duration of the function in seconds, 0 means infinite
     * @return true if the operation is successfully started
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean RealtimeLipsync(int threshold, int timeout) {
        if (threshold > 1023) throw new IllegalArgumentException(Integer.toString(threshold));
        if (timeout > 255) throw new IllegalArgumentException(Integer.toString(timeout));

        SendCommand(CMD_LIPSYNC);
        SendArgument(-1);
        SendArgument((threshold >> 5) & 0x1F);
        SendArgument(threshold & 0x1F);
        SendArgument((timeout >> 4) & 0x0F);
        SendArgument(timeout & 0x0F);

        char sts = ReceiveArgumentAsChar();
        if (sts == STS_LIPSYNC) return true;
        ReadStatus(sts);
        return false;
    }

    /**
     * Starts recognition of a custom command. Results are available after #hasFinished() returns true.
     * The module is busy until recognition completes and it cannot accept other commands. You can interrupt recognition
     * with #stop().
     *
     * @param group (0-16) is the target group, or one of the values in #Groups
     */
    @SuppressWarnings("WeakerAccess")
    public void RecognizeCommand(int group) {
        if (group < 0 || group > 16) throw new IllegalArgumentException(Integer.toString(group));

        SendCommand(CMD_RECOG_SD);
        SendArgument(group);
    }

    /**
     * Starts recognition of a built-in word. Results are available after #hasFinished() returns true.
     * The module is busy until recognition completes and it cannot saccept other commands. You can interrupt recognition
     * with #stop().
     *
     * @param wordset (0-3) is the target word set, or one of the values in #Wordset, (4-31) is the target custom
     *                grammar, if present
     */
    @SuppressWarnings("WeakerAccess")
    public void RecognizeWord(int wordset) {
        if (wordset < 0 || wordset > 31) throw new IllegalArgumentException(Integer.toString(wordset));

        SendCommand(CMD_RECOG_SI);
        SendArgument(wordset);
    }

    /**
     * Starts recording a message. Manually check for completion with #hasFinished().
     * <p>
     * The module is busy until recording times out or the end of memory is reached.You can interrupt an ongoing
     * recording with #stop().
     *
     * @param index   index (0-31) is the index of the target message slot
     * @param bits    bits (8) specifies the audio format (see #MessageType)
     * @param timeout timeout (0-31) is the maximum recording time (0=infinite)
     */
    @SuppressWarnings("WeakerAccess")
    public void RecordMessageAsync(int index, MessageType bits, int timeout) {
        if (index < 0 || index > 31) throw new IllegalArgumentException(Integer.toString(index));
        if (timeout < 0 || timeout > 31) throw new IllegalArgumentException(Integer.toString(timeout));

        SendCommand(CMD_RECORD_RP);
        SendArgument(-1);
        SendArgument(index);
        SendArgument(bits.getValue());
        SendArgument(timeout);
    }

    /**
     * Removes a custom command from a group.
     *
     * @param group (0-16) is the target group, or one of the values in #Groups
     * @param index (0-31) is the index of the command within the selected group
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean RemoveCommand(int group, int index) {
        if (group < 0 || group > 16) throw new IllegalArgumentException(Integer.toString(group));
        if (index < 0 || index > 31) throw new IllegalArgumentException(Integer.toString(index));

        SendCommand(CMD_UNGROUP_SD);
        SendArgument(group);
        SendArgument(index);

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Empties internal memory for custom commands/groups and messages.
     * <p>
     * It will take some time for the whole process to complete (EasyVR3 is faster)
     * and it cannot be interrupted.During this time the module cannot accept any other command.
     * The sound table and custom grammars data is not affected.
     *
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean ResetAll() {
        SendCommand(CMD_RESETALL);
        SendCommand('R');

        return GetResponse(5000) == STS_SUCCESS;
    }

    /**
     * Empties internal memory for custom commands/groups only. Messages are not affected.
     * <p>
     * It will take some time for the whole process to complete (EasyVR3 is faster) and it cannot be interrupted.
     * During this time the module cannot accept any other command.
     * The sound table and custom grammars data is not affected.
     *
     * @param wait specifies whether to wait until the operation is complete (or times out)
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean ResetCommands(Boolean wait) {
        SendCommand(CMD_RESETALL);
        SendArgument('D');

        return GetResponse(5000) == STS_SUCCESS;
    }

    /**
     * Empties internal memory used for messages only. Commands/groups are not affected.
     * <p>
     * It will take some time for the whole process to complete (EasyVR3 is faster) and it cannot be interrupted.
     * During this time the module cannot accept any other command. The sound table and custom grammars data is not
     * affected.
     *
     * @param wait specifies whether to wait until the operation is complete (or times out)
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean ResetMessages(Boolean wait) {
        SendCommand(CMD_RESETALL);
        SendArgument('M');

        return GetResponse(5000) == STS_SUCCESS;
    }

    /**
     * Plays a SonicNet token and waits for completion.
     *
     * @param bits  (4 or 8) specifies the length of transmitted token
     * @param token token is the index of the SonicNet token to play (0-255 for 8-bit tokens or 0-15 for 4-bit tokens)
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean SendToken(BitNumber bits, byte token) {
        SendCommand(CMD_SEND_SN);
        SendArgument(bits.getValue());
        SendArgument((token >> 5) & 0x1F);
        SendArgument(token & 0x1F);
        SendArgument(0);
        SendArgument(0);

        return GetResponse(EASYVR_TOKEN_TIMEOUT) == STS_SUCCESS;
    }

    /// <summary>
    ///     Starts immediate playback of a SonicNet token. Manually check for completion with #hasFinished().
    /// </summary>
    /// <param name="bits">bits (4 or 8) specifies the length of trasmitted token</param>
    /// <param name="token">token is the index of the SonicNet token to play (0-255 for 8-bit tokens or 0-15 for 4-bit tokens)</param>
    /// <remarks>
    ///     The module is busy until playback completes and it cannot accept other commands.You can interrupt playback
    ///     with #stop().
    /// </remarks>
    @SuppressWarnings("WeakerAccess")
    public void SendTokenAsync(BitNumber bits, byte token) {
        switch (bits) {
            case BITS_4:
                if (token > 15)
                    throw new IllegalArgumentException("Invalid token for token length (must be between 0-15)");
                break;
            case BITS_8:
                if (token > 255)
                    throw new IllegalArgumentException("Invalid token for token length (must be between 0-255)");
                break;
            default:
                throw new IllegalArgumentException(Integer.toString(bits.getValue()));
        }

        SendCommand(CMD_SEND_SN);
        SendArgument(bits.getValue());
        SendArgument(token);
    }

    /**
     * Enables or disables fast recognition for custom commands and passwords.
     * Fast SD/SV recognition can improve response time.
     *
     * @param mode (0-1) is one of the values in #CommandLatency
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean SetCommandLatency(CommandLatency mode) {
        SendCommand(CMD_FAST_SD);
        SendArgument(-1);
        SendArgument(mode.getValue());

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Sets the delay before any reply of the module.
     *
     * @param millis millis (0-1000) is the delay duration in milliseconds, rounded to
     *               10 units in range 10-100 and to 100 units in range 100-1000.
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean SetDelay(int millis) {
        if (millis > 1000) throw new IllegalArgumentException(Integer.toString(millis));

        SendCommand(CMD_DELAY);
        if (millis <= 10)
            SendArgument((byte) millis);
        else if (millis <= 100)
            SendArgument((byte) (millis / 10 + 9));
        else if (millis <= 1000)
            SendArgument((byte) (millis / 100 + 18));
        else
            return false;

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Sets the confidence threshold to use for recognition of built-in words or custom grammars.
     *
     * @param knob (0-4) is one of values in #Knob
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean SetKnob(Knob knob) {
        SendCommand(CMD_KNOB);
        SendArgument(knob.getValue());

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Sets the language to use for recognition of built-in words.
     *
     * @param lang (0-5) is one of values in #Language
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean SetLanguage(Language lang) {
        SendCommand(CMD_LANGUAGE);
        SendArgument(lang.getValue());

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Sets the strictness level to use for recognition of custom commands.
     *
     * @param level level (1-5) is one of values in #Level
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean SetLevel(Level level) {
        SendCommand(CMD_LEVEL);
        SendArgument(level.getValue());

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Sets the operating distance of the microphone.
     * This setting represents the distance between the microphone and the
     * user's mouth, in one of three possible configurations.
     *
     * @param distance dist (1-3) is one of values in #Distance
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean SetMicDistance(Distance distance) {
        SendCommand(CMD_MIC_DIST);
        SendArgument(-1);
        SendArgument(distance.getValue());

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Configures an I/O pin as an input with optional pull-up and return its value
     *
     * @param pin    (1-3) is one of values in #PinNumber
     * @param config (2-4) is one of the input values in #PinConfig
     * @return integer is the logical value of the pin
     */
    @SuppressWarnings("WeakerAccess")
    public int SetPinInput(PinNumber pin, PinConfig config) {
        if (config == PinConfig.INPUT_HIZ || config == PinConfig.INPUT_STRONG)
            throw new IllegalArgumentException("Invalid Pin Configuration");

        SendCommand(CMD_QUERY_IO);
        SendArgument(pin.getValue());
        SendArgument(config.getValue());

        if (GetResponse() == STS_PIN)
            return ArgumentEncoding.ConvertArgumentCode(GetResponse());
        return -1;
    }

    /**
     * Configures an I/O pin as an output and sets its value
     *
     * @param pin   (1-3) is one of values in #PinNumber
     * @param value (0-1) is one of the output values in #PinConfig, or Arduino style HIGH and LOW macros
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean SetPinOutput(PinNumber pin, PinConfig value) {
        if (pin.getValue() > 3)
            throw new IllegalArgumentException("Invalid Pin number");

        if (value.getValue() > 1)
            throw new IllegalArgumentException("Invalid output value");

        SendCommand(CMD_QUERY_IO);
        SendArgument(pin.getValue());
        SendArgument(value.getValue());

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Sets the timeout to use for any recognition task.
     *
     * @param seconds (0-31) is the maximum time the module keep listening
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean SetTimeout(int seconds) {
        if (seconds < 0 || seconds > 31) throw new IllegalArgumentException(Integer.toString(seconds));
        SendCommand(CMD_TIMEOUT);
        SendArgument(seconds);

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Sets the trailing silence duration for recognition of built-in words or custom grammars.
     *
     * @param duration (0-31) is the silence duration as defined in #TrailingSilence
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean SetTrailingSilence(TrailingSilence duration) {
        SendCommand(CMD_TRAILING);
        SendArgument(-1);
        SendArgument(duration.getValue());

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Puts the module in sleep mode.
     *
     * @param mode mode is one of values in #WakeMode, optionally combined with one of
     *             the values in #ClapSense
     * @return true if the operation is successful
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean Sleep(WakeMode mode) {
        SendCommand(CMD_SLEEP);
        SendArgument(mode.getValue());

        return GetResponse() == STS_SUCCESS;
    }

    /**
     * Interrupts pending recognition or playback operations.
     *
     * @return True if the request is satisfied and the module is back to ready
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean Stop() {
        SendCommand(CMD_BREAK);

        char rx = GetResponse();
        return rx == STS_INTERR || rx == STS_SUCCESS;
    }

    /**
     * Retrieves the name of a command contained in a custom grammar. It must be called after #dumpGrammar()
     *
     * @return points to a string of least 32 characters that holds the command label when
     * the function returns
     */
    @SuppressWarnings("WeakerAccess")
    public String GetNextWordLabel() {
        String name;

        Character count = ReceiveArgumentAsChar();

        if (count == null) {
            return null;
        }
        if (count == -1)
            count = (char) 32;

        int length = ArgumentEncoding.ConvertArgumentCode(count);

        StringBuilder tempString = new StringBuilder();

        for (int i = 0; i < length; i++) {
            Character rxChar = ReceiveArgumentAsChar();
            if (rxChar == null) {
                return null;
            }
            if (rxChar == '^') {
                rxChar = ReceiveArgumentAsChar();
                if (rxChar == null)
                    return null;
                tempString.append(ArgumentEncoding.ConvertArgumentCode(rxChar));
                --length;
            } else {
                tempString.append(rxChar);
            }
        }

        name = tempString.toString();
        return name;
    }

    /**
     * Starts training of a custom command. Results are available after #hasFinished() returns true.
     * The module is busy until training completes and it cannot accept other commands. You can interrupt training with
     * #stop().
     *
     * @param group (0-16) is the target group, or one of the values in #Groups
     * @param index (0-31) is the index of the command within the selected group
     */
    @SuppressWarnings("WeakerAccess")
    public void TrainCommand(int group, int index) {
        if (group < 0 || group > 16) throw new IllegalArgumentException(Integer.toString(group));
        if (index < 0 || group > 16) throw new IllegalArgumentException(Integer.toString(index));

        SendCommand(CMD_TRAIN_SD);
        SendArgument(group);
        SendArgument(index);
    }

    /**
     * Verifies training of a custom command (useful after import). Similarly to #trainCommand(), you should check results
     * after #hasFinished() returns true
     *
     * @param group (0-16) is the target group, or one of the values in #Groups
     * @param index (0-31) is the index of the command within the selected group
     */
    @SuppressWarnings("WeakerAccess")
    public void VerifyCommand(byte group, byte index) {
        if (group < 0 || group > 31) throw new IllegalArgumentException(Integer.toString(group));
        if (index < 0 || index > 31) throw new IllegalArgumentException(Integer.toString(index));

        SendCommand(CMD_SERVICE);
        SendArgument(SVC_VERIFY_SD);
        SendArgument(group);
        SendArgument(index);
    }

    private char GetResponse() {
        return GetResponse(DEF_TIMEOUT);
    }

    private void ReadStatus(char rx) {
        _status.V = 0;
        Value = 0;

        switch (rx) {
            case STS_SUCCESS:
                return;

            case STS_SIMILAR:

                _status.Builtin = true;
                rx = ReceiveArgumentAsChar();
                Value = rx;
                return;

            case STS_RESULT:
                _status.Command = true;

                rx = ReceiveArgumentAsChar();
                Value = rx;

                return;

            case STS_TOKEN:
                _status.Token = true;

                rx = ReceiveArgumentAsChar();
                Value = rx << 5;

                rx = ReceiveArgumentAsChar();
                Value |= rx;

                return;

            case STS_AWAKEN:
                _status.Awakened = true;
                return;

            case STS_TIMEOUT:
                _status.Timeout = true;
                return;

            case STS_INVALID:
                _status.Invalid = true;
                return;

            case STS_ERROR:
                _status.Error = true;

                rx = ReceiveArgumentAsChar();
                Value = rx << 4;

                rx = ReceiveArgumentAsChar();
                Value |= rx;

                return;
        }

        // unexpected condition (communication error)
        _status.V = 0;
        _status.Error = true;
        Value = 0;
    }

    public class DumpGrammarResult {
        byte flags;
        int count;
    }

    private class Status {
        public Boolean Awakened;
        public Boolean Builtin;
        public Boolean Command;
        public Boolean Conflict;
        public Boolean Error;
        public Boolean Invalid;
        public Boolean Memfull;
        public Boolean Timeout;
        public Boolean Token;
        public byte V;
    }
}
