package rs.raf.pds.faulttolerance;

import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import rs.raf.pds.faulttolerance.gRPC.*;
import rs.raf.pds.zookeeper.core.SyncPrimitive;

public class AppClient extends SyncPrimitive {
	
	final String appRoot;
	String leaderNodeName = null;
	String leaderHostNamePort;
	
	ManagedChannel channel = null;
	AccountServiceGrpc.AccountServiceBlockingStub blockingStub = null;
			
	protected AppClient(String zkAddress, String appRoot) throws KeeperException, InterruptedException {
		super(zkAddress);
		this.appRoot = appRoot;
	}
	@Override
	public void process(WatchedEvent event) {
		try {
			checkLeader();


		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean checkCurrentLeader(String leaderHostNamePort) {
		/*LeaderRequest request = LeaderRequest.newBuilder().build();
		try {
			LeaderInfo leaderInfo = blockingStub.getLeaderInfo(request);
			return leaderInfo.getHostnamePort().equals(leaderHostNamePort);
		} catch (Exception e) {
			// Log the error
			System.err.println("Error checking current leader: " + e.getMessage());
			return false; // Indicate that the leader check failed
		}*/
		ManagedChannel channel = null;
		try {
			String[] splits = leaderHostNamePort.split(":");
			channel = ManagedChannelBuilder.forAddress(splits[0], Integer.parseInt(splits[1]))
					.usePlaintext()
					.build();

			AccountServiceGrpc.AccountServiceBlockingStub blockingStub = AccountServiceGrpc.newBlockingStub(channel);
			LeaderRequest request = LeaderRequest.newBuilder().build();
			LeaderInfo leaderInfo = blockingStub.getLeaderInfo(request);
			if(leaderInfo.getHostnamePort().equals(leaderHostNamePort)){
				return true;




			} else {
				System.out.println("Leader is not active.");
				return false;
			}
		} catch (Exception e) {
			System.out.println("Error while checking leader activity: " + e.getMessage());
			return false;
		} finally {
			// Ensure proper shutdown of the channel
			if (channel != null) {
				channel.shutdown();
			}
		}
	}

	public synchronized void checkLeader() throws KeeperException, InterruptedException {
		//Thread.sleep(100);
    	List<String> list = zk.getChildren(appRoot, false);
        System.out.println("There are total:"+list.size()+ " replicas for elections!");
        for (int i=0; i<list.size(); i++)
        	System.out.print("NODE:"+list.get(i)+", ");
        System.out.println();

        if (list.size() == 0) {
            System.out.println("0 Elemenata ? ");
        } else {
			System.out.println("Posle totala, prvi deo");
			Integer minValue = Integer.parseInt(list.get(0).substring(AppServer.REPLICA_NODE_SEQUENCE_INDEX));
			String minNodeName = list.get(0);

			for (int i = 1; i < list.size(); i++) {

				Integer tempValue = Integer.parseInt(list.get(i).substring(AppServer.REPLICA_NODE_SEQUENCE_INDEX));
				if (minValue > tempValue) {
					minValue = tempValue;
					minNodeName = list.get(i);
				}
			}
			if (leaderNodeName == null || !minNodeName.equals(leaderNodeName) ) {
				System.out.println("Posle totala, drugi deo postavljanja novog lidera");
				leaderNodeName = minNodeName;
				byte[] b = zk.getData(appRoot + "/" + leaderNodeName, true, null);
				leaderHostNamePort = new String(b);

				System.out.println("Leader je " + leaderNodeName);
				System.out.println("**************");
				//blockingStub = getBlockingStub(leaderHostNamePort);
			}
			else if(!checkCurrentLeader(leaderHostNamePort)){
				System.out.println("Leader is not active yet. Waiting...");

				Thread.sleep(3000);
				checkLeader();

			}else{
				blockingStub = getBlockingStub(leaderHostNamePort);

				}
			}

				//if (channel != null)
				//	channel.shutdown();


			}





public  AccountServiceGrpc.AccountServiceBlockingStub getBlockingStub(String hostNamePort){//grpc za odredjeni port i host, tj od naseg do lidera, znaci klijent sa serverom komunicira preko grpc-a
	String[] splits = hostNamePort.split(":");
	channel = ManagedChannelBuilder.forAddress(splits[0], Integer.parseInt(splits[1]))
			.usePlaintext()
			.build();

	return AccountServiceGrpc.newBlockingStub(channel);
}
/*
	private void inviteServer() throws KeeperException, InterruptedException {
		for (int i=0; i<10; i++) {
	        try { 
	        	
	        	inviteServerFunctions(blockingStub);
	        		
	        	Thread.sleep(2000);
	        	
	        	System.out.println("Leader je "+leaderNodeName);
	        
	        }catch(RuntimeException e) {
	        	System.out.println("Server has crashed!");
	        	e.printStackTrace();
	        	//channel.shutdown();
	        	
	        	checkLeader();


      	
	        } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
         

	}*/
private void inviteServer() throws KeeperException, InterruptedException {
	try {
		blockingStub = getBlockingStub(leaderHostNamePort);
		for (int i = 0; i < 10; i++) {
			inviteServerFunctions(blockingStub);
			Thread.sleep(2000);
			System.out.println("Leader je " + leaderNodeName);
		}
	} catch (RuntimeException e) {
		System.out.println("Server has crashed!");
		e.printStackTrace();
		checkLeader(); // Recheck and establish connection to new leader
	} catch (InterruptedException e) {
		e.printStackTrace();
	} finally {
		if (channel != null && !channel.isShutdown()) {
			channel.shutdown();
		}
	}
}

	public static void main(String[] args) {
        
		if (args.length != 1) {
			System.out.println("Usage java -cp PDS-FT1-1.0.jar;.;lib/* rs.raf.pds.faulttolerance.AppClient <zookeeper_server_host:port>");
		    System.exit(1);
		}
		
		AppClient client;
		try {
			client = new AppClient(args[0], AppServer.APP_ROOT_NODE);
			client.checkLeader();
			client.inviteServer();
			
			
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
    }
	
	
	private void inviteServerFunctions(AccountServiceGrpc.AccountServiceBlockingStub blockingStub) {
		// Poziv AppServer-a preko gRPC-a
        System.out.println("Prvi poziv je getAmount()");
        AccountRequest request = AccountRequest.newBuilder()
        			.setRequestId(1)
        			.setOpType(AccountRequestType.GET)
        			.build();
        
        AccountResponse response;
        
        synchronized(this) {
        	response = blockingStub.getAmount(request);
        }
        ispisResponse(response, request);
        
        
         
        System.out.println("Drugi poziv je addAmount()");
        request = AccountRequest.newBuilder()
        			.setRequestId(2)
        			.setOpType(AccountRequestType.ADD)
        			.setAmount(150.0f)
        			.build();
        synchronized(this) {        
        	response = blockingStub.addAmount(request);
        }
        ispisResponse(response, request);
        
        
        
        System.out.println("Treci poziv je witdrawAmount()");
        request = AccountRequest.newBuilder()
        			.setRequestId(2)
        			.setOpType(AccountRequestType.ADD)
        			.setAmount(70.0f)
        			.build();
                
        synchronized(this) {  
        	response = blockingStub.witdrawAmount(request);
        }
        ispisResponse(response, request);
        
        
        System.out.println("Cetvrti poziv je witdrawAmount()");
        request = AccountRequest.newBuilder()
        			.setRequestId(2)
        			.setOpType(AccountRequestType.ADD)
        			.setAmount(100.0f)
        			.build();
        synchronized(this) {        
        	response = blockingStub.witdrawAmount(request);
        }
        ispisResponse(response, request);
	}
	
	public static void ispisResponse(AccountResponse response, AccountRequest request) {
		if (response.getStatus() == RequestStatus.STATUS_OK) {
			System.out.println("STATUS OK! REQUEST = "+request.getOpType()+ ", AMOUNT = "+response.getBalance());
		}else if (response.getStatus() == RequestStatus.UPDATE_REJECTED_NOT_LEADER) {
			System.out.println("UPDATE_REJECTED_NOT_LEADER! "+request.getOpType().toString());
		}
		else if (response.getStatus() == RequestStatus.WITDRAWAL_REJECT_NOT_SUFFICIENT_AMOUNT) { 
			System.out.println(".WITDRAWAL_REJECT_NOT_SUFFICIENT_AMOUNT! Amount"+request.getAmount());
		}
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
