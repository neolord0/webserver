package kr.dogfoot.webserver.parser.util;

import kr.dogfoot.webserver.util.http.HttpString;

import java.util.Date;

public class ByteParser {
    private static byte[][] monthes =
            {{(byte) 'J', (byte) 'a', (byte) 'n'},
                    {(byte) 'F', (byte) 'e', (byte) 'b'},
                    {(byte) 'M', (byte) 'a', (byte) 'r'},
                    {(byte) 'A', (byte) 'p', (byte) 'r'},
                    {(byte) 'M', (byte) 'a', (byte) 'y'},
                    {(byte) 'J', (byte) 'u', (byte) 'n'},
                    {(byte) 'J', (byte) 'u', (byte) 'l'},
                    {(byte) 'A', (byte) 'u', (byte) 'g'},
                    {(byte) 'S', (byte) 'e', (byte) 'p'},
                    {(byte) 'O', (byte) 'c', (byte) 't'},
                    {(byte) 'N', (byte) 'o', (byte) 'v'},
                    {(byte) 'D', (byte) 'e', (byte) 'c'}};

    protected static void error(String mth, String msg)
            throws ParserException {
        throw new ParserException(mth + ": " + msg);
    }

    public static final int parseInt(byte[] buf, int radix, ParseState ps) throws ParserException {
        int off = -1;
        if (ps.isSkipable)
            ps.start = off = skipSpaces(buf, ps);
        else
            ps.start = off = ps.ioff;
        // Parse the integer from byte[] straight (without creating Strings)
        int len = (ps.bufend > 0) ? ps.bufend : buf.length;
        int ret = 0;
        int oldret = 0;
        boolean neg = false;
        if (buf[off] == HttpString.Negative) {
            neg = true;
            off++;
        }
        while (off < len) {
            int digit = ((int) buf[off]) & 0xff;
            if ((digit >= (byte) '0') && (digit <= (byte) '9')) {
                ret = ret * radix + (digit - (byte) '0');
            } else if (radix >= 10) {
                if ((digit >= 'A') && (digit <= 'Z')) {
                    if ((digit - 'A') + 10 < radix)
                        ret = ret * radix + (digit - 'A' + 10);
                    else
                        break;
                } else if ((digit >= 'a') && (digit <= 'z')) {
                    if ((digit - 'a') + 10 < radix)
                        ret = ret * radix + digit - 'a' + 10;
                    else
                        break;
                } else {
                    break;
                }
            } else {
                break;
            }
            if (ret < oldret) {
                error("parseInt",
                        "Integer overflow: " + new String(buf, 0, ps.start, len));
            } else {
                oldret = ret;
            }
            off++;
        }
        if (ret < oldret) {
            error("parseInt",
                    "Integer overflow: " + new String(buf, 0, ps.start, len));
        }
        // Return, after updating the parsing state:
        ps.ooff = off;
        ps.end = off;
        if (ps.ooff == ps.ioff)
            // We didn't get any number, err
            error("parseInt", "No number available.");
        return neg ? -ret : ret;
    }

    public static final int parseInt(byte[] buf, ParseState ps) throws ParserException {
        return parseInt(buf, 10, ps);
    }

    /**
     * Parse an integer, and return an updated pointer.
     */
    public static final long parseLong(byte[] buf, int radix, ParseState ps) throws ParserException {
        // Skip spaces if needed
        int off = -1;
        if (ps.isSkipable)
            ps.start = off = skipSpaces(buf, ps);
        else
            ps.start = off = ps.ioff;
        // Parse the integer from byte[] straight (without creating Strings)
        int len = (ps.bufend > 0) ? ps.bufend : buf.length;
        long ret = 0;
        long oldret = 0;
        boolean neg = false;
        if (buf[off] == HttpString.Negative) {
            neg = true;
            off++;
        }
        while (off < len) {
            int digit = ((int) buf[off]) & 0xff;
            if ((digit >= (byte) '0') && (digit <= (byte) '9')) {
                ret = ret * radix + (digit - (byte) '0');
            } else if (radix >= 10) {
                if ((digit >= 'A') && (digit <= 'Z')) {
                    if ((digit - 'A') + 10 < radix)
                        ret = ret * radix + (digit - 'A' + 10);
                    else
                        break;
                } else if ((digit >= 'a') && (digit <= 'z')) {
                    if ((digit - 'a') + 10 < radix)
                        ret = ret * radix + digit - 'a' + 10;
                    else
                        break;
                } else {
                    break;
                }
            } else {
                break;
            }
            if (ret < oldret) {
                error("parseLong",
                        "Long overflow: " + new String(buf, 0, ps.start, len));
            } else {
                oldret = ret;
            }
            off++;
        }
        if (ret < oldret) {
            error("parseLong",
                    "Long overflow: " + new String(buf, 0, ps.start, len));
        }
        // Return, after updating the parsing state:
        ps.ooff = off;
        ps.end = off;
        if (ps.ooff == ps.ioff)
            // We didn't get any number, err
            error("parseLong", "No number available.");
        return neg ? -ret : ret;
    }

