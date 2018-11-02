package myzip;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class MyZipGui extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static MainFrame mainFrame;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainFrame = new MainFrame("MyZipProgram");
					mainFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
