
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel implements MouseListener {

	private BufferedImage initialImg;
	private Image scaledImg;

	/**
	 * Create the panel.
	 * @throws IOException 
	 */
	public ImagePanel(int width, int height, String name, String code)
	{
		try {
			String filePath = findfileName(name,code);
			File file = new File(filePath);
			URL url = file.toURI().toURL();

			this.initialImg = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String findfileName (String name, String code) {
		String result = "Exe_Files//GPS//";
		if (code.contains("VPL"))
			return result + "utilities//Pre_Processing//" + name + "VPL.jpg";

		else if (code.contains("Ellipsoid"))
			result += "Ellipsoid//";
		else if (code.contains("Sphere"))
			result += "Sphere//";
		else if (code.contains("Sill"))
			result += "Sill//";
		else if (code.contains("Cylinder"))
			result += "Cylinder//";
		else 
			result += "Dislocations//";

		result += name;

		if (code.contains("ERR"))
			return result + "ERR.jpg";
		else if (code.contains("INV"))
			return result + "INV.jpg";
		else 
			return result + "RSL.jpg";	
	}

	@Override
	protected void paintComponent(Graphics g) {
		scaledImg = initialImg.getScaledInstance(this.getWidth(), this.getHeight(),Image.SCALE_SMOOTH);    
		super.paintComponent(g); 
		if (scaledImg != null) {
			g.drawImage(scaledImg, 0, 0, this);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// Do nothing		

	}
	@Override
	public void mousePressed(MouseEvent e) {
		// Do nothing		

	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// Do nothing		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// Do nothing		

	}
}
