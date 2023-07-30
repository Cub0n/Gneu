/* This code is part of Gneu, free software to built and compute parallel contraint satisfaction networks,
written 2003 by Benjamin Krämer, Andreas Schlicker & Christian Langner,
You may use, distribute an modify this code under the conditions of the Gnu Public License: www.gnu.org/copyleft/gpl.html

GneuBuild provides a GUI for Gneu to create PCS networks
*/

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
import java.util.Map.Entry;

import javax.swing.JDialog;

class GneuBuild extends Frame implements WindowListener, ActionListener
{
 private PcsCanvas c = new PcsCanvas();
 public static boolean changesUnsaved=false; // true if changes are made to the PCS network und not saved yet
 private String mode=new String("none"); // to tell the canvas which mode is used: add neurons oder links
 private boolean firstSave=true; // false if a new file has not been saved yet
 public static ArrayList selectedNeurons=new ArrayList();
 public static ArrayList selectedLinks=new ArrayList();
 private static TextField nameField=new TextField("", 10);
 private static TextField activationField=new TextField("", 10);
 private static TextField weightField=new TextField("", 10);

 public String getMode() {
        return mode;
 }

 public void setMode(String s) {
        mode=s;
 }

 GneuBuild ()
 {
  super("Gneu");
  MenuBar mbar = new MenuBar();
  setMenuBar(mbar);

  Menu fileMenu = new Menu("File");
  mbar.add(fileMenu);
  MenuItem openMI=new MenuItem("Open");
  fileMenu.add(openMI);
  openMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadFile(1);
            }
        });
  MenuItem newMI=new MenuItem("New");
  fileMenu.add(newMI);
  newMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFile(2);
                Gneu.neuronArr=new ArrayList();
                c.repaint();
                changesUnsaved=false;
                firstSave=false;
            }
        });
  MenuItem saveMI=new MenuItem("Save");
  fileMenu.add(saveMI);
  saveMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFile(1);
            }
        });
  MenuItem saveAsMI=new MenuItem("Save as");
  fileMenu.add(saveAsMI);
  saveAsMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFile(2);
            }
        });
  MenuItem exitMI=new MenuItem("Exit");
  fileMenu.add(exitMI);
  exitMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (changesUnsaved==false) {
                        System.exit(0);
                }
                else {
                        saveFile(2);
                        System.exit(0);
                }
            }
        });

  Menu prefMenu = new Menu("Preferences");
  mbar.add(prefMenu);
  MenuItem edPrefMI=new MenuItem("Edit Preferences");
  edPrefMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edPref();
            }
        });
  prefMenu.add(edPrefMI);
  MenuItem loadPrefMI=new MenuItem("Load Preferences");
  loadPrefMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadFile(2);
            }
        });
  prefMenu.add(loadPrefMI);

  Menu helpMenu = new Menu("Help");
  mbar.add(helpMenu);
  MenuItem helpContentMI=new MenuItem("Contents");
  helpContentMI.addActionListener (new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
                showHelpContents(1);
        }
  });
  helpMenu.add(helpContentMI);

  MenuItem aboutMI=new MenuItem("About");
  aboutMI.addActionListener (new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
                showHelpContents(2);
        }
  });
  helpMenu.add(aboutMI);

  setLayout(new BorderLayout(0,0));
  Panel panel1 = new Panel();
  panel1.setBackground(Color.lightGray);
  Panel panel2 = new Panel();
  panel2.setBackground(Color.lightGray);
  add("West", panel1);
  panel1.setLayout(new GridLayout(13,1,10,10));
  add("Center", panel2);

  Button sim=new Button("Simulate");
  sim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Gneu.simulate();
                changesUnsaved=true;
                c.repaint();
            }
  });
  panel1.add(sim);

  Button addNeuron=new Button("Add neuron");
  addNeuron.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e) {
        mode="addneuron";
        }
  });
  panel1.add(addNeuron);
  /*
  Button addUnidiLink=new Button("Add unidirectional link");
  addUnidiLink.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e) {
        mode="adduunidlink";
        }
  });
  panel1.add(addUnidiLink);
  */
  Button addBidiLink=new Button("Add link");
  addBidiLink.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e) {
        mode="addlink";
        }
  });
  panel1.add(addBidiLink);

  Button delete=new Button("Delete");
  delete.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e) {
                for (int i=0; i<selectedLinks.size(); i+=2) { // delete all selected links
                        ((Neuron) Gneu.neuronArr.get(((Integer)selectedLinks.get(i)).intValue())).deleteLink(((Integer)selectedLinks.get(i+1)).intValue());
                }
                for (int j=0; j<selectedNeurons.size(); j++) { // delete all selected neurons
                        Gneu.deleteNeuron(((Integer)selectedNeurons.get(j)).intValue());
                }
                selectedNeurons=new ArrayList();
                selectedLinks=new ArrayList();
                changesUnsaved=true;
                c.repaint();
        }
  });
  panel1.add(delete);
  panel1.add(new Label("Name"));
  panel1.add(nameField);
  panel1.add(new Label("Activation"));
  panel1.add(activationField);
  panel1.add(new Label("Weight"));
  panel1.add(weightField);

  Panel panel3=new Panel();
  panel3.setLayout(new GridLayout(1,2,5,5));
  Button ok=new Button("OK");
  ok.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e) {
                test3: for (int j=0; j<selectedNeurons.size(); j++) {
                        try {
                                ((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(j)).intValue())).setName(nameField.getText());
                                ((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(j)).intValue())).setActNew(Double.parseDouble(activationField.getText()));
                                ((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(j)).intValue())).setActOld(Double.parseDouble(activationField.getText()));
                                ((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(j)).intValue())).setActStart(Double.parseDouble(activationField.getText()));
                                changesUnsaved=true;
                        } catch (Exception exc) {
                                warn("Unrecognized format of the activation or weight value. Please enter numbers such as \"1\" or \"0.028\"");
                                break test3;
                        }
                }
                test4: for (int k=0; k<selectedLinks.size(); k+=2) {
                        try {
                                ((Neuron) Gneu.neuronArr.get(((Integer)selectedLinks.get(k)).intValue())).setWeight(((Integer)selectedLinks.get(k+1)).intValue(), Double.parseDouble(weightField.getText()));
                                changesUnsaved=true;
                        } catch (Exception exc) {
                                warn("Unrecognized format of the activation or weight value. Please enter numbers such as \"1\" or \"0.028\"");
                                break test4;
                        }
                }
                c.repaint();
        }
  });
  panel3.add(ok);

  Button reset=new Button("Reset");
  reset.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e) {
                if (selectedNeurons.size()==1) {
                        nameField.setText(((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(0)).intValue())).getName());
                        activationField.setText(((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(0)).intValue())).getActNew()+"");
                }
        }
  });
  panel3.add(reset);
  panel1.add(panel3);

  Panel panel4=new Panel();
  panel4.setLayout(new GridLayout(1,2,5,5));
  Button activeB=new Button("active");
  activeB.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e) {
                for (int i=0; i<selectedNeurons.size(); i++) {
                        ((Neuron)Gneu.neuronArr.get(((Integer)selectedNeurons.get(i)).intValue())).setActive(true);
                }
                c.repaint();
                changesUnsaved=true;
        }
  });
  panel4.add(activeB);

  Button passiveB=new Button("passive");
  passiveB.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e) {
                for (int i=0; i<selectedNeurons.size(); i++) {
                        ((Neuron)Gneu.neuronArr.get(((Integer)selectedNeurons.get(i)).intValue())).setActive(false);
                }
                c.repaint();
                changesUnsaved=true;
        }
  });
  panel4.add(passiveB);
  panel1.add(panel4);

  c.setSize(600,580);
  c.setBackground(Color.white);
  panel2.add(c);

  addWindowListener(this);
  }

 public void windowClosing (WindowEvent e) {
   dispose();
   if (changesUnsaved==false) {
                        System.exit(0);
                }
                else {
                        saveFile(2);
                        System.exit(0);
                }
  }

  public void windowClosed (WindowEvent e) { }
  public void windowOpened (WindowEvent e) { }
  public void windowIconified (WindowEvent e) { }
  public void windowDeiconified (WindowEvent e) { }
  public void windowActivated (WindowEvent e) { }
  public void windowDeactivated (WindowEvent e) { }
  public void actionPerformed (ActionEvent e) { }

  public void saveFile(int whichDialog) { // whichDialog=1: save without dialog, 2: "save as", 3. save preferences file wihout dialog, 4. "save as" for preferences
        String warnString=new String("Error while trying to write the file.");
        FileDialog fd=new FileDialog (this, "Save as", FileDialog.SAVE);
        if (whichDialog==2 || whichDialog==4) {
                fd.show();
        }
        switch (whichDialog) {
        case 1: if (!Gneu.outputNet()) warn(warnString);
                changesUnsaved=false;
                if (firstSave==false) {
                        saveFile(2);
                }
                break;
        case 2: if (fd.getFile()!=null) {
                        Gneu.setOutputNet(fd.getDirectory()+fd.getFile());
                        if (!Gneu.outputNet()) warn(warnString);
                        changesUnsaved=false;
                        firstSave=true;
                }
                break;
        case 3: if (!Gneu.outputParams()) warn(warnString);
                break;
        case 4: if (fd.getFile()!=null) {
                        Gneu.setOutputParams(fd.getDirectory()+fd.getFile());
                        if (!Gneu.outputParams()) warn(warnString);
                }
                break;
        }
  }

  public void loadFile (int whichDialog) { // whichDialog=1: open input file, 2: open preferences file
        FileDialog fd = new FileDialog(this, "Open", FileDialog.LOAD);
        fd.show();
        String parse=null;
        if (whichDialog==1) {
                if(fd.getFile()!=null) {
                        Gneu.setInputNet(fd.getDirectory()+fd.getFile());
                        Gneu.setOutputNet(fd.getDirectory()+fd.getFile());
                        parse=Gneu.parseNet();
                        changesUnsaved=false;
                }
        }
        else if (whichDialog==2) {
                if(fd.getFile()!=null) {
                        Gneu.setInputParams(fd.getDirectory()+fd.getFile());
                        Gneu.setOutputParams(fd.getDirectory()+fd.getFile());
                        parse=Gneu.parseParams();

                }
        }

        if(parse==null) {
                if (whichDialog==1) {
                        c.repaint();
                }
        }
        else {
                warn(parse);
        }
        //System.out.println(fd.getDirectory()+fd.getFile()+" "+whichDialog);
        }

 public void warn (String warnString) {
        final Dialog warnDialog= new Dialog(this, "Warning", true);
        warnDialog.setLayout(new FlowLayout());
        Button okButton=new Button("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
                warnDialog.hide();
                }
        });
        warnDialog.add(new Label(warnString));
        warnDialog.add(okButton);
        warnDialog.pack();
        warnDialog.show();
 }

 public void edPref() {
        final Dialog prefDialog= new Dialog(this, "Preferences", true);
        //prefDialog.resize(400,300);
        prefDialog.setLayout(new BorderLayout(5,5));
        Panel panelEdit=new Panel();
        panelEdit.setLayout(new GridLayout(2,2,10,10));
        panelEdit.add(new Label("c:"));
        final TextField cText=new TextField(Gneu.getC()+"", 5);
        panelEdit.add(cText);
        panelEdit.add(new Label("delta:"));
        final TextField deltaText=new TextField(Gneu.getDelta()+"", 5);
        panelEdit.add(deltaText);
        Panel panelConfirm=new Panel();
        panelConfirm.setLayout(new GridLayout(1,4,10,10));
        Button okButton=new Button("OK");
        Button cancelButton=new Button("Cancel");
        Button saveButton=new Button ("Save");
        Button saveAsButton=new Button ("Save As");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean number=true;
                try {
                        Gneu.setC(Double.parseDouble(cText.getText()));
                        Gneu.setDelta(Double.parseDouble(deltaText.getText()));
                } catch (Exception e) {
                        number=false;
                        warn ("Unrecognized format of one or more values. Please enter numbers such as \"1\" or \"0.01\"");
                }

                if (number) {
                        prefDialog.hide();
                }
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefDialog.hide();
            }
        });
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean number=true;
                try {
                        Gneu.setC(Double.parseDouble(cText.getText()));
                        Gneu.setDelta(Double.parseDouble(deltaText.getText()));
                } catch (Exception e) {
                        number=false;
                        warn ("Unrecognized format of one or more values. Please enter numbers such as \"1\" or \"0.01\"");
                }

                if (number) {
                        saveFile(3);
                }
            }
        });
        saveAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean number=true;
                try {
                        Gneu.setC(Double.parseDouble(cText.getText()));
                        Gneu.setDelta(Double.parseDouble(deltaText.getText()));
                } catch (Exception e) {
                        number=false;
                        warn ("Unrecognized format of one or more values. Please enter numbers such as \"1\" or \"0.01\"");
                }

                if (number) {
                        saveFile(4);
                }
            }
        });

        panelConfirm.add(okButton);
        panelConfirm.add(cancelButton);
        panelConfirm.add(saveButton);
        panelConfirm.add(saveAsButton);
        prefDialog.add(panelEdit,BorderLayout.CENTER);
        prefDialog.add(panelConfirm,BorderLayout.SOUTH);
        prefDialog.pack();
        prefDialog.show();
   }

  public void showHelpContents(int whichHelp) { // whichHelp: whether "Help - Contents" (1) or "About" (2) is shown
        final JDialog helpDialog=new JDialog(this, "Help - Contents", true);
        helpDialog.getContentPane().setLayout(new FlowLayout());
        TextArea helpText=new TextArea("",20,40,TextArea.SCROLLBARS_VERTICAL_ONLY);
        helpText.setBackground(Color.white);
        helpText.setEditable(false);
        BufferedReader helpInput;
        try {
                if (whichHelp==1) {
                        helpInput = new BufferedReader(new FileReader ("help.txt"));
                }
                else {
                        helpInput = new BufferedReader(new FileReader ("about.txt"));
                }
                StringBuffer lineBuffer = new StringBuffer();
                String line=new String();
                line=helpInput.readLine();
                lineBuffer.append(line);
                while ((line = helpInput.readLine()) != null) {
                        lineBuffer.append("\n");
                        lineBuffer.append(line);
                }
                helpText.setText(lineBuffer.toString());
                helpInput.close();
        }
        catch (IOException e) {
                helpText.setText("File \"help.txt\" not found.");
        }
        helpDialog.getContentPane().add(helpText);
        helpDialog.pack();
        helpDialog.setVisible(true);
        this.addWindowListener (new WindowAdapter () {
                public void windowClosing (WindowEvent e) {
                        setVisible(false);
                        dispose();
                }
        });
  }

  static public void checkSelection(MouseEvent e) {
        //deselect all if Shift is not pressed
        int x=e.getX();
        int y=e.getY();
        if (!e.isShiftDown()) {
                selectedNeurons=new ArrayList();
                selectedLinks=new ArrayList();
        }
        boolean neuronFound=false;
        boolean linkFound=false;
        test: for (int i = 0; i < Gneu.neuronArr.size(); ++i) {
                int x1=((Neuron) Gneu.neuronArr.get(i)).getX();
                int y1=((Neuron) Gneu.neuronArr.get(i)).getY();
                //System.out.println(i);
                if ((x-x1)*(x-x1) + (y-y1)*(y-y1) <=36) {
                        //System.out.println("Hit: "+i+" "+((Neuron) Gneu.neuronArr.get(i)).getName());
                        selectedNeurons.add(new Integer(i));
                        neuronFound=true;
                        boolean sameName=true; // true if all selected neurons have the same name
                        boolean sameAct=true; // true if all selected neurons have the same activation
                        test1: for (int j=0; j<selectedNeurons.size(); j++) {
                                if (!((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(0)).intValue())).getName().equals(((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(j)).intValue())).getName())) {
                                        sameName=false;
                                }
                                if (((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(0)).intValue())).getActNew()!=((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(j)).intValue())).getActNew()) {
                                        sameAct=false;
                                }
                                if (!sameName && !sameAct) {
                                break test1;
                                }
                        }
                        // if one neuron ist selected, show properties of the selected neuron in the text fields
                        if (selectedNeurons.size()==1 || sameName) {
                                nameField.setText(((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(0)).intValue())).getName());
                        } else {
                                nameField.setText("");
                        }
                        if (selectedNeurons.size()==1 || sameAct) {
                                activationField.setText(((Neuron) Gneu.neuronArr.get(((Integer)selectedNeurons.get(0)).intValue())).getActNew()+"");
                        } else {
                                activationField.setText("");
                        }
                        if (!e.isShiftDown()) {
                                weightField.setText("");
                        }
                        break test;
                }
        }
        if (!neuronFound) for (int i = 0; i < Gneu.neuronArr.size(); ++i) {
                int x1=((Neuron) Gneu.neuronArr.get(i)).getX();
                int y1=((Neuron) Gneu.neuronArr.get(i)).getY();
                Object[] links = ((Neuron) Gneu.neuronArr.get(i)).getLinks();
		for (int j = 0; j < links.length; ++j) {
		        Entry next = (Entry) links[j];
                        int x2=((Neuron) Gneu.neuronArr.get(((Integer) next.getKey()).intValue())).getX();
                        int y2=((Neuron) Gneu.neuronArr.get(((Integer) next.getKey()).intValue())).getY();
                        double a=((double)(y2-y1)/(x2-x1));
                        double b=((double)y1-a*x1);
                        boolean between1=false;
                        boolean between2=false;
                        if ((y<y1+3 && y>y2-3) || (y>y1-3 && y<y2+3)) between1=true;
                        if ((x<x1 && x>x2) || (x>x1 && x<x2)) between2=true;
                        if (Math.abs((a*x+b)-y)<5 && between1 && between2) {
                                boolean sameWeight=true; // true if all selected links have the same weight
                                int target=j;
                                // System.out.println("Hit link: "+i+" - "+((Neuron) Gneu.neuronArr.get(i)).getName()+" number "+target+" - x="+ x+" y="+y+"a="+a);
                                selectedLinks.add(new Integer(i));
                                selectedLinks.add(new Integer(target));
                                test2: for (int k=0; k<selectedLinks.size(); k+=2) {
                                        double weightA=((Neuron) Gneu.neuronArr.get(((Integer)selectedLinks.get(k)).intValue())).getWeight(((Integer)selectedLinks.get(k+1)).intValue());
                                        double weightB=((Neuron) Gneu.neuronArr.get(((Integer)selectedLinks.get(0)).intValue())).getWeight(((Integer)selectedLinks.get(1)).intValue());
                                        if (weightA!=weightB) {
                                                sameWeight=false;
                                                break test2;
                                        }
                                }
                                if (selectedLinks.size()==2 || sameWeight) {
                                        weightField.setText(((Neuron) Gneu.neuronArr.get(((Integer)selectedLinks.get(0)).intValue())).getWeight(((Integer)selectedLinks.get(1)).intValue())+"");
                                } else {
                                        weightField.setText("");
                                }
                                if (!e.isShiftDown()) {
                                        nameField.setText("");
                                        activationField.setText("");
                                }
                                linkFound=true;
                        }
                }
        }
        if (!neuronFound && !linkFound && !e.isShiftDown()) {
                nameField.setText("");
                activationField.setText("");
                weightField.setText("");
        }
  }

}

