/* This is gneu, free Software to built and compute parallel contraint satisfaction networks,
written 2003 by Benjamin Krämer & Andreas Schlicker
You may use, distribute an modify it under the conditions of the Gnu Public License: www.gnu.org/copyleft/gpl.html
 */
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

class Gneu {

	private static String inputNet=new String("input.txt");
	private static String outputNet=new String("output.txt");
	private static String inputParams="input.txt";
	private static String outputParams="params.txt";
	private static String outputResults="output.txt";

        private static GneuBuild gb;

		private static double c = 0.001;
        public static void setC(double cNew) {
                c=cNew;
        }

        public static double getC() {
                return c;
        }

	// simulation ends when |a(t)-a(t-1)| <= c, i.e. the activation of a neuron did not change more than c between two steps of the simulation
	private static double delta = 0.05;
        public static void setDelta(double cDelta) {
                delta=cDelta;
        }

        public static double getDelta() {
                return delta;
        }
	// in each step, the activation of each neuron is decreased: a(t+1) = a(t)*(1-delta);
	/* test: create 3 neurons
	static Neuron neuron0 = new Neuron(12, 13, 0.2, "Nummer 0");
	static Neuron neuron1 = new Neuron(12, 13, 0, "Nummer 1");
	static Neuron neuron2 = new Neuron(12, 13, 1, "Nummer 2");end of test */
	public static ArrayList neuronArr;

        public static void simulate() {
		System.out.println("Simulating... Delta: " + delta);
		int i;
		int repeat = 0;
		double diff = 2.0;
		// used to store the difference between old [a(t)] and new acitvation [a(t+1)]
		boolean breakControl = false;
		// true if the acitvation of every neutron did not change more than c, i.e. the network is stable
		double net = 0.0;
		while (breakControl == false) {
			breakControl = true;
			for (i = 0; i < neuronArr.size(); ++i) {
                                ((Neuron) neuronArr.get(i)).setActOld(((Neuron) neuronArr.get(i)).getActNew());
                        }
                        for (i = 0; i < neuronArr.size(); ++i) {
				//System.out.println("neuron: "+i+", name: "+neuronArr[i].name);
				Object[] links = ((Neuron) neuronArr.get(i)).getLinks();
				for (int j = 0; j < links.length; ++j) {
					/** Get the next entry from the list. Use next.getKey() to get the index and next.getValue() to get the weight. */
					Entry next = (Entry) links[j];
					// activation of all neurons connected to the one actually used * weight of the connections
					/** With ((Integer) next.getKey()).intValue()) you can get the index of the next link target. */
					net += ((Neuron) neuronArr.get(((Integer) next.getKey()).intValue())).getActOld() * ((Double) next.getValue()).doubleValue();
					//System.out.println("net: "+net);
				}
				if (net > 0) {
					((Neuron) neuronArr.get(i)).setActNew(((Neuron) neuronArr.get(i)).getActOld() * (1.0 - delta) + net * (1.0 - ((Neuron) neuronArr.get(i)).getActOld()));
				}
				else {
					((Neuron) neuronArr.get(i)).setActNew(((Neuron) neuronArr.get(i)).getActOld() * (1.0 - delta) + net * (1.0 + ((Neuron) neuronArr.get(i)).getActOld()));
				}
				diff = Math.abs(((Neuron) neuronArr.get(i)).getActNew() - ((Neuron) neuronArr.get(i)).getActOld());
				//System.out.println(((Neuron) neuronArr.get(i)).getName()+" diff: "+diff);
				//System.out.println(((Neuron) neuronArr.get(i)).getName()+"activation: "+((Neuron) neuronArr.get(i)).getActNew());
				if (diff > c)
					breakControl = false;
				net = 0.0;
			}
			++repeat;
			//System.out.println("repeat: "+repeat+" diff: "+diff);
		}
        }

