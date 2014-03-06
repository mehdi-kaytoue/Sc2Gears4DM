import hu.belicza.andras.sc2gearspluginapi.GeneralServices;
import hu.belicza.andras.sc2gearspluginapi.PluginDescriptor;
import hu.belicza.andras.sc2gearspluginapi.PluginServices;
import hu.belicza.andras.sc2gearspluginapi.api.GuiUtilsApi;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpCallback;
import hu.belicza.andras.sc2gearspluginapi.api.listener.ReplayOpsPopupMenuItemListener;
import hu.belicza.andras.sc2gearspluginapi.impl.BasePlugin;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 * Displays the window which allows the user to generate XML and CSV files from
 * replays
 * 
 * @author Guillaume BOSC, Mehdi KAYTOUE
 * 
 */
public class Sc2Gears4DM extends BasePlugin {
	/** Hander of the new replay ops popup menu item. */

	/*
	 * Attributes declarations
	 */
	private Integer showSc2ConverterForPatternMiningItemHandler;
	private JDialog window;
	private final JFileChooser fc = new JFileChooser();
	private javax.swing.JButton BBrowse;
	private javax.swing.JButton BGenerate;
	private javax.swing.JCheckBox CBBuild;
	private javax.swing.JCheckBox CBClickMini;
	private javax.swing.JCheckBox CBPlayer;
	private javax.swing.JCheckBox CBMap;
	private javax.swing.JCheckBox CBIcon;
	private javax.swing.JCheckBox CBReplay;
	private javax.swing.JCheckBox CBHotKey;
	private javax.swing.JCheckBox CBMove;
	private javax.swing.JCheckBox CBResearch;
	private javax.swing.JCheckBox CBRightClick;
	private javax.swing.JCheckBox CBSelect;
	private javax.swing.JCheckBox CBTrain;
	private javax.swing.JCheckBox CBUpgrade;
	private javax.swing.JCheckBox CBUseBA;
	private javax.swing.JCheckBox CBUseUA;
	private javax.swing.JCheckBox CBXml;
	private javax.swing.JTextField TDirectory;
	private javax.swing.JTextField TPrefix;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JProgressBar PBar;
	private File[] filesEns;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel logoInsa;
	private javax.swing.JLabel logoLiris;
	private JProgressBar PBarDownload;
	private JDialog DDownload;
	JLabel jLabel7;

	private boolean alreadyDownloaded;

	/*
	 * End of attributes declarations
	 */

	public Sc2Gears4DM() {
	}

	@Override
	public void init(final PluginDescriptor pluginDescriptor,
			final PluginServices pluginServices,
			final GeneralServices generalServices) {
		super.init(pluginDescriptor, pluginServices, generalServices);

		final ImageIcon boIcon = new ImageIcon(
				"./Plugins/Sc2Gears4DM/fig/icon.png");
		showSc2ConverterForPatternMiningItemHandler = generalServices
				.getCallbackApi()
				.addReplayOpsPopupMenuItem(
						"Generate exhaustive information from selected replay(s)",
						boIcon, new ReplayOpsPopupMenuItemListener() {
							@Override
							public void actionPerformed(final File[] files,
									final ReplayOpCallback replayOpCallback,
									final Integer handler) {
								filesEns = files;
								displayWindow(generalServices, files.length);
							}
						});
	}

	/**
	 * Display the window
	 * 
	 * @param generalServices
	 *            : the genral service of the Sc2Gears API
	 * @param filesLengh
	 *            : the number of files (replays)
	 */
	protected void displayWindow(GeneralServices generalServices, int filesLengh) {
		final GuiUtilsApi guiUtils = generalServices.getGuiUtilsApi();
		window = new JDialog(guiUtils.getMainFrame(),
				"Generate files from replays");
		alreadyDownloaded = false;
		Image icone = Toolkit.getDefaultToolkit().getImage(
				"./Plugins/Sc2Gears4DM/fig/icon.png");
		window.setIconImage(icone);
		initComponents(filesLengh);
		PBar.setVisible(false);
		PBar.setValue(0);
		PBar.setStringPainted(true);
		CBXml.setToolTipText("Generates one XML-file by replay which contains all the selected actions.");
		CBPlayer.setToolTipText("Generates the players information file which contains all the information about all the players.");
		CBReplay.setToolTipText("Generates the replays information file which contains all the information about all the replays.");
		CBMap.setToolTipText("Generates the two maps files which contain the starting position information and all the information about all the maps for the later.");
		CBIcon.setToolTipText("<html>Generates one map icon file (jpg format) by different map contained in the replays selection.<br/><b><font color='red'>Be careful, this option may double execution time !</font></b></html>");

		displayCSV(false);

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		guiUtils.centerWindowToWindow(window, guiUtils.getMainFrame());
		
		
		window.setVisible(true);
	}

