/* The following code was generated by JFlex 1.4.1 on 11/11/05 10:20 */

package commons;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.1
 * on 11/11/05 10:20 from the specification file
 * <tt>src/org/pargres/parser/Parser_ini.flex</tt>
 */
class LexIni {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = {
     0,  0,  0,  0,  0,  0,  0,  0,  0, 30, 33,  0,  0, 34,  0,  0, 
     0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
    30,  0,  0, 28,  0,  0,  0, 29, 32, 32, 39, 26, 39, 31, 25, 39, 
    24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 39, 39, 36, 38, 37,  0, 
     0, 13,  6, 14, 16, 11,  1, 10, 18,  8, 27, 20, 15, 23,  9,  2, 
    19, 27,  3,  4,  7,  5, 22, 17, 12, 21, 27,  0,  0,  0,  0, 28, 
     0, 13,  6, 14, 16, 11,  1, 10, 18,  8, 27, 20, 15, 23,  9,  2, 
    19, 27,  3,  4,  7,  5, 22, 17, 12, 21, 27,  0, 35,  0,  0,  0
  };

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\22\2\1\3\2\4\1\5\1\6\1\4"+
    "\2\6\1\1\2\4\3\2\1\7\5\2\1\10\2\2"+
    "\1\11\1\12\7\2\1\13\15\2\1\0\1\14\1\0"+
    "\1\15\1\0\1\16\1\17\1\20\1\21\1\22\5\2"+
    "\1\23\7\2\1\24\3\2\1\25\3\2\1\26\1\27"+
    "\1\30\1\31\1\32\12\2\1\33\1\34\1\0\1\35"+
    "\2\2\1\36\2\2\1\37\1\2\1\40\1\41\1\42"+
    "\2\2\1\43\4\2\1\44\2\2\1\45\1\46\2\2"+
    "\1\47\1\50\1\2\1\51\1\2\1\14\1\0\1\52"+
    "\1\53\6\2\1\54\3\2\1\55\1\2\1\56\1\2"+
    "\1\57\2\2\1\60\3\2\1\61\1\62\1\2\1\63"+
    "\2\2\1\64\1\2\1\65\2\2\1\66\5\2\1\67"+
    "\1\70\1\71\2\0\1\72\46\0\1\73\27\0\1\74"+
    "\23\0";

