package rs.raf.pds.faulttolerance;

import java.io.IOException;
import java.util.List;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import rs.raf.pds.faulttolerance.gRPC.*;
import rs.raf.pds.faulttolerance.gRPC.AccountServiceGrpc.AccountServiceImplBase;


public class AccountServiceGRPCServer extends AccountServiceImplBase  {

	final AccountService service;
	final AppServer node;
	private AccountServiceGrpc.AccountServiceBlockingStub leaderStub;

	protected AccountServiceGRPCServer(AccountService service, AppServer node) {
		this.service = service;
		this.node = node;
		leaderStub =null;
	}
	
	@Override
	public void addAmount(AccountRequest request, StreamObserver<AccountResponse> responseObserver) {
		AccountResponse response;
		if (!node.isLeader()) {
			response = AccountResponse.newBuilder().
					setRequestId(request.getRequestId()).
					setStatus(RequestStatus.UPDATE_REJECTED_NOT_LEADER).
					build();
				}else {
			 
					float amount = service.addAmount(request.getAmount(), true);
					response = AccountResponse.newBuilder().
							setRequestId(request.getRequestId()).
							setStatus(RequestStatus.STATUS_OK).
							setBalance(amount).
							build();
		}
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
	
	@Override
	public void witdrawAmount(AccountRequest request, StreamObserver<AccountResponse> responseObserver) {
		AccountResponse response = null;
		if (!node.isLeader()) {
			response = AccountResponse.newBuilder().
					setRequestId(request.getRequestId()).
					setStatus(RequestStatus.UPDATE_REJECTED_NOT_LEADER).
					build();
				}else {
			
					float amount = service.witdrawAmount(request.getAmount(), true);
					if (amount<0) {
						response = AccountResponse.newBuilder().
								setRequestId(request.getRequestId()).
								setStatus(RequestStatus.WITDRAWAL_REJECT_NOT_SUFFICIENT_AMOUNT).
								build();
					}
					else {
						response = AccountResponse.newBuilder().
								setRequestId(request.getRequestId()).
								setStatus(RequestStatus.STATUS_OK).
								setBalance(amount).
								build();
					}
		}
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
	@Override
	public void getAmount(AccountRequest request, StreamObserver<AccountResponse> responseObserver) {
	     AccountResponse response = service.getAmount(request);
	     
	     responseObserver.onNext(response);
		 responseObserver.onCompleted(); 
	}
	@Override
	public void appendLog(LogEntry request, StreamObserver<LogResponse> responseObserver) {
		System.out.println("DOBIO SAM NESTO ZA UPIS");
		byte[] data = request.getLogEntryData().toByteArray();
		LogResponse response;
		
		try {
			response = this.service.appendLog(request.getEntryAtIndex(), data);
			if(response.getStatus().equals(LogStatus.LOG_HASNT_LAST_ENTRY)){
				requestMissingLogs(response.getLastEntryIndex());


			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response = LogResponse.newBuilder().
					setStatus(LogStatus.IO_ERROR).
					setEntryAtIndex(request.getEntryAtIndex()).
					build();
		}
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
	@Override
	public void missingEntry(LogRequest request, StreamObserver<LogEntry> responseObserver) {
		System.out.println("LIDER DOBIO ZAHTEV ZA IDEKS: " + request.getLaskKnownIndex()) ;
		long lastKnownIndex = request.getLaskKnownIndex();

		try {
			List<LogEntry> logEntries = this.service.getMissingLogEntries(lastKnownIndex);

			for (LogEntry logEntry : logEntries) {
				responseObserver.onNext(logEntry);

				System.out.println("LIDER POSLAO" + logEntry.getEntryAtIndex()) ;
			}

		} catch (IOException e) {
			e.printStackTrace();

			responseObserver.onError(e);
		}

		responseObserver.onCompleted();
	}
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void requestMissingLogs(Long lastKnownIndex) {
		if (leaderStub == null) {
			initializeLeaderStub();
		}

		LogRequest request = LogRequest.newBuilder()
				.setLaskKnownIndex(lastKnownIndex)
				.build();

		System.out.println("FOLLOWER SALJE ZAHETV ZA LOG") ;

		leaderStub.missingEntry(request, new StreamObserver<LogEntry>() {
			@Override
			public void onNext(LogEntry logEntry) {
				System.out.println("FOLLOWER DOBIJA OD LIDERA" + logEntry.getEntryAtIndex()) ;

				byte[] data = logEntry.getLogEntryData().toByteArray();
                try {
                    service.appendLog(lastKnownIndex +1, data);
					System.out.println("FOLLOWER POKUSAVA DA DODA SVOM LOGU") ;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


			}

			@Override
			public void onError(Throwable throwable) {

				throwable.printStackTrace();
			}

			@Override
			public void onCompleted() {
				// Handle completion of the RPC call
				System.out.println("Finished receiving missing logs from leader.");
			}
		});
	}

	private void initializeLeaderStub() {
		String leaderAddress = node.getLeaaderGRPCAddress();

		ManagedChannel channel = ManagedChannelBuilder.forTarget(leaderAddress)
				.usePlaintext()
				.build();

		leaderStub = AccountServiceGrpc.newBlockingStub(channel);
	}
	@Override
	public void getLeaderInfo(LeaderRequest req, StreamObserver<LeaderInfo> response) {
		LeaderInfo leader = null;
		if (node.isLeader()) {
			leader = LeaderInfo.newBuilder().
					  setImLeader(true).
					  setHostnamePort(node.getMyGRPCAddress()).
					  build();
		}
		else {
			leader = LeaderInfo.newBuilder().
					  setImLeader(false).
					  setHostnamePort(node.getLeaaderGRPCAddress()).
					  build();
		}
		response.onNext(leader);
		response.onCompleted();
	}
}