    public static final long parseLong(byte[] buf, ParseState ps) throws ParserException {
        return parseLong(buf, 10, ps);
    }

    public static boolean unquote(byte[] buf, ParseState ps) {
        int off = -1;
        int len = -1;
        if (ps.isSkipable)
            off = skipSpaces(buf, ps);
        else
            off = ps.ioff;
        len = (ps.bufend > 0) ? ps.bufend : buf.length;
        if ((off < len) && (buf[off] == (byte) '"')) {
            ps.start = ps.ioff = ++off;
            while (off < len) {
                if (buf[off] == (byte) '"') {
                    ps.end = ps.bufend = off;
                    return true;
                } else {
                    off++;
                }
            }
        } else {
            ps.start = off;
            ps.end = len;
        }
        return false;
    }

    public static final int skipSpaces(byte[] buf, ParseState ps) {
        int len = (ps.bufend > 0) ? ps.bufend : buf.length;
        int off = ps.ioff;
        while (off < len) {
            if ((buf[off] != (byte) ' ')
                    && (buf[off] != (byte) '\t')
                    && (buf[off] != ps.separator)) {
                ps.ioff = off;
                return off;
            }
            off++;
        }
        return off;
    }

    public static final int nextItem(byte[] buf, ParseState ps) throws ParserException {
        // Skip leading spaces, if needed:
        int off = -1;
        int len = -1;
        boolean found = false;

        if (ps.isSkipable)
            ps.start = off = skipSpaces(buf, ps);
        else
            ps.start = off = ps.ioff;
        len = (ps.bufend > 0) ? ps.bufend : buf.length;
        // Parse !
        if (off >= len)
            return -1;
        // Setup for parsing, and parse
        ps.start = off;
        loop:
        while (off < len) {
            if (buf[off] == (byte) '"') {
                // A quoted item, receive as one chunk
                off++;
                while (off < len) {
                    if (buf[off] == (byte) '\\') {
                        off += 2;
                    } else if (buf[off] == (byte) '"') {
                        off++;
                        continue loop;
                    } else {
                        off++;
                    }
                }
                if (off == len)
                    error("nextItem", "Un-terminated quoted item.");
            } else if ((buf[off] == ps.separator)
                    || (ps.spaceIsSep
                    && ((buf[off] == ' ') || (buf[off] == '\t')))) {
                found = true;
                break loop;
            }
            off++;
        }
        ps.end = off;
        // Content start is set, we are right at the end of item
        if (ps.isSkipable) {
            ps.ioff = off;
            ps.ooff = skipSpaces(buf, ps);
        }
        // Check for either the end of the list, or the separator:
        if (ps.ooff < ps.bufend) {
            if (buf[ps.ooff] == ps.separator)
                ps.ooff++;
        }

        if (ps.permitNoSeparator == false && found == false) {
            return -1;
        }
        return (ps.end > ps.start) ? ps.start : -1;
    }

    private final static byte lowerCase(int x) {
        if ((x >= 'A') && (x <= 'Z'))
            x = (x - 'A' + 'a');
        return (byte) (x & 0xff);
    }

