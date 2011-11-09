package nl.koziuk.crowsync.persist;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import nl.koziuk.crowsync.systray.Activity;
import nl.koziuk.crowsync.util.OSUtil;

public class LogFile {
    private final String filename;

    private List<Activity> log = new LinkedList<Activity>();

    /**
     * Creates LogFile object and tries to open an existing log.
     * 
     * @param filename The path to the log file.
     * @throws IOExecption
     */
    public LogFile(String filename) throws IOException {
        this.filename = filename;

        FileInputStream fstream = null;

        try {
            fstream = new FileInputStream(filename);
            parseLog(fstream);
        } catch (FileNotFoundException e) {
            File f;
            f = new File(filename);
            if (!f.exists()) {
                f.createNewFile();
            }
        } finally {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e) {
                    // om nom nom!
                }
            }
        }
    }

    /**
     * Saves the log list to the log file.
     */
    public void save() throws IOException {
        FileOutputStream fstream = null;

        try {
            fstream = new FileOutputStream(filename);
            writeLog(fstream);
        } catch (FileNotFoundException e) {
            File f;
            f = new File(filename);
            if (!f.exists()) {
                f.createNewFile();
                fstream = new FileOutputStream(f);
                writeLog(fstream);
            }
        } finally {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e) {
                    // om nom nom!
                }
            }
        }
    }

    /**
     * Returns the log.
     * 
     * @return The log list.
     */
    public List<Activity> getLog() {
        return log;
    }

    /**
     * Set the log.
     * 
     * @param log The log list.
     */
    public void setLog(List<Activity> log) {
        this.log = log;
    }

    /**
     * Writes the log.
     * 
     * @param fstream The file output stream.
     * @throws IOException
     */
    private void writeLog(FileOutputStream fstream) throws IOException {
        PrintWriter printWriter = new PrintWriter(fstream);

        // log format:
        // 123456, type, Game, Filename, Message
        for (Activity activity : log) {
            String newline = OSUtil.isWindows() ? "\r\n" : "\n";

            String line = Long.toString(activity.getTimestamp()) + ", "
                    + escape(activity.getName()) + ", "
                    + escape(activity.getGame()) + ", "
                    + escape(activity.getFilename()) + ", "
                    + escape(activity.getDescription());
            printWriter.write(line + newline);
        }

        printWriter.close();
    }

    /**
     * Parses log with regular expressions.
     * 
     * @param fstream The file input stream.
     * @throws IOException
     */
    private void parseLog(FileInputStream fstream) throws IOException {
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String line;

        log.clear();

        while ((line = bufferedReader.readLine()) != null) {
            String[] splitted = line.split("(?<!\\\\),");
            if (splitted.length == 5) {
                String timestamp = splitted[0].trim();
                String type = unescape(splitted[1]).trim();
                String game = unescape(splitted[2]).trim();
                String filename = unescape(splitted[3]).trim();
                // String message = unescape(splitted[4]).trim(); (unused)

                Activity activity = new Activity(type, filename, game, timestamp);
                log.add(activity);
            }
        }
    }

    /**
     * Escapes " from a string.
     * 
     * @param unescaped The string to be escaped.
     * @return The escaped string.
     */
    private static String escape(String unescaped) {
        if (unescaped == null) {
            return null;
        }

        String unescaped1 = unescaped.replaceAll("\\\\", "\\\\\\\\");
        return unescaped1.replaceAll(",", "\\\\,");
    }

    /**
     * Unescapes " from a string.
     * 
     * @param escaped The escaped string.
     * @return The unescaped string.
     */
    private static String unescape(String escaped) {
        if (escaped == null) {
            return null;
        }

        String escaped1 = escaped.replaceAll("\\\\,", ",");
        return escaped1.replaceAll("\\\\\\\\", "\\\\");
    }
}
