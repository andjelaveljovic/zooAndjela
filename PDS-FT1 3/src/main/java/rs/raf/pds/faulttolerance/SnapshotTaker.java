package rs.raf.pds.faulttolerance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SnapshotTaker {
    private static final long SNAPSHOT_INTERVAL_MS = 6000;
    private Timer timer;
    AccountService as;
    String fileName;

    public SnapshotTaker() {
        this.timer = new Timer(true);

    }



    public void start(AccountService accService, String logFileName) {
        timer.scheduleAtFixedRate(new SnapshotTask(), SNAPSHOT_INTERVAL_MS, SNAPSHOT_INTERVAL_MS);
        this.as = accService;
        this.fileName= logFileName;
    }

    private class SnapshotTask extends TimerTask {
        @Override
        public void run() {
            as.takeSnapshot();

            System.out.println("Taking snapshot...");
        }
    }
    private void clearLogFile(String fileName) {
        try {
            File logFile = new File(fileName);
            FileWriter fw = new FileWriter(logFile);
            fw.write("");
            fw.close();
            System.out.println("Log file cleared: " + fileName);
        } catch (IOException e) {
            System.err.println("Error clearing log file: " + e.getMessage());
        }
    }
}