	/**
	 * Parse the neural net definition in the inputNet file.
	 * @return null if no error occurred while parsing the input file, a String containing the errors else.
	 */
	public static String parseNet() {
                BufferedReader fileReader = null;
		StringBuffer result = new StringBuffer();
                neuronArr=new ArrayList();
		try {
			/** Open a FileReader and construct a BufferedReader to get efficient access to the file. */
			fileReader = new BufferedReader(new FileReader(inputNet));
			/** Save the current line, the words and the number of the line. */
			String line;
			StringTokenizer lineContent;
			int lineCount = 1;
			line = fileReader.readLine();
			while (line != null) {
				/** Ignore lines containing comments. */
				if (line.startsWith("#")) {
					line = fileReader.readLine();
					++lineCount;
					continue;
				}
				/** Split the line into tokens with the default delimiter set " \t\n\r\f". */
				lineContent = new StringTokenizer(line);
				String keyword = lineContent.nextToken();
				if (!lineContent.hasMoreTokens()) {
					line = fileReader.readLine();
					++lineCount;
					continue;
				}
				/** Switch by keyword */
				if (keyword.equalsIgnoreCase("NEURON")) {
					try {
						/** Construct a new Neuron and add it to the ArrayList. 
						 * The line has to be constructed like:
						 * NEURON <activation> or
						 * NEURON <activation> [active | passive] or
						 * NEURON <name> <activation> <xpos> <ypos> or
						 * NEURON <name> <activation> <xpos> <ypos> [active | passive] 
						 */
						if (lineContent.countTokens() == 1)
							neuronArr.add(new Neuron(Double.parseDouble(lineContent.nextToken())));
						else if (lineContent.countTokens() == 2)
							neuronArr.add(new Neuron(Double.parseDouble(lineContent.nextToken()), lineContent.nextToken()));
						else if (lineContent.countTokens() == 4)
							neuronArr.add(new Neuron(lineContent.nextToken(), Double.parseDouble(lineContent.nextToken()), Integer.parseInt(lineContent.nextToken()), Integer.parseInt(lineContent.nextToken())));
						else if (lineContent.countTokens() == 5)
							neuronArr.add(new Neuron(lineContent.nextToken(), Double.parseDouble(lineContent.nextToken()), Integer.parseInt(lineContent.nextToken()), Integer.parseInt(lineContent.nextToken()), lineContent.nextToken()));

					}
					/** If there is an error report it. */
					catch (Exception e) {
						result.append(" Error in line: " + lineCount+":");
						result.append(" Could not parse: \"" + line+"\".");
					}
				}
				else if (keyword.equalsIgnoreCase("CONNECT")) {
					try {
						/** Construct a new edge between two neurons.
						 * The line has to be constructed like:
						 * CONNECT <source> <target> <weight> [<target> <weight>]
						 */
						int source = Integer.parseInt(lineContent.nextToken());
						while (lineContent.hasMoreTokens())
							addLink(source, Integer.parseInt(lineContent.nextToken()), Double.parseDouble(lineContent.nextToken()));
					}
					catch (Exception e) {
						result.append(" Error in line: " + lineCount+":");
						result.append(" Could not parse: \"" + line+"\".");
					}
				}
				/** Get the next line. */
				line = fileReader.readLine();
				++lineCount;
                        }

		}
		catch (IOException e) {
			return "Could not open file.";
		}
		finally {
			try {
				fileReader.close();
			}
			catch (Exception ex) {}
		}
		if (result.length() == 0)
			return null;
		return result.toString();
	}

