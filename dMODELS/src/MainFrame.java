import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;


public class MainFrame extends JFrame implements ActionListener {

	private File datFile;
	private JTable datTable,invResultTable, bfResultTable, errResultTable;
	private static JFrame frame;
	private JPanel mainPanel, bfPanel, invPanel, errPanel;
	private JTextField fileName, sphereRadius, searchRadius, numGrid;
	private JTextField site, x, y, altitude, e, de, n, dn, u, du;
	private JTextField maxDepth, minDepth, bestfitSolution, computeError, plotContour;
	private JRadioButton buttonDislocation, buttonSpheroid, buttonSill, buttonSphere, buttonCylinder, buttonGPS, buttonGravity, buttonTilt, buttonInSAR;
	private ButtonGroup groupGeo, groupSource;
	private JTabbedPane tabbedPane;

	private DefaultTableModel datModel, invResultModel, bfResultModel, errResultModel;
	static 	String PROJECT_NAME;
	static String GEO_DATA;
	static String SOURCE;
	private static final String[] dat_columns = {"SITE","X[UTM]","Y[UTM]","Altitude(m_asl)","E(m/yr)","dE(m/yr)","N(m/yr)","dN(m/yr)","U(m/yr)","dU(m/yr)"};

	private ArrayList<String>  paramLines;
	private String[] datLines, bfLines, invLines, errLines;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new MainFrame();
				} catch (Exception e) {e.printStackTrace();}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws IOException 
	 */
	public MainFrame() throws IOException {
		super();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws IOException 
	 */
	private void initialize() throws IOException {
		setTitle("dMODELS");
		setBounds(500, 500, 1044, 735);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		//File 
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		//Open
		JMenuItem mnItemOpen = new JMenuItem("Open");
		mnItemOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(null);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					datFile = chooser.getSelectedFile();
					String fileName = datFile.getAbsolutePath();
					readFile(fileName);
					if (fileName.contains("Input"))
						displayParam();
					else 
						displayTable();
				}
			}
		});
		mnFile.add(mnItemOpen);

		//New tab
		JMenuItem mnItemNewTab = new JMenuItem("New Tab");
		mnFile.add(mnItemNewTab);

		//New Window
		JMenuItem mnItemNewWin = new JMenuItem("New Window");
		mnFile.add(mnItemNewWin);
		mnFile.addSeparator();

		//Close
		JMenuItem mnItemClose = new JMenuItem("Close");
		mnFile.add(mnItemClose);

		//Close All
		JMenuItem mnItemCloseAll = new JMenuItem("Close All");
		mnFile.add(mnItemCloseAll);
		mnFile.addSeparator();

		//Exit
		JMenuItem mnItemExit = new JMenuItem("Exit");
		mnItemExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(JFrame.EXIT_ON_CLOSE);
			}
		});
		mnFile.add(mnItemExit);

		//Edit
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		//Tabbed Frame
		getContentPane().setLayout(null);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(6, 231, 801, 434);
		getContentPane().add(tabbedPane);

		//Panel 1
		mainPanel = new JPanel();
		mainPanel.setLayout(null);
		tabbedPane.addTab("Data", null, mainPanel, null);

		datTable = new JTable(); 
		datModel = new DefaultTableModel();
		datModel.setColumnIdentifiers(dat_columns);
		datTable.setModel(datModel);

		// JScrollPane
		JScrollPane pane = new JScrollPane(datTable);
		pane.setBounds(20, 215, 743, 153);

		mainPanel.add(pane);

		// Compile button
		JButton compileButton = new JButton("Compile");
		compileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					scanParam();
					runPreProcessing();
					//runInversionCode();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		compileButton.setBounds(828, 191, 178, 29);
		getContentPane().add(compileButton);

		//Delete button
		JButton deleteButton = new JButton("Delete");
		deleteButton.setBounds(817, 600, 203, 25);
		getContentPane().add(deleteButton);
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int i = datTable.getSelectedRow();
				if (i < 0)
					JOptionPane.showMessageDialog(frame, "Deletion Error. No data found.");
				else 
					datModel.removeRow(i);
			}
		});

		//Add button
		JButton addButton = new JButton("Add");
		addButton.setBounds(817, 560, 203, 29);
		getContentPane().add(addButton);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Object[] row = new Object[10];
				row[0] = site.getText();
				row[1] = x.getText();
				row[2] = y.getText();
				row[3] = altitude.getText();
				row[4] = e.getText();
				row[5] = de.getText();
				row[6] = n.getText();
				row[7] = dn.getText();
				row[8] = u.getText();
				row[9] = du.getText();
				datModel.addRow(row);
			}
		});

		//Update button
		JButton updateButton = new JButton("Update");     
		updateButton.setBounds(817, 634, 203, 25);
		getContentPane().add(updateButton);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int i = datTable.getSelectedRow();
				datModel.setValueAt(site.getText(), i, 0);
				datModel.setValueAt(x.getText(), i, 1);
				datModel.setValueAt(y.getText(), i, 2);
				datModel.setValueAt(altitude.getText(), i, 3);
				datModel.setValueAt(e.getText(), i, 4);
				datModel.setValueAt(de.getText(), i, 5);
				datModel.setValueAt(n.getText(), i, 6);
				datModel.setValueAt(dn.getText(), i, 7);
				datModel.setValueAt(u.getText(), i, 8);
				datModel.setValueAt(du.getText(), i, 9);
			}
		});

		displayTextFields();
		displayLabels();
		displaySourceChoice();
		paramLines = new ArrayList<String>();
		datLines = new String[0];


		setVisible(true);
		//initialize the data files 
	}

	/**
	 * Manage the choices of source and geo data.
	 */
	private void displaySourceChoice() {
		JLabel lblChooseTheType = new JLabel("Choose the type of source:");
		lblChooseTheType.setBounds(34, 122, 174, 16);
		getContentPane().add(lblChooseTheType);

		buttonSphere = new JRadioButton("Sphere");
		buttonDislocation = new JRadioButton("Dislocation");
		buttonSpheroid = new JRadioButton("Spheroid");
		buttonSill = new JRadioButton("Sill");

		buttonCylinder = new JRadioButton("Cylinder");
		buttonCylinder.setBounds(54, 220, 141, 23);
		getContentPane().add(buttonCylinder);

		groupGeo = new ButtonGroup();
		groupGeo.add(buttonSphere);
		groupGeo.add(buttonDislocation);
		groupGeo.add(buttonSpheroid);
		groupGeo.add(buttonSphere);
		groupGeo.add(buttonSill);
		groupGeo.add(buttonCylinder);

		JLabel lblChooseGeoData = new JLabel("Choose the type of geodetic data:");
		lblChooseGeoData.setBounds(34, 11, 227, 16);
		getContentPane().add(lblChooseGeoData);

		buttonSphere.setBounds(54, 140, 74, 23);
		buttonSphere.addActionListener(this);
		getContentPane().add(buttonSphere);

		buttonDislocation.setBounds(54, 160, 164, 23);
		buttonDislocation.addActionListener(this);
		getContentPane().add(buttonDislocation);

		buttonSpheroid.setBounds(54, 180, 141, 23);
		buttonSpheroid.addActionListener(this);
		getContentPane().add(buttonSpheroid);

		buttonSill.setBounds(54, 200, 141, 23);
		buttonSill.addActionListener(this);
		getContentPane().add(buttonSill);

		buttonGPS = new JRadioButton("GPS");
		buttonGPS.addActionListener(this);
		buttonGPS.setBounds(54, 33, 92, 23);
		getContentPane().add(buttonGPS);

		buttonGravity = new JRadioButton("Gravity");
		buttonGravity.addActionListener(this);
		buttonGravity.setBounds(54, 55, 92, 23);
		getContentPane().add(buttonGravity);

		buttonInSAR = new JRadioButton("InSAR");
		buttonInSAR.addActionListener(this);
		buttonInSAR.setBounds(54, 77, 92, 23);
		getContentPane().add(buttonInSAR);

		buttonTilt = new JRadioButton("Tilt");
		buttonTilt.addActionListener(this);
		buttonTilt.setBounds(54, 99, 92, 23);
		getContentPane().add(buttonTilt);

		groupSource = new ButtonGroup();
		groupSource.add(buttonGravity);
		groupSource.add(buttonInSAR);
		groupSource.add(buttonTilt);
		groupSource.add(buttonGPS);
	}

	//set action for buttons
	public void actionPerformed(ActionEvent e) {

		if ("GPS".equals(e.getActionCommand())) {
			GEO_DATA = "GPS"; buttonCylinder.setEnabled(false); buttonSphere.setEnabled(true); buttonSpheroid.setEnabled(true); buttonSill.setEnabled(true); buttonDislocation.setEnabled(true); }
		if ("Gravity".equals(e.getActionCommand())) {
			GEO_DATA = "Gravity"; buttonCylinder.setEnabled(true); buttonSphere.setEnabled(true); buttonSpheroid.setEnabled(true); buttonSill.setEnabled(false); buttonDislocation.setEnabled(false); }
		if ("InSAR".equals(e.getActionCommand())) {
			GEO_DATA = "InSAR"; buttonCylinder.setEnabled(false); buttonSphere.setEnabled(true); buttonSpheroid.setEnabled(true); buttonSill.setEnabled(true); buttonDislocation.setEnabled(false); }
		if ("Tilt".equals(e.getActionCommand())) {
			GEO_DATA = "Tilt"; buttonCylinder.setEnabled(false); buttonSphere.setEnabled(true); buttonSpheroid.setEnabled(true); buttonSill.setEnabled(true); buttonDislocation.setEnabled(true); }
		if (e.getActionCommand().equals("Sphere") || e.getActionCommand().equals("Spheroid") ||  e.getActionCommand().equals("Dislocations") ||  e.getActionCommand().equals("Sill") || e.getActionCommand().equals("Cylinder"))
			SOURCE = e.getActionCommand().equals("Spheroid") ?  "Ellipsoid" : e.getActionCommand();
	}

	/**
	 * Step 1: running the pre_processing code
	 */
	private void runPreProcessing() throws IOException
	{
		if (plotContour.equals("Y")) {
			if (groupGeo.getSelection() == null || groupSource.getSelection() == null) {
				JOptionPane pane = new JOptionPane("Please indicate the type of geodetic data and source to proceed.");
				JDialog dialog = pane.createDialog("Error");
				dialog.setModal(false); // Makes the dialog not modal
				dialog.setVisible(true);
				return;
			}
			if (paramLines.isEmpty() || datLines.length == 0) {
				JOptionPane pane = new JOptionPane("Please enter InputDataFile and/or parameters to proceed.");
				JDialog dialog = pane.createDialog("Error: Input files not found");
				dialog.setModal(false); // Makes the dialog not modal
				dialog.setVisible(true);
				return;
			}
			//		try {
			//			ProcessBuilder process = new ProcessBuilder("C:\\Users\\Thy\\workspace\\dMODELS\\Exe_Files\\utilities\\Pre_Processing\\PreProcessing.exe");
			//			process.directory(new File("C:\\Users\\Thy\\workspace\\dMODELS\\Exe_Files\\utilities\\Pre_Processing"));
			//			Process p = process.start();
			//			p.waitFor();
			//		   p.getInputStream().close();
			//		    p.getOutputStream().close();
			//		    p.getErrorStream().close(); 
			//			
			//		} catch (InterruptedException e) {
			//			System.out.println("Cannot sleep!");
			//		}
			ImageFrame image = new ImageFrame(PROJECT_NAME, SOURCE, "VPL");
			image.setVisible(true);
		}
	}

	/**
	 * Step 2: running the inversion code depend on the user's input
	 */
	private void runInversionCode() throws IOException {
		//best-fit solution
		//		try {
		//			ProcessBuilder process = new ProcessBuilder("C:\\Users\\Thy\\workspace\\dMODELS\\Exe_Files\\sphere\\GPSSphereTopo.exe");
		//			process.directory(new File("C:\\Users\\Thy\\workspace\\dMODELS\\Exe_Files\\sphere"));
		//			Process p = process.start();
		//			p.waitFor();
		//		   p.getInputStream().close();
		//		    p.getOutputStream().close();
		//		    p.getErrorStream().close(); 
		//		} catch (InterruptedException e) {
		//			System.out.println("Cannot sleep!");
		//		}
		if (bestfitSolution.getText().equals("Y")) {
			//plot of best fit solution + inversion
			ImageFrame image = new ImageFrame(PROJECT_NAME, SOURCE, "RSL");
			image.setVisible(true);
			compileRSL_INV();
		}
		if (computeError.getText().equals("Y")) {
			//plot of errors
			ImageFrame image = new ImageFrame(PROJECT_NAME, SOURCE, "ERR");
			image.setVisible(true);
			compileERR();
		}
	}

	/**
	 * Step 3a: compile best-fit solution and inversion statistics
	 */
	private void compileRSL_INV() 
	{
		//bfPanel
		bfPanel = new JPanel();
		bfPanel.setLayout(null);

		bfResultTable = new JTable();
		bfResultModel = new DefaultTableModel();
		bfResultTable.setModel(bfResultModel);
		bfResultTable.setBounds(361, 34, 508, 237);

		JScrollPane pane = new JScrollPane(bfResultTable);
		pane.setBounds(18, 169, 744, 201);

		bfPanel.add(pane);
		readFile("RSL");
		displayBFResultTable();

		String[] param = bfLines[11].trim().split("\\s+");

		JLabel noteLabel = new JLabel(bfLines[0]);
		noteLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
		noteLabel.setBounds(18, 9, 471, 16);
		bfPanel.add(noteLabel);

		JLabel datafileLabel = new JLabel(bfLines[1]);
		datafileLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
		datafileLabel.setBounds(18, 36, 228, 16);
		bfPanel.add(datafileLabel);

		JLabel lblSourceParameters = new JLabel("SOURCE PARAMETERS");
		lblSourceParameters.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		lblSourceParameters.setBounds(258, 37, 164, 16);
		bfPanel.add(lblSourceParameters);

		JLabel lblX2v = new JLabel(bfLines[2]);
		lblX2v.setBounds(18, 65, 120, 16);
		bfPanel.add(lblX2v);

		JLabel lblNumLoop = new JLabel(bfLines[3]);
		lblNumLoop.setBounds(18, 93, 140, 16);
		bfPanel.add(lblNumLoop);

		JLabel lblXYparam = new JLabel(bfLines[6]);
		lblXYparam.setBounds(258, 61, 164, 16);
		bfPanel.add(lblXYparam);

		JLabel lblZparam = new JLabel(bfLines[7]);
		lblZparam.setBounds(258, 86, 267, 16);
		bfPanel.add(lblZparam);

		JLabel lbldPParam = new JLabel(bfLines[8]);
		lbldPParam.setBounds(258, 111, 267, 16);
		bfPanel.add(lbldPParam);

		JLabel lbldVParam = new JLabel(bfLines[9]);
		lbldVParam.setBounds(258, 136, 287, 16);
		bfPanel.add(lbldVParam);

		JLabel lblX = new JLabel("X0: " + param[0]);
		lblX.setBounds(545, 37, 120, 16);
		bfPanel.add(lblX);

		JLabel lblY = new JLabel("Y0: "+  param[1]);
		lblY.setBounds(662, 37, 100, 16);
		bfPanel.add(lblY);

		JLabel lblZ = new JLabel("Z0: " +  param[2]);
		lblZ.setBounds(545, 61, 120, 16);
		bfPanel.add(lblZ);

		JLabel lblRadius = new JLabel("Radius: " +  param[3]);
		lblRadius.setBounds(545, 86, 140, 16);
		bfPanel.add(lblRadius);

		JLabel lblDp = new JLabel("dP(-): " + param[4]);
		lblDp.setBounds(545, 111, 120, 16);
		bfPanel.add(lblDp);

		JLabel lblDv = new JLabel("dV: " +  param[5]);
		lblDv.setBounds(545, 136, 109, 16);
		bfPanel.add(lblDv);

		tabbedPane.addTab("Best-fit Solution", null, bfPanel, null);

		//invPanel
		invPanel = new JPanel();
		invPanel.setLayout(null);

		invResultTable = new JTable();
		invResultModel = new DefaultTableModel();
		invResultTable.setModel(invResultModel);
		invResultTable.setBounds(361, 34, 508, 237);

		JScrollPane invPane = new JScrollPane(invResultTable);
		invPane.setBounds(297, 7, 479, 369);

		invPanel.add(invPane);
		readFile("INV");
		displayINVResultTable();

		JLabel noteLabel2 = new JLabel(invLines[0]);
		noteLabel2.setBounds(6, 7, 279, 43);
		invPanel.add(noteLabel2);

		JLabel datafileLabel2 = new JLabel(invLines[1]);
		datafileLabel2.setBounds(6, 49, 279, 16);
		invPanel.add(datafileLabel2);

		JLabel lblNumLoop2 = new JLabel(invLines[2]);
		lblNumLoop2.setBounds(6, 79, 279, 16);
		invPanel.add(lblNumLoop2);

		JLabel lblInversionParameters = new JLabel("INVERSION PARAMETERS");
		lblInversionParameters.setBounds(6, 107, 166, 16);
		invPanel.add(lblInversionParameters);

		JLabel lblX2v2 = new JLabel(invLines[5]);
		lblX2v2.setBounds(6, 135, 279, 16);
		invPanel.add(lblX2v2);

		JLabel lblXYparam2 = new JLabel(invLines[6]);
		lblXYparam2.setBounds(6, 163, 279, 16);
		invPanel.add(lblXYparam2);

		JLabel lblZparam2 = new JLabel(invLines[7]);
		lblZparam2.setBounds(6, 191, 279, 16);
		invPanel.add(lblZparam2);

		JLabel lblDp2 = new JLabel(invLines[8]);
		lblDp2.setBounds(6, 219, 279, 16);
		invPanel.add(lblDp2);

		JLabel lblDv2 = new JLabel(invLines[9]);
		lblDv2.setBounds(6, 247, 279, 16);
		invPanel.add(lblDv2);

		tabbedPane.addTab("Inversion Statistics", null, invPanel, null);
	}

	/**
	 * Step 3b: display best-fit solution 
	 */
	private void displayBFResultTable() {
		bfResultModel.setColumnIdentifiers(bfLines[13].split("\\s+"));
		for (int i = 14; i < bfLines.length; i++) {
			String[] splited = bfLines[i].trim().split("\\s+");
			bfResultModel.addRow(splited);
		}
		bfResultTable.setModel(bfResultModel);
	}

	/**
	 * Step 3c: display inversion statistics
	 */
	private void displayINVResultTable() {
		invResultModel.setColumnIdentifiers(invLines[11].split("\\s+"));
		for (int i = 12; i < invLines.length; i++) {
			String[] splited = invLines[i].trim().split("\\s+");
			invResultModel.addRow(splited);
		}
		invResultTable.setModel(invResultModel);
	}

	/**
	 * Step 4a: compile error estimation
	 */
	private void compileERR() {
		//errPanel
		errPanel = new JPanel();
		errPanel.setLayout(null);

		errResultTable = new JTable();
		errResultModel = new DefaultTableModel();
		errResultTable.setModel(errResultModel);
		errResultTable.setBounds(361, 34, 508, 237);

		JScrollPane pane = new JScrollPane(errResultTable);
		pane.setBounds(297, 7, 479, 369);

		errPanel.add(pane);
		readFile("ERR");
		displayERRResultTable();

		JLabel noteLabel3 = new JLabel(errLines[0]);
		noteLabel3.setBounds(6, 8, 279, 16);
		errPanel.add(noteLabel3);

		JLabel datafileLabel2 = new JLabel(errLines[1]);
		datafileLabel2.setBounds(6, 42, 285, 16);
		errPanel.add(datafileLabel2);

		JLabel lblNumLoop3 = new JLabel(errLines[2]);
		lblNumLoop3.setBounds(6, 62, 279, 16);
		errPanel.add(lblNumLoop3);

		JLabel lblEstimtedError = new JLabel(errLines[4]);
		lblEstimtedError.setBounds(6, 86, 279, 16);
		errPanel.add(lblEstimtedError);

		JLabel lblXYparam3 = new JLabel(errLines[5]);
		lblXYparam3.setBounds(6, 114, 279, 16);
		errPanel.add(lblXYparam3);

		JLabel lblZparam3 = new JLabel(errLines[6]);
		lblZparam3.setBounds(6, 142, 279, 16);
		errPanel.add(lblZparam3);

		JLabel lblDp3 = new JLabel(errLines[7]);
		lblDp3.setBounds(6, 170, 279, 16);
		errPanel.add(lblDp3);

		JLabel lblDv3 = new JLabel(errLines[8]);
		lblDv3.setBounds(6, 187, 279, 16);
		errPanel.add(lblDv3);

		JLabel lblX0 = new JLabel(errLines[10]);
		lblX0.setBounds(6, 239, 279, 16);
		errPanel.add(lblX0);

		JLabel lblY0 = new JLabel(errLines[11]);
		lblY0.setBounds(6, 255, 279, 16);
		errPanel.add(lblY0);

		JLabel lblZ0 = new JLabel(errLines[12]);
		lblZ0.setBounds(6, 277, 279, 16);
		errPanel.add(lblZ0);

		JLabel lbldP = new JLabel(errLines[13]);
		lbldP.setBounds(6, 298, 279, 16);
		errPanel.add(lbldP);

		JLabel lbldV = new JLabel(errLines[14]);
		lbldV.setBounds(6, 315, 279, 16);
		errPanel.add(lbldV);

		tabbedPane.addTab("Estimated Errors", null, errPanel, null);
	}

	/**
	 * Step 4b: display error estimation
	 */
	private void displayERRResultTable() {
		errResultModel.setColumnIdentifiers(errLines[16].split("\\s+"));
		for (int i = 17; i < errLines.length; i++) {
			String[] splited = errLines[i].trim().split("\\s+");
			errResultModel.addRow(splited);
		}
		errResultTable.setModel(errResultModel);
	}


	private void displayLabels() {
		JLabel FileName = new JLabel("File Name:");
		FileName.setBounds(20, 15, 85, 16);
		mainPanel.add(FileName);

		JLabel NumGrid = new JLabel("Number of random grid searches:");
		NumGrid.setBounds(20, 65, 238, 16);
		mainPanel.add(NumGrid);

		JLabel SphereRadius = new JLabel("Sphere radius (meters):");
		SphereRadius.setBounds(20, 97, 161, 16);
		mainPanel.add(SphereRadius);

		JLabel SearchRadius = new JLabel("Search radius (meters):");
		SearchRadius.setBounds(20, 129, 161, 16);
		mainPanel.add(SearchRadius);

		JSeparator separator = new JSeparator();
		separator.setBounds(10, 47, 731, 12);
		mainPanel.add(separator);

		JLabel MaxDepth = new JLabel("Max source depth (meters):");
		MaxDepth.setBounds(405, 97, 200, 16);
		mainPanel.add(MaxDepth);

		maxDepth = new JTextField();
		maxDepth.setColumns(10);
		maxDepth.setBounds(611, 92, 130, 26);
		mainPanel.add(maxDepth);

		JLabel BestFitSolution = new JLabel("Find best-fit solution? (Y/N)");
		BestFitSolution.setBounds(405, 129, 198, 16);
		mainPanel.add(BestFitSolution);

		bestfitSolution = new JTextField();
		bestfitSolution.setColumns(10);
		bestfitSolution.setBounds(611, 124, 60, 26);
		mainPanel.add(bestfitSolution);

		JLabel ComputeError = new JLabel("Compute errors? (Y/N)");
		ComputeError.setBounds(405, 161, 200, 16);
		mainPanel.add(ComputeError);

		computeError = new JTextField();
		computeError.setColumns(10);
		computeError.setBounds(611, 156, 60, 26);
		mainPanel.add(computeError);

		JLabel PlotContour = new JLabel("Plot contour map of the volcano (map.txt)? (Y/N)");
		PlotContour.setBounds(20, 162, 311, 16);
		mainPanel.add(PlotContour);

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 191, 726, 12);
		mainPanel.add(separator_1);

		plotContour = new JTextField();
		plotContour.setColumns(10);
		plotContour.setBounds(333, 156, 60, 26);
		mainPanel.add(plotContour);

		JLabel MinDepth = new JLabel("Min source depth (meters):");
		MinDepth.setBounds(405, 65, 200, 16);
		mainPanel.add(MinDepth);

		minDepth = new JTextField();
		minDepth.setColumns(10);
		minDepth.setBounds(611, 60, 130, 26);
		mainPanel.add(minDepth);

		JLabel Site = new JLabel(dat_columns[0]);
		Site.setBounds(817, 258, 31, 16);
		getContentPane().add(Site);

		JLabel X = new JLabel(dat_columns[1]);
		X.setBounds(817, 288, 67, 16);
		getContentPane().add(X);

		JLabel Y = new JLabel(dat_columns[2]);
		Y.setBounds(817, 318, 52, 16);
		getContentPane().add(Y);

		JLabel Altitude = new JLabel(dat_columns[3]);
		Altitude.setBounds(817, 348, 100, 16);
		getContentPane().add(Altitude);

		JLabel E = new JLabel(dat_columns[4]);
		E.setBounds(817, 378, 67, 16);
		getContentPane().add(E);

		JLabel DE = new JLabel(dat_columns[5]);
		DE.setBounds(817, 408, 67, 16);
		getContentPane().add(DE);

		JLabel N = new JLabel(dat_columns[6]);
		N.setBounds(817, 438, 67, 16);
		getContentPane().add(N);

		JLabel DN = new JLabel(dat_columns[7]);
		DN.setBounds(817, 468, 67, 16);
		getContentPane().add(DN);

		JLabel U = new JLabel(dat_columns[8]);
		U.setBounds(817, 498, 61, 16);
		getContentPane().add(U);

		JLabel DU = new JLabel(dat_columns[9]);
		DU.setBounds(817, 528, 67, 16);
		getContentPane().add(DU);

		JSeparator separator_2 = new JSeparator();
		separator_2.setBackground(Color.LIGHT_GRAY);
		separator_2.setBounds(817, 231, 205, 12);
		getContentPane().add(separator_2);
	}

	private void displayTextFields(){
		fileName = new JTextField();
		fileName.setBounds(107, 10, 151, 26);
		mainPanel.add(fileName);
		fileName.setColumns(10);

		sphereRadius = new JTextField();
		sphereRadius.setBounds(241, 92, 130, 26);
		mainPanel.add(sphereRadius);
		sphereRadius.setColumns(10);

		searchRadius = new JTextField();
		searchRadius.setBounds(241, 124, 130, 26);
		mainPanel.add(searchRadius);
		searchRadius.setColumns(10);

		numGrid = new JTextField();
		numGrid.setBounds(241, 60, 130, 26);
		mainPanel.add(numGrid);
		numGrid.setColumns(10);

		site = new JTextField();
		site.setBounds(915, 254, 100, 25);
		getContentPane().add(site);

		x = new JTextField();
		x.setBounds(915, 284, 100, 25);
		getContentPane().add(x);

		y = new JTextField();
		y.setBounds(915, 314, 100, 25);
		getContentPane().add(y);

		altitude = new JTextField();
		altitude.setBounds(915, 344, 100, 25);
		getContentPane().add(altitude);

		e = new JTextField();
		e.setBounds(915, 374, 100, 25);
		getContentPane().add(e);

		de = new JTextField();
		de.setBounds(915, 404, 100, 25);
		getContentPane().add(de);

		n = new JTextField();
		n.setBounds(915, 434, 100, 25);
		getContentPane().add(n);

		dn = new JTextField();
		dn.setBounds(915, 464, 100, 25);
		getContentPane().add(dn);

		u = new JTextField();
		u.setBounds(915, 494, 100, 25);
		getContentPane().add(u);

		du = new JTextField();
		du.setBounds(915, 524, 100, 25);
		getContentPane().add(du);
	}


	private void writeFile(String filename) throws IOException {
		//get the true file name
		Path p = Paths.get(filename);
		//TODO edit this 
		filename = "\\Users\\ThyDo\\Documents\\workspace\\dMODELS\\Exe_Files\\GPS\\utilities\\Pre_Processing\\" + p.getFileName().toString();
		System.out.println("Filename: "+ filename);
		//Check if the file is written already. Delete all the available lines
		//Write the new file 
		File f = new File(filename);
		if (f.exists())
			f.delete();

		FileWriter fw = new FileWriter(f,true); 
		BufferedWriter bw = new BufferedWriter(fw);
		if (filename.contains("Data")) {
			Iterator<String> iter = paramLines.iterator();
			while (iter.hasNext()) {
				bw.write(iter.next());
				bw.write("\n");
			}
		}
		else {
			for (String line : datLines) {
				bw.write(line);
				bw.write("\n");
			}
		}
		bw.flush();
		bw.close();
	}

	private void readFile(String fileName) {
		try {
			if (fileName.contains("InputDataFile")) {
				if (!paramLines.isEmpty()) {
					paramLines = new ArrayList<String>();
				}
				BufferedReader reader = new BufferedReader (new InputStreamReader (new FileInputStream (fileName)));
				String line;
				while ((line = reader.readLine ()) != null)
				{
					if (line.startsWith("%") || line.trim ().isEmpty())
						continue; // Ignore the line
					paramLines.add(line);
				}
				reader.close();
				//save a copy in src
				writeFile(fileName);
				PROJECT_NAME = paramLines.get(0).replace(".txt", "");
				displayParam();
			}
			else if (fileName.contains("RSL") ) {
				bfLines = new String[0];
				String file = "Exe_Files//"+ GEO_DATA +"//" + SOURCE + "//" + PROJECT_NAME + "RSL.txt";
				bfLines = Files.readAllLines(new File(file).toPath()).toArray(new String[0]);
			}
			else if (fileName.contains("INV")) {
				invLines = new String[0];
				String file = "Exe_Files//"+ GEO_DATA +"//" + SOURCE + "//" + PROJECT_NAME + "INV.txt";
				invLines = Files.readAllLines(new File(file).toPath()).toArray(new String[0]);
			}
			else if (fileName.contains("ERR")) {
				errLines = new String[0];
				String file = "Exe_Files//"+ GEO_DATA +"//" + SOURCE + "//" + PROJECT_NAME + "ERR.txt";
				errLines = Files.readAllLines(new File(file).toPath()).toArray(new String[0]);
			}
			else {
				datLines = Files.readAllLines(new File(fileName).toPath()).toArray(new String[0]);
				//save a copy in src
				writeFile(fileName);
			}
		} 
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog (null, "Error Accessing The Data File "
					+ fileName + "\n\n" + e.getMessage(), 
					"Please try again!å", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	private void scanParam() throws IOException {
		if (!paramLines.isEmpty())
		{
			paramLines = new ArrayList<String>();
		}
		paramLines.add(fileName.getText() + " ");
		paramLines.add(numGrid.getText() + " ");
		paramLines.add(sphereRadius.getText() + " ");
		paramLines.add(minDepth.getText() + " ");
		paramLines.add(maxDepth.getText() + " ");
		paramLines.add(bestfitSolution.getText() + " ");
		paramLines.add(computeError.getText() + " ");
		paramLines.add(plotContour.getText() + " ");
		//save a copy in src
		writeFile("InputDataFile.txt");
		PROJECT_NAME = paramLines.get(0).replace(".txt", "");

		for (String data : paramLines) {
			if (data.equals("")) {
				JOptionPane pane = new JOptionPane("Please enter all parameters to proceed.");
				JDialog dialog = pane.createDialog("Error");
				dialog.setModal(false); // Makes the dialog not modal
				dialog.setVisible(true);
				return;
			}
		}
	}


	private void displayTable() {
		for (int i = 2; i < datLines.length; i++) {
			String[] splited = datLines[i].split("\\s+");
			datModel.addRow(splited);
		}
		datTable.setModel(datModel);
		datTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}


	private void displayParam() {
		fileName.setText(paramLines.get(0));
		numGrid.setText(paramLines.get(1));
		sphereRadius.setText(paramLines.get(2));
		searchRadius.setText(paramLines.get(3));
		minDepth.setText(paramLines.get(4));
		maxDepth.setText(paramLines.get(5));
		bestfitSolution.setText(paramLines.get(6));
		computeError.setText(paramLines.get(7));
		plotContour.setText(paramLines.get(8));
	}
}

