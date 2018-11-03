package myzip;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// constructor
	public MainFrame(String title) {

		// define GUI components
		setMinimumSize(new Dimension(400, 300));
		setLocationByPlatform(true);
		setBounds(100, 100, 450, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 15, 125, 15, 125, 15 };
		gridBagLayout.rowHeights = new int[] { 15, 30, 15, 20, 30, 15, 20, 40, 15 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.00, 0.0, 0.4, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);
		setTitle(title);

		JButton bArchive = new JButton("Добавить в архив...");
		bArchive.setMargin(new Insets(2, 5, 2, 5));
		bArchive.setMinimumSize(new Dimension(125, 30));
		bArchive.setPreferredSize(new Dimension(125, 30));
		GridBagConstraints gbc_bArchive = new GridBagConstraints();
		gbc_bArchive.fill = GridBagConstraints.HORIZONTAL;
		gbc_bArchive.gridx = 1;
		gbc_bArchive.gridy = 1;
		getContentPane().add(bArchive, gbc_bArchive);

		JButton bExtract = new JButton("Извлечь из архива...");
		bExtract.setMargin(new Insets(2, 5, 2, 5));
		bExtract.setToolTipText("Извлечь файл из архива...");
		bExtract.setMinimumSize(new Dimension(125, 30));
		bExtract.setPreferredSize(new Dimension(125, 30));
		GridBagConstraints gbc_bExtract = new GridBagConstraints();
		gbc_bExtract.fill = GridBagConstraints.HORIZONTAL;
		gbc_bExtract.gridx = 3;
		gbc_bExtract.gridy = 1;
		getContentPane().add(bExtract, gbc_bExtract);

		JScrollPane scrollPaneForProgressBar = new JScrollPane();
		GridBagConstraints gbcForProgressBar = new GridBagConstraints();
		gbcForProgressBar.anchor = GridBagConstraints.SOUTH;
		gbcForProgressBar.gridheight = 3;
		gbcForProgressBar.gridwidth = 3;
		gbcForProgressBar.fill = GridBagConstraints.BOTH;
		gbcForProgressBar.gridx = 1;
		gbcForProgressBar.gridy = 3;
		getContentPane().add(scrollPaneForProgressBar, gbcForProgressBar);

		JPanel panelForProgressBar = new JPanel();
		panelForProgressBar.setBorder(new EmptyBorder(3, 3, 3, 3));
		scrollPaneForProgressBar.setViewportView(panelForProgressBar);
		panelForProgressBar.setLayout(new GridLayout(5, 0, 0, 3));

		JLabel lbHistory = new JLabel("История");
		GridBagConstraints gbc_lbHistory = new GridBagConstraints();
		gbc_lbHistory.anchor = GridBagConstraints.WEST;
		gbc_lbHistory.gridx = 1;
		gbc_lbHistory.gridy = 6;
		getContentPane().add(lbHistory, gbc_lbHistory);

		JScrollPane scrollPaneForTextArea = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 7;
		getContentPane().add(scrollPaneForTextArea, gbc_scrollPane);

		JTextArea taHistory = new JTextArea();
		scrollPaneForTextArea.setViewportView(taHistory);

		// event handling
		bArchive.addActionListener(e -> {

			FileDialog fd = new FileDialog(this, "Выберите файл для сжатия", FileDialog.LOAD);
			fd.setVisible(true);
			if (fd.getFile() == null) {
				return;
			}

			// run action in a new thread
			Runnable runArchive = new Runnable() {
				@Override
				public void run() {
					//create tmp progressBar
					JProgressBar progressBar = new JProgressBar();
					progressBar.setStringPainted(true);
					progressBar.setValue(0);
					progressBar.setString("Идет подготовка к архивации...");
					panelForProgressBar.add(progressBar);
					panelForProgressBar.revalidate();

					Haffman fileArchive = new Haffman(fd);
					fileArchive.toArchive(progressBar, taHistory);

					panelForProgressBar.remove(progressBar);
					panelForProgressBar.revalidate();
				}
			};
			new Thread(runArchive).start();

		});

		bExtract.addActionListener(e -> {
			FileDialog fd = new FileDialog(this, "Выберите файл для извлечения из архива", FileDialog.LOAD);
			fd.setVisible(true);
			if (fd.getFile() == null) {
				return;
			}
			// run action in a new thread
			Runnable runExtract = new Runnable() {
				@Override
				public void run() {
					//create tmp progressBar
					JProgressBar progressBar = new JProgressBar();
					progressBar.setStringPainted(true);
					progressBar.setValue(0);
					progressBar.setString("Идет подготовка к извлечению...");
					panelForProgressBar.add(progressBar);
					panelForProgressBar.revalidate();

					Haffman fileExtract = new Haffman(fd);
					fileExtract.toExtract(progressBar, taHistory);

					panelForProgressBar.remove(progressBar);
					panelForProgressBar.repaint();
					panelForProgressBar.revalidate();
				}
			};
			new Thread(runExtract).start();

		});
	}

}