class PcsCanvas extends Canvas implements MouseListener, MouseMotionListener, KeyListener
{
 PcsCanvas ()
 {
  super();
  addMouseListener(this);
  addMouseMotionListener(this);
  addKeyListener(this);
 }
 int x=0;
 int y=0;
 int remind=-1;
 public void paint (Graphics g)
 {
  for (int i = 0; i < Gneu.neuronArr.size(); ++i) {
        Object[] links = ((Neuron) Gneu.neuronArr.get(i)).getLinks();
	for (int j = 0; j < links.length; ++j) {
                Entry next = (Entry) links[j];
                double weight = ((Double) next.getValue()).doubleValue();
                if (weight<0) {
                        g.setColor(Color.red);
                }
                else {
                        g.setColor(Color.green);
                }
                int startX=((Neuron)Gneu.neuronArr.get(i)).getX();
                int startY=((Neuron) Gneu.neuronArr.get(i)).getY();
                int targetX=((Neuron) Gneu.neuronArr.get(((Integer) next.getKey()).intValue())).getX();
                int targetY=((Neuron) Gneu.neuronArr.get(((Integer) next.getKey()).intValue())).getY();
                g.drawLine(startX,startY,targetX,targetY);

                for (int k=0; k<GneuBuild.selectedLinks.size();k+=2)
                {
                        if (GneuBuild.selectedLinks.get(k)!=null && GneuBuild.selectedLinks.get(k+1)!=null && next.getKey()!=null)
                                {
                                        if (((Integer)GneuBuild.selectedLinks.get(k)).intValue()==i && ((Integer)GneuBuild.selectedLinks.get(k+1)).intValue()==j)
                                        {
                                                g.drawLine(startX-1,startY,targetX-1,targetY);
                                                g.drawLine(startX,startY-1,targetX,targetY-1);
                                                g.drawLine(startX+1,startY,targetX+1,targetY);
                                                g.drawLine(startX,startY+1,targetX,targetY+1);
                                        }
                        }
                }
                g.setColor(Color.black);
        }
   }
   for (int i = 0; i < Gneu.neuronArr.size(); ++i) {
        for (int j=0;j<GneuBuild.selectedNeurons.size();j++)
        {
                if (((Integer)GneuBuild.selectedNeurons.get(j)).intValue()==i) {
                        g.setColor(Color.red);
                }
        }
        g.fillOval(((Neuron) Gneu.neuronArr.get(i)).getX()-6,((Neuron) Gneu.neuronArr.get(i)).getY()-6, 12, 12);
        g.setColor(Color.black);
        double act=((Neuron) Gneu.neuronArr.get(i)).getActNew();
        String s=new String(((Neuron) Gneu.neuronArr.get(i)).getName()+" "+act);
        if (act<0) {
                g.setColor(Color.blue);
                }
                else {
                        g.setColor(Color.orange);
                }
        g.drawString(s, ((Neuron) Gneu.neuronArr.get(i)).getX()+8,((Neuron) Gneu.neuronArr.get(i)).getY()-8);
        g.setColor(Color.black);
  }
 }
 public void mouseClicked (MouseEvent e) {}
 public void mouseReleased (MouseEvent e) {}
 public void mousePressed (MouseEvent e)
 {
        if (Gneu.getMode().equals("addneuron")) {
                Gneu.addNeuron(e.getX(), e.getY(), 0.001, "n");
                Gneu.setMode("none");
                repaint();
                GneuBuild.changesUnsaved=true;
        }
        else if (Gneu.getMode().equals("addlink")) {
                if (GneuBuild.selectedNeurons.size()==1) {
                        remind=((Integer)GneuBuild.selectedNeurons.get(0)).intValue();
                        GneuBuild.checkSelection(e);
                        if (GneuBuild.selectedNeurons.size()==1) {
                                Gneu.addLink(remind, ((Integer)GneuBuild.selectedNeurons.get(0)).intValue(), 0.01);// create link
                                GneuBuild.changesUnsaved=true;
                        }
                }
                remind=-1;
                Gneu.setMode("none");
                repaint();
        }
        else {
              GneuBuild.checkSelection(e);
              repaint();
        }
 }
 public void mouseEntered (MouseEvent e) { }
 public void mouseExited (MouseEvent e) { }
 public void mouseDragged (MouseEvent e) {
        if (GneuBuild.selectedNeurons.size()==1) {
                ((Neuron) Gneu.neuronArr.get(((Integer)GneuBuild.selectedNeurons.get(0)).intValue())).setX(e.getX());
                ((Neuron) Gneu.neuronArr.get(((Integer)GneuBuild.selectedNeurons.get(0)).intValue())).setY(e.getY());
                GneuBuild.changesUnsaved=true;
                repaint();
        }
 }
 public void mouseMoved (MouseEvent e) { }
 public void keyPressed (KeyEvent e) { }
 public void keyReleased (KeyEvent e) { }
 public void keyTyped (KeyEvent e) { }
}