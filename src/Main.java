import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import org.bitlet.wetorrent.Metafile;
import org.bitlet.wetorrent.Torrent;
import org.bitlet.wetorrent.disk.PlainFileSystemTorrentDisk;
import org.bitlet.wetorrent.disk.TorrentDisk;
import org.bitlet.wetorrent.peer.IncomingPeerListener;


public class Main {
	
	private static final int PORT = 6881;

	public static void main(String[] args) throws Exception {
		System.out.println("Welcome to the Academic Torrents Download tool!");

		
        // read torrent filename from command line arg
        String filename = args[0];

        // Parse the metafile
        Metafile metafile = new Metafile(new BufferedInputStream(new FileInputStream(new File(filename))));

        // Create the torrent disk, this is the destination where the torrent file/s will be saved
        TorrentDisk tdisk = new PlainFileSystemTorrentDisk(metafile, new File("."));
        tdisk.init();
        
        IncomingPeerListener peerListener = new IncomingPeerListener(PORT);
        peerListener.start();

        Torrent torrent = new Torrent(metafile, tdisk, peerListener);
        torrent.startDownload();

        while (!torrent.isCompleted()) {

            try {
                Thread.sleep(1000);
            } catch(InterruptedException ie) {
                break;
            }

            torrent.tick();
            System.out.printf("Got %s peers, completed %d bytes\n",
                    torrent.getPeersManager().getActivePeersNumber(),
                    torrent.getTorrentDisk().getCompleted());
        }
        
        System.out.println("Finished");
        torrent.interrupt();
        peerListener.interrupt();
        
		
		
		
	}

}