	/**
	 * Parse the parameters from the inputParams file.
	 * @return null if no error occurred while parsing the input file, a String containing the errors else.
	 */
	public static String parseParams() {
		BufferedReader fileReader = null;
		StringBuffer result = new StringBuffer();
		try {
			/** Open a FileReader and construct a BufferedReader to get efficient access to the file. */
			fileReader = new BufferedReader(new FileReader(inputParams));
			/** Save the current line, the words and the number of the line. */
			String line;
			StringTokenizer lineContent;
			int lineCount = 1;
			line = fileReader.readLine();
			while (line != null) {
				/** Ignore lines containing comments. */
				if (line.startsWith("#")) {
					/** Get the next line. */
					line = fileReader.readLine();
					++lineCount;
					continue;
				}
				/** Split the line into tokens with the default delimiter set " \t\n\r\f". */
				lineContent = new StringTokenizer(line);
				String keyword = lineContent.nextToken();
				if (!lineContent.hasMoreTokens()) {
					/** Get the next line. */
					line = fileReader.readLine();
					++lineCount;
					continue;
				}
				/** Switch by keyword */
				if (keyword.equalsIgnoreCase("C")) {
					try {
						c = Integer.parseInt(lineContent.nextToken());
					}
					/** If there is an error report it. */
					catch (Exception e) {
						result.append(" Error in line: " + lineCount+":");
						result.append(" Could not parse: \"" + line+"\".");
					}
				}
				else if (keyword.equalsIgnoreCase("DELTA")) {
					try {
						delta = Double.parseDouble(lineContent.nextToken());
					}
					catch (Exception e) {
						result.append(" Error in line: " + lineCount+":");
						result.append(" Could not parse: \"" + line+"\".");
					}
				}
				/** Get the next line. */
				line = fileReader.readLine();
				++lineCount;
			}

		}
		catch (Exception e) {
			return "Could not open file.";
		}
		finally {
			try {
				fileReader.close();
			}
			catch (Exception ex) {}
		}
		if (result.length() == 0)
			return null;
		return result.toString();
	}

	/**
	 * Write the neurons and the links to a file.
	 * @return true if the operation was successful, false else.
	 */
	public static boolean outputNet() {
		BufferedWriter fileWriter = null;
		try {
			fileWriter = new BufferedWriter(new FileWriter(outputNet));
			Iterator iter = neuronArr.iterator();
			while (iter.hasNext()) {
				Neuron neuron = (Neuron) iter.next();
				if (neuron.getName().equals(""))
					fileWriter.write("NEURON " + neuron.getActStart() + " " + (neuron.isActive() ? "active\n" : "passive\n"));
				else
					fileWriter.write("NEURON " + neuron.getName() + " " + neuron.getActStart() + " " + neuron.getX() + " " + neuron.getY() + " " + (neuron.isActive() ? "active\n" : "passive\n"));
			}
			for (int source = 0; source < neuronArr.size(); ++source) {
				Neuron neuron = (Neuron) neuronArr.get(source);
				Object[] links = neuron.getLinks();
				StringBuffer connects = new StringBuffer();
				for (int i = 0; i < links.length; ++i) {
					Entry entry = (Entry) links[i];
					connects.append(" " + ((Integer) entry.getKey()) + " " + ((Double) entry.getValue()));
				}
				if (connects.length() != 0)
					fileWriter.write("CONNECT " + source + connects.toString() + "\n");
			}
		}
		catch (Exception e) {
			return false;
		}
		finally {
			try {
				fileWriter.close();
			}
			catch (Exception ex) {}
		}
		return true;
	}

	/**
	 * Write the parameters to a file.
	 * @return true if the operation was successful, false else.
	 */
	public static boolean outputParams() {
		BufferedWriter fileWriter = null;
		try {
			fileWriter = new BufferedWriter(new FileWriter(outputParams));
			fileWriter.write("C " + c + "\n");
			fileWriter.write("DELTA " + delta + "\n");
		}
		catch (Exception e) {
			return false;
		}
		finally {
			try {
				fileWriter.close();
			}
			catch (Exception ex) {}
		}
		return true;
	}

	public static boolean fileOutput() {
		boolean ret = true;
		try {
			File outputFile = new File(outputNet);
			DataOutputStream output = new DataOutputStream(new FileOutputStream(outputFile));
			for (int i = 0; i < neuronArr.size(); i++) {
				output.writeBytes(i + " " + ((Neuron) neuronArr.get(i)).getName() + " " + ((Neuron) neuronArr.get(i)).getActNew() + "\n");
			}
			output.close();
		}
		catch (IOException ioe) {
			ret = false;
		}
		return ret;
	}

