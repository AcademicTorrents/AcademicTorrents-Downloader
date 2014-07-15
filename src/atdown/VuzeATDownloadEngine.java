package atdown;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerListener;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.global.GlobalManagerDownloadRemovalVetoException;
import org.gudy.azureus2.core3.peer.PEPeer;

import smartnode.models.Entry;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.AzureusCoreException;
import com.aelitis.azureus.core.AzureusCoreFactory;


public class VuzeATDownloadEngine implements DownloadEngine{

	AzureusCore core;
	
	public VuzeATDownloadEngine() throws Exception {		
		
		System.setProperty("azureus.install.path",Main.ATDIR);
		System.setProperty( "azureus.app.name","ATDownloader");
		
		try{
			core = AzureusCoreFactory.create();
		}catch (Throwable re){
			Main.println("Error starting core: " + re.getLocalizedMessage());
		}
		
	    if (!core.isStarted())
	    	core.start();
	    
	  //remove previous stuff
	    try {
	    	
		    GlobalManager globalManager = core.getGlobalManager();
		    for (DownloadManager d : globalManager.getDownloadManagers()){
		    	System.out.println("Removed: " + d.getDisplayName());
		    	
					globalManager.removeDownloadManager(d);
		    }
		} catch (GlobalManagerDownloadRemovalVetoException e) {
			e.printStackTrace();
			throw new Exception("Error setting up engine");
		}
	    
	    
	    
//	    new PojoExplorer(core);
//	    PojoExplorer.pausethread();


	}
	
	public void shutdown(){
		Main.println("Shutting down...");
		try {
			core.requestStop();
		} catch (AzureusCoreException aze) {
			Main.println("Could not end session gracefully - forcing exit.....");
			core.stop();
		}
	}
	
	
	
	public void download(Entry entry, String specificFile) throws InterruptedException, GlobalManagerDownloadRemovalVetoException, IOException {
		
		Main.println("Downloading: " + entry.getName());
//	    new File(Main.ATDIR + "/az-config").delete();
//	    System.setProperty("azureus.config.path", Main.ATDIR + "/az-config");

	    
	   // [Start/Stop Rules, Torrent Removal Rules, Share Hoster, Default Tracker Web, Core Update Checker, Core Patch Checker, Platform Checker, UPnP, DHT, DHT Tracker, Magnet URI Handler, External Seed, Local Tracker, Tracker Peer Auth, Network Status, Buddy, RSS]

//	    PluginManager.getDefaults().setDefaultPluginEnabled(PluginManagerDefaults.PID_PLATFORM_CHECKER, false);
//	    PluginManager.getDefaults().setDefaultPluginEnabled(PluginManagerDefaults.PID_CORE_PATCH_CHECKER, false);
//	    PluginManager.getDefaults().setDefaultPluginEnabled(PluginManagerDefaults.PID_CORE_UPDATE_CHECKER, false);
//	    PluginManager.getDefaults().setDefaultPluginEnabled(PluginManagerDefaults.PID_PLUGIN_UPDATE_CHECKER, false);
//	    
	    

	    
	    File downloadDirectory = new File("."); //Destination directory
	    //if(downloadDirectory.exists() == false) downloadDirectory.mkdir();
	    
	    //File downloadedTorrentFile = new File(Main.ATDIR + "/" + entry.getInfohash() + ".torrent");
	    
	    //Start the download of the torrent 
	    GlobalManager globalManager = core.getGlobalManager();
	    
	    //FileUtils.copyInputStreamToFile(new ByteArrayInputStream(entry.getTorrentFile()), downloadedTorrentFile);
	    
	    DownloadManager manager = globalManager.addDownloadManager(Main.ATDIR + entry.getInfohash() + ".torrent",
	                                                               downloadDirectory.getAbsolutePath());
	    
	    //manager.getDiskManager().
	    
//	    for (DiskManagerFileInfo i : manager.getDiskManager().getFiles()){
//	    	
////	    	i.
//	    }
	    
	    
	    
	    //Main.println("Downloading");
	    DownloadManagerListener listener = new DownloadStateListener();
	    manager.addListener(listener);    
//	    Main.println(manager.getErrorDetails());
//	    new PojoExplorer(manager);
//	    PojoExplorer.pausethread();
	    globalManager.startAllDownloads();
	    
	    //core.requestStop();

	}

	@Override
	public void ls(Entry entry) throws Exception {
		
	}

}


class DownloadStateListener implements DownloadManagerListener {

	Thread progressChecker;
	
