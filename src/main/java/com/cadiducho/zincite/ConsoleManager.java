package com.cadiducho.zincite;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

/**
 * A meta-class to handle all logging and input-related console improvements.
 * Based on GlowstoneMC stuff
 */
public final class ConsoleManager {

    private static final Logger logger = Logger.getLogger("");
    private static final String CONSOLE_DATE = "HH:mm:ss";
    private static final String FILE_DATE = "dd/MM/yyyy HH:mm:ss";
    private final ZinciteBot server;

    private boolean running = true;

    public ConsoleManager(ZinciteBot server, boolean enableFileLog) {
        this.server = server;

        if (enableFileLog) {
            for (Handler h : logger.getHandlers()) {
                logger.removeHandler(h);
            }

            // add log handler which writes to console
            logger.addHandler(new FancyConsoleHandler());

            // set system output streams
            System.setOut(new PrintStream(new LoggerOutputStream(Level.INFO), true));
            System.setErr(new PrintStream(new LoggerOutputStream(Level.WARNING), true));
        }
    }

    public void startConsole(boolean enableConsoleReader, boolean enableFileLog) {
        if (enableFileLog) {
            for (Handler handler : logger.getHandlers()) {
                if (handler.getClass() == FancyConsoleHandler.class) {
                    handler.setFormatter(new DateOutputFormatter(CONSOLE_DATE));
                }
            }
        }

        if (enableConsoleReader) {
            startConsoleReader();
        }
    }

    private void startConsoleReader() {
        ConsoleCommandThread consoleCommandThread = new ConsoleCommandThread();
        consoleCommandThread.setName("ZinciteConsoleCommandThread");
        consoleCommandThread.setDaemon(true);
        consoleCommandThread.start();
    }

    public void startFile(String logfile) {
        File parent = new File(logfile).getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            logger.log(Level.WARNING, "Could not create log folder: {0}", parent);
        }
        Handler fileHandler = new RotatingFileHandler(logfile);
        fileHandler.setFormatter(new DateOutputFormatter(FILE_DATE));
        logger.addHandler(fileHandler);
    }

    public void stop() {
        running = false;
        for (Handler handler : logger.getHandlers()) {
            handler.flush();
            handler.close();
        }
    }

    private static class LoggerOutputStream extends ByteArrayOutputStream {
        private final String separator = System.getProperty("line.separator");
        private final Level level;

        public LoggerOutputStream(Level level) {
            this.level = level;
        }

        @Override
        public synchronized void flush() throws IOException {
            super.flush();
            String record = toString();
            reset();

            if (!record.isEmpty() && !record.equals(separator)) {
                logger.logp(level, "LoggerOutputStream", "log" + level, record);
            }
        }
    }

    private static class RotatingFileHandler extends StreamHandler {
        private final SimpleDateFormat dateFormat;
        private final String template;
        private final boolean rotate;
        private String filename;

        public RotatingFileHandler(String template) {
            this.template = template;
            rotate = template.contains("%D");
            dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            filename = calculateFilename();
            updateOutput();
        }

        private void updateOutput() {
            try {
                setOutputStream(new FileOutputStream(filename, true));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Unable to open " + filename + " for writing", ex);
            }
        }

        private void checkRotate() {
            if (rotate) {
                String newFilename = calculateFilename();
                if (!filename.equals(newFilename)) {
                    filename = newFilename;
                    // note that the console handler doesn't see this message
                    super.publish(new LogRecord(Level.INFO, "Log rotating to: " + filename));
                    updateOutput();
                }
            }
        }

        private String calculateFilename() {
            return template.replace("%D", dateFormat.format(new Date()));
        }

        @Override
        public synchronized void publish(LogRecord record) {
            if (!isLoggable(record)) {
                return;
            }
            checkRotate();
            super.publish(record);
            super.flush();
        }

        @Override
        public synchronized void flush() {
            checkRotate();
            super.flush();
        }
    }

    private class ConsoleCommandThread extends Thread {
        @Override
        public void run() {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in))) {
                while (running) {
                    String consoleCommand = buffer.readLine();
                    if (consoleCommand == null || consoleCommand.trim().isEmpty()) {
                        continue;
                    }

                    switch (consoleCommand) {
                        case "stop":
                            server.shutdown();
                            break;
                        case "ping":
                            System.out.println("pong");
                            break;
                        default:
                            System.out.println("Opción no válida.\n");
                    }
                }
            } catch (IOException | IllegalArgumentException ex) {
                logger.log(Level.SEVERE, "Error while reading commands", ex);
            }
        }
    }

    private class FancyConsoleHandler extends ConsoleHandler {
        FancyConsoleHandler() {
            setFormatter(new DateOutputFormatter(CONSOLE_DATE));
            setOutputStream(System.out);
        }
    }

    private class DateOutputFormatter extends Formatter {
        private final SimpleDateFormat date;

        DateOutputFormatter(String pattern) {
            date = new SimpleDateFormat(pattern);
        }

        @Override
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();

            builder.append(date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(formatMessage(record));
            builder.append('\n');

            if (record.getThrown() != null) {
                // StringWriter's close() is trivial
                @SuppressWarnings("resource")
                StringWriter writer = new StringWriter();
                record.getThrown().printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }
    }
}