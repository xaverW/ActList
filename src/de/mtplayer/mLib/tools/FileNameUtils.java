/*
 * MTPlayer Copyright (C) 2017 W. Xaver W.Xaver[at]googlemail.com
 * https://sourceforge.net/projects/mtplayer/
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package de.mtplayer.mLib.tools;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

public class FileNameUtils {
    /**
     * Valid characters for Windows in file names:
     * Based on http://msdn.microsoft.com/en-us/library/windows/desktop/aa365247(v=vs.85).aspx
     */
    public static final String REGEXP_ILLEGAL_CHARACTERS_WINDOWS = "[:\\\\/*?|<>\"]";
    public static final String REGEXP_ILLEGAL_CHARACTERS_WINDOWS_PATH = "[:/*?|<>\"]";
    /**
     * Valid characters for all UNIX-like OS.
     */
    public static final String REGEXP_ILLEGAL_CHARACTERS_OTHERS = "[:\\\\/*|<>]";
    public static final String REGEXP_ILLEGAL_CHARACTERS_OTHERS_PATH = "[:\\\\*|<>]";

    public static String checkDateiname(final String name, final boolean isPath) {
        // dient nur zur Anzeige für Probleme (Textfeld wird rot)
        String ret = name;
        boolean isWindowsPath = false;
        final String splitChar;

        if (SystemInfo.isWindows()) {
            splitChar = "\\\\";
        } else {
            splitChar = "/";
        }

        if (SystemInfo.isWindows()) {
            ret = removeWindowsTrailingDots(ret);
            if (isPath) {
                if (ret.length() > 1 && ret.charAt(1) == ':') {
                    // damit auch "d:" und nicht nur "d:\" als Pfad geht
                    isWindowsPath = true;
                    ret = ret.replaceFirst(":", ""); // muss zum Schluss wieder rein, kann aber so nicht ersetzt werden
                }
            }
        } else {
            ret = removeStartingDots(ret);
        }

        if (isPath && ret.contains(File.separator)) {
            String str = "";
            final String[] sa = ret.split(splitChar); // Regex
            for (String s : sa) {
                if (!s.isEmpty()) {
                    str += File.separator + convertToNativeEncoding(s, false); //sind ja nur noch die Ordnernamen
                }
            }
            if (!ret.startsWith(File.separator)) {
                str = str.replaceFirst(splitChar, ""); // wieder Regex
            }
            if (ret.endsWith(File.separator)) {
                str = str + File.separator;
            }
            ret = str;
        } else {
            ret = convertToNativeEncoding(ret, false);
        }

        if (isWindowsPath) {
            // c: wieder herstellen
            if (ret.length() == 1) {
                ret = ret + ":";
            } else if (ret.length() > 1) {
                ret = ret.charAt(0) + ":" + ret.substring(1);
            }
        }

        return ret;
    }

    /**
     * Remove stray trailing dots from string when we are on Windows OS.
     *
     * @param fileName A filename string that might include trailing dots.
     * @return Cleanup string with no dots anymore.
     */
    private static String removeWindowsTrailingDots(String fileName) {
        // machte unter Win noch Probleme, zB. bei dem Titel: "betrifft: ..."
        // "." und " " am Ende machen Probleme
        while (!fileName.isEmpty() && (fileName.endsWith(".") || fileName.endsWith(" "))) {
            fileName = fileName.substring(0, fileName.length() - 1);
        }
        return fileName;
    }

    /**
     * Remove dots from string when we are on Linux/OS X
     *
     * @param fileName A filename string that might start with dots.
     * @return Cleanup string with no dots anymore.
     */
    private static String removeStartingDots(String fileName) {
        // machte unter OS X/Linux Probleme, zB. bei dem Titel: "....Paula"
        while (!fileName.isEmpty() && (fileName.startsWith("."))) {
            fileName = fileName.substring(1, fileName.length());
        }
        return fileName;
    }

    /**
     * Convert a filename from Java´s native UTF-16 to OS native character encoding.
     *
     * @param fileName The UTF-16 filename string.
     * @return Natively encoded string for the OS.
     */
    public static String convertToNativeEncoding(String fileName, boolean isPath) {
        String ret = fileName;

        ret = removeIllegalCharacters(ret, isPath);

        //convert our filename to OS encoding...
        try {
            final CharsetEncoder charsetEncoder = Charset.defaultCharset().newEncoder();
            charsetEncoder.onMalformedInput(CodingErrorAction.REPLACE); // otherwise breaks on first unconvertable char
            charsetEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            charsetEncoder.replaceWith(new byte[]{'_'});

            final ByteBuffer buf = charsetEncoder.encode(CharBuffer.wrap(ret));
            if (buf.hasArray()) {
                ret = new String(buf.array());
            }

            //remove NUL character from conversion...
            ret = ret.replaceAll("\\u0000", "");
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Convert a filename from Java´s native UTF-16 to US-ASCII character encoding.
     *
     * @param fileName The UTF-16 filename string.
     * @return US-ASCII encoded string for the OS.
     */
    public static String convertToASCIIEncoding(String fileName, boolean isPath) {
        String ret = fileName;

        ret = ret.replace("ä", "ae");
        ret = ret.replace("ö", "oe");
        ret = ret.replace("ü", "ue");
        ret = ret.replace("Ä", "Ae");
        ret = ret.replace("Ö", "Oe");
        ret = ret.replace("Ü", "Ue");
        ret = ret.replace("ß", "ss");

        // ein Versuch zu vereinfachen
        ret = cleanUnicode(ret);

        ret = removeIllegalCharacters(ret, isPath);

        //convert our filename to OS encoding...
        try {
            final CharsetEncoder charsetEncoder = Charset.forName("US-ASCII").newEncoder();
            charsetEncoder.onMalformedInput(CodingErrorAction.REPLACE); // otherwise breaks on first unconvertable char
            charsetEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            charsetEncoder.replaceWith(new byte[]{'_'});

            final ByteBuffer buf = charsetEncoder.encode(CharBuffer.wrap(ret));
            if (buf.hasArray()) {
                ret = new String(buf.array());
            }

            //remove NUL character from conversion...
            ret = ret.replaceAll("\\u0000", "");
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private static String cleanUnicode(String ret) {
        String r = "";
        char c;
        for (int i = 0; i < ret.length(); ++i) {
            c = ret.charAt(i);
            //char hex = ret.charAt(i);
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.BASIC_LATIN) {
                r += c;
            } else if (c == 'ß') {
                r += "ß";
            } else // Buchstaben
            {
                switch (c) {
                    case 'Â':
                    case 'À':
                    case 'Å':
                    case 'Á':
                        r += "A";
                        break;
                    case 'å':
                    case 'á':
                    case 'à':
                    case 'â':
                        r += "a";
                        break;
                    case 'Č':
                        r += "C";
                        break;
                    case 'ć':
                    case 'č':
                    case 'ç':
                        r += "c";
                        break;
                    case 'Đ':
                        r += "D";
                        break;
                    case 'É':
                    case 'È':
                        r += "E";
                        break;
                    case 'é':
                    case 'è':
                    case 'ê':
                    case 'ě':
                    case 'ë':
                        r += "e";
                        break;
                    case 'í':
                        r += "i";
                        break;
                    case 'ñ':
                        r += "n";
                        break;
                    case 'ó':
                    case 'ô':
                    case 'ø':
                        r += "o";
                        break;
                    case 'Š':
                        r += "S";
                        break;
                    case 'ś':
                    case 'š':
                    case 'ş':
                        r += "s";
                        break;
                    case 'ł':
                    case 'Ł':
                        r += "t";
                        break;
                    case 'û':
                    case 'ù':
                        r += "u";
                        break;
                    case 'ý':
                        r += "y";
                        break;
                    case 'Ž':
                    case 'Ź':
                        r += "Z";
                        break;
                    case 'ž':
                    case 'ź':
                        r += "z";
                        break;
                    case 'æ':
                        r += "ae";
                        break;
                    case '–':
                        r += "-";
                        break;
                    case '„':
                        r += "\"";
                        break;
                    case '”':
                    case '“':
                    case '«':
                    case '»':
                        r += "\"";
                        break;
                    case '?':
                        r += "?";
                        break;
                    case '°':
                    case '™':
                        break;
                    case '…':
                        r += "...";
                        break;
                    case '€':
                        r += "€";
                        break;
                    case '´':
                    case '’':
                    case '‘':
                    case '¿':
                        r += "'";
                        break;
                    case '\u0096':
                        r += "-";
                        break;
                    case '\u0085':
                        break;
                    case '\u0080':
                        break;
                    case '\u0084':
                        break;
                    case '\u0092':
                        break;
                    case '\u0093':
                        break;
                    case '\u0091':
                        r += "-";
                        break;
                    case '\n':
                        break;
                    default:
                        r += "_";
                        break;
                }
            }
        }
        return r;
    }

    /**
     * Remove illegal characters from String based on current OS.
     *
     * @param input  The input string
     * @param isPath
     * @return Cleaned-up string.
     */
    public static String removeIllegalCharacters(final String input, boolean isPath) {
        String ret = input;

        switch (Functions.getOs()) {
            case MAC:
            case LINUX:
                //On OSX the VFS take care of writing correct filenames to FAT filesystems...
                //Just remove the default illegal characters
                ret = removeStartingDots(ret);
                ret = ret.replaceAll(isPath ? REGEXP_ILLEGAL_CHARACTERS_OTHERS_PATH : REGEXP_ILLEGAL_CHARACTERS_OTHERS, "_");
                break;

            case WIN64:
            case WIN32:
                //we need to be more careful on Windows when using e.g. FAT32
                //Therefore be more conservative by default and replace more characters.
                ret = removeWindowsTrailingDots(ret);
                ret = ret.replaceAll(isPath ? REGEXP_ILLEGAL_CHARACTERS_WINDOWS_PATH : REGEXP_ILLEGAL_CHARACTERS_WINDOWS, "_");
                break;

            default:
                //we need to be more careful on Linux when using e.g. FAT32
                //Therefore be more conservative by default and replace more characters.
                ret = removeStartingDots(ret);
                ret = ret.replaceAll(isPath ? REGEXP_ILLEGAL_CHARACTERS_WINDOWS_PATH : REGEXP_ILLEGAL_CHARACTERS_WINDOWS, "_");
                break;
        }

        return ret;
    }
}
