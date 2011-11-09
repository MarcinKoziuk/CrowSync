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
package nl.koziuk.crowsync;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This exception is thrown when a problem is encountered that couldn't be
 * handled by crow.
 * 
 * @author marcin
 */
public class CrowSyncException extends RuntimeException {
    private final String description;
    private final Exception wrappedException;

    /**
     * Create an exception without an existing exception.
     * 
     * @param description The problem's description.
     */
    public CrowSyncException(String description) {
        this(description, new RuntimeException());
    }

    /**
     * Creates the exception.
     * 
     * @param description The problem's description.
     */
    public CrowSyncException(String description, Exception wrappedException) {
        this.description = description;
        this.wrappedException = wrappedException;
    }

    /**
     * Returns the problem's description.
     * 
     * @return The problem's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a string containing the wrapped exception's stacktrace.
     * 
     * @return The wrapped exception's stacktrace.
     */
    public String getStackTraceString() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        wrappedException.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * Returns the original exception.
     * 
     * @return The original exception.
     */
    public Exception getWrappedException() {
        return wrappedException;
    }
}
