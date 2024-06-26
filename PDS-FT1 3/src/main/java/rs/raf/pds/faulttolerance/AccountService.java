package rs.raf.pds.faulttolerance;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import rs.raf.pds.faulttolerance.commands.AddValueCommand;
import rs.raf.pds.faulttolerance.commands.Command;
import rs.raf.pds.faulttolerance.commands.SubValueCommand;
import rs.raf.pds.faulttolerance.gRPC.*;

public class AccountService {
	private static final String SNAPSHOT_FILE_PATH = "accSnapshot.ser";

	ByteArrayInputStream bais;
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	DataInputStream dis = new DataInputStream(bais);
	DataOutputStream dos = new DataOutputStream(baos);


	boolean isLeader = false;
	float amount = 0.0f;
	ReplicatedLog log;

	public AccountService(ReplicatedLog log) {
		this.log = log;
	}
	public float addAmount(float value, boolean applyLog) {

		if (applyLog) {
			AddValueCommand command = new AddValueCommand(value);
			appendCommandToLogAndReplicate(command);
		}
		amount += value;

		return amount;
	}
	public float witdrawAmount(float value, boolean applyLog) {

		if (amount>=value) {

			if (applyLog) {
				SubValueCommand command = new SubValueCommand(value);
				appendCommandToLogAndReplicate(command);
			}

			amount -= value;

			return amount;

		}else
		{
			return -1;

		}

	}


	public AccountResponse getAmount(AccountRequest req) {
		AccountResponse resp = AccountResponse.newBuilder()
				.setRequestId(req.getRequestId())
				.setBalance(amount)
				.setStatus(RequestStatus.STATUS_OK)
				.build();

		return resp;
	}
	public void appendCommandToLogAndReplicate(Command command) {
		try {
			//command.serialize(dos);
			//log.appendAndReplicate(baos.toByteArray());
			//baos.reset();

			log.appendAndReplicate(command.writeToString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public List<LogEntry> getMissingLogEntries(long lastKnownIndex) throws IOException {
		long currentLogIndex = log.getLastLogEntryIndex();
		List<LogEntry> logEntries = new ArrayList<>();

		for (long i = lastKnownIndex ; i <= currentLogIndex; i++) {
			byte[] data = log.readLogEntryByIndex(i); // Method to get log entry by index
			if (data != null) {
				LogEntry logEntry = LogEntry.newBuilder()
						.setEntryAtIndex(i)
						.setLogEntryData(ByteString.copyFrom(data))
						.build();
				logEntries.add(logEntry);
			}
		}

		return logEntries;
	}


	public LogResponse appendLog(Long entryIndex, byte[] data) throws IOException {
		if (log.getLastLogEntryIndex()<(entryIndex-1)) {
			LogResponse response = LogResponse.newBuilder().
									setStatus(LogStatus.LOG_HASNT_LAST_ENTRY).
									setLastEntryIndex(log.lastLogEntryIndex).
									setEntryAtIndex(entryIndex).
									build();
			return response;
		}

		log.appendToLocalLog(data);

		//DataInputStream ds = new DataInputStream(new ByteArrayInputStream(data));
		Scanner sc = new Scanner(new String(data));

		//int commandType = ds.readInt();
		int commandType = sc.nextInt();

		if (commandType == Command.AddValueType) {
			//AddValueCommand command = AddValueCommand.deserialize(ds);
			AddValueCommand command = new AddValueCommand(sc.nextFloat());
			addAmount(command.getValue(), false);

		}else if (commandType == Command.SubValueType) {
			//SubValueCommand command = SubValueCommand.deserialize(ds);
			SubValueCommand command = new SubValueCommand(sc.nextFloat());
			witdrawAmount(command.getValue(), false);
		}

		LogResponse response = LogResponse.newBuilder().
				setStatus(LogStatus.LOG_OK).
				setEntryAtIndex(entryIndex).
				build();

		return response;
	}

	public void setServerState(boolean isLeader) {
		this.isLeader = isLeader;
	}


	public long getLastIndex() {
		return log.lastLogEntryIndex;
	}


	public synchronized void takeSnapshot() {
		try {
			FileOutputStream fileOut = new FileOutputStream(SNAPSHOT_FILE_PATH);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);

			// Save amount and last log index
			SnapshotData snapshotData = new SnapshotData(amount, log.getLastLogEntryIndex());
			out.writeObject(snapshotData);

			out.close();
			fileOut.close();
			System.out.println("Snapshot saved successfully");
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
	public void loadSnapshot() {
		try {
			File snapshotFile = new File(SNAPSHOT_FILE_PATH);
			if (!snapshotFile.exists() || snapshotFile.length() == 0) {
				// If snapshot file doesn't exist or is empty, initialize to default values
				this.amount = 0.0f;
				log.lastLogEntryIndex = 0L;
				System.out.println("Snapshot file is empty or doesn't exist. Initializing values to 0.");
				return;
			}

			FileInputStream fileIn = new FileInputStream(snapshotFile);
			ObjectInputStream in = new ObjectInputStream(fileIn);

			// Read the object from the stream
			Object obj = in.readObject();

			if (obj instanceof SnapshotData) {
				SnapshotData snapshotData = (SnapshotData) obj;
				this.amount = snapshotData.getAmount();
				log.lastLogEntryIndex = snapshotData.getLastLogIndex();
			} else {
				System.out.println("Unexpected object type in snapshot file");
				// Handle the situation where the object read is not of expected type
				this.amount = 0.0f;
				log.lastLogEntryIndex = -1L;
			}

			System.out.println("Loaded last AMOUNT: " + amount);
			System.out.println("Loaded last LOG INDEX: " + log.lastLogEntryIndex);

			in.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException e) {
			// Handle exceptions
			e.printStackTrace();
		}
	}


	private static class SnapshotData implements Serializable {
		private final float amount;
		private final long lastLogIndex;

		public SnapshotData(float amount, long lastLogIndex) {
			this.amount = amount;
			this.lastLogIndex = lastLogIndex;
		}

		public float getAmount() {
			return amount;
		}

		public long getLastLogIndex() {
			return lastLogIndex;
		}
	}

}
