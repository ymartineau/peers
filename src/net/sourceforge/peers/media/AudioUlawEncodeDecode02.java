package net.sourceforge.peers.media;

/*File AudioUlawEncodeDecode02.java
Copyright 2003, R.G.Baldwin

This program is designed to illustrate 8-bit ULAW
encoding and decoding, and to compare that
process with simple truncation from sixteen to
eight bits.

The 8-bit ULAW values produced by the program
match the values produced by Sun's ULAW encoding
algorithm.  The 16-bit values produced by
decoding the ULAW values match the values
produced by two different decoding algorithm
implementations that I found on the web and
used for verifying this program.

The program produces two sets of output data. The
first set shows the numeric results of simply
truncating a series of sample values from sixteen
bits to eight bits, preserving the eight most
significant bits, and then restoring those
truncated values back into a 16-bit format.

The second set of output data shows the numeric
results of encoding the same set of sample values
into 8-bit ULAW values, and then decoding those
ULAW values back into 16-bit values.

The results of the truncation experiment are
shown in the following table.  Each line in this
output shows the original sample value, the
16-bit representation of the truncated value,the
difference between the original sample value and
the truncated value, and that difference (error)
expressed as a percent of the original sample
value.  (Because of the difficulties encountered
when dividing by zero, a percent error was not
computed for a sample value of zero.)

Process and display truncation
0 0 0
1 0 1 100.0%
2 0 2 100.0%
3 0 3 100.0%
4 0 4 100.0%
5 0 5 100.0%
7 0 7 100.0%
9 0 9 100.0%
13 0 13 100.0%
17 0 17 100.0%
25 0 25 100.0%
33 0 33 100.0%
49 0 49 100.0%
65 0 65 100.0%
97 0 97 100.0%
129 0 129 100.0%
193 0 193 100.0%
257 256 1 0.38910505%
385 256 129 33.506493%
513 512 1 0.19493178%
769 768 1 0.130039%
1025 1024 1 0.09756097%
1537 1536 1 0.06506181%
2049 2048 1 0.048804294%
3073 3072 1 0.03254149%
4097 4096 1 0.024408104%
6145 6144 1 0.016273392%
8193 8192 1 0.012205541%
12289 12288 1 0.008137358%
16385 16384 1 0.006103143%
24577 24576 1 0.004068845%


The results of the ULAW experiment are shown in
the following table.  Each line in this output
shows the original sample value, the ULAW byte
value in hex notation, the 16-bit value produced
by decoding the ULAW byte, the difference between
the original sample value and the decoded value,
and that difference (error) expressed as a
percent of the original sample value.  (Because
of the difficulties encountered when dividing by
zero, a percent error was not computed for a
sample value of zero.)

Process and display ULAW
0 0xff 0 0
1 0xff 0 1 100.0%
2 0xff 0 2 100.0%
3 0xff 0 3 100.0%
4 0xfe 8 -4 -100.0%
5 0xfe 8 -3 -60.0%
7 0xfe 8 -1 -14.285714%
9 0xfe 8 1 11.111111%
13 0xfd 16 -3 -23.076923%
17 0xfd 16 1 5.882353%
25 0xfc 24 1 4.0%
33 0xfb 32 1 3.030303%
49 0xf9 48 1 2.0408163%
65 0xf7 64 1 1.5384616%
97 0xf3 96 1 1.0309278%
129 0xef 132 -3 -2.3255813%
193 0xeb 196 -3 -1.5544041%
257 0xe7 260 -3 -1.1673151%
385 0xdf 396 -11 -2.857143%
513 0xdb 524 -11 -2.1442494%
769 0xd3 780 -11 -1.4304291%
1025 0xcd 1052 -27 -2.6341465%
1537 0xc5 1564 -27 -1.7566688%
2049 0xbe 2108 -59 -2.8794534%
3073 0xb6 3132 -59 -1.919948%
4097 0xaf 4092 5 0.12204052%
6145 0xa7 6140 5 0.08136696%
8193 0x9f 8316 -123 -1.5012816%
12289 0x97 12412 -123 -1.0008951%
16385 0x8f 16764 -379 -2.3130913%
24577 0x87 24956 -379 -1.5420922%

Tested using SDK 1.4.1 under Win2000
************************************************/

public class AudioUlawEncodeDecode02{
  static int value = 0;
  static int increment = 1;
  static int limit = 4;
  static short shortValue = (short)value;

  public static void main(
                        String args[]){

    System.out.println(
               "Process and display truncation");
    processAndDisplayTruncation();

    System.out.println();
    System.out.println(
                     "Process and display ULAW");
    //Reinitialize values in the processing loop.
    value = 0;
    increment = 1;
    limit = 4;
    shortValue = (short)value;
    processAndDisplayUlaw();

  }//end main
  //-------------------------------------------//

  static short truncate(short sample){
    //Mask 8 lsb
    return (short)(sample & 0xff00);
  }//end truncate
  //-------------------------------------------//