	public DownloadStateListener() {
		
		progressChecker = new Thread(new ATThreadChecker());
		progressChecker.setDaemon(true);
		
	}

	
	public void stateChanged(DownloadManager manager, int state) {
		switch (state) {
		case DownloadManager.STATE_DOWNLOADING:
			//Main.println("Downloading....");
			// Start a new daemon thread periodically check
			// the progress of the upload and print it out
			// to the command line
			if (!progressChecker.isAlive())
				progressChecker.start();
			break;
		case DownloadManager.STATE_CHECKING:
			Main.println("Checking Existing Data.." + manager.getDisplayName());
			break;
		case DownloadManager.STATE_ERROR:
			System.out.println("\nError : ( Check Log " + manager.getErrorDetails());
			break;
		case DownloadManager.STATE_STOPPED:
			//Main.println("\nStopped.." + manager.getDisplayName());
			break;
		case DownloadManager.STATE_ALLOCATING:
			Main.println("\nAllocating File Space.." + manager.getDisplayName());
			break;
		case DownloadManager.STATE_INITIALIZING:
			Main.println("\nInitializing.." + manager.getDisplayName());
			break;
		default :
			//Main.println("state:" + state);
			
		}
	}

	static class ATThreadChecker implements Runnable{

		public void run() {
			try {
				boolean downloadCompleted = false;
				while (!downloadCompleted) {
					AzureusCore core = AzureusCoreFactory.getSingleton();
					List<DownloadManager> managers = core.getGlobalManager().getDownloadManagers();

					if (managers.size() < 1){
						Main.println("Download Halted!");
						downloadCompleted = true;
						break;								
					}
					
					// maybe in the future we allways try ourself?
//					core.getGlobalManager().getDownloadManagers().get(0).getPeerManager()
//					.addPeer("127.0.0.1", 6801, 6801, false, null);
					
					String peers = getPeerString(core);
					
					long totalReceivedRate = 0;
					long totalSize = 0;
					long totalRemaining = 0;
					
					
					for (DownloadManager man : managers){
						
						try{
							//totalRemaining += man.getDiskManager().getRemainingExcludingDND();
							
							totalRemaining += man.getDiskManager().getRemaining();
							
							totalReceivedRate += man.getStats().getDataReceiveRate();
							
							totalSize += man.getSize();
								
						}catch(Exception e){
							System.out.println("Error with stats list " + man.getDisplayName());
						}
					}
					
					int terminalWidth = jline.TerminalFactory.get().getWidth();
					
					for (int i = 0; i < terminalWidth; i++)
						Main.print("\b");
					
					for (int i = 0; i < terminalWidth; i++)
						Main.print(" ");
					
					Main.print("\r");

					
					// There is only one in the queue.
					Main.print(String.format("%." + (terminalWidth-1) + "s",Main.humanReadableByteCount(totalReceivedRate, true) + "/s " + 
							Main.humanReadableByteCountRatio(totalSize - totalRemaining, totalSize, true) + "/" + 
							+ ((int)((totalSize - totalRemaining)/(totalSize*1.0)*100)) + "%, " 
							+ peers));
					
					
					
					
					
					
					
					
					// There is only one in the queue.
//					DownloadManager man = managers.get(0);
//					Main.print(Main.humanReadableByteCount(man.getStats().getDataReceiveRate(), true) + "/s " + 
//							Main.humanReadableByteCountRatio(man.getSize() - man.getDiskManager().getRemainingExcludingDND(), man.getSize(),true) + "/" + 
//							+ (man.getStats().getCompleted() / 10.0) + "%, " 
//							+ man.getNbSeeds() + " Mirrors " + peers.toString());
//					downloadCompleted = man.isDownloadComplete(true);
//					Main.print("\r");
					// Check every 1 seconds on the progress
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		
	};
	
	
	
	
	public void downloadComplete(DownloadManager manager) {
		Main.println("Download Completed");
		AzureusCore core = AzureusCoreFactory.getSingleton();
		
		// if done
		if (core.getGlobalManager().isSeedingOnly()){
			
			try {
				core.requestStop();
			} catch (AzureusCoreException aze) {
				Main.println("Could not end session gracefully - forcing exit.....");
				core.stop();
			}
		}
	}

	@Override
	public void completionChanged(DownloadManager manager, boolean bCompleted) {
		System.out.println("completionChanged");
		
	}

	@Override
	public void positionChanged(DownloadManager download, int oldPosition,
			int newPosition) {
		System.out.println("positionChanged");
		
	}

	@Override
	public void filePriorityChanged(DownloadManager download,
			DiskManagerFileInfo file) {
		System.out.println("filePriorityChanged");
		
	}
	
	
	
	public static String getPeerString(AzureusCore core) throws NamingException {


		List<String> peers = new ArrayList<String>();
		List<DownloadManager> managers = core.getGlobalManager().getDownloadManagers();
		
		
		final Map<String, Long> rawPeers = new HashMap<String, Long>();
		final Map<String, String> peerType = new HashMap<String, String>();
		
		for (DownloadManager m  : managers){
			try{
				for (PEPeer p : m.getPeerManager().getPeers()){
					
					Long speed = rawPeers.get(p.getIPHostName());
					
					Long speedLocal = p.getStats().getDataReceiveRate();
					
					if (speed != null)
						speed = speed + speedLocal;
					else
						speed = speedLocal;
					
					String iphostname = p.getIPHostName();
					rawPeers.put(iphostname, speed);
					
					String prot = p.getProtocol();
					if (prot.contains("HTTP")){
						prot = "http";
					}else if (prot.contains("FTP")){
						prot = "ftp";
					}else{
						prot = "";
					}
					
					peerType.put(iphostname, prot);
				}
			}catch(Exception e){
				System.out.println("Error with peer list " + m.getDisplayName());
			}
		}
		
		List<String> tosort = new ArrayList<String>(rawPeers.keySet());
		Collections.sort(tosort, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				
				long o1r = rawPeers.get(o1);
				long o2r = rawPeers.get(o2);
				
				if (o2r > o1r)
					return 1;
				else if (o2r == o1r)
					return 0;
				else 
					return -1;
			}
		});
		
		
		int count = 0;
		for (String opstring : tosort){
			
			long dlrate = rawPeers.get(opstring);
			
			//if (dlrate != 0){
				count++;
				
				String pstring = tryForDNSName(opstring);
				
				// only show some peers but show all edu
				if (!(count > 3) || pstring.contains(".edu")){
					
					pstring = pstring + " " + Main.humanReadableByteCount(dlrate, true) + "/s" + " " + peerType.get(opstring);
				
				
					peers.add(pstring);
						
				}
				
		}
		
//		for (int i = 0; i < peers.size() ; i++){
//			
//			peers.set(i, peers.get(i) + " " + peerType.get);
//		}
		
		
		return tosort.size() + " Mirrors " + peers.toString();
		
	}
	
	
	public static String tryForDNSName(String pstring) throws NamingException{
		
		if (!hasAlpha(pstring)){
			pstring = getRevName(pstring);
			
		}
		
		//check if dns resolved
		if (hasAlpha(pstring)){
			// get rid of last .
			if (pstring.length() == pstring.lastIndexOf('.')+1)
				pstring = pstring.substring(0, pstring.length()-1);
			
			// get end of dns
			if (pstring.contains(".com."))
				pstring = pstring.substring(pstring.lastIndexOf('.',pstring.lastIndexOf(".com.")-1)+1);
			else if (pstring.contains(".edu."))
				pstring = pstring.substring(pstring.lastIndexOf('.',pstring.lastIndexOf(".edu.")-1)+1);
			else if (pstring.contains(".org."))
				pstring = pstring.substring(pstring.lastIndexOf('.',pstring.lastIndexOf(".org.")-1)+1);
			else
				pstring = pstring.substring(pstring.lastIndexOf('.',pstring.lastIndexOf('.')-1)+1);
		}
		
		return pstring;
	}
	
	
	
	public static String getRevName(String oipAddr) throws NamingException {
		
		String ipAddr = oipAddr;
		try{
			Properties env = new Properties();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
			InitialDirContext idc = new InitialDirContext(env);
			
			  String revName = null;
			  String[] quads = ipAddr.split("\\.");
			 
			  //StringBuilder would be better, I know.
			  ipAddr = "";
			 
			  for (int i = quads.length - 1; i >= 0; i--) {
			    ipAddr += quads[i] + ".";
			  }
			 
			  ipAddr += "in-addr.arpa.";
			  Attributes attrs = idc.getAttributes(ipAddr, new String[] {"PTR"});
			  Attribute attr = attrs.get("PTR");
			 
			  if (attr != null) {
			    revName = (String) attr.get(0);
			  }
			  
			  return revName;
		}catch (Exception e){
			
			 return oipAddr;
		}
		 
		 
	}
	
	
	public static boolean hasAlpha(String name) {
	    char[] chars = name.toCharArray();

	    for (char c : chars) {
	        if(Character.isLetter(c)) {
	            return true;
	        }
	    }

	    return false;
	}
	
	
	
}
