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
package nl.koziuk.crowsync.systray;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;

import nl.koziuk.crowsync.util.IconUtil;

/**
 * Describes an activity to be logged.
 * 
 * @author marcin
 */
public class Activity {
    public static final int STARTED = 0;
    public static final int STOPPED = 1;
    public static final int RESTARTED = 2;
    public static final int REMOVED = 3;
    public static final int RECEIVED = 4;
    public static final int SENT = 5;

    private static final String[] TYPE_STRINGS = {
            "STARTED", "STOPPED", "RESTARTED", "REMOVED", "RECEIVED", "SENT"
    };
    private static final String[] TYPE_ICONS = {
            "start.png", "stop.png", "restart.png", "trash.png", "received.png", "copied.png"
    };

    private int type;
    private final String filename;
    private final String game;

    private final long timestamp;

    /**
     * Create an activity with the specified type.
     * 
     * @param type The type.
     */
    public Activity(int type) {
        this(type, null, null);
    }

    /**
     * Create an activity with the type and other information.
     * 
     * @param type The type.
     * @param filename The filename the activity was done to.
     * @param game The game it belongs to.
     */
    public Activity(int type, String filename, String game) {
        this(type, filename, game, System.currentTimeMillis());
    }

    /**
     * Re-create an old activity with string arguments.
     * 
     * @param type The type, as a string.
     * @param filename The filename.
     * @param game The game.
     * @param timestame The timestamp, as a string.
     */
    public Activity(String typeStr, String filename, String game, String timestampStr) {
        long timestamp = 0;

        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            // swallow
        }

        this.type = typeFromString(typeStr);
        this.filename = filename;
        this.game = game;
        this.timestamp = timestamp;
    }

    /**
     * Re-create an old activity.
     * 
     * @param type The type.
     * @param filename The filename.
     * @param game The game.
     * @param timestame The timestamp.
     */
    public Activity(int type, String filename, String game, long timestamp) {
        if (type < 0 || type > TYPE_STRINGS.length) {
            throw new IllegalArgumentException("Activity type not in range.");
        }

        this.type = type;
        this.filename = filename;
        this.game = game;
        this.timestamp = timestamp;
    }

    /**
     * Copy activity.
     * 
     * @param activity The activity.
     */
    public Activity(Activity activity) {
        type = activity.type;
        filename = activity.filename;
        game = activity.game;
        timestamp = activity.timestamp;
    }

    /**
     * Get the activity's type.
     * 
     * @return The type.
     */
    public int getType() {
        return type;
    }

    /**
     * Set the activity's type.
     * 
     * @param type The type.
     */
    public void setType(int type) {
        if (type < 0 || type > TYPE_STRINGS.length) {
            throw new IllegalArgumentException("Activity type not in range.");
        }

        this.type = type;
    }

    /**
     * Returns the game an action was performed to.
     * 
     * @return The game.
     */
    public String getGame() {
        return game;
    }

    /**
     * Get the filename the activity worked on.
     * 
     * @return The filename.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Get the timestamp of the activity.
     * 
     * @return The timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the date of the activity.
     * 
     * @return The date.
     */
    public Date getDate() {
        return new Date(timestamp);
    }

    /**
     * Return a date string based on a format.
     * 
     * @param format The date format
     * @return A date string.
     */
    public String getFormattedDate(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(getDate());
    }

    /**
     * Return the name of the activity, based on its type.
     * 
     * @return type The activity name.
     */
    public String getName() {
        return TYPE_STRINGS[type];
    }

    /**
     * Return an ImageIcon representing the type of the activity.
     * 
     * @return An icon that belongs to the activity.
     */
    public ImageIcon getImageIcon() {
        return IconUtil.imageIcon("16x16/" + TYPE_ICONS[type]);
    }

    /**
     * Return the activity description, based on its type.
     * 
     * @return The activity description.
     */
    public String getDescription() {
        final String DATE_FORMAT = "E, dd MMM yyyy 'at' HH:mm";
        switch (type) {
        case STARTED:
            return "Started on " + getFormattedDate(DATE_FORMAT) + ".";
        case STOPPED:
            return "Stopped on " + getFormattedDate(DATE_FORMAT) + ".";
        case RESTARTED:
            return "Restarted on " + getFormattedDate(DATE_FORMAT) + ".";
        case REMOVED:
            return "Removed " + game + " save file \"" + filename + "\".";
        case RECEIVED:
            return "Received " + game + " save file \"" + filename + "\".";
        case SENT:
            return "Sent " + game + " save file \"" + filename + "\".";
        default:
            return "?";
        }
    }

    /**
     * Creates a type integer from a string.
     * 
     * @param string The type as a String.
     * @return The type as an integer.
     */
    private static int typeFromString(String string) {
        for (int i = 0; i < TYPE_STRINGS.length; i++) {
            if (TYPE_STRINGS[i].equalsIgnoreCase(string)) {
                return i;
            }
        }

        throw new IllegalArgumentException("Not a valid type string.");
    }

}
