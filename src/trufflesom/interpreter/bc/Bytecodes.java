/**
 * Copyright (c) 2017 Michael Haupt, github@haupz.de
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package trufflesom.interpreter.bc;

import java.util.stream.Stream;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;


public class Bytecodes {

  // Bytecodes used by the simple object machine
  public static final byte HALT              = 0;
  public static final byte DUP               = 1;
  public static final byte PUSH_LOCAL        = 2;
  public static final byte PUSH_ARGUMENT     = 3;
  public static final byte PUSH_FIELD        = 4;
  public static final byte PUSH_BLOCK        = 5;
  public static final byte PUSH_BLOCK_NO_CTX = 6;
  public static final byte PUSH_CONSTANT     = 7;
  public static final byte PUSH_GLOBAL       = 8;
  public static final byte POP               = 9;
  public static final byte POP_LOCAL         = 10;
  public static final byte POP_ARGUMENT      = 11;
  public static final byte POP_FIELD         = 12;
  public static final byte SEND              = 13;
  public static final byte SUPER_SEND        = 14;
  public static final byte RETURN_LOCAL      = 15;
  public static final byte RETURN_NON_LOCAL  = 16;
  public static final byte RETURN_SELF       = 17;

  public static final byte INC = 18;
  public static final byte DEC = 19;

  public static final byte INC_FIELD      = 20;
  public static final byte INC_FIELD_PUSH = 21;

  public static final byte JUMP                  = 22;
  public static final byte JUMP_ON_TRUE_TOP_NIL  = 23;
  public static final byte JUMP_ON_FALSE_TOP_NIL = 24;
  public static final byte JUMP_ON_TRUE_POP      = 25;
  public static final byte JUMP_ON_FALSE_POP     = 26;
  public static final byte JUMP_BACKWARDS        = 27;

  public static final byte JUMP2                  = 28;
  public static final byte JUMP2_ON_TRUE_TOP_NIL  = 29;
  public static final byte JUMP2_ON_FALSE_TOP_NIL = 30;
  public static final byte JUMP2_ON_TRUE_POP      = 31;
  public static final byte JUMP2_ON_FALSE_POP     = 32;
  public static final byte JUMP2_BACKWARDS        = 33;

  public static final byte Q_PUSH_GLOBAL = 34;
  public static final byte Q_SEND        = 35;
  public static final byte Q_SEND_1      = 36;
  public static final byte Q_SEND_2      = 37;
  public static final byte Q_SEND_3      = 38;

  public static final byte INVALID = -1;

  public static final byte NUM_JUMP_BYTECODES = 6;

  private static final String[] PADDED_BYTECODE_NAMES;
  private static final String[] BYTECODE_NAMES;

  public static final byte NUM_BYTECODES;

  private static void checkBytecodeIndex(final byte bytecode) {
    if (bytecode < 0 || bytecode >= NUM_BYTECODES) {
      throw new IllegalArgumentException("illegal bytecode: " + bytecode);
    }
  }

  public static String getBytecodeName(final byte bytecode) {
    checkBytecodeIndex(bytecode);
    return BYTECODE_NAMES[bytecode];
  }

  public static String getPaddedBytecodeName(final byte bytecode) {
    checkBytecodeIndex(bytecode);
    return PADDED_BYTECODE_NAMES[bytecode];
  }

  public static int getBytecodeLength(final byte bytecode) {
    return BYTECODE_LENGTH[bytecode];
  }

  // Static array holding lengths of each bytecode
  @CompilationFinal(dimensions = 1) private static final int[] BYTECODE_LENGTH;

  static {
    NUM_BYTECODES = Q_SEND_3 + 1;

    PADDED_BYTECODE_NAMES = new String[] {
        "HALT            ", "DUP             ", "PUSH_LOCAL      ",
        "PUSH_ARGUMENT   ", "PUSH_FIELD      ",
        "PUSH_BLOCK      ", "PUSH_BLOCK_NO_CTX",
        "PUSH_CONSTANT   ", "PUSH_GLOBAL     ", "POP             ",
        "POP_LOCAL       ", "POP_ARGUMENT    ", "POP_FIELD       ",
        "SEND            ", "SUPER_SEND      ", "RETURN_LOCAL    ",
        "RETURN_NON_LOCAL",

        "RETURN_SELF     ",

        "INC             ",
        "DEC             ",

        "INC_FIELD       ",
        "INC_FIELD_PUSH  ",

        "JUMP            ",
        "JUMP_ON_TRUE_TOP_NIL",
        "JUMP_ON_FALSE_TOP_NIL",
        "JUMP_ON_TRUE_POP",
        "JUMP_ON_FALSE_POP",
        "JUMP_BACKWARDS  ",

        "JUMP2           ",
        "JUMP2_ON_TRUE_TOP_NIL",
        "JUMP2_ON_FALSE_TOP_NIL",
        "JUMP2_ON_TRUE_POP",
        "JUMP2_ON_FALSE_POP",
        "JUMP2_BACKWARDS ",

        "Q_PUSH_GLOBAL   ",
        "Q_SEND          ",
        "Q_SEND_1        ",
        "Q_SEND_2        ",
        "Q_SEND_3        ",
    };

    assert PADDED_BYTECODE_NAMES.length == NUM_BYTECODES : "Inconsistency between number of bytecodes and defined padded names";

    BYTECODE_NAMES = Stream.of(PADDED_BYTECODE_NAMES).map(String::trim).toArray(String[]::new);

    BYTECODE_LENGTH = new int[] {
        1, // HALT
        1, // DUP
        3, // PUSH_LOCAL
        3, // PUSH_ARGUMENT
        3, // PUSH_FIELD
        2, // PUSH_BLOCK
        2, // PUSH_BLOCK_NO_CTX
        2, // PUSH_CONSTANT
        2, // PUSH_GLOBAL
        1, // POP
        3, // POP_LOCAL
        3, // POP_ARGUMENT
        3, // POP_FIELD
        2, // SEND
        2, // SUPER_SEND
        1, // RETURN_LOCAL
        2, // RETURN_NON_LOCAL
        1, // RETURN_SELF

        1, // INC
        1, // DEC

        3, // INC_FIELD
        3, // INC_FIELD_PUSH

        3, // JUMP
        3, // JUMP_ON_TRUE_TOP_NIL
        3, // JUMP_ON_FALSE_TOP_NIL
        3, // JUMP_ON_TRUE_POP
        3, // JUMP_ON_FALSE_POP
        3, // JUMP_BACKWARDS

        3, // JUMP2
        3, // JUMP2_ON_TRUE_TOP_NIL
        3, // JUMP2_ON_FALSE_TOP_NIL
        3, // JUMP2_ON_TRUE_POP
        3, // JUMP2_ON_FALSE_POP
        3, // JUMP2_BACKWARDS

        2, // Q_PUSH_GLOBAL
        2, // Q_SEND
        2, // Q_SEND_1
        2, // Q_SEND_2
        2, // Q_SEND_3
    };

    assert BYTECODE_LENGTH.length == NUM_BYTECODES : "The BYTECODE_LENGTH array is not having the same size as number of bytecodes";
  }
}