	/**
	 * Update the window settings and displays
	 * 
	 * @param bool
	 */
	private void displayCSV(boolean bool) {
		jLabel3.setVisible(bool);
		jLabel5.setVisible(bool);
		TPrefix.setVisible(bool);
		CBBuild.setVisible(bool);
		CBClickMini.setVisible(bool);
		CBHotKey.setVisible(bool);
		CBMove.setVisible(bool);
		CBResearch.setVisible(bool);
		CBRightClick.setVisible(bool);
		CBSelect.setVisible(bool);
		CBTrain.setVisible(bool);
		CBUpgrade.setVisible(bool);
		CBUseBA.setVisible(bool);
		CBUseUA.setVisible(bool);

		int shiftProgress = 0;
		if (PBar.isVisible()) {
			shiftProgress = 20;
		}
		if (bool) {
			window.setSize(552, 450 + shiftProgress);

		} else {
			window.setSize(552, 330 + shiftProgress);
		}

	}

	/**
	 * Generate the parsing when the user clicks on the Generate button
	 * 
	 * @param evt
	 */
	private void BGenerateActionPerformed(java.awt.event.ActionEvent evt) {
		if (TDirectory.getText().compareTo("") != 0) {
			int retour = JOptionPane.showConfirmDialog(window,
					"Do you really want to generate with these options ?",
					"Confirm generation", JOptionPane.YES_NO_OPTION);
			if (retour == JOptionPane.YES_OPTION) {
				BGenerate.setVisible(false);
				PBar.setVisible(true);

				/*
				 * The list of selected actions by the user
				 */
				List<DifferentActions> listAction = new ArrayList<DifferentActions>();
				if (CBBuild.isSelected())
					listAction.add(DifferentActions.Build);
				if (CBClickMini.isSelected())
					listAction.add(DifferentActions.ClickMini);
				if (CBHotKey.isSelected())
					listAction.add(DifferentActions.HotKey);
				if (CBMove.isSelected())
					listAction.add(DifferentActions.Move);
				if (CBResearch.isSelected())
					listAction.add(DifferentActions.Research);
				if (CBRightClick.isSelected())
					listAction.add(DifferentActions.RightClick);
				if (CBSelect.isSelected())
					listAction.add(DifferentActions.Select);
				if (CBTrain.isSelected())
					listAction.add(DifferentActions.Train);
				if (CBUpgrade.isSelected())
					listAction.add(DifferentActions.Upgrade);
				if (CBUseUA.isSelected())
					listAction.add(DifferentActions.UseUA);
				if (CBUseBA.isSelected())
					listAction.add(DifferentActions.UseBA);

				/*
				 * Call for the other class which executes the parsing
				 */
				new FileGenerator(filesEns, generalServices, this,
						TDirectory.getText(), TPrefix.getText(),
						CBXml.isSelected(), CBPlayer.isSelected(),
						CBReplay.isSelected(), CBMap.isSelected(),
						CBIcon.isSelected(), listAction);
			}
		} else {
			JOptionPane
					.showMessageDialog(
							window,
							"Directory is not supposed to be empty.",
							"Field error", JOptionPane.ERROR_MESSAGE);
		}

	}

	private void CBXmlActionPerformed(java.awt.event.ActionEvent evt) {
		displayCSV(CBXml.isSelected());
	}

