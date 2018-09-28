package myzip;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

// ЗАДАЧИ
// разобратся с layout
// почитать про потоки
// сделать отдельный поток на длинные операции - считывание файла...
// сделать считывание больших файлов


public class MyZipGui extends JFrame {
	public static MainFrame mainFrame;

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			mainFrame = new MainFrame("MyZipProgram");
			mainFrame.setVisible(true);

		});
	}
}