	/**
	 * Construct a link between source and target with the given weight.
	 * @param source The number of the source neuron.
	 * @param target The number of the target neuron.
	 * @param weight The weight of the link.
	 */
	public static void addLink(int source, int target, double weight) {
		((Neuron) neuronArr.get(source)).addLink(target, weight);
	}

	public static String getInputNet() {
		return inputNet;
	}

	public static String getInputParams() {
		return inputParams;
	}

	public static String getOutputNet() {
		return outputNet;
	}

	public static String getOutputParams() {
		return outputParams;
	}

	public static void setInputNet(String string) {
		inputNet = string;
	}

	public static void setInputParams(String string) {
		inputParams = string;
	}

	public static void setOutputNet(String string) {
		outputNet = string;
	}

	public static void setOutputParams(String string) {
		outputParams = string;
	}

	public static String getOutputResults() {
		return outputResults;
	}

	public static void setOutputResults(String string) {
		outputResults = string;
	}

        public static String getMode() {
                if (gb!=null) {
                       return gb.getMode();
                }
                else {
                        return "none";
                }
        }

        public static void setMode(String s) {
                if (gb!=null) {
                       gb.setMode(s);
                }
        }

        public static void addNeuron (int x, int y, double act, String name) {
                neuronArr.add(new Neuron(name, act, x, y));
        }

        public static void deleteAllLinks(int number) {
                Neuron neuron=((Neuron) neuronArr.get(number));
                Object[] links = neuron.getLinks();
                for (int i=0; i<links.length; i++) {
                        Entry next = (Entry) links[i];
                        ((Neuron) neuronArr.get(((Integer) next.getKey()).intValue())).deleteLink(new Integer(number));
                }
                neuron.deleteAllLinksUnidirec();
        }

        public static void deleteNeuron(int number) {
                deleteAllLinks(number);
                // remove the neuron
                // change all links to neurons with numbers > number
        }

	public static void main(String args[]) {
		neuronArr = new ArrayList();

		String help = new String("Usage: -i <name of input file> -o <name of output file>\nIf Gneu is started without arguments, default values are used:\nInput file: " + inputNet + ", output file: " + outputNet);
		// use command line arguments for filenames
		boolean graph = false;
		int k = 0;
		while (k < args.length) {
			if (args[k].equals("-i")) {
				++k;
				inputNet = args[k];
                                ++k;
			}
			else if (args[k].equals("-o")) {
				++k;
				outputNet = args[k];
                                ++k;
			}
			else if (args[k].equals("-h")) {
				System.out.println(help);
				System.exit(1);
			}
			else if (args[k].equals("-g")) {
				graph = true;
				k=10;
			}
		}
		if (graph == true) {
                        gb = new GneuBuild();
                        //gb.setBounds(100, 100, 650, 550);
                        gb.pack();
			gb.show();
		}
		else {
                        String s=new String();
                        s=parseNet();
                        if (s!=null) System.out.println(s);
                        /*for (int i = 0; i < neuronArr.size(); ++i) {
				System.out.println("neuron: " + i + ", name: " + ((Neuron) neuronArr.get(i)).getName() + ", activation: " + ((Neuron) neuronArr.get(i)).getActNew());
                                Object[] links = ((Neuron) neuronArr.get(i)).getLinks();
                                System.out.println("links: ");
                                for (int j=0;j<links.length;++j) {
                                        Entry next = (Entry) links[j];
                                        System.out.println(((Integer) next.getKey()).intValue()+" - "+ ((Double) next.getValue()).doubleValue());
                                }
                        }*/
                        simulate();
			System.out.println("\nactivations in state of maximum contraint satisfaction:");
			for (int i = 0; i < neuronArr.size(); ++i) {
				System.out.println("neuron: " + i + ", name: " + ((Neuron) neuronArr.get(i)).getName() + ", activation: " + ((Neuron) neuronArr.get(i)).getActNew());
				if (!fileOutput()) {
					System.out.println("Output file not found or error while writing file.");
				}
			}
		}
	}
}