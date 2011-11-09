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

package nl.koziuk.crowsync.gui;

import java.util.Enumeration;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * For modifying the default UI font.
 * 
 * @author marcin
 */
public class UIFont {

    /**
     * Modify the toolkit's default UI font.
     * 
     * @param font The font to use.
     */
    public static void changeFont(javax.swing.plaf.FontUIResource font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();

        if (font == null) {
            throw new IllegalArgumentException();
        }

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }

}
