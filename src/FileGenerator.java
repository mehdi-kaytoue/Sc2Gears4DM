import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.api.ReplayFactoryApi.ReplayContent;
import hu.belicza.andras.sc2gearspluginapi.api.enums.LadderSeason;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IGameEvents;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IMapInfo;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IMessageEvents;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayer;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerId;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IPlayerSelectionTracker;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.IReplay;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.ReplayConsts.MapObject;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IBaseUseAbilityAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IBuildAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IHotkeyAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ILeaveGameAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IMoveScreenAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IResearchAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ISelectAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.ITrainAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IUpgradeAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IUseBuildingAbilityAction;
import hu.belicza.andras.sc2gearspluginapi.api.sc2replay.action.IUseUnitAbilityAction;
import hu.belicza.andras.sc2gearspluginapi.impl.util.Pair;

import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * This class executes the parsing to XML and CSV files from a replay
 * 
 * @author Guillaume BOSC, Mehdi KAYTOUE
 */
public class FileGenerator extends Thread {
	/*
	 * Attributes declarations
	 */

	/**
	 * All the file to parse
	 */
	File[] files;

	/**
	 * The general service to parse the files
	 */
	GeneralServices generalServices;

	/**
	 * The calling class
	 */
	Sc2Gears4DM caller;

	/**
	 * The path to the folder where the user want to save the files
	 */
	String path;

	/**
	 * The prefix chosen by the user for the files names
	 */
	String prefix;

	/**
	 * True if we have to create the XML files, false otherwise
	 */
	boolean executeXML;

	/**
	 * True if we have to create the player CSV file, false otherwise
	 */
	boolean executePlayer;

	/**
	 * True if we have to create the map CSV file, false otherwise
	 */
	boolean executeMap;

	/**
	 * True if we have to create the map icons, false otherwise
	 */
	boolean executeIcon;

	/**
	 * True if we have to create the replay CSV file, false otherwise
	 */
	boolean executeReplay;

	/**
	 * List of the actions to parse
	 */
	List<DifferentActions> listAction;

	public static final String PLAYER_HEADER = "BattleNetId\t"
			+ "BattleNetProfileUrl\t" + "BattleNetSubId\t" + "FullName\t"
			+ "Gateway\t" + "Name\t" + "Region\t" + "Sc2RankProfileUrl\n";

	public static final String REPLAY_HEADER = "filename\t" + "player1\t"
			+ "player2\t" + "winner\t" + "race1\t" + "raceFinal1\t" + "race2\t"
			+ "raceFinal2\t" + "type1\t" + "type2\t" + "handicap1\t"
			+ "handicap2\t" + "action.count1\t" + "action.count2\t"
			+ "effective.action.count1\t" + "effective.action.count2\t"
			+ "excluded.action.count1\t" + "excluded.action.count2\t"
			+ "excluded.effective.action.count1\t"
			+ "excluded.effective.action.count2\t" + "message.count.1\t"
			+ "message.count.2\t" + "race.matchup\t" + "gateway\t"
			+ "savetime\t" + "timezone.recorder\t" + "replay.version\t"
			+ "gateway.ladder.season\t" + "game.length.sec\t" + "game.type\t"
			+ "map.filename\n";

	public static final String MAPS_HEADER = "filename\t" + "author\t"
			+ "name\t" + "desc.short\t" + "desc.long\t" + "boundary.bottom\t"
			+ "boundary.left\t" + "boundary.right\t" + "boundary.top\t"
			+ "size.height\t" + "size.width\t" + "size.playable\t"
			+ "sc2.version\t" + "gateway.ladder.season\t"
			+ "start.location.list\t" + "map.objects.list\n";

	public final static String LOCATION_HEADER = "filename\t" + "player1\t"
			+ "player2\t" + "p1.x\t" + "p1.y\t" + "p2.x\t" + "p2.y\n";

	BufferedWriter playerWriter;
	BufferedWriter replayWriter;
	BufferedWriter locationWriter;
	BufferedWriter mapWriter;

	TreeSet<myPlayerId> players = new TreeSet<myPlayerId>();

	TreeSet<String> uniqMaps = new TreeSet<String>();

	protected int comptBar;
	protected int maxSize;

	protected int numFile;

	String replaysNull;
	String replaysNot1v1;
	String mapNotFound;
	String noStartLocation;
	String mapIconNotFound;
	
	String filePath;

	/*
	 * End of attributes declarations
	 */

	class myPlayerId implements Comparable<myPlayerId> {
		IPlayerId id;

		public myPlayerId(IPlayerId o) {
			this.id = o;
		}

		@Override
		public int compareTo(myPlayerId o) {
			return this.id.getBattleNetId() - o.id.getBattleNetId();
		}
	}

