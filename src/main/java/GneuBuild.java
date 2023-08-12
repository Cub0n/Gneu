import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JDialog;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * This code is part of Gneu, free Software to built and compute parallel
 * constraint satisfaction networks.
 *
 * Written 2003 by Benjamin Kraemer & Andreas Schlicker & Christian Langner
 *
 * GneuBuild provides a GUI for Gneu to create PCS networks
 * </p>
 */
class GneuBuild extends Frame implements WindowListener, ActionListener {

	private final PcsCanvas c = new PcsCanvas();

	public static boolean changesUnsaved = false; // true if changes are made to the PCS network and not saved yet

	private String mode = "none"; // to tell the canvas which mode is used: add neurons oder links

	private boolean firstSave = true; // false if a new file has not been saved yet

	static final List<Integer> selectedNeurons = new ArrayList<>();

	static final List<Integer> selectedLinks = new ArrayList<>();

	private static final TextField nameField = new TextField(StringUtils.EMPTY, 10);

	private static final TextField activationField = new TextField(StringUtils.EMPTY, 10);

	private static final TextField weightField = new TextField(StringUtils.EMPTY, 10);

	public String getMode() {
		return mode;
	}

	public void setMode(String s) {
		mode = s;
	}

	GneuBuild() {
		super("Gneu");
		MenuBar mbar = new MenuBar();
		setMenuBar(mbar);

		Menu fileMenu = new Menu("File");
		mbar.add(fileMenu);
		MenuItem openMI = new MenuItem("Open");
		fileMenu.add(openMI);
		openMI.addActionListener(evt -> loadFile(1));
		MenuItem newMI = new MenuItem("New");
		fileMenu.add(newMI);
		newMI.addActionListener(evt -> {
			saveFile(2);
			Gneu.neuronArr = new ArrayList<>();
			c.repaint();
			changesUnsaved = false;
			firstSave = false;
		});
		MenuItem saveMI = new MenuItem("Save");
		fileMenu.add(saveMI);
		saveMI.addActionListener(evt -> saveFile(1));
		MenuItem saveAsMI = new MenuItem("Save as");
		fileMenu.add(saveAsMI);
		saveAsMI.addActionListener(evt -> saveFile(2));
		MenuItem exitMI = new MenuItem("Exit");
		fileMenu.add(exitMI);
		exitMI.addActionListener(evt -> {
			if (changesUnsaved) {
				saveFile(2);
			}
			System.exit(0);
		});

		Menu prefMenu = new Menu("Preferences");
		mbar.add(prefMenu);
		MenuItem edPrefMI = new MenuItem("Edit Preferences");
		edPrefMI.addActionListener(evt -> edPref());
		prefMenu.add(edPrefMI);
		MenuItem loadPrefMI = new MenuItem("Load Preferences");
		loadPrefMI.addActionListener(evt -> loadFile(2));
		prefMenu.add(loadPrefMI);

		Menu helpMenu = new Menu("Help");
		mbar.add(helpMenu);
		MenuItem helpContentMI = new MenuItem("Contents");
		helpContentMI.addActionListener(evt -> showHelpContents(1));
		helpMenu.add(helpContentMI);

		MenuItem aboutMI = new MenuItem("About");
		aboutMI.addActionListener(evt -> showHelpContents(2));
		helpMenu.add(aboutMI);

		setLayout(new BorderLayout(0, 0));
		Panel panel1 = new Panel();
		panel1.setBackground(Color.lightGray);
		Panel panel2 = new Panel();
		panel2.setBackground(Color.lightGray);
		add("West", panel1);
		panel1.setLayout(new GridLayout(13, 1, 10, 10));
		add("Center", panel2);

		Button sim = new Button("Simulate");
		sim.addActionListener(evt -> {
			Gneu.simulate();
			changesUnsaved = true;
			c.repaint();
		});
		panel1.add(sim);

		Button addNeuron = new Button("Add neuron");
		addNeuron.addActionListener(e -> mode = "addneuron");
		panel1.add(addNeuron);
		/*
		 * Button addUnidiLink=new Button("Add unidirectional link");
		 * addUnidiLink.addActionListener (new ActionListener () { public void
		 * actionPerformed (ActionEvent e) { mode="adduunidlink"; } });
		 * panel1.add(addUnidiLink);
		 */
		Button addBidiLink = new Button("Add link");
		addBidiLink.addActionListener(e -> mode = "addlink");
		panel1.add(addBidiLink);

		Button delete = new Button("Delete");
		delete.addActionListener(e -> {
			for (int i = 0; i < selectedLinks.size(); i += 2) { // delete all selected links
				Gneu.neuronArr.get((selectedLinks.get(i))).deleteLink(selectedLinks.get(i + 1).intValue());
			}
			for (Integer lSelectedNeuron : selectedNeurons) { // delete all selected neurons
				Gneu.deleteNeuron((lSelectedNeuron));
			}
			selectedNeurons.clear();
			selectedLinks.clear();
			changesUnsaved = true;
			c.repaint();
		});
		panel1.add(delete);
		panel1.add(new Label("Name"));
		panel1.add(nameField);
		panel1.add(new Label("Activation"));
		panel1.add(activationField);
		panel1.add(new Label("Weight"));
		panel1.add(weightField);

		Panel panel3 = new Panel();
		panel3.setLayout(new GridLayout(1, 2, 5, 5));
		Button ok = new Button("OK");
		ok.addActionListener(e -> {
			test3: for (int j = 0; j < selectedNeurons.size(); j++) {
				try {
					Gneu.neuronArr.get((selectedNeurons.get(j))).setName(nameField.getText());
					Gneu.neuronArr.get((selectedNeurons.get(j)))
							.setActNew(Double.parseDouble(activationField.getText()));
					Gneu.neuronArr.get((selectedNeurons.get(j)))
							.setActOld(Double.parseDouble(activationField.getText()));
					Gneu.neuronArr.get((selectedNeurons.get(j)))
							.setActStart(Double.parseDouble(activationField.getText()));
					changesUnsaved = true;
				} catch (Exception exc) {
					warn("Unrecognized format of the activation or weight value. Please enter numbers such as \"1\" or \"0.028\"");
					break test3;
				}
			}
			test4: for (int k = 0; k < selectedLinks.size(); k += 2) {
				try {
					Gneu.neuronArr.get((selectedLinks.get(k))).setWeight((selectedLinks.get(k + 1)),
							Double.parseDouble(weightField.getText()));
					changesUnsaved = true;
				} catch (Exception exc) {
					warn("Unrecognized format of the activation or weight value. Please enter numbers such as \"1\" or \"0.028\"");
					break test4;
				}
			}
			c.repaint();
		});
		panel3.add(ok);

		Button reset = new Button("Reset");
		reset.addActionListener(e -> {
			if (selectedNeurons.size() == 1) {
				nameField.setText(Gneu.neuronArr.get((selectedNeurons.get(0))).getName());
				activationField.setText(Gneu.neuronArr.get((selectedNeurons.get(0))).getActNew() + StringUtils.EMPTY);
			}
		});
		panel3.add(reset);
		panel1.add(panel3);

		Panel panel4 = new Panel();
		panel4.setLayout(new GridLayout(1, 2, 5, 5));
		Button activeB = new Button("active");
		activeB.addActionListener(e -> {
			for (Integer lSelectedNeuron : selectedNeurons) {
				Gneu.neuronArr.get((lSelectedNeuron)).setActive(true);
			}
			c.repaint();
			changesUnsaved = true;
		});
		panel4.add(activeB);

		Button passiveB = new Button("passive");
		passiveB.addActionListener(e -> {
			for (Integer lSelectedNeuron : selectedNeurons) {
				Gneu.neuronArr.get((lSelectedNeuron)).setActive(false);
			}
			c.repaint();
			changesUnsaved = true;
		});
		panel4.add(passiveB);
		panel1.add(panel4);

		c.setSize(600, 580);
		c.setBackground(Color.white);
		panel2.add(c);

		addWindowListener(this);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		dispose();
		if (changesUnsaved) {
			saveFile(2);
		}
		System.exit(0);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO document why this method is empty
	}

