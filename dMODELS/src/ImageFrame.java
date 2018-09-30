import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ImageFrame extends JFrame {

	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private JTabbedPane sourceTabbedPane;
	private String name,code,source;
	private JFileChooser chooser;
	private int tabIndex;

	/**
	 * Create the frame.
	 */
	public ImageFrame(String name, String source, String code) {
		this.name = name;
		this.code = code;
		this.source = source;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 878, 640);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmSaveAs = new JMenuItem("Save");
		mnFile.add(mntmSaveAs);

		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser  = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int returnVal = chooser.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					try {
						String filename = findfileName();
						BufferedImage image = ImageIO.read(new File ( filename ).toURI().toURL());
						ImageIO.write(image, "jpg", file);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
				tabIndex = sourceTabbedPane.getSelectedIndex();
			}
		};
		tabbedPane.addChangeListener(changeListener);

		tabbedPane.setBounds(0, 0, 878, 596);
		contentPane.add(tabbedPane);

		computeImages();
	}

	private void computeImages() {
		if (code.contains("VPL")) {
			ImagePanel plot = new ImagePanel(contentPane.getWidth(),contentPane.getHeight(), name, code);
			tabbedPane.addTab("Vector Plot" + " (" + name + ") ", null, plot, null);
			setTitle("Vector Plot");
		}
		else if (code.contains("RSL")) {
			ImagePanel bestfit = new ImagePanel(contentPane.getWidth(),contentPane.getHeight(), name, source + "RSL");
			ImagePanel inversion = new ImagePanel(contentPane.getWidth(),contentPane.getHeight(), name, source + "INV");

			tabbedPane.addTab("Best-fit solution" + " (" + name + ") ", null, bestfit, null);
			tabbedPane.addTab("Inversion statistics" + " (" + name + ") ", null, inversion, null);
			setTitle("Optimized results");
		}
		else if (code.contains("ERR")) {
			ImagePanel error = new ImagePanel(contentPane.getWidth(),contentPane.getHeight(), name, source + "ERR");
			tabbedPane.addTab("Estimated errors statistics" + " (" + name + ") ", null, error, null);
			setTitle("Estimated errors");
		}
	}

	public String findfileName () {
		String result = "Exe_Files//GPS//";
		if (code.contains("VPL"))
			return result + "utilities//Pre_Processing//" + name + "VPL.jpg";

		else if (source.contains("Ellipsoid"))
			result += "Ellipsoid//";
		else if (source.contains("Sphere"))
			result += "Sphere//";
		else if (source.contains("Sill"))
			result += "Sill//";
		else if (source.contains("Cylinder"))
			result += "Cylinder//";
		else 
			result += "Dislocations//";

		result += name;

		String tabName =  sourceTabbedPane.getTitleAt(tabIndex);
		if (tabName != null && tabName.contains("Vector Plot"))
			result += "VPL.jpg";
		else if (tabName.contains("Best-fit solution"))
			result += "RSL.jpg";
		else if (tabName.contains("Estimated errors statistics"))
			result += "ERR.jpg";
		else if (tabName.contains("Inversion statistics"))
			result += "INV.jpg";

		return result;
	}
}