  private static int [] zzUnpackAction() {
    int [] result = new int[275];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\50\0\120\0\170\0\240\0\310\0\360\0\u0118"+
    "\0\u0140\0\u0168\0\u0190\0\u01b8\0\u01e0\0\u0208\0\u0230\0\u0258"+
    "\0\u0280\0\u02a8\0\u02d0\0\u02f8\0\u0320\0\u0348\0\50\0\u0370"+
    "\0\u0398\0\u03c0\0\50\0\u03e8\0\u0410\0\u0438\0\u0460\0\u0488"+
    "\0\u04b0\0\u04d8\0\u0500\0\u0528\0\u0550\0\u0578\0\u05a0\0\u05c8"+
    "\0\240\0\u05f0\0\u0618\0\240\0\u0640\0\u0668\0\u0690\0\u06b8"+
    "\0\u06e0\0\u0708\0\u0730\0\u0758\0\u0780\0\u07a8\0\u07d0\0\u07f8"+
    "\0\u0820\0\u0848\0\u0870\0\u0898\0\u08c0\0\u08e8\0\u0910\0\u0938"+
    "\0\u0960\0\u0988\0\u0348\0\u09b0\0\u0370\0\50\0\u09d8\0\50"+
    "\0\50\0\50\0\50\0\240\0\u0a00\0\u0a28\0\u0a50\0\u0a78"+
    "\0\u0aa0\0\240\0\u0ac8\0\u0af0\0\u0b18\0\u0b40\0\u0b68\0\u0b90"+
    "\0\u0bb8\0\240\0\u0be0\0\u0c08\0\u0c30\0\240\0\u0c58\0\u0c80"+
    "\0\u0ca8\0\240\0\240\0\240\0\240\0\240\0\u0cd0\0\u0cf8"+
    "\0\u0d20\0\u0d48\0\u0d70\0\u0d98\0\u0dc0\0\u0de8\0\u0e10\0\u0e38"+
    "\0\240\0\240\0\u0e60\0\240\0\u0e88\0\u0eb0\0\240\0\u0ed8"+
    "\0\u0f00\0\240\0\u0f28\0\240\0\240\0\240\0\u0f50\0\u0f78"+
    "\0\u0fa0\0\u0fc8\0\u0ff0\0\u1018\0\u1040\0\240\0\u1068\0\u1090"+
    "\0\240\0\240\0\u10b8\0\u10e0\0\240\0\240\0\u1108\0\240"+
    "\0\u1130\0\u1158\0\u1158\0\240\0\240\0\u1180\0\u11a8\0\u11d0"+
    "\0\u11f8\0\u1220\0\u1248\0\240\0\u1270\0\u1298\0\u12c0\0\240"+
    "\0\u12e8\0\240\0\u1310\0\240\0\u1338\0\u1360\0\240\0\u1388"+
    "\0\u13b0\0\u13d8\0\240\0\240\0\u1400\0\240\0\u1428\0\u1450"+
    "\0\240\0\u1478\0\240\0\u14a0\0\u14c8\0\240\0\u14f0\0\u1518"+
    "\0\u1540\0\u1568\0\u1590\0\240\0\240\0\240\0\u15b8\0\u15e0"+
    "\0\240\0\u1608\0\u1630\0\u1658\0\u1680\0\u16a8\0\u16d0\0\u16f8"+
    "\0\u1720\0\u1748\0\u1770\0\u1798\0\u17c0\0\u17e8\0\u1810\0\u1838"+
    "\0\u1860\0\u1888\0\u18b0\0\u18d8\0\u1900\0\u1928\0\u1950\0\u1978"+
    "\0\u19a0\0\u19c8\0\u19f0\0\u1a18\0\u1a40\0\u1a68\0\u1a90\0\u1ab8"+
    "\0\u1ae0\0\u1b08\0\u1b30\0\u1b58\0\u1b80\0\u1ba8\0\u1bd0\0\50"+
    "\0\u1bf8\0\u1c20\0\u1c48\0\u1c70\0\u1c98\0\u1cc0\0\u1ce8\0\u1d10"+
    "\0\u1d38\0\u1d60\0\u1d88\0\u1db0\0\u1dd8\0\u1e00\0\u1e28\0\u1e50"+
    "\0\u1e78\0\u1ea0\0\u1ec8\0\u1ef0\0\u1f18\0\u1f40\0\u1f68\0\50"+
    "\0\u1f90\0\u1fb8\0\u1fe0\0\u2008\0\u2030\0\u2058\0\u2080\0\u20a8"+
    "\0\u20d0\0\u20f8\0\u2120\0\u2148\0\u2170\0\u2198\0\u21c0\0\u21e8"+
    "\0\u2210\0\u2238\0\u2260";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[275];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11"+
    "\1\12\1\13\1\14\1\15\1\5\1\16\1\17\1\20"+
    "\1\21\1\22\1\23\4\5\1\24\1\25\1\26\1\27"+
    "\1\5\1\2\1\30\1\31\1\32\1\27\1\33\1\34"+
    "\1\35\1\36\1\37\2\27\51\0\1\5\1\40\1\41"+
    "\11\5\1\42\13\5\2\0\2\5\14\0\2\5\1\43"+
    "\25\5\2\0\2\5\14\0\30\5\2\0\2\5\14\0"+
    "\1\5\1\44\2\5\1\45\5\5\1\46\15\5\2\0"+
    "\2\5\14\0\3\5\1\47\24\5\2\0\2\5\14\0"+
    "\12\5\1\50\11\5\1\51\3\5\2\0\2\5\14\0"+
    "\2\5\1\52\16\5\1\53\6\5\2\0\2\5\14\0"+
    "\3\5\1\54\4\5\1\55\17\5\2\0\2\5\14\0"+
    "\1\5\1\56\2\5\1\57\23\5\2\0\2\5\14\0"+
    "\2\5\1\60\25\5\2\0\2\5\14\0\3\5\1\61"+
    "\4\5\1\62\2\5\1\63\2\5\1\64\11\5\2\0"+
    "\2\5\14\0\3\5\1\65\4\5\1\66\5\5\1\67"+
    "\6\5\1\70\2\5\2\0\2\5\14\0\1\5\1\71"+
    "\12\5\1\72\13\5\2\0\2\5\14\0\7\5\1\73"+
    "\20\5\2\0\2\5\14\0\7\5\1\74\2\5\1\75"+
    "\1\5\1\76\13\5\2\0\2\5\14\0\21\5\1\77"+
    "\6\5\2\0\2\5\14\0\14\5\1\100\13\5\2\0"+
    "\2\5\14\0\7\5\1\101\4\5\1\102\13\5\2\0"+
    "\2\5\43\0\1\25\1\103\46\0\1\104\17\0\35\105"+
    "\1\106\12\105\36\0\1\31\50\0\1\107\51\0\1\33"+
    "\51\0\1\110\51\0\1\111\1\112\47\0\1\113\2\0"+
    "\2\5\1\114\25\5\2\0\2\5\14\0\1\5\1\115"+
    "\26\5\2\0\2\5\14\0\16\5\1\116\11\5\2\0"+
    "\2\5\14\0\17\5\1\117\10\5\2\0\2\5\14\0"+
    "\26\5\1\120\1\5\2\0\2\5\14\0\5\5\1\121"+
    "\20\5\1\122\1\5\2\0\2\5\14\0\16\5\1\123"+
    "\11\5\2\0\2\5\14\0\12\5\1\124\15\5\2\0"+
    "\2\5\14\0\6\5\1\125\21\5\2\0\2\5\14\0"+
    "\4\5\1\126\23\5\2\0\2\5\14\0\12\5\1\127"+
    "\15\5\2\0\2\5\14\0\6\5\1\130\10\5\1\131"+
    "\10\5\2\0\2\5\14\0\6\5\1\132\21\5\2\0"+
    "\2\5\14\0\16\5\1\133\11\5\2\0\2\5\14\0"+
    "\1\5\1\134\26\5\2\0\2\5\14\0\15\5\1\135"+
    "\12\5\2\0\2\5\14\0\17\5\1\136\10\5\2\0"+
    "\2\5\14\0\6\5\1\137\1\140\20\5\2\0\2\5"+
    "\14\0\3\5\1\141\24\5\2\0\2\5\14\0\15\5"+
    "\1\142\12\5\2\0\2\5\14\0\17\5\1\143\4\5"+
    "\1\144\3\5\2\0\2\5\14\0\16\5\1\145\11\5"+
    "\2\0\2\5\14\0\11\5\1\146\16\5\2\0\2\5"+
    "\14\0\4\5\1\147\7\5\1\150\13\5\2\0\2\5"+
    "\14\0\3\5\1\151\24\5\2\0\2\5\14\0\23\5"+
    "\1\152\2\5\1\153\1\5\2\0\2\5\14\0\3\5"+
    "\1\154\24\5\2\0\2\5\14\0\3\5\1\155\24\5"+
    "\2\0\2\5\14\0\6\5\1\156\21\5\2\0\2\5"+
    "\14\0\12\5\1\157\15\5\2\0\2\5\14\0\25\5"+
    "\1\160\2\5\2\0\2\5\14\0\10\5\1\161\17\5"+
    "\2\0\2\5\14\0\13\5\1\162\14\5\2\0\2\5"+
    "\26\0\1\163\14\0\1\104\17\0\40\107\1\0\1\33"+
    "\1\34\1\0\4\107\1\0\26\5\1\164\1\5\2\0"+
    "\2\5\14\0\3\5\1\165\24\5\2\0\2\5\14\0"+
    "\12\5\1\166\15\5\2\0\2\5\14\0\12\5\1\167"+
    "\15\5\2\0\2\5\14\0\3\5\1\170\24\5\2\0"+
    "\2\5\14\0\12\5\1\171\15\5\2\0\2\5\14\0"+
    "\2\5\1\172\25\5\2\0\2\5\14\0\20\5\1\173"+
    "\7\5\2\0\2\5\14\0\12\5\1\174\15\5\2\0"+
    "\2\5\14\0\10\5\1\175\17\5\2\0\2\5\14\0"+
    "\1\5\1\176\10\5\1\177\15\5\2\0\2\5\14\0"+
    "\7\5\1\200\20\5\2\0\2\5\14\0\16\5\1\201"+
    "\11\5\2\0\2\5\14\0\4\5\1\202\23\5\2\0"+
    "\2\5\14\0\14\5\1\203\13\5\2\0\2\5\14\0"+
    "\2\5\1\204\25\5\2\0\2\5\14\0\3\5\1\205"+
    "\24\5\2\0\2\5\14\0\12\5\1\206\15\5\2\0"+
    "\2\5\14\0\10\5\1\207\17\5\2\0\2\5\14\0"+
    "\16\5\1\210\11\5\2\0\2\5\14\0\12\5\1\211"+
    "\15\5\2\0\2\5\14\0\12\5\1\212\15\5\2\0"+
    "\2\5\14\0\7\5\1\213\20\5\2\0\2\5\14\0"+
    "\6\5\1\214\21\5\2\0\2\5\14\0\15\5\1\215"+
    "\12\5\2\0\2\5\14\0\12\5\1\216\15\5\2\0"+
    "\2\5\14\0\2\5\1\217\5\5\1\220\17\5\2\0"+
    "\2\5\14\0\7\5\1\221\20\5\2\0\2\5\43\0"+
    "\1\222\1\0\1\223\4\0\1\223\11\0\12\5\1\224"+
    "\15\5\2\0\2\5\14\0\2\5\1\225\25\5\2\0"+
    "\2\5\14\0\6\5\1\226\21\5\2\0\2\5\14\0"+
    "\15\5\1\227\12\5\2\0\2\5\14\0\12\5\1\230"+
    "\15\5\2\0\2\5\14\0\2\5\1\231\25\5\2\0"+
    "\2\5\14\0\15\5\1\232\12\5\2\0\2\5\14\0"+
    "\7\5\1\233\20\5\2\0\2\5\14\0\22\5\1\234"+
    "\5\5\2\0\2\5\14\0\22\5\1\235\5\5\2\0"+
    "\2\5\14\0\14\5\1\236\13\5\2\0\2\5\14\0"+
    "\6\5\1\237\21\5\2\0\2\5\14\0\6\5\1\240"+
    "\21\5\2\0\2\5\14\0\12\5\1\241\15\5\2\0"+
    "\2\5\14\0\6\5\1\242\21\5\2\0\2\5\14\0"+
    "\7\5\1\243\20\5\2\0\2\5\14\0\12\5\1\244"+
    "\15\5\2\0\2\5\14\0\10\5\1\245\17\5\2\0"+
    "\2\5\43\0\1\222\20\0\2\5\1\246\25\5\2\0"+
    "\2\5\14\0\6\5\1\247\21\5\2\0\2\5\14\0"+
    "\12\5\1\250\15\5\2\0\2\5\14\0\25\5\1\251"+
    "\2\5\2\0\2\5\14\0\14\5\1\252\13\5\2\0"+
    "\2\5\14\0\1\253\27\5\2\0\2\5\14\0\12\5"+
    "\1\254\15\5\2\0\2\5\14\0\15\5\1\255\12\5"+
    "\2\0\2\5\14\0\3\5\1\256\24\5\2\0\2\5"+
    "\14\0\3\5\1\257\24\5\2\0\2\5\14\0\10\5"+
    "\1\260\17\5\2\0\2\5\14\0\11\5\1\261\16\5"+
    "\2\0\2\5\14\0\7\5\1\262\20\5\2\0\2\5"+
    "\14\0\10\5\1\263\17\5\2\0\2\5\14\0\14\5"+
    "\1\264\13\5\2\0\2\5\14\0\6\5\1\265\21\5"+
    "\2\0\2\5\14\0\6\5\1\266\21\5\2\0\2\5"+
    "\14\0\15\5\1\267\12\5\2\0\2\5\14\0\15\5"+
    "\1\270\12\5\2\0\2\5\14\0\10\5\1\271\17\5"+
    "\2\0\2\5\14\0\16\5\1\272\11\5\2\0\2\5"+
    "\14\0\1\5\1\273\26\5\2\0\2\5\14\0\12\5"+
    "\1\274\15\5\2\0\2\5\14\0\6\5\1\275\21\5"+
    "\2\0\2\5\14\0\11\5\1\276\16\5\2\0\2\5"+
    "\14\0\30\5\2\0\2\5\1\277\1\300\12\0\2\5"+
    "\1\301\25\5\2\0\2\5\43\0\1\302\5\0\1\277"+
    "\46\0\1\277\1\300\41\0\1\302\4\0\1\303\1\304"+
    "\15\0\1\305\11\0\1\306\1\0\1\307\1\310\1\311"+
    "\2\0\1\312\1\0\1\313\6\0\1\303\15\0\1\314"+
    "\11\0\1\315\1\0\1\316\1\317\1\320\2\0\1\321"+
    "\1\0\1\322\5\0\1\303\1\304\24\0\1\323\47\0"+
    "\1\324\47\0\1\325\1\0\1\326\45\0\1\327\36\0"+
    "\1\330\60\0\1\331\36\0\1\332\5\0\1\333\52\0"+
    "\1\334\47\0\1\335\47\0\1\336\1\0\1\337\45\0"+
    "\1\340\36\0\1\341\60\0\1\342\36\0\1\343\5\0"+
    "\1\344\55\0\1\345\42\0\1\346\54\0\1\347\56\0"+
    "\1\350\35\0\1\351\41\0\1\352\57\0\1\352\43\0"+
    "\1\353\47\0\1\354\5\0\1\355\46\0\1\356\42\0"+
    "\1\357\54\0\1\360\56\0\1\361\35\0\1\362\41\0"+
    "\1\363\57\0\1\363\43\0\1\364\47\0\1\365\5\0"+
    "\1\366\32\0\1\367\54\0\1\370\55\0\1\371\56\0"+
    "\1\350\26\0\1\350\53\0\1\372\45\0\1\373\61\0"+
    "\1\374\32\0\1\375\54\0\1\376\55\0\1\377\67\0"+
    "\1\u0100\1\361\35\0\1\361\26\0\1\361\53\0\1\u0101"+
    "\45\0\1\u0102\61\0\1\u0103\41\0\1\u0104\43\0\1\u0105"+
    "\62\0\1\u0106\51\0\1\350\34\0\1\u0106\53\0\1\u0107"+
    "\45\0\1\u0108\43\0\1\u0109\62\0\1\u010a\51\0\1\361"+
    "\34\0\1\u010a\53\0\1\u010b\54\0\1\350\32\0\1\326"+
    "\57\0\1\350\45\0\1\u010c\56\0\1\361\32\0\1\337"+
    "\57\0\1\361\45\0\1\u010d\47\0\1\u010e\47\0\1\u010f"+
    "\46\0\1\u0110\47\0\1\u0111\44\0\1\u0112\47\0\1\u0113"+
    "\71\0\1\350\47\0\1\361\20\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[8840];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\1\11\24\1\1\11\3\1\1\11\47\1\1\0"+
    "\1\1\1\0\1\11\1\0\4\11\47\1\1\0\37\1"+
    "\1\0\53\1\2\0\1\1\46\0\1\11\27\0\1\11"+
    "\23\0";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[275];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
  private ParserIni yyparser;   

  public LexIni(java.io.Reader r, ParserIni yyparser) {
    this(r);
    this.yyparser = yyparser;
  }


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  LexIni(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  LexIni(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzPushbackPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead,
                                            zzBuffer.length-zzEndRead);

    if (numRead < 0) {
      return true;
    }
    else {
      zzEndRead+= numRead;
      return false;
    }
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = zzPushbackPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void zzDoEOF() throws java.io.IOException {
    if (!zzEOFDone) {
      zzEOFDone = true;
      yyclose();
    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public int yylex() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      boolean zzR = false;
      for (zzCurrentPosL = zzStartRead; zzCurrentPosL < zzMarkedPosL;
                                                             zzCurrentPosL++) {
        switch (zzBufferL[zzCurrentPosL]) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          yycolumn = 0;
          zzR = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          zzR = false;
          yycolumn++;
        }
      }

      if (zzR) {
        // peek one character ahead if it is \n (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof) 
            zzPeek = false;
          else 
            zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = zzLexicalState;


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 23: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_AND;
          }
        case 61: break;
        case 20: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_NOT;
          }
        case 62: break;
        case 8: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_BY;
          }
        case 63: break;
        case 54: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_EXTRACT;
          }
        case 64: break;
        case 14: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_VERTBAR;
          }
        case 65: break;
        case 11: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_AS;
          }
        case 66: break;
        case 26: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_AVG;
          }
        case 67: break;
        case 4: 
          { return (int) yycharat(0);
          }
        case 68: break;
        case 24: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_ANY;
          }
        case 69: break;
        case 9: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_IS;
          }
        case 70: break;
        case 49: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_NULLIF;
          }
        case 71: break;
        case 40: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_DATE;
          }
        case 72: break;
        case 27: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_MIN;
          }
        case 73: break;
        case 2: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_NAME;
          }
        case 74: break;
        case 15: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_DIFERENTE;
          }
        case 75: break;
        case 35: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_NULL;
          }
        case 76: break;
        case 17: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_MAIOR_IG;
          }
        case 77: break;
        case 38: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_LIKE;
          }
        case 78: break;
        case 45: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_COUNT;
          }
        case 79: break;
        case 42: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_FALSE;
          }
        case 80: break;
        case 5: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_PLIC;
          }
        case 81: break;
        case 18: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_FOR;
          }
        case 82: break;
        case 31: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_USER;
          }
        case 83: break;
        case 43: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_ORDER;
          }
        case 84: break;
        case 58: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_INDICATOR;
          }
        case 85: break;
        case 47: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_WHERE;
          }
        case 86: break;
        case 33: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_THEN;
          }
        case 87: break;
        case 3: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_INTNUM;
          }
        case 88: break;
        case 55: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_COALESCE;
          }
        case 89: break;
        case 36: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_ELSE;
          }
        case 90: break;
        case 50: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_ESCAPE;
          }
        case 91: break;
        case 37: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_CASE;
          }
        case 92: break;
        case 29: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_FROM;
          }
        case 93: break;
        case 39: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_DESC;
          }
        case 94: break;
        case 51: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1;  
 	      this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_EXISTS;
          }
        case 95: break;
        case 44: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_GROUP;
          }
        case 96: break;
        case 1: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;
	      this.yyparser.yyerror("ParserIni Syntax error"); return -1;
          }
        case 97: break;
        case 25: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_ALL;
          }
        case 98: break;
        case 16: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_MENOR_IG;
          }
        case 99: break;
        case 56: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_DISTINCT;
          }
        case 100: break;
        case 30: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_SOME;
          }
        case 101: break;
        case 59: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_INTERVAL2;
          }
        case 102: break;
        case 19: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_SUM;
          }
        case 103: break;
        case 21: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_END;
          }
        case 104: break;
        case 41: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_WHEN;
          }
        case 105: break;
        case 32: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_TRUE;
          }
        case 106: break;
        case 57: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_SUBSTRING;
          }
        case 107: break;
        case 48: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_SELECT;
          }
        case 108: break;
        case 53: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_BETWEEN;
          }
        case 109: break;
        case 22: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_ASC;
          }
        case 110: break;
        case 10: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_IN;
          }
        case 111: break;
        case 7: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_OR;
          }
        case 112: break;
        case 13: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_STRING;
          }
        case 113: break;
        case 34: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_INTO;
          }
        case 114: break;
        case 28: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_MAX;
          }
        case 115: break;
        case 46: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_LIMIT;
          }
        case 116: break;
        case 12: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_APPROXNUM;
          }
        case 117: break;
        case 52: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_HAVING;
          }
        case 118: break;
        case 60: 
          { this.yyparser.line = yyline + 1 ; this.yyparser.column = yycolumn + 1 ;              
              this.yyparser.yylval = new ParserIniVal(yytext()); return ParserIni.TK_INTERVAL1;
          }
        case 119: break;
        case 6: 
          { 
          }
        case 120: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            zzDoEOF();
              { return 0; }
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
