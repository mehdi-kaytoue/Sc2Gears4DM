import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.api.ReplayFactoryApi.ReplayContent;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IReplay;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This class runs the sub-frame which update the progress bar for downloading
 * required maps
 * 
 * 
 * @author Guillaume BOSc - Mehdi KAYTOUE
 * 
 */
public class DownloadMaps extends Thread {

	/*
	 * Begin ttributes declaration
	 */
	File[] filesEns;
	GeneralServices generalServices;
	Sc2Gears4DM caller;
	int comptBar;
	String mapDL;
	String error;
	int nbMapsDl;
	String filePath;

	/*
	 * End Attributes declaration
	 */

	/**
	 * Create and launch the DownloadMaps thread.
	 * 
	 * @param filesEns
	 *            : the array of files to analyse
	 * @param generalServices
	 *            : the GeneralServices attribute
	 * @param caller
	 *            : the caller thread
	 */
	public DownloadMaps(File[] filesEns, GeneralServices generalServices,
			Sc2Gears4DM caller) {
		this.filesEns = filesEns;
		this.generalServices = generalServices;
		this.caller = caller;
		this.comptBar = 0;
		this.mapDL = "";
		this.error = "";
		this.nbMapsDl = 0;
		this.filePath = "";

		this.start();
	}

	/**
	 * Execute the thread program : For each file, check if the file map exists
	 * in the user's directory, and download if need be.
	 */
	public void run() {
		caller.getDDownload().setModal(true);
		caller.getWindow().setEnabled(false);

		/*
		 * The list of servers which contain all map
		 */
		List<String> servers = new ArrayList<String>();
		servers.add("us");
		servers.add("eu");
		servers.add("cn");
		servers.add("kr");
		servers.add("sg");
		servers.add("xx");

		String effectiveDirectory = "";
		String directory = "";

		try {
			directory = generalServices.getInfoApi().getSc2MapsFolder()
					.getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if (directory.compareTo("") == 0) {

			return;
		}

		int percent = filesEns.length / 100;
		int percentEffectif = 0;
		for (File file : filesEns) {

			/*
			 * Update the progress bar settings
			 */
			if (percentEffectif >= percent) {
				comptBar += percentEffectif;
				caller.getPBarDownload().setValue(comptBar);
				percentEffectif -= percent;
			}
			percentEffectif++;

			IReplay replay = generalServices.getReplayFactoryApi().parseReplay(
					file.getAbsolutePath(), EnumSet.of(ReplayContent.MAP_INFO));

			effectiveDirectory = directory;
			String fileString = replay.getMapFileName();
			String[] fileV = fileString.split("/");
			for (int i = 0; i < fileV.length - 1; i++) {
				effectiveDirectory += "/" + fileV[i];
			}
			String fileName = fileV[fileV.length - 1];

			if (new File(effectiveDirectory + "/" + fileName).exists()) {
				continue;
			}

			try {
				for (String server : servers) {

					URL website = new URL("http://" + server
							+ ".depot.battle.net:1119/" + fileName);
					try {
						ReadableByteChannel rbc = Channels.newChannel(website
								.openStream());
						new File(effectiveDirectory).mkdirs();
						FileOutputStream fos = new FileOutputStream(
								effectiveDirectory + "/" + fileName);
						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
						fos.close();
						rbc.close();
						
						mapDL += fileString + "\r\n";
						nbMapsDl++;

						break;
					} catch (FileNotFoundException e) {
						System.out.println(fileString + " : the "+ server + " server does not contain this map !");
					} catch (ConnectException e) {
						System.out.println("The "+ server + " server is unreachable !");
						error += "The "+ server + " server was unreachable to donwload " + fileString+ " !";
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		writeLogInfo();

		caller.getPBarDownload().setValue(filesEns.length);

		/*
		 * Enable the main frame Hide the download frame
		 */
		caller.getWindow().setEnabled(true);
		caller.getDDownload().setVisible(false);

		/*
		 * End of the execution : update window settings
		 */
		javax.swing.JOptionPane.showMessageDialog(null,
				"All required maps have been downloaded !");

		// Show the log information
		try {
			Desktop desk = Desktop.getDesktop();
			desk.open(new File(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	/**
	 * Write the log information into temporary file
	 */
	private void writeLogInfo() {
		try {
			File file = File.createTempFile("DL_Maps", ".txt");
			filePath = file.getCanonicalPath();
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsolutePath());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("\t\t\tMaps Downloaded Information\r\n\r\n");

			bw.write("\r\n" + nbMapsDl
					+ " MAP(S) DOWNLOADED :\r\n----------\r\n");
			bw.write(mapDL);
			
			bw.write("\r\n\r\n ERRORS :\r\n----------\r\n");
			bw.write(error);

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