	// whichDialog=1: save without dialog, 2: "save as", 3. save preferences
	// file without dialog, 4. "save as" for preferences
	private void saveFile(int whichDialog) {
		String warnString = "Error while trying to write the file.";
		FileDialog fd = new FileDialog(this, "Save as", FileDialog.SAVE);
		if ((whichDialog == 2) || (whichDialog == 4)) {
			fd.setVisible(true);
		}
		switch (whichDialog) {
		case 1:
			if (!Gneu.outputNet()) {
				warn(warnString);
			}
			changesUnsaved = false;
			if (!firstSave) {
				saveFile(2);
			}
			break;
		case 2:
			if (fd.getFile() != null) {
				Gneu.setOutputNet(fd.getDirectory() + fd.getFile());
				if (!Gneu.outputNet()) {
					warn(warnString);
				}
				changesUnsaved = false;
				firstSave = true;
			}
			break;
		case 3:
			if (!Gneu.outputParams()) {
				warn(warnString);
			}
			break;
		case 4:
			if (fd.getFile() != null) {
				Gneu.setOutputParams(fd.getDirectory() + fd.getFile());
				if (!Gneu.outputParams()) {
					warn(warnString);
				}
			}
			break;
		default:
			break;
		}

	}

	private void loadFile(int whichDialog) { // whichDialog=1: open input file, 2: open preferences file
		FileDialog fd = new FileDialog(this, "Open", FileDialog.LOAD);
		fd.setVisible(true);
		String parse = null;
		if (whichDialog == 1) {
			if (fd.getFile() != null) {
				Gneu.setInputNet(fd.getDirectory() + fd.getFile());
				Gneu.setOutputNet(fd.getDirectory() + fd.getFile());
				parse = Gneu.parseNet();
				changesUnsaved = false;
			}
		} else if ((whichDialog == 2) && (fd.getFile() != null)) {
			Gneu.setInputParams(fd.getDirectory() + fd.getFile());
			Gneu.setOutputParams(fd.getDirectory() + fd.getFile());
			parse = Gneu.parseParams();
		}

		if (parse == null) {
			if (whichDialog == 1) {
				c.repaint();
			}
		} else {
			warn(parse);
		}
		// System.out.println(fd.getDirectory()+fd.getFile()+StringUtils.SPACE+whichDialog);
	}