	/**
	 * Executes the generation of XML and/or CSV files Gives values to the
	 * attributes
	 * 
	 * @param files
	 *            : the files to parse
	 * @param generalServices
	 *            : the general service of the Sc2Gears API
	 * @param caller
	 *            : the calling object
	 * @param path
	 *            : the directory path
	 * @param prefix
	 *            : the prefix name of the files to create
	 * @param executeXML
	 *            : true if we have to perform the XML parsing, false otherwise
	 * @param executeCSV
	 *            : true if we have to perform the CSV parsing, false otherwise
	 * @param listAction
	 *            : the list of the actions to parse
	 */
	public FileGenerator(File[] files, GeneralServices generalServices,
			Sc2Gears4DM caller, String path, String prefix, boolean executeXML,
			boolean executePlayer, boolean executeReplay, boolean executeMap,
			boolean executeIcon, List<DifferentActions> listAction) {
		this.files = files;
		this.generalServices = generalServices;
		this.caller = caller;
		this.path = path;
		this.prefix = prefix;
		this.executeXML = executeXML;
		this.executePlayer = executePlayer;
		this.executeMap = executeMap;
		this.executeReplay = executeReplay;
		this.executeIcon = executeIcon;
		this.listAction = listAction;
		this.maxSize = 0;
		this.comptBar = 0;
		this.numFile = 0;
		this.replaysNull = "";
		this.replaysNot1v1 = "";
		this.mapNotFound = "";
		this.noStartLocation = "";
		this.mapIconNotFound = "";
		this.filePath = "";

		/*
		 * Update the progress bar settings
		 */
		if (this.executeXML) {
			maxSize += files.length;
			new File(this.path + "/xml").mkdir();
		}

		if (this.executePlayer) {
			maxSize += files.length;
			try {
				File filePlayer = new File(this.path + "/players.csv");
				if (!filePlayer.exists()) {
					filePlayer.createNewFile();
				}
				FileWriter fwPlayer = new FileWriter(
						filePlayer.getAbsolutePath());
				playerWriter = new BufferedWriter(fwPlayer);
				playerWriter.write(PLAYER_HEADER);

			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (this.executeReplay) {
			maxSize += files.length;
			try {
				File fileReplay = new File(this.path + "/replays.csv");
				if (!fileReplay.exists()) {
					fileReplay.createNewFile();
				}
				FileWriter fwReplay = new FileWriter(
						fileReplay.getAbsolutePath());
				replayWriter = new BufferedWriter(fwReplay);
				replayWriter.write(REPLAY_HEADER);
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (this.executeMap) {
			maxSize += files.length;

			try {

				File fileLocation = new File(this.path + "/startLocations.csv");
				if (!fileLocation.exists()) {
					fileLocation.createNewFile();
				}
				FileWriter fwLocation = new FileWriter(
						fileLocation.getAbsolutePath());
				locationWriter = new BufferedWriter(fwLocation);

				File fileMap = new File(this.path + "/maps.csv");
				if (!fileMap.exists()) {
					fileMap.createNewFile();
				}
				FileWriter fwMap = new FileWriter(fileMap.getAbsolutePath());
				mapWriter = new BufferedWriter(fwMap);

				mapWriter.write(MAPS_HEADER);
				locationWriter.write(LOCATION_HEADER);

			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		if (this.executeIcon) {
			maxSize += files.length;
			new File(this.path + "/maps-icons").mkdir();
		}

		this.caller.getPBar().setValue(0);
		this.caller.getPBar().setMaximum(maxSize);

		/*
		 * Launch the thread
		 */
		this.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {

		int percentEffective = 0;
		int percent = maxSize / 100;
		int notParsed = 0;
		for (File file : files) {
			
			/*
			 * Update the progress bar status
			 */
			if (percentEffective >= percent) {
				this.comptBar += percentEffective;
				caller.getPBar().setValue(this.comptBar);
				percentEffective -= percent;
			}
			
			this.numFile++;
			IReplay replay = generalServices.getReplayFactoryApi().parseReplay(
					file.getAbsolutePath(),
					EnumSet.of(ReplayContent.EXTENDED_MAP_INFO,
							ReplayContent.MAP_INFO,
							ReplayContent.MAP_ATTRIBUTES,
							ReplayContent.GAME_EVENTS,
							ReplayContent.MESSAGE_EVENTS));

			if (replay == null) {
				notParsed++;
				percentEffective++;
				replaysNull += file.getName() + "\r\n";
				continue;
			}
			if (replay.getPlayers().length != 2
					|| replay.getPlayers()[0].getType() != ReplayConsts.PlayerType.HUMAN
					|| replay.getPlayers()[1].getType() != ReplayConsts.PlayerType.HUMAN) {
				notParsed++;
				percentEffective++;
				replaysNot1v1 += file.getName() + "\r\n";
				continue;
			}

			if (executeXML) {
				generateXML(replay, file.getName());
				percentEffective++;
			}

			if (executePlayer) {
				try {
					parsePlayerInfo(replay);
					percentEffective++;
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			if (executeMap) {
				try {
					writeMapInfo(replay);
					writeStartLocation(file, replay);

					percentEffective++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (executeReplay) {
				try {
					writeReplayInfo(file, replay);
					percentEffective++;
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			if (executeIcon) {
				writeImage(replay);
				percentEffective++;
			}
		}
		caller.getPBar().setValue(this.maxSize);
		writeLogInfo();

		closeFiles();

		System.err.println("Replay not parsed: " + notParsed);

		/*
		 * End of the execution : update window settings
		 */
		javax.swing.JOptionPane
				.showMessageDialog(null, "Files are generated !");
		
		
		try {
			Desktop desk = Desktop.getDesktop();
			desk.open(new File(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.caller.getPBar().setVisible(false);
		this.caller.getBGenerate().setVisible(true);
	}

	private void writeLogInfo() {
		try {
			File file = File.createTempFile("Log",".txt");
			filePath = file.getCanonicalPath();
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsolutePath());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("\t\t\tExecution Information\r\n\r\n");
			
			bw.write("\r\nREPLAYS NULL :\r\n----------\r\n");
			bw.write(replaysNull);
			
			bw.write("\r\nREPLAYS NOT 1VS1 :\r\n----------\r\n");
			bw.write(replaysNot1v1);
			
			bw.write("\r\nMAPS NOT FOUND :\r\n----------\r\n");
			bw.write(mapNotFound);
			
			bw.write("\r\nICONS NOT FOUND :\r\n----------\r\n");
			bw.write(mapIconNotFound);
			
			bw.write("\r\nSTART LOCATIONS NOT FOUND :\r\n----------\r\n");
			bw.write(noStartLocation);
			
			bw.close();		
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void closeFiles() {

		players.clear();
		try {
			if (executePlayer)
				playerWriter.close();

			if (executeReplay)
				replayWriter.close();

			if (executeMap) {
				mapWriter.close();
				locationWriter.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Generates the XML file corresponding to this replay
	 * 
	 * @param replay
	 *            : the replay to parse
	 */
	protected void generateXML(IReplay replay, String fileName) {

		// The root of the XML file
		Element racine = new Element("replay");
		Document document = new Document(racine);

		/*
		 * Game information
		 */
		Element infos = new Element("info");
		racine.addContent(infos);

		Attribute valGameFormat = new Attribute("format",
				replay.getFormat().stringValue);
		infos.setAttribute(valGameFormat);

		Attribute version = new Attribute("version", replay.getReplayVersion());
		infos.setAttribute(version);

		Attribute gatewayGame = new Attribute("gateway", replay.getGateway()
				.toString());
		infos.setAttribute(gatewayGame);

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Attribute saveTime = new Attribute("saveTime", dateFormat.format(replay
				.getSaveTime()));
		infos.setAttribute(saveTime);

		Attribute gameType = new Attribute("gameType", replay.getGameType()
				.toString());
		infos.setAttribute(gameType);

		Attribute mapName = new Attribute("mapName", replay.getMapFileName());
		infos.setAttribute(mapName);

		Attribute leagueMatchup = new Attribute("leagueMatchUp",
				replay.getLeagueMatchup());
		infos.setAttribute(leagueMatchup);

		Attribute length = new Attribute(
				"length",
				String.valueOf(replay.getGameSpeed().convertToRealTime(
						replay.getFrames()) >> ReplayConsts.FRAME_BITS_IN_SECOND));
		infos.setAttribute(length);

		/*
		 * Players information
		 */
		Element joueur1 = new Element("player");
		racine.addContent(joueur1);
		Element joueur2 = new Element("player");
		racine.addContent(joueur2);

		Map<String, String> playerHashMap = new HashMap<String, String>();
		playerHashMap.put(replay.getPlayers()[0].getPlayerId().getName(), "1");
		playerHashMap.put(replay.getPlayers()[1].getPlayerId().getName(), "2");

		// Name
		Attribute nom1 = new Attribute("name", replay.getPlayers()[0]
				.getPlayerId().getName());
		joueur1.setAttribute(nom1);

		Attribute nom2 = new Attribute("name", replay.getPlayers()[1]
				.getPlayerId().getName());
		joueur2.setAttribute(nom2);

		// ID
		Attribute id1 = new Attribute("id", playerHashMap.get(replay
				.getPlayers()[0].getPlayerId().getName()));
		joueur1.setAttribute(id1);

		Attribute id2 = new Attribute("id", playerHashMap.get(replay
				.getPlayers()[1].getPlayerId().getName()));
		joueur2.setAttribute(id2);

		// Battle Net ID
		Attribute idBattleNet1 = new Attribute("idBattleNet",
				String.valueOf(replay.getPlayers()[0].getPlayerId()
						.getBattleNetId()));
		joueur1.setAttribute(idBattleNet1);

		Attribute idBattleNet2 = new Attribute("idBattleNet",
				String.valueOf(replay.getPlayers()[1].getPlayerId()
						.getBattleNetId()));
		joueur2.setAttribute(idBattleNet2);

		// BattleNet SUbID
		Attribute subID1 = new Attribute("subIDBattleNet",
				String.valueOf(replay.getPlayers()[0].getPlayerId()
						.getBattleNetSubId()));
		joueur1.setAttribute(subID1);

		Attribute subID2 = new Attribute("subIDBattleNet",
				String.valueOf(replay.getPlayers()[1].getPlayerId()
						.getBattleNetSubId()));
		joueur2.setAttribute(subID2);

		// BattleNet URL
		Attribute url1 = new Attribute("URLBattleNet", replay.getPlayers()[0]
				.getPlayerId().getBattleNetProfileUrl(
						ReplayConsts.BnetLanguage.ENGLISH));
		joueur1.setAttribute(url1);

		Attribute url2 = new Attribute("URLBattleNet", replay.getPlayers()[1]
				.getPlayerId().getBattleNetProfileUrl(
						ReplayConsts.BnetLanguage.ENGLISH));
		joueur2.setAttribute(url2);

		// Gateway
		Attribute gateway1 = new Attribute("gateway", replay.getPlayers()[0]
				.getPlayerId().getGateway().toString());
		joueur1.setAttribute(gateway1);

		Attribute gateway2 = new Attribute("gateway", replay.getPlayers()[1]
				.getPlayerId().getGateway().toString());
		joueur2.setAttribute(gateway2);

		// Statut
		String valStatut1 = "-1";
		String valStatut2 = "-1";
		if (replay.getPlayers()[1].isWinner() != null) {
			if (replay.getPlayers()[1].isWinner()) {
				valStatut1 = "loser";
				valStatut2 = "winner";
			} else if (replay.getPlayers()[0].isWinner()) {
				valStatut1 = "winner";
				valStatut2 = "loser";
			}
		}
		Attribute statut1 = new Attribute("statut", valStatut1);
		joueur1.setAttribute(statut1);
		Attribute statut2 = new Attribute("statut", valStatut2);
		joueur2.setAttribute(statut2);

		// Start locations
		String valPosX1 = "-1";
		String valPosY1 = "-1";
		String valPosX2 = "-1";
		String valPosY2 = "-1";
		if (replay.getPlayers()[0].getStartLocation() != null
				&& replay.getPlayers()[1].getStartLocation() != null) {
			valPosX1 = String.valueOf(replay.getPlayers()[0].getStartLocation()
					.getX());
			valPosY1 = String.valueOf(replay.getPlayers()[0].getStartLocation()
					.getY());
			valPosX2 = String.valueOf(replay.getPlayers()[1].getStartLocation()
					.getX());
			valPosY2 = String.valueOf(replay.getPlayers()[1].getStartLocation()
					.getY());
		}
		Attribute posX1 = new Attribute("x", valPosX1);
		joueur1.setAttribute(posX1);
		Attribute posY1 = new Attribute("y", valPosY1);
		joueur1.setAttribute(posY1);
		Attribute posX2 = new Attribute("x", valPosX2);
		joueur2.setAttribute(posX2);
		Attribute posY2 = new Attribute("y", valPosY2);
		joueur2.setAttribute(posY2);

		// Race
		Attribute race1 = new Attribute("race",
				replay.getPlayers()[0].getRaceString());
		joueur1.setAttribute(race1);
		Attribute race2 = new Attribute("race",
				replay.getPlayers()[1].getRaceString());
		joueur2.setAttribute(race2);

		/*
		 * Replay actions
		 */
		Element contenu = new Element("actions");
		racine.addContent(contenu);

		IPlayer[] players = replay.getPlayers();
		IPlayerSelectionTracker[] selectionTrackers = new IPlayerSelectionTracker[players.length];

		for (int i = 0; i < selectionTrackers.length; i++)
			selectionTrackers[i] = generalServices.getReplayUtilsApi()
					.createPlayerSelectionTracker();

		IGameEvents gameEvents = replay.getGameEvents();

		/*
		 * Analyse each action of the replay
		 */
		for (IAction action : replay.getGameEvents().getActions()) {

			Element actionElement = new Element("action");

			Attribute timeAction = new Attribute(
					"time",
					String.valueOf(replay.getGameSpeed().convertToRealTime(
							action.getFrame()) >> ReplayConsts.FRAME_BITS_IN_SECOND));
			actionElement.setAttribute(timeAction);

			Attribute playerAction = new Attribute("player",
					playerHashMap.get(replay.getPlayers()[action.getPlayer()]
							.getPlayerId().getName()));
			actionElement.setAttribute(playerAction);

			if (listAction.contains(DifferentActions.Build)
					&& action instanceof IBuildAction) {
				contenu.addContent(actionElement);

				Attribute typeAction = new Attribute("type", "BuildAction");
				actionElement.setAttribute(typeAction);

				IBuildAction actionBuilding = (IBuildAction) action;
				Attribute buildingAction = new Attribute("name",
						actionBuilding.getBuilding().stringValue);
				actionElement.setAttribute(buildingAction);

				Attribute posXAction = new Attribute("x",
						String.valueOf(actionBuilding.getTargetX() / 65536));
				actionElement.setAttribute(posXAction);

				Attribute posYAction = new Attribute("y",
						String.valueOf(actionBuilding.getTargetY() / 65536));
				actionElement.setAttribute(posYAction);

			} else if (listAction.contains(DifferentActions.Research)
					&& action instanceof IResearchAction) {
				IResearchAction actionR = (IResearchAction) action;

				contenu.addContent(actionElement);

				Attribute typeAction = new Attribute("type", "ResearchAction");
				actionElement.setAttribute(typeAction);

				Attribute researchAction = new Attribute("research",
						actionR.getResearch().stringValue);
				actionElement.setAttribute(researchAction);
			} else if (listAction.contains(DifferentActions.Upgrade)
					&& action instanceof IUpgradeAction) {
				IUpgradeAction actionUp = (IUpgradeAction) action;

				contenu.addContent(actionElement);

				Attribute typeAction = new Attribute("type", "UpgradeAction");
				actionElement.setAttribute(typeAction);

				Attribute upagradeAction = new Attribute("name",
						actionUp.getUpgrade().stringValue);
				actionElement.setAttribute(upagradeAction);

			} else if (listAction.contains(DifferentActions.Train)
					&& action instanceof ITrainAction) {

				contenu.addContent(actionElement);

				ITrainAction actionTrain = (ITrainAction) action;

				Attribute typeAction = new Attribute("type", "TrainAction");
				actionElement.setAttribute(typeAction);

				Attribute trainAction = new Attribute("name",
						actionTrain.getUnit().stringValue);
				actionElement.setAttribute(trainAction);

			} else if (listAction.contains(DifferentActions.Select)
					&& action instanceof ISelectAction) {
				contenu.addContent(actionElement);

				ISelectAction actionSelect = (ISelectAction) action;

				Attribute typeAction = new Attribute("type", "SelectAction");
				actionElement.setAttribute(typeAction);

				selectionTrackers[action.getPlayer()]
						.processSelectAction(actionSelect);

				Attribute etatCourant = new Attribute("currentSelection",
						gameEvents.getSelectionString(selectionTrackers[action
								.getPlayer()].getCurrentSelection()));
				actionElement.setAttribute(etatCourant);

				String deselectedUnit = String.valueOf(actionSelect
						.getDeselectedUnitsCount());
				Attribute deselUnit = new Attribute("deselectedUnits",
						deselectedUnit);
				actionElement.setAttribute(deselUnit);

				String deselectedRetainUnit = "0";
				if (actionSelect.getRemoveIndices() != null) {
					deselectedRetainUnit = String.valueOf(actionSelect
							.getRemoveIndices().length);
				}
				Attribute removeIndices = new Attribute("remove",
						deselectedRetainUnit);
				actionElement.setAttribute(removeIndices);

				String selectedUnit = "0";
				if (actionSelect.getRetainIndices() != null) {
					selectedUnit = String.valueOf(actionSelect
							.getRetainIndices().length)
							+ "-"
							+ String.valueOf(actionSelect.getRetainIndices()[0]);
				}

				Attribute selUnit = new Attribute("selectedUnits", selectedUnit);
				actionElement.setAttribute(selUnit);

				if (actionSelect.getUnitTypes() != null) {
					String typeUnit = "";
					String idUnit = "";
					int compteur = 0;
					for (int i = 0; i < actionSelect.getUnitTypes().length; i++) {
						typeUnit = gameEvents.getUnitName(actionSelect
								.getUnitTypes()[i]);

						for (int j = 1; j <= actionSelect
								.getUnitsOfTypeCounts()[i]; j++) {
							idUnit = Integer.toHexString(actionSelect
									.getUnitIds()[compteur]);

							Element newUnit = new Element("unit");
							actionElement.addContent(newUnit);

							Attribute idUnitAtt = new Attribute("id", idUnit);
							newUnit.setAttribute(idUnitAtt);

							if (typeUnit != null) {
								Attribute typeUnitAtt = new Attribute("type",
										typeUnit);
								newUnit.setAttribute(typeUnitAtt);
							}

							compteur++;
						}
					}
				}
			} else if (listAction.contains(DifferentActions.HotKey)
					&& action instanceof IHotkeyAction) {
				contenu.addContent(actionElement);

				IHotkeyAction actionHK = (IHotkeyAction) action;

				Attribute typeAction = new Attribute("type", "HotkeyAction");
				actionElement.setAttribute(typeAction);

				String operation = "";
				if (actionHK.isHotkeyAssignAdd()) {
					operation = "add";
				} else if (actionHK.isHotkeyAssignOverwrite()) {
					operation = "overwrite";
				} else if (actionHK.isSelect()) {
					operation = "select";
				}

				Attribute operationAtt = new Attribute("operation", operation);
				actionElement.setAttribute(operationAtt);

				Attribute numHK = new Attribute("numero",
						String.valueOf(actionHK.getNumber()));
				actionElement.setAttribute(numHK);

				selectionTrackers[action.getPlayer()]
						.processHotkeyAction((IHotkeyAction) action);

				Attribute HKSel = new Attribute(
						"hotkeySelection",
						gameEvents.getSelectionString(selectionTrackers[action
								.getPlayer()].getHotkeySelectionLists()[actionHK
								.getNumber()]));
				actionElement.setAttribute(HKSel);

				Attribute etatCourant = new Attribute("currentSelection",
						gameEvents.getSelectionString(selectionTrackers[action
								.getPlayer()].getCurrentSelection()));
				actionElement.setAttribute(etatCourant);

			} else if (action instanceof ILeaveGameAction) {
				contenu.addContent(actionElement);

				Attribute typeAction = new Attribute("type", "LeaveGameAction");
				actionElement.setAttribute(typeAction);

			} else if (listAction.contains(DifferentActions.UseBA)
					&& action instanceof IUseBuildingAbilityAction) {

				contenu.addContent(actionElement);

				IUseBuildingAbilityAction actionBuilAbil = (IUseBuildingAbilityAction) action;

				Attribute typeAction = new Attribute("type",
						"UseBuildingAbilityAction");
				actionElement.setAttribute(typeAction);

				Attribute buildingName = new Attribute("building",
						actionBuilAbil.getBuilding().stringValue);
				actionElement.setAttribute(buildingName);

				Attribute abilityName = new Attribute("ability",
						actionBuilAbil.getBuildingAbility().stringValue);
				actionElement.setAttribute(abilityName);
			} else if (listAction.contains(DifferentActions.UseUA)
					&& action instanceof IUseUnitAbilityAction) {

				contenu.addContent(actionElement);

				IUseUnitAbilityAction actionUnitAbil = (IUseUnitAbilityAction) action;

				Attribute typeAction = new Attribute("type",
						"UseUnitAbilityAction");
				actionElement.setAttribute(typeAction);

				Attribute buildingName = new Attribute("unit",
						actionUnitAbil.getUnit().stringValue);
				actionElement.setAttribute(buildingName);

				Attribute abilityName = new Attribute("ability",
						actionUnitAbil.getUnitAbility().stringValue);
				actionElement.setAttribute(abilityName);
			} else if (listAction.contains(DifferentActions.RightClick)
					&& listAction.contains(DifferentActions.ClickMini)
					&& action instanceof IBaseUseAbilityAction) {

				IBaseUseAbilityAction actionBaseUseAbility = (IBaseUseAbilityAction) action;

				String type = "";
				if (listAction.contains(DifferentActions.ClickMini)
						&& actionBaseUseAbility.isMinimapClick()) {
					type = "Minimap Click";
					contenu.addContent(actionElement);
				} else if (listAction.contains(DifferentActions.RightClick)
						&& actionBaseUseAbility.isRightClick()) {
					type = "Right Click";
					contenu.addContent(actionElement);
				}/*
				 * else { contenu.addContent(actionElement); if
				 * (actionBaseUseAbility.isAbilityFailed()) { type =
				 * "Ability Failed"; } else if
				 * (actionBaseUseAbility.isAutocast()) { type = "Autocast"; }
				 * else if (actionBaseUseAbility.isQueued()) { type = "Queued";
				 * } else if (actionBaseUseAbility.isToggleAbility()) { type =
				 * "Toggle Ability"; } else if
				 * (actionBaseUseAbility.isWireframeCancel()) { type =
				 * "Wire Frame Cancel"; } else if
				 * (actionBaseUseAbility.isWireframeClick()) { type =
				 * "Wire Frame Click"; } else if
				 * (actionBaseUseAbility.isWireframeUnload()) { type =
				 * "Wire Frame Unload"; } else { type = "BaseUseAbilityAction";
				 * } }
				 */
				Attribute typeAction = new Attribute("type", type);
				actionElement.setAttribute(typeAction);

				if (actionBaseUseAbility.isRightClick()) {
					if (actionBaseUseAbility.hasTargetUnit()
							&& gameEvents
									.getUnitName((short) actionBaseUseAbility
											.getTargetType()) != null) {
						Attribute typeUnit = new Attribute(
								"targetType",
								gameEvents
										.getUnitName((short) actionBaseUseAbility
												.getTargetType()));
						actionElement.setAttribute(typeUnit);

						Attribute idUnit = new Attribute("targetId",
								Integer.toHexString(actionBaseUseAbility
										.getTargetId()));
						actionElement.setAttribute(idUnit);
					}
				}

				if (actionBaseUseAbility.hasTargetPoint()) {
					Attribute posX = new Attribute(
							"targetX",
							String.valueOf(actionBaseUseAbility.getTargetX() / 65536));
					actionElement.setAttribute(posX);

					Attribute posY = new Attribute(
							"targetY",
							String.valueOf(actionBaseUseAbility.getTargetY() / 65536));
					actionElement.setAttribute(posY);
				}
			} else if (listAction.contains(DifferentActions.Move)
					&& action instanceof IMoveScreenAction) {

				contenu.addContent(actionElement);

				IMoveScreenAction actionMove = (IMoveScreenAction) action;

				Attribute typeAction = new Attribute("type", "MoveScreenAction");
				actionElement.setAttribute(typeAction);

				if (actionMove.hasLocation()) {
					Attribute posX = new Attribute("x",
							String.valueOf(actionMove.getX() / 256));
					actionElement.setAttribute(posX);

					Attribute posY = new Attribute("y",
							String.valueOf(actionMove.getY() / 256));
					actionElement.setAttribute(posY);
				}
				if (actionMove.hasHeightOffset()) {
					Attribute offset = new Attribute("heightOffset",
							String.valueOf(actionMove.getHeightOffset()));
					actionElement.setAttribute(offset);
				}
				if (actionMove.hasDistance()) {
					Attribute distance = new Attribute("distance",
							String.valueOf(actionMove.getDistance() / 256));
					actionElement.setAttribute(distance);
				}
				if (actionMove.hasPitch()) {
					Attribute pitch = new Attribute("pitch",
							String.valueOf(actionMove.getPitch() / 256));
					actionElement.setAttribute(pitch);
				}
				if (actionMove.hasYaw()) {
					Attribute yaw = new Attribute("yaw",
							String.valueOf(actionMove.getYaw() / 256));
					actionElement.setAttribute(yaw);
				}
			}

		}

		/*
		 * Save the file to the correct directory Format : [PREFIX]
		 * FileName-NumFile.xml
		 */
		save(this.path + "/xml/" + this.prefix + fileName + "-" + numFile
				+ ".xml", document);

	}

	/**
	 * Save the XML file
	 * 
	 * @param file
	 *            : file name
	 * @param document
	 *            : the document XML
	 */
	protected static void save(String file, Document document) {
		try {
			XMLOutputter sortie = new XMLOutputter(Format.getCompactFormat());
			FileOutputStream fic = new FileOutputStream(file);
			sortie.output(document, fic);
			fic.flush();
			fic.close();
		} catch (java.io.IOException e) {
		}
	}

	protected void writePlayerInfo(myPlayerId p) throws IOException {
		playerWriter
				.write(p.id.getBattleNetId()
						+ "\t"
						+ p.id.getBattleNetProfileUrl(ReplayConsts.BnetLanguage.ENGLISH)
						+ "\t" + p.id.getBattleNetSubId() + "\t"
						+ p.id.getFullName() + "\t" + p.id.getGateway() + "\t"
						+ p.id.getName() + "\t" + p.id.getRegion() + "\t"
						+ p.id.getSc2ranksProfileUrl() + "\n");
	}

	protected void parsePlayerInfo(IReplay replay) throws IOException {
		myPlayerId p1 = new myPlayerId(generalServices.getInfoApi()
				.getAliasGroupPlayerId(replay.getPlayers()[0].getPlayerId()));
		myPlayerId p2 = new myPlayerId(generalServices.getInfoApi()
				.getAliasGroupPlayerId(replay.getPlayers()[1].getPlayerId()));

		if (players.add(p1))
			writePlayerInfo(p1);
		if (players.add(p2))
			writePlayerInfo(p2);

	}

	protected static int[] writeMessages(IReplay replay) throws IOException {
		int[] counts = { 0, 0 };

		// Print in-game chat along with minimap pings:
		for (IMessageEvents.IMessage message : replay.getMessageEvents()
				.getMessages()) {
			if (message instanceof IMessageEvents.IText)
				if (message.getClient() <= 1 && message.getClient() >= 0)
					counts[message.getClient()]++;
		}
		return counts;
	}

	protected void writeReplayInfo(File f, IReplay replay) throws IOException {
		int[] messageCount = writeMessages(replay);
		IPlayer p1 = replay.getPlayers()[0];
		IPlayer p2 = replay.getPlayers()[1];

		replayWriter.write(f.getName()
				+ "\t"
				+ p1.getPlayerId().getFullName()
				+ "\t"
				+ p2.getPlayerId().getFullName()
				+ "\t"
				+ (replay.getWinnerNames() == p1.getPlayerId().getName() ? p1
						.getPlayerId().getFullName() : p2.getPlayerId()
						.getFullName())
				+ "\t"
				+ p1.getRace()
				+ "\t"
				+ p1.getFinalRace()
				+ "\t"
				+ p2.getRace()
				+ "\t"
				+ p2.getFinalRace()
				+ "\t"
				+ p1.getType()
				+ "\t"
				+ p2.getType()
				+ "\t"
				+ p1.getHandicap()
				+ "\t"
				+ p2.getHandicap()
				+ "\t"
				+ p1.getActionsCount()
				+ "\t"
				+ p2.getActionsCount()
				+ "\t"
				+ p1.getEffectiveActionsCount()
				+ "\t"
				+ p2.getEffectiveActionsCount()
				+ "\t"
				+ p1.getExcludedActionsCount()
				+ "\t"
				+ p2.getExcludedActionsCount()
				+ "\t"
				+ p1.getExcludedEffectiveActionsCount()
				+ "\t"
				+ p2.getExcludedEffectiveActionsCount()
				+ "\t"
				+ messageCount[0]
				+ "\t"
				+ messageCount[1]
				+ "\t"
				+ replay.getRaceMatchup()
				+ "\t"
				+ replay.getGateway()
				+ "\t"
				+ replay.getSaveTime()
				+ "\t"
				+ replay.getSaveTimeZone()
				+ "\t"
				+ replay.getReplayVersion()
				+ "\t"
				+ LadderSeason.getByDate(replay.getSaveTime(),
						replay.getGateway()) + "\t" + replay.getGameLengthSec()
				+ "\t" + replay.getGameType() + "\t" + replay.getMapFileName()
				+ "\n");
	}

	protected void writeMapInfo(IReplay replay) throws IOException {
		if (replay.getMapInfo() == null) {
			System.err.println("MapInfo not found:\t "
					+ replay.getMapFileName());
			mapNotFound += replay.getMapFileName() + "\r\n";
			return;
		}

		boolean added = uniqMaps.add(replay.getMapFileName());
		if (!added)
			return;

		Map<String, String> map = replay.getMapInfo()
				.getLocaleAttributeMapMap().get("US-en");
		if (map == null)
			map = replay.getMapInfo().getLocaleAttributeMapMap().values()
					.iterator().next();

		if (map == null) {
			System.err.println("Map InfoMap not found:\t "
					+ replay.getMapFileName());
			mapNotFound += replay.getMapFileName() + "\r\n";
			return;
		}

		StringBuffer buff = new StringBuffer("");
		try {
			IMapInfo mapInfo = replay.getMapInfo();
			buff.append(replay.getMapFileName()
					+ "\t"
					+ map.get("DocInfo/Author")
					+ "\t"
					+ map.get("DocInfo/Name")
					+ "\t"
					+ map.get("DocInfo/DescShort").replaceAll("\n", ";")
					+ "\t"
					+ map.get("DocInfo/DescLong").replaceAll("\n", ";")
					+ "\t"
					+ mapInfo.getBoundaryBottom()
					+ "\t"
					+ mapInfo.getBoundaryLeft()
					+ "\t"
					+ mapInfo.getBoundaryRight()
					+ "\t"
					+ mapInfo.getBoundaryTop()
					+ "\t"
					+ mapInfo.getHeight()
					+ "\t"
					+ mapInfo.getWidth()
					+ "\t"
					+ mapInfo.getPlayableSizeString()
					+ "\t"
					+ replay.getReplayVersion()
					+ "\t"
					+ LadderSeason.getByDate(replay.getSaveTime(),
							replay.getGateway()) + "\t");

			for (Point p : mapInfo.getStartLocationList())
				buff.append(p.x + "," + p.y + ";");
			buff.append("\t");

			for (Pair<MapObject, Point> pair : mapInfo.getMapObjectList())
				buff.append(getMapObjectString(pair.value1) + "|"
						+ pair.value2.x + "," + pair.value2.y + ";");

			buff.append("\n");
			mapWriter.write(buff.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void writeImage(IReplay replay) {
		IMapInfo mapInfo = replay.getMapInfo();
		if (mapInfo == null) {
			mapNotFound += replay.getMapFileName() + "\r\n";
			return;
		}
		ImageIcon icon = mapInfo.getPreviewIcon();
		if (icon == null) {
			System.err.println("Map icon not found for: "
					+ replay.getMapFileName());
			mapIconNotFound += replay.getMapFileName() + "\r\n";
			return;
		}

		String filename = this.path + "/maps-icons/"
				+ replay.getMapFileName().replaceAll("/", "");
		if (new File(filename).exists())
			return;
		try {

			Image img = icon.getImage();
			BufferedImage bi = new BufferedImage(img.getWidth(null),
					img.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = bi.createGraphics();
			g2.drawImage(img, 0, 0, null);
			g2.dispose();

			ImageIO.write(bi, "jpg", new File(filename + ".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected static String getMapObjectString(MapObject o) {
		if (o.equals(MapObject.DESTRUCTIBLE_DEBRIS_4X4))
			return "DESTRUCTIBLE_DEBRIS_4X4";
		if (o.equals(MapObject.DESTRUCTIBLE_DEBRIS_6X6))
			return "DESTRUCTIBLE_DEBRIS_6X6";
		if (o.equals(MapObject.DESTRUCTIBLE_ROCK_2X6_HORIZONTAL))
			return "DESTRUCTIBLE_ROCK_2X6_HORIZONTAL";
		if (o.equals(MapObject.DESTRUCTIBLE_ROCK_2X6_VERTICAL))
			return "DESTRUCTIBLE_ROCK_2X6_VERTICAL";
		if (o.equals(MapObject.DESTRUCTIBLE_ROCK_4X4))
			return "DESTRUCTIBLE_ROCK_4X4";
		if (o.equals(MapObject.DESTRUCTIBLE_ROCK_6X6))
			return "DESTRUCTIBLE_ROCK_6X6";
		if (o.equals(MapObject.MINERAL_FIELD))
			return "MINERAL_FIELD";
		if (o.equals(MapObject.RICH_MINERAL_FIELD))
			return "RICH_MINERAL_FIELD";
		if (o.equals(MapObject.SPACE_PLATFORM_GEYSER))
			return "SPACE_PLATFORM_GEYSER";
		if (o.equals(MapObject.VESPENE_GEYSER))
			return "VESPENE_GEYSER";
		if (o.equals(MapObject.XEL_NAGA_TOWER))
			return "XEL_NAGA_TOWER";
		return "UNKNOWN_MAP_OBJECT";
	}

	protected void writeStartLocation(File f, IReplay replay)
			throws IOException {
		IPlayer p1 = replay.getPlayers()[0];
		IPlayer p2 = replay.getPlayers()[1];
		if (p1.getStartLocation() == null || p2.getStartLocation() == null) {
			System.err.println("Location not found:\t"
					+ replay.getMapFileName());
			noStartLocation += replay.getMapFileName() + "\r\n";
			return;
		}
		locationWriter.write(f.getName() + "\t"
				+ p1.getPlayerId().getFullName() + "\t"
				+ p2.getPlayerId().getFullName() + "\t"
				+ p1.getStartLocation().x + "\t" + p1.getStartLocation().y
				+ "\t" + p2.getStartLocation().x + "\t"
				+ p2.getStartLocation().y + "\n");
	}
}