    public static int parseMonth(byte[] buf, ParseState ps) throws ParserException {
        int off = -1;
        if (ps.isSkipable)
            off = ps.start = skipSpaces(buf, ps);
        else
            off = ps.start = ps.ioff;
        int len = (ps.bufend > 0) ? ps.bufend : buf.length;
        if (len < 3) {
            error("parseMonth", "Invalid month name (too short).");
            // NOT REACHED
            return -1;
        }
        // Compare to get the month:
        for (int i = 0; i < monthes.length; i++) {
            int mo = off;
            byte[] m = monthes[i];
            boolean ok = true;
            month_loop:
            for (int j = 0; j < m.length; j++, mo++) {
                if (lowerCase(m[j]) != lowerCase(buf[mo])) {
                    ok = false;
                    break month_loop;
                }
            }
            if (ok) {
                if (mo - off == m.length) {
                    // Skip remaining chars of month
                    off += 3;
                    while (off < len) {
                        byte l = lowerCase(buf[off++]);
                        if ((l < 'a') || (l > 'z'))
                            break;
                    }
                    ps.ooff = ps.end = off;
                }
                return i;
            }
        }
        error("parseMonth", "Invalid month name (unknown).");
        // NOT REACHED
        return -1;
    }

    public static long parseDeltaSecond(byte[] buf, ParseState ps) throws ParserException {
        return parseInt(buf, ps);
    }

    public static long parseDate(byte[] buf, ParseState ps) throws ParserException {
        int d = -1;
        int m = -1;
        int y = -1;
        int hh = -1;
        int mm = -1;
        int ss = -1;
        // My prefered argument as to why HTTP is broken
        ParseState it = ParseState.pooledObject();
        it.ioff = ps.ioff;
        it.bufend = ((ps.bufend > -1) ? ps.bufend : buf.length);
        ps.permitNoSeparator = false;
        // Skip the day name:
        if (nextItem(buf, ps) < 0)
            error("parseDate", "Invalid date format (no day)");
        ps.prepare();
        int off = skipSpaces(buf, ps);
        // First fork:
        if ((buf[off] >= (byte) '0') && (buf[off] <= (byte) '9')) {
            // rfc 1123, or rfc 1036
            d = parseInt(buf, ps);
            ps.prepare();
            if (buf[ps.ioff] == (byte) ' ') {
                // rfc 1123
                m = parseMonth(buf, ps);
                ps.prepare();
                if ((y = parseInt(buf, ps) - 1900) < 0)
                    y += 1900;
                ps.prepare();
                ps.separator = (byte) ':';
                hh = parseInt(buf, ps);
                ps.prepare();
                mm = parseInt(buf, ps);
                ps.prepare();
                ss = parseInt(buf, ps);
            } else {
                // rfc 1036
                ps.separator = (byte) '-';
                m = parseMonth(buf, ps);
                ps.prepare();
                y = parseInt(buf, ps);
                ps.prepare();
                ps.separator = (byte) ':';
                hh = parseInt(buf, ps);
                ps.prepare();
                mm = parseInt(buf, ps);
                ps.prepare();
                ss = parseInt(buf, ps);
            }
        } else {
            m = parseMonth(buf, ps);
            ps.prepare();
            d = parseInt(buf, ps);
            ps.prepare();
            ps.separator = (byte) ':';
            hh = parseInt(buf, ps);
            ps.prepare();
            mm = parseInt(buf, ps);
            ps.prepare();
            ss = parseInt(buf, ps);
            ps.prepare();
            ps.separator = (byte) ' ';
            y = parseInt(buf, ps) - 1900;
        }
        ParseState.release(it);
        return Date.UTC(y, m, d, hh, mm, ss);
    }

    public static double parseQuality(byte[] buf, ParseState ps) throws ParserException {
        // Skip spaces if needed
        int off = -1;
        if (ps.isSkipable)
            ps.start = off = skipSpaces(buf, ps);
        else
            ps.start = off = ps.ioff;
        // Parse the integer from byte[] straight (without creating Strings)
        int len = (ps.bufend > 0) ? ps.bufend : buf.length;
        String str = new String(buf, 0, off, len - off);
        try {
            return Double.valueOf(str).doubleValue();
        } catch (Exception ex) {
            error("parseQuality", "Invalid floating point number.");
        }
        // Not reached:
        return 1.0;
    }
}
