package com.aurora7795;

/**
 * Created by Martin Bradford Gago on 09/02/2017.
 */


public enum ArgumentCode {
    ArgNegOne('@'),
    Arg0('A'),
    Arg1('B'),
    Arg2('C'),
    Arg3('D'),
    Arg4('E'),
    Arg5('F'),
    Arg6('G'),
    Arg7('H'),
    Arg8('I'),
    Arg9('J'),
    Arg10('K'),
    Arg11('L'),
    Arg12('M'),
    Arg13('N'),
    Arg14('O'),
    Arg15('P'),
    Arg16('Q'),
    Arg17('R'),
    Arg18('S'),
    Arg19('T'),
    Arg20('U'),
    Arg21('V'),
    Arg22('W'),
    Arg23('X'),
    Arg24('Y'),
    Arg25('Z'),
    Arg26('^'),
    Arg27('['),
    Arg28('\\'),
    Arg29(']'),
    Arg30('_'),
    Arg31('`');

    private char value;

    ArgumentCode(char b) {
        this.value = b;
    }

    public static ArgumentCode getArgumentCodeForChar(final char argumentChar)
    {
        for (ArgumentCode type : ArgumentCode.values())
            if (type.value == argumentChar)
                return type;

        return null;
    }

    public char getValue() {
        return value;
    }
}