  //This encoding method is loosely based on
  // online material at:
  // http://www.speech.cs.cmu.edu/comp.speech/
  // Section2/Q2.7.html
  static byte encode(short sample){
    final short BIAS = 132;//0x84
    final short CLIP = 32635;//32767-BIAS

    //Convert sample to sign-magnitude
    int sign = sample & 0x8000;
    if(sign != 0){
      sample = (short)-sample;
      sign = 0x80;
    }//end if

    //Because of the bias that is added, allowing
    // a value larger than CLIP would result in
    // integer overflow, so clip it.
    if(sample > CLIP) sample = CLIP;

    //Convert from 16-bit linear PCM to ulaw
    //Adding this bias guarantees a 1 bit in the
    // exponent region of the data, which is the
    // eight bits to the right of the sign bit.
    sample += BIAS;

    //Exponent value is the position of the first
    // 1 to the right of the sign bit in the
    // exponent region of the data.
    //Find the position of the first 1 to the
    // right of the sign bit, counting from right
    // to left in the exponent region.  The
    // exponent position (value) can range from 0
    // to 7.
    //Could use a table lookup but will compute
    // on the fly instead because that is better
    // for teaching the algorithm.
    int exp;
    //Shift sign bit off to the left
    short temp = (short)(sample << 1);
    for(exp = 7; exp > 0; exp--){
      if((temp & 0x8000) != 0) break;//found it
      temp = (short)(temp << 1);//shift and loop
    }//end for loop

    //The mantissa is the four bits to the right
    // of the first 1 bit in the exponent region.
    // Shift those four bits to the four lsb of
    // the 16-bit value.
    temp = (short)(sample >> (exp + 3));
    //Mask and save those four bits
    int mantis = temp & 0x000f;
    //Construct the complement of the ulaw byte.
    //Set the sign bit in the msb of the 8-bit
    // byte.  The value of sign is either 0x00 or
    // 0x80.
    //Position the exponent in the three bits to
    // the right of the sign bit.
    //Set the 4-bit mantissa in the four lsb of
    // the byte.
    //Note that the one's complement of this
    // value will be returned.
    byte ulawByte = (byte)(sign | (exp << 4) |
                                         mantis);
    //Now complement to create actual ulaw byte
    // and return it.
    return (byte)~ulawByte;
  }//end encode
  //-------------------------------------------//

  //This decode method is loosely based on
  // material at:
  // http://web.umr.edu/~dcallier/school/
  // 311_final_report.doc
  //That material was published by David Callier
  // and Chess Combites as a semester project and
  // has been reformulated into Java code by this
  // author..
  static short decode(byte ulawByte){
    //Perform one's complement to undo the one's
    // complement at the end of the encode
    // algorithm.
    ulawByte = (byte)(~ulawByte);
    //Get the sign bit from the ulawByte
    int sign = ulawByte & 0x80;
    //Get the value of the exponent in the three
    // bytes to the right of the sign bit.
    int exp = (ulawByte & 0x70) >> 4;
    //Get the mantissa by masking off and saving
    // the four lsb in the ulawByte.
    int mantis = ulawByte & 0xf;
    //Construct the 16-bit output value as type
    // int for simplicity and cast to short
    // before returning.
    int rawValue =
               (mantis << (12 - 8 + (exp - 1))) +
                              (132 << exp) - 132;
    //Change the sign if necessary and return
    // the 16-bit estimate of the original
    // sample value.
    return (short)((sign != 0)
                        ? - rawValue : rawValue);
  }//end decode
  //-------------------------------------------//

  static void processAndDisplayTruncation(){
    while((shortValue >= 0) &
                           (shortValue < 32000)){
      short result = truncate(shortValue);
      System.out.print(shortValue + " ");
      System.out.print(result + " ");
      System.out.print(shortValue - result);
      if(shortValue > 0){
        System.out.println(" " +
            ((float)(100.0*(shortValue - result)/
                             shortValue)) + "%");
      }else{
        System.out.println();
      }//end else
      value = value + increment;
      shortValue = (short)value;
      if(value > limit){
        increment *= 2;
        limit *= 2;
      }//end if
      if(increment > 32000)break;
    }//end while loop
  }//end processAndDisplayTruncation
  //-------------------------------------------//

  static void processAndDisplayUlaw(){
    while((shortValue >= 0) &
                           (shortValue < 32000)){
      byte ulawByte = encode(shortValue);
      short result = decode(ulawByte);
      System.out.print(shortValue + " ");
      System.out.print("0x" +
                         Integer.toHexString(
                         ulawByte & 0xff) + " ");
      System.out.print(result + " ");
      System.out.print(shortValue - result);
      if(shortValue > 0){
        System.out.println(" " +
            ((float)(100.0*(shortValue - result)/
                             shortValue)) + "%");
      }else{
        System.out.println();
      }//end else
      value = value + increment;
      shortValue = (short)value;
      if(value > limit){
        increment *= 2;
        limit *= 2;
      }//end if
      if(increment > 32000)break;
    }//end while loop
  }//end processAndDisplayTruncation
  //-------------------------------------------//
}//end class AudioUlawEncodeDecode02.java