	private void BBrowseActionPerformed(java.awt.event.ActionEvent evt) {
		int returnVal = fc.showOpenDialog(window);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			TDirectory.setText(fc.getSelectedFile().getPath());
		}
	}

	/**
	 * Creates the components and display them
	 * 
	 * @param filesLengh
	 *            : the number of files to parse
	 */
	private void initComponents(int filesLengh) {

		PBar = new javax.swing.JProgressBar(0, filesLengh);
		BGenerate = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		CBXml = new javax.swing.JCheckBox();
		CBPlayer = new javax.swing.JCheckBox();
		TDirectory = new javax.swing.JTextField();
		BBrowse = new javax.swing.JButton();
		TPrefix = new javax.swing.JTextField();
		jLabel5 = new javax.swing.JLabel();
		CBBuild = new javax.swing.JCheckBox();
		CBUpgrade = new javax.swing.JCheckBox();
		CBTrain = new javax.swing.JCheckBox();
		CBMove = new javax.swing.JCheckBox();
		CBSelect = new javax.swing.JCheckBox();
		CBHotKey = new javax.swing.JCheckBox();
		CBResearch = new javax.swing.JCheckBox();
		CBUseBA = new javax.swing.JCheckBox();
		CBClickMini = new javax.swing.JCheckBox();
		CBRightClick = new javax.swing.JCheckBox();
		CBUseUA = new javax.swing.JCheckBox();
		jLabel4 = new javax.swing.JLabel();
		logoInsa = new javax.swing.JLabel();
		logoLiris = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		CBReplay = new javax.swing.JCheckBox();
		CBMap = new javax.swing.JCheckBox();
		CBIcon = new javax.swing.JCheckBox();

		window.setResizable(false);

		PBar.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentShown(java.awt.event.ComponentEvent evt) {
				displayCSV(CBXml.isSelected());
			}

			public void componentHidden(java.awt.event.ComponentEvent evt) {
				displayCSV(CBXml.isSelected());
			}
		});

		BGenerate.setText("Generate");
		BGenerate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		BGenerate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				BGenerateActionPerformed(evt);
			}
		});

		jLabel1.setFont(new java.awt.Font("Ubuntu", 1, 15));
		jLabel1.setText("Files to generate : ");

		jLabel2.setFont(new java.awt.Font("Ubuntu", 1, 15));
		jLabel2.setText("Directory :");

		jLabel3.setFont(new java.awt.Font("Ubuntu", 1, 15));
		jLabel3.setText("XML Files prefix :");

		CBXml.setText("XML Files");
		CBXml.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				CBXmlActionPerformed(evt);
			}
		});

		CBPlayer.setText("Players Info.");

		TDirectory.setBackground(java.awt.Color.lightGray);
		TDirectory.setFocusable(false);

		BBrowse.setText("Browse");
		BBrowse.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				BBrowseActionPerformed(evt);
			}
		});

		jLabel5.setFont(new java.awt.Font("Ubuntu", 1, 15));
		jLabel5.setText("Select action types :");

		CBBuild.setText("Build");
		CBUpgrade.setText("Upgrade");
		CBTrain.setText("Train");
		CBMove.setText("Move Screen");
		CBSelect.setText("Select");
		CBHotKey.setText("HotKey");
		CBResearch.setText("Research");
		CBUseBA.setText("Use Building Ability");
		CBClickMini.setText("Click Minimap");
		CBRightClick.setText("Right Click");
		CBUseUA.setText("Use Unit Ability");

		jLabel4.setFont(new java.awt.Font("DejaVu Serif", 0, 10));
		jLabel4.setForeground(java.awt.Color.black);
		jLabel4.setText("Authors : Guillaume BOSC, Mehdi KAYTOUE");

		logoInsa.setIcon(new javax.swing.ImageIcon(
				"./Plugins/Sc2Gears4DM/fig/INSA.jpg"));

		logoLiris.setIcon(new javax.swing.ImageIcon(
				"./Plugins/Sc2Gears4DM/fig/LIRIS.jpg"));

		jLabel6.setFont(new java.awt.Font("DejaVu Serif", 0, 10));
		jLabel6.setForeground(java.awt.Color.black);
		jLabel6.setText("Contact : mehdi.kaytoue@insa-lyon.fr");

		CBReplay.setText("Replays Info.");

		CBMap.setText("Maps Info.");
		CBMap.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (!alreadyDownloaded) {
					int retour = JOptionPane.showConfirmDialog(window,
							"Do you want to download the corresponding maps ?",
							"Download maps", JOptionPane.YES_NO_OPTION);
					if (retour == JOptionPane.YES_OPTION) {
						downloadMapFile();
						alreadyDownloaded = true;
					}
				}
			}
		});

		CBIcon.setText("Map Icons");
		CBIcon.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (!alreadyDownloaded) {
					int retour = JOptionPane.showConfirmDialog(window,
							"Do you want to download the corresponding maps ?",
							"Download maps", JOptionPane.YES_NO_OPTION);
					if (retour == JOptionPane.YES_OPTION) {
						downloadMapFile();
						alreadyDownloaded = true;
					}
				}
			}
		});

		/*
		 * Display the window layout
		 */
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				window.getContentPane());
		window.getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGap(23, 23, 23)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		jLabel6)
																.addGap(0,
																		0,
																		Short.MAX_VALUE))
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		jLabel4)
																.addContainerGap(
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		Short.MAX_VALUE))
												.addGroup(
														layout.createSequentialGroup()
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.TRAILING)
																				.addGroup(
																						layout.createSequentialGroup()
																								.addComponent(
																										logoInsa)
																								.addGap(61,
																										61,
																										61)
																								.addComponent(
																										BGenerate)
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																										javax.swing.GroupLayout.DEFAULT_SIZE,
																										Short.MAX_VALUE)
																								.addComponent(
																										logoLiris))
																				.addGroup(
																						javax.swing.GroupLayout.Alignment.LEADING,
																						layout.createSequentialGroup()
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																												.addComponent(
																														jLabel3)
																												.addComponent(
																														jLabel2)
																												.addComponent(
																														jLabel5))
																								.addGap(41,
																										41,
																										41)
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																												.addGroup(
																														layout.createSequentialGroup()
																																.addGap(0,
																																		0,
																																		Short.MAX_VALUE)
																																.addGroup(
																																		layout.createParallelGroup(
																																				javax.swing.GroupLayout.Alignment.LEADING)
																																				.addComponent(
																																						CBTrain)
																																				.addComponent(
																																						CBMove)
																																				.addComponent(
																																						CBHotKey)))
																												.addGroup(
																														layout.createSequentialGroup()
																																.addGroup(
																																		layout.createParallelGroup(
																																				javax.swing.GroupLayout.Alignment.LEADING)
																																				.addGroup(
																																						layout.createSequentialGroup()
																																								.addComponent(
																																										CBUseUA)
																																								.addGap(2,
																																										2,
																																										2)
																																								.addGroup(
																																										layout.createParallelGroup(
																																												javax.swing.GroupLayout.Alignment.LEADING)
																																												.addComponent(
																																														CBRightClick)
																																												.addComponent(
																																														CBClickMini)
																																												.addComponent(
																																														CBUseBA)
																																												.addComponent(
																																														CBSelect)))
																																				.addComponent(
																																						CBBuild)
																																				.addComponent(
																																						CBUpgrade)
																																				.addComponent(
																																						CBResearch)
																																				.addComponent(
																																						TPrefix,
																																						javax.swing.GroupLayout.PREFERRED_SIZE,
																																						242,
																																						javax.swing.GroupLayout.PREFERRED_SIZE))
																																.addGap(0,
																																		0,
																																		Short.MAX_VALUE))))
																				.addGroup(
																						javax.swing.GroupLayout.Alignment.LEADING,
																						layout.createSequentialGroup()
																								.addComponent(
																										jLabel1)
																								.addGap(52,
																										52,
																										52)
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																												.addGroup(
																														layout.createSequentialGroup()
																																.addComponent(
																																		TDirectory,
																																		javax.swing.GroupLayout.PREFERRED_SIZE,
																																		242,
																																		javax.swing.GroupLayout.PREFERRED_SIZE)
																																.addPreferredGap(
																																		javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																.addComponent(
																																		BBrowse))
																												.addGroup(
																														layout.createSequentialGroup()
																																.addGroup(
																																		layout.createParallelGroup(
																																				javax.swing.GroupLayout.Alignment.LEADING)
																																				.addComponent(
																																						CBPlayer)
																																				.addComponent(
																																						CBXml))
																																.addPreferredGap(
																																		javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																.addGroup(
																																		layout.createParallelGroup(
																																				javax.swing.GroupLayout.Alignment.LEADING)
																																				.addComponent(
																																						CBReplay)
																																				.addGroup(
																																						layout.createSequentialGroup()
																																								.addComponent(
																																										CBMap)
																																								.addGap(20,
																																										20,
																																										20)
																																								.addComponent(
																																										CBIcon)))))
																								.addGap(0,
																										45,
																										Short.MAX_VALUE))
																				.addComponent(
																						PBar,
																						javax.swing.GroupLayout.Alignment.LEADING,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						Short.MAX_VALUE))
																.addGap(23, 23,
																		23)))));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addGap(23, 23, 23)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(jLabel1)
												.addGroup(
														javax.swing.GroupLayout.Alignment.TRAILING,
														layout.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
																.addComponent(
																		CBXml)
																.addComponent(
																		CBReplay)))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(CBPlayer)
												.addComponent(CBMap)
												.addComponent(CBIcon))
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(
														layout.createSequentialGroup()
																.addGap(13, 13,
																		13)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.BASELINE)
																				.addComponent(
																						jLabel2)
																				.addComponent(
																						TDirectory,
																						javax.swing.GroupLayout.PREFERRED_SIZE,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						javax.swing.GroupLayout.PREFERRED_SIZE)
																				.addComponent(
																						BBrowse))
																.addGap(18, 18,
																		18)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.BASELINE)
																				.addComponent(
																						TPrefix,
																						javax.swing.GroupLayout.PREFERRED_SIZE,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						javax.swing.GroupLayout.PREFERRED_SIZE)
																				.addComponent(
																						jLabel3))
																.addGap(18, 18,
																		18)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.BASELINE)
																				.addComponent(
																						jLabel5)
																				.addComponent(
																						CBBuild)
																				.addComponent(
																						CBMove)
																				.addComponent(
																						CBSelect))
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.BASELINE)
																				.addComponent(
																						CBUpgrade)
																				.addComponent(
																						CBHotKey)
																				.addComponent(
																						CBRightClick))
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.BASELINE)
																				.addComponent(
																						CBResearch)
																				.addComponent(
																						CBTrain)
																				.addComponent(
																						CBClickMini))
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.BASELINE)
																				.addComponent(
																						CBUseUA)
																				.addComponent(
																						CBUseBA))
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																		99,
																		Short.MAX_VALUE)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING)
																				.addComponent(
																						BGenerate,
																						javax.swing.GroupLayout.Alignment.TRAILING)
																				.addComponent(
																						logoInsa,
																						javax.swing.GroupLayout.Alignment.TRAILING)))
												.addGroup(
														layout.createSequentialGroup()
																.addGap(0,
																		0,
																		Short.MAX_VALUE)
																.addComponent(
																		logoLiris)))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(PBar,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										20,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(30, 30, 30)
								.addComponent(jLabel4)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jLabel6)));
		window.pack();
	}

	@Override
	public void destroy() {
		// Remove the registered replay ops popup menu item
		generalServices.getCallbackApi().removeReplayOpsPopupMenuItem(
				showSc2ConverterForPatternMiningItemHandler);
	}

	/**
	 * getter method for the progress bar attributes
	 * 
	 * @return PBar attribute
	 */
	public javax.swing.JProgressBar getPBar() {
		return PBar;
	}
	
	/**
	 * getter method for the progress bar attributes
	 * 
	 * @return PBar attribute
	 */
	public javax.swing.JProgressBar getPBarDownload() {
		return PBarDownload;
	}

	/**
	 * getter method for the generate button attributes
	 * 
	 * @return BGenerate attribute
	 */
	public javax.swing.JButton getBGenerate() {
		return BGenerate;
	}
	
	
	/**
	 * Getter method for the download sub-frame : DDownload
	 * @return DDownload : the sub-frame for the download map function
	 */
	public JDialog getDDownload() {
		return DDownload;
	}
	
	
	/**
	 * Getter method for the main frame : window
	 * @return window : the main frame of the plugin
	 */
	public JDialog getWindow() {
		return window;
	}

	/**
	 * Display the DDownload sub-frame to download required maps.
	 * Launch the thread which executes the download. 
	 */
	protected void downloadMapFile() {
		DDownload = new javax.swing.JDialog(window, "Download maps");
		PBarDownload = new javax.swing.JProgressBar(0, filesEns.length);
		PBarDownload.setValue(0);
		PBarDownload.setMaximum(filesEns.length);
		PBarDownload.setStringPainted(true);

		jLabel7 = new javax.swing.JLabel();
		jLabel7.setText("Download in progress...");

		
		/*
		 * Display the DDownload frame layout
		 */
		javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(
				DDownload.getContentPane());
		DDownload.getContentPane().setLayout(jDialog1Layout);
		jDialog1Layout
				.setHorizontalGroup(jDialog1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jDialog1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jDialog1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																PBarDownload,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addGroup(
																jDialog1Layout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel7)
																		.addGap(0,
																				0,
																				Short.MAX_VALUE)))
										.addContainerGap()));
		jDialog1Layout
				.setVerticalGroup(jDialog1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								jDialog1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(jLabel7)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(
												PBarDownload,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap()));
		
		
		DDownload.setSize(300, 100);
		DDownload.setResizable(false);
		
		generalServices.getGuiUtilsApi().centerWindowToWindow(DDownload,
				generalServices.getGuiUtilsApi().getMainFrame());
		
		DDownload.setVisible(true);
		
		// Launch the thread
		new DownloadMaps(filesEns, generalServices, this);	
		
	}
	
	

}
