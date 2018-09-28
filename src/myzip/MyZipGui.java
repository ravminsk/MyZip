package myzip;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class MyZipGui extends JFrame {
	public static MainFrame mainFrame;

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			mainFrame = new MainFrame("MyZipProgram");
			mainFrame.setVisible(true);

		});
	}
}
