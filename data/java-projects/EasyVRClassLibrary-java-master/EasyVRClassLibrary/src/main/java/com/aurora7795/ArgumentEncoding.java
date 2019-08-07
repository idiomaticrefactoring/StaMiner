package com.aurora7795;

import static com.aurora7795.ArgumentCode.*;

public class ArgumentEncoding {
    public static int ConvertArgumentCode(char argumentCode) {
        ArgumentCode tempChar = getArgumentCodeForChar(argumentCode);

        if (tempChar != null) {
            switch (tempChar) {
                case ArgNegOne:
                    return -1;
                case Arg0:
                    return 0;
                case Arg1:
                    return 1;
                case Arg2:
                    return 2;
                case Arg3:
                    return 3;
                case Arg4:
                    return 4;
                case Arg5:
                    return 5;
                case Arg6:
                    return 6;
                case Arg7:
                    return 7;
                case Arg8:
                    return 8;
                case Arg9:
                    return 9;
                case Arg10:
                    return 10;
                case Arg11:
                    return 11;
                case Arg12:
                    return 12;
                case Arg13:
                    return 13;
                case Arg14:
                    return 14;
                case Arg15:
                    return 15;
                case Arg16:
                    return 16;
                case Arg17:
                    return 17;
                case Arg18:
                    return 18;
                case Arg19:
                    return 19;
                case Arg20:
                    return 20;
                case Arg21:
                    return 21;
                case Arg22:
                    return 22;
                case Arg23:
                    return 23;
                case Arg24:
                    return 24;
                case Arg25:
                    return 25;
                case Arg26:
                    return 26;
                case Arg27:
                    return 27;
                case Arg28:
                    return 28;
                case Arg29:
                    return 29;
                case Arg30:
                    return 30;
                case Arg31:
                    return 31;

            }
        }
        throw new IllegalArgumentException();
    }


    public static ArgumentCode IntToArgumentCode(int integer) {
        switch (integer) {
            case -1:
                return ArgNegOne;
            case 0:
                return Arg0;
            case 1:
                return Arg1;
            case 2:
                return Arg2;
            case 3:
                return Arg3;
            case 4:
                return Arg4;
            case 5:
                return Arg5;
            case 6:
                return Arg6;
            case 7:
                return Arg7;
            case 8:
                return Arg8;
            case 9:
                return Arg9;
            case 10:
                return Arg10;
            case 11:
                return Arg11;
            case 12:
                return Arg12;
            case 13:
                return Arg13;
            case 14:
                return Arg14;
            case 15:
                return Arg15;
            case 16:
                return Arg16;
            case 17:
                return Arg17;
            case 18:
                return Arg18;
            case 19:
                return Arg19;
            case 20:
                return Arg20;
            case 21:
                return Arg21;
            case 22:
                return Arg22;
            case 23:
                return Arg23;
            case 24:
                return Arg24;
            case 25:
                return Arg25;
            case 26:
                return Arg26;
            case 27:
                return Arg27;
            case 28:
                return Arg28;
            case 29:
                return Arg29;
            case 30:
                return Arg30;
            case 31:
                return Arg31;

        }
        throw new IllegalArgumentException();
    }

    public static char IntToArgumentChar(int integer) {
        ArgumentCode tempChar = IntToArgumentCode(integer);
        return tempChar.getValue();
    }
}