	private void warn(String warnString) {
		final Dialog warnDialog = new Dialog(this, "Warning", true);
		warnDialog.setLayout(new FlowLayout());
		Button okButton = new Button("OK");
		okButton.addActionListener(evt -> warnDialog.setVisible(false));
		warnDialog.add(new Label(warnString));
		warnDialog.add(okButton);
		warnDialog.pack();
		warnDialog.setVisible(true);
	}

	private void edPref() {
		final Dialog prefDialog = new Dialog(this, "Preferences", true);
		// prefDialog.resize(400,300);
		prefDialog.setLayout(new BorderLayout(5, 5));
		Panel panelEdit = new Panel();
		panelEdit.setLayout(new GridLayout(2, 2, 10, 10));
		panelEdit.add(new Label("c:"));
		final TextField cText = new TextField(Gneu.getC() + StringUtils.EMPTY, 5);
		panelEdit.add(cText);
		panelEdit.add(new Label("delta:"));
		final TextField deltaText = new TextField(Gneu.getDelta() + StringUtils.EMPTY, 5);
		panelEdit.add(deltaText);
		Panel panelConfirm = new Panel();
		panelConfirm.setLayout(new GridLayout(1, 4, 10, 10));
		Button okButton = new Button("OK");
		Button cancelButton = new Button("Cancel");
		Button saveButton = new Button("Save");
		Button saveAsButton = new Button("Save As");
		okButton.addActionListener(evt -> {
			boolean number = true;
			try {
				Gneu.setC(Double.parseDouble(cText.getText()));
				Gneu.setDelta(Double.parseDouble(deltaText.getText()));
			} catch (Exception e) {
				number = false;
				warn("Unrecognized format of one or more values. Please enter numbers such as \"1\" or \"0.01\"");
			}

			if (number) {
				prefDialog.setVisible(false);
			}
		});
		cancelButton.addActionListener(evt -> prefDialog.setVisible(false));
		saveButton.addActionListener(evt -> {
			boolean number = true;
			try {
				Gneu.setC(Double.parseDouble(cText.getText()));
				Gneu.setDelta(Double.parseDouble(deltaText.getText()));
			} catch (Exception e) {
				number = false;
				warn("Unrecognized format of one or more values. Please enter numbers such as \"1\" or \"0.01\"");
			}

			if (number) {
				saveFile(3);
			}
		});
		saveAsButton.addActionListener(evt -> {
			boolean number = true;
			try {
				Gneu.setC(Double.parseDouble(cText.getText()));
				Gneu.setDelta(Double.parseDouble(deltaText.getText()));
			} catch (Exception e) {
				number = false;
				warn("Unrecognized format of one or more values. Please enter numbers such as \"1\" or \"0.01\"");
			}

			if (number) {
				saveFile(4);
			}
		});

		panelConfirm.add(okButton);
		panelConfirm.add(cancelButton);
		panelConfirm.add(saveButton);
		panelConfirm.add(saveAsButton);
		prefDialog.add(panelEdit, BorderLayout.CENTER);
		prefDialog.add(panelConfirm, BorderLayout.SOUTH);
		prefDialog.pack();
		prefDialog.setVisible(true);
	}

