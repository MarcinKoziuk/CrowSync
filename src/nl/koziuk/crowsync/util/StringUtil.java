/*
 * Copyright (c) 2011, Marcin Koziuk <marcin.koziuk@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package nl.koziuk.crowsync.util;

/**
 * Utility class for operations on a String.
 * 
 * @author marcin
 */
public class StringUtil {

    /**
     * Creates a comma-separated list from an object array.
     * 
     * @param array The array.
     * @return The string containing the list.
     */
    public static String commaStringFromArray(Object[] array) {
        StringBuilder result = new StringBuilder();
        final String COMMA = ", ";
        for (int i = 0; i < array.length; i++) {
            result.append(array[i].toString());
            result.append(COMMA);
        }
        return result.substring(0, result.length() - COMMA.length());

    }
}
