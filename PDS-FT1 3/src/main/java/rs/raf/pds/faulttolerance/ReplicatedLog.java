package rs.raf.pds.faulttolerance;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReplicatedLog {

	public static interface LogReplicator {
		public void replicateOnFollowers(Long entryAtIndex, byte[] data);
	}
	
	Long lastLogEntryIndex = 0L;
	final LogReplicator node;
	FileOutputStream fs;
	OutputStreamWriter writer;
	String fileName;
	
	public ReplicatedLog(String fileName, LogReplicator node) throws FileNotFoundException {
		this.node = node;
		fs = new FileOutputStream(fileName);
		this.fileName = fileName;
		writer = new OutputStreamWriter(fs);
	}
		
	public void appendAndReplicate(byte[] data) throws IOException {
		Long lastLogEntryIndex = appendToLocalLog(data);
		node.replicateOnFollowers(lastLogEntryIndex, data);  
	}
	
	protected Long appendToLocalLog(byte[] data) throws IOException {
		String s = new String(data);
		System.out.println("Log #"+lastLogEntryIndex+":"+s);
		
		//fs.write(data);

		//fs.flush();
		writer.write(s);
		writer.write("\r\n");
		writer.flush();
		fs.flush();
		
		return ++lastLogEntryIndex;
	}

	protected Long getLastLogEntryIndex() {
		return lastLogEntryIndex;
	}

	// Function to read all log entries from the log file
	public List<byte[]> readLogEntries() throws IOException {
		List<byte[]> logEntries = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				logEntries.add(line.getBytes());
			}
		}

		return logEntries;
	}
	// Function to read a specific log entry by index
	public byte[] readLogEntryByIndex(Long index) throws IOException {
		if (index < 1 || index > lastLogEntryIndex) {
			throw new IllegalArgumentException("Invalid log index");
		}

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			long currentIndex = 0;
			while ((line = br.readLine()) != null) {
				currentIndex++;
				if (currentIndex == index) {
					return line.getBytes();
				}
			}
		}

		return null;
	}


	
}