	private void showHelpContents(int whichHelp) { // whichHelp: whether "Help - Contents" (1) or "About" (2) is shown
		final JDialog helpDialog = new JDialog(this, "Help - Contents", true);
		helpDialog.getContentPane().setLayout(new FlowLayout());
		TextArea helpText = new TextArea(StringUtils.EMPTY, 20, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
		helpText.setBackground(Color.white);
		helpText.setEditable(false);
		BufferedReader helpInput;
		try {
			if (whichHelp == 1) {
				helpInput = new BufferedReader(new FileReader("help.txt"));
			} else {
				helpInput = new BufferedReader(new FileReader("about.txt"));
			}
			StringBuilder lineBuffer = new StringBuilder();
			String line = helpInput.readLine();
			lineBuffer.append(line);
			while ((line = helpInput.readLine()) != null) {
				lineBuffer.append("\n");
				lineBuffer.append(line);
			}
			helpText.setText(lineBuffer.toString());
			helpInput.close();
		} catch (IOException e) {
			helpText.setText("File \"help.txt\" not found.");
		}
		helpDialog.getContentPane().add(helpText);
		helpDialog.pack();
		helpDialog.setVisible(true);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				dispose();
			}
		});
	}

	public static void checkSelection(MouseEvent e) {
		// deselect all if Shift is not pressed
		int x = e.getX();
		int y = e.getY();
		if (!e.isShiftDown()) {
			selectedNeurons.clear();
			selectedLinks.clear();
		}
		boolean neuronFound = false;
		boolean linkFound = false;
		test: for (int i = 0; i < Gneu.neuronArr.size(); ++i) {
			int x1 = Gneu.neuronArr.get(i).getX();
			int y1 = Gneu.neuronArr.get(i).getY();
			// System.out.println(i);
			if ((((x - x1) * (x - x1)) + ((y - y1) * (y - y1))) <= 36) {
				// System.out.println("Hit: "+i+StringUtils.SPACE+((Neuron)
				// Gneu.neuronArr.get(i)).getName());
				selectedNeurons.add(i);
				neuronFound = true;
				boolean sameName = true; // true if all selected neurons have the same name
				boolean sameAct = true; // true if all selected neurons have the same activation
				test1: for (int j = 0; j < selectedNeurons.size(); j++) {
					if (!Gneu.neuronArr.get((selectedNeurons.get(0))).getName()
							.equals(Gneu.neuronArr.get((selectedNeurons.get(j))).getName())) {
						sameName = false;
					}
					if (Gneu.neuronArr.get((selectedNeurons.get(0))).getActNew() != Gneu.neuronArr
							.get((selectedNeurons.get(j))).getActNew()) {
						sameAct = false;
					}
					if (!sameName && !sameAct) {
						break test1;
					}
				}
				// if one neuron is selected, show properties of the selected neuron in the
				// text fields
				if ((selectedNeurons.size() == 1) || sameName) {
					nameField.setText(Gneu.neuronArr.get((selectedNeurons.get(0))).getName());
				} else {
					nameField.setText(StringUtils.EMPTY);
				}
				if ((selectedNeurons.size() == 1) || sameAct) {
					activationField
							.setText(Gneu.neuronArr.get((selectedNeurons.get(0))).getActNew() + StringUtils.EMPTY);
				} else {
					activationField.setText(StringUtils.EMPTY);
				}
				if (!e.isShiftDown()) {
					weightField.setText(StringUtils.EMPTY);
				}
				break test;
			}
		}

		if (!neuronFound) {
			for (int i = 0; i < Gneu.neuronArr.size(); ++i) {
				int x1 = Gneu.neuronArr.get(i).getX();
				int y1 = Gneu.neuronArr.get(i).getY();
				Object[] links = Gneu.neuronArr.get(i).getLinks();
				for (int j = 0; j < links.length; ++j) {
					Entry next = (Entry) links[j];
					int x2 = Gneu.neuronArr.get(((Integer) next.getKey())).getX();
					int y2 = Gneu.neuronArr.get(((Integer) next.getKey())).getY();
					double a = ((double) (y2 - y1) / (x2 - x1));
					double b = (y1 - (a * x1));
					boolean between1 = false;
					boolean between2 = false;
					if (((y < (y1 + 3)) && (y > (y2 - 3))) || ((y > (y1 - 3)) && (y < (y2 + 3)))) {
						between1 = true;
					}
					if (((x < x1) && (x > x2)) || ((x > x1) && (x < x2))) {
						between2 = true;
					}
					if ((Math.abs(((a * x) + b) - y) < 5) && between1 && between2) {
						boolean sameWeight = true; // true if all selected links have the same weight
						int target = j;
						// System.out.println("Hit link: "+i+" - "+((Neuron)
						// Gneu.neuronArr.get(i)).getName()+" number "+target+" - x="+ x+"
						// y="+y+"a="+a);
						selectedLinks.add(i);
						selectedLinks.add(target);
						test2: for (int k = 0; k < selectedLinks.size(); k += 2) {
							double weightA = Gneu.neuronArr.get((selectedLinks.get(k)))
									.getWeight((selectedLinks.get(k + 1)));
							double weightB = Gneu.neuronArr.get((selectedLinks.get(0)))
									.getWeight((selectedLinks.get(1)));
							if (weightA != weightB) {
								sameWeight = false;
								break test2;
							}
						}
						if ((selectedLinks.size() == 2) || sameWeight) {
							weightField.setText(
									Gneu.neuronArr.get((selectedLinks.get(0))).getWeight((selectedLinks.get(1)))
											+ StringUtils.EMPTY);
						} else {
							weightField.setText(StringUtils.EMPTY);
						}
						if (!e.isShiftDown()) {
							nameField.setText(StringUtils.EMPTY);
							activationField.setText(StringUtils.EMPTY);
						}
						linkFound = true;
					}
				}
			}
		}
		if (!neuronFound && !linkFound && !e.isShiftDown()) {
			nameField.setText(StringUtils.EMPTY);
			activationField.setText(StringUtils.EMPTY);
			weightField.setText(StringUtils.EMPTY);
		}
	}

}

class PcsCanvas extends Canvas implements MouseListener, MouseMotionListener, KeyListener {
	PcsCanvas() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}

	private int remind = -1;

	@Override
	public void paint(Graphics g) {
		for (int i = 0; i < Gneu.neuronArr.size(); ++i) {
			Object[] links = Gneu.neuronArr.get(i).getLinks();
			for (int j = 0; j < links.length; ++j) {
				Entry next = (Entry) links[j];
				double weight = ((Double) next.getValue());
				if (weight < 0) {
					g.setColor(Color.red);
				} else {
					g.setColor(Color.green);
				}
				int startX = Gneu.neuronArr.get(i).getX();
				int startY = Gneu.neuronArr.get(i).getY();
				int targetX = Gneu.neuronArr.get(((Integer) next.getKey())).getX();
				int targetY = Gneu.neuronArr.get(((Integer) next.getKey())).getY();
				g.drawLine(startX, startY, targetX, targetY);

				for (int k = 0; k < GneuBuild.selectedLinks.size(); k += 2) {
					if ((GneuBuild.selectedLinks.get(k) != null) && (GneuBuild.selectedLinks.get(k + 1) != null)
							&& (next.getKey() != null) && (GneuBuild.selectedLinks.get(k).intValue() == i)
							&& (GneuBuild.selectedLinks.get(k + 1).intValue() == j)) {
						g.drawLine(startX - 1, startY, targetX - 1, targetY);
						g.drawLine(startX, startY - 1, targetX, targetY - 1);
						g.drawLine(startX + 1, startY, targetX + 1, targetY);
						g.drawLine(startX, startY + 1, targetX, targetY + 1);
					}
				}

				g.setColor(Color.black);
			}
		}
		for (int i = 0; i < Gneu.neuronArr.size(); ++i) {
			for (Integer lElement : GneuBuild.selectedNeurons) {
				if (lElement.intValue() == i) {
					g.setColor(Color.red);
				}
			}
			g.fillOval(Gneu.neuronArr.get(i).getX() - 6, Gneu.neuronArr.get(i).getY() - 6, 12, 12);
			g.setColor(Color.black);
			double act = Gneu.neuronArr.get(i).getActNew();
			String s = (Gneu.neuronArr.get(i).getName() + StringUtils.SPACE + act);
			if (act < 0) {
				g.setColor(Color.blue);
			} else {
				g.setColor(Color.orange);
			}
			g.drawString(s, Gneu.neuronArr.get(i).getX() + 8, Gneu.neuronArr.get(i).getY() - 8);
			g.setColor(Color.black);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (Gneu.getMode().equals("addneuron")) {
			Gneu.addNeuron(e.getX(), e.getY(), 0.001, "n");
			Gneu.setMode("none");
			repaint();
			GneuBuild.changesUnsaved = true;
		} else {
			if (Gneu.getMode().equals("addlink")) {
				if (GneuBuild.selectedNeurons.size() == 1) {
					remind = (GneuBuild.selectedNeurons.get(0));
					GneuBuild.checkSelection(e);
					if (GneuBuild.selectedNeurons.size() == 1) {
						Gneu.addLink(remind, (GneuBuild.selectedNeurons.get(0)), 0.01);// create link
						GneuBuild.changesUnsaved = true;
					}
				}
				remind = -1;
				Gneu.setMode("none");
			} else {
				GneuBuild.checkSelection(e);
			}
			repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (GneuBuild.selectedNeurons.size() == 1) {
			Gneu.neuronArr.get((GneuBuild.selectedNeurons.get(0))).setX(e.getX());
			Gneu.neuronArr.get((GneuBuild.selectedNeurons.get(0))).setY(e.getY());
			GneuBuild.changesUnsaved = true;
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO document why this method is empty
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO document why this method is empty
	}
}