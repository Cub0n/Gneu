
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * This is Gneu, free Software to built and compute parallel constraint
 * satisfaction networks.
 *
 * Written 2003 by Benjamin Kraemer & Andreas Schlicker
 * </p>
 */
public final class Gneu {

	private static String inputNet = "input.txt";
	private static String outputNet = "output.txt";
	private static String inputParams = "input.txt";
	private static String outputParams = "params.txt";
	private static GneuBuild gb;

	private static double c = 0.001;

	private static final Logger LOG = Logger.getLogger(Gneu.class.getName());

	static void setC(double cNew) {
		c = cNew;
	}

	static double getC() {
		return c;
	}

	// simulation ends when |a(t)-a(t-1)| <= c, i.e. the activation of a neuron did
	// not change more than c between two steps of the simulation
	private static double delta = 0.05;

	static void setDelta(double cDelta) {
		delta = cDelta;
	}

	static double getDelta() {
		return delta;
	}

	// in each step, the activation of each neuron is decreased: a(t+1) =
	// a(t)*(1-delta);
	/*
	 * test: create 3 neurons static Neuron neuron0 = new Neuron(12, 13, 0.2,
	 * "Nummer 0"); static Neuron neuron1 = new Neuron(12, 13, 0, "Nummer 1");
	 * static Neuron neuron2 = new Neuron(12, 13, 1, "Nummer 2");end of test
	 */
	static List<Neuron> neuronArr;

	static void simulate() {
		LOG.info("Simulating... Delta: " + delta);
		int i;

		// used to store the difference between old [a(t)] and new activation [a(t+1)]
		boolean breakControl = false;
		// true if the activation of every neutron did not change more than c, i.e. the
		// network is stable
		double net = 0.0;
		while (!breakControl) {
			breakControl = true;
			for (i = 0; i < neuronArr.size(); ++i) {
				neuronArr.get(i).setActOld(neuronArr.get(i).getActNew());
			}
			for (i = 0; i < neuronArr.size(); ++i) {
				// LOG.info("neuron: "+i+", name: "+neuronArr[i].name);
				Object[] links = neuronArr.get(i).getLinks();
				for (Object lLink : links) {
					/**
					 * Get the next entry from the list. Use next.getKey() to get the index and
					 * next.getValue() to get the weight.
					 */
					Entry next = (Entry) lLink;
					// activation of all neurons connected to the one actually used * weight of the
					// connections
					/**
					 * With ((Integer) next.getKey()).intValue()) you can get the index of the next
					 * link target.
					 */
					net += neuronArr.get(((Integer) next.getKey())).getActOld()
							* ((Double) next.getValue()).doubleValue();
					// LOG.info("net: "+net);
				}
				if (net > 0) {
					neuronArr.get(i).setActNew((neuronArr.get(i).getActOld() * (1.0 - delta))
							+ (net * (1.0 - neuronArr.get(i).getActOld())));
				} else {
					neuronArr.get(i).setActNew((neuronArr.get(i).getActOld() * (1.0 - delta))
							+ (net * (1.0 + neuronArr.get(i).getActOld())));
				}
				double diff = Math.abs(neuronArr.get(i).getActNew() - neuronArr.get(i).getActOld());
				// LOG.info(((Neuron) neuronArr.get(i)).getName()+" diff: "+diff);
				// LOG.info(((Neuron) neuronArr.get(i)).getName()+"activation:
				// "+((Neuron) neuronArr.get(i)).getActNew());
				if (diff > c) {
					breakControl = false;
				}
				net = 0.0;
			}
		}
	}

	/**
	 * Parse the neural net definition in the inputNet file.
	 *
	 * @return null if no error occurred while parsing the input file, a String
	 *         containing the errors else.
	 */
	static String parseNet() {

		StringBuilder result = new StringBuilder();
		neuronArr = new ArrayList<>();
		try (BufferedReader fileReader = new BufferedReader(new FileReader(inputNet))) {

			/* Save the current line, the words and the number of the line. */
			String line;
			StringTokenizer lineContent;
			int lineCount = 1;
			line = fileReader.readLine();
			while (line != null) {
				/* Ignore lines containing comments. */
				if (line.startsWith("#")) {
					line = fileReader.readLine();
					++lineCount;
					continue;
				}
				/* Split the line into tokens with the default delimiter set " \t\n\r\f". */
				lineContent = new StringTokenizer(line);
				String keyword = lineContent.nextToken();
				if (!lineContent.hasMoreTokens()) {
					line = fileReader.readLine();
					++lineCount;
					continue;
				}
				/* Switch by keyword */
				if (keyword.equalsIgnoreCase("NEURON")) {
					try {
						/*
						 * Construct a new Neuron and add it to the ArrayList. The line has to be
						 * constructed like: NEURON <activation> or NEURON <activation> [active |
						 * passive] or NEURON <name> <activation> <xpos> <ypos> or NEURON <name>
						 * <activation> <xpos> <ypos> [active | passive]
						 */
						if (lineContent.countTokens() == 1) {
							neuronArr.add(new Neuron(Double.parseDouble(lineContent.nextToken())));
						} else if (lineContent.countTokens() == 2) {
							neuronArr.add(
									new Neuron(Double.parseDouble(lineContent.nextToken()), lineContent.nextToken()));
						} else if (lineContent.countTokens() == 4) {
							neuronArr.add(
									new Neuron(lineContent.nextToken(), Double.parseDouble(lineContent.nextToken()),
											Integer.parseInt(lineContent.nextToken()),
											Integer.parseInt(lineContent.nextToken())));
						} else if (lineContent.countTokens() == 5) {
							neuronArr.add(
									new Neuron(lineContent.nextToken(), Double.parseDouble(lineContent.nextToken()),
											Integer.parseInt(lineContent.nextToken()),
											Integer.parseInt(lineContent.nextToken()), lineContent.nextToken()));
						}

					}
					/* If there is an error report it. */
					catch (NumberFormatException e) {
						result.append(" Error in line: " + lineCount + ":");
						result.append(" Could not parse: \"" + line + "\".");
					}
				} else if (keyword.equalsIgnoreCase("CONNECT")) {
					try {
						/*
						 * Construct a new edge between two neurons. The line has to be constructed
						 * like: CONNECT <source> <target> <weight> [<target> <weight>]
						 */
						int source = Integer.parseInt(lineContent.nextToken());
						while (lineContent.hasMoreTokens()) {
							addLink(source, Integer.parseInt(lineContent.nextToken()),
									Double.parseDouble(lineContent.nextToken()));
						}
					} catch (NumberFormatException e) {
						result.append(" Error in line: " + lineCount + ":");
						result.append(" Could not parse: \"" + line + "\".");
					}
				}
				/* Get the next line. */
				line = fileReader.readLine();
				++lineCount;
			}

		} catch (IOException e) {
			return "Could not open file.";
		}

		if (result.length() == 0) {
			return null;
		}

		return result.toString();
	}

	/**
	 * Parse the parameters from the inputParams file.
	 *
	 * @return null if no error occurred while parsing the input file, a String
	 *         containing the errors else.
	 */
	static String parseParams() {

		StringBuilder result = new StringBuilder();
		try (BufferedReader fileReader = new BufferedReader(new FileReader(inputParams))) {

			/* Save the current line, the words and the number of the line. */
			String line;
			StringTokenizer lineContent;
			int lineCount = 1;
			line = fileReader.readLine();
			while (line != null) {
				/* Ignore lines containing comments. */
				if (line.startsWith("#")) {
					/* Get the next line. */
					line = fileReader.readLine();
					++lineCount;
					continue;
				}
				/* Split the line into tokens with the default delimiter set " \t\n\r\f". */
				lineContent = new StringTokenizer(line);
				String keyword = lineContent.nextToken();
				if (!lineContent.hasMoreTokens()) {
					/** Get the next line. */
					line = fileReader.readLine();
					++lineCount;
					continue;
				}
				/* Switch by keyword */
				if (keyword.equalsIgnoreCase("C")) {
					try {
						c = Integer.parseInt(lineContent.nextToken());
					}
					/* If there is an error report it. */
					catch (NumberFormatException e) {
						result.append(" Error in line: " + lineCount + ":");
						result.append(" Could not parse: \"" + line + "\".");
					}
				} else if (keyword.equalsIgnoreCase("DELTA")) {
					try {
						delta = Double.parseDouble(lineContent.nextToken());
					} catch (NumberFormatException e) {
						result.append(" Error in line: " + lineCount + ":");
						result.append(" Could not parse: \"" + line + "\".");
					}
				}
				/* Get the next line. */
				line = fileReader.readLine();
				++lineCount;
			}

		} catch (IOException e) {
			return "Could not open file.";
		}

		return result.toString();
	}

	/**
	 * Write the neurons and the links to a file.
	 *
	 * @return true if the operation was successful, false else.
	 */
	static boolean outputNet() {

		try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputNet))) {
			for (Neuron neuron : neuronArr) {
				if (neuron.getName().equals("")) {
					fileWriter.write(
							"NEURON " + neuron.getActStart() + " " + (neuron.isActive() ? "active\n" : "passive\n"));
				} else {
					fileWriter.write("NEURON " + neuron.getName() + " " + neuron.getActStart() + " " + neuron.getX()
							+ " " + neuron.getY() + " " + (neuron.isActive() ? "active\n" : "passive\n"));
				}
			}
			for (int source = 0; source < neuronArr.size(); ++source) {
				Neuron neuron = neuronArr.get(source);
				Object[] links = neuron.getLinks();
				StringBuilder connects = new StringBuilder();
				for (Object lLink : links) {
					Entry entry = (Entry) lLink;
					connects.append(" " + (entry.getKey()) + " " + (entry.getValue()));
				}
				if (connects.length() != 0) {
					fileWriter.write("CONNECT " + source + connects.toString() + "\n");
				}
			}
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Write the parameters to a file.
	 *
	 * @return true if the operation was successful, false else.
	 */
	static boolean outputParams() {

		try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputParams))) {
			fileWriter.write("C " + c + "\n");
			fileWriter.write("DELTA " + delta + "\n");
			fileWriter.flush();
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	private static boolean fileOutput() {
		boolean ret = true;
		try (DataOutputStream output = new DataOutputStream(new FileOutputStream(new File(outputNet)))) {
			for (int i = 0; i < neuronArr.size(); i++) {
				output.writeBytes(i + StringUtils.SPACE + neuronArr.get(i).getName() + StringUtils.SPACE
						+ neuronArr.get(i).getActNew() + "\n");
			}
			output.flush();
		} catch (IOException ioe) {
			ret = false;
		}
		return ret;
	}

	/**
	 * Construct a link between source and target with the given weight.
	 *
	 * @param source The number of the source neuron.
	 * @param target The number of the target neuron.
	 * @param weight The weight of the link.
	 */
	static void addLink(int source, int target, double weight) {
		neuronArr.get(source).addLink(target, weight);
	}

	static void setInputNet(String string) {
		inputNet = string;
	}

	static void setInputParams(String string) {
		inputParams = string;
	}

	static void setOutputNet(String string) {
		outputNet = string;
	}

	static void setOutputParams(String string) {
		outputParams = string;
	}

	static String getMode() {
		if (gb != null) {
			return gb.getMode();
		}
		return "none";
	}

	static void setMode(String s) {
		if (gb != null) {
			gb.setMode(s);
		}
	}

	static void addNeuron(int x, int y, double act, String name) {
		neuronArr.add(new Neuron(name, act, x, y));
	}

	static void deleteNeuron(int number) {
		Neuron neuron = (neuronArr.get(number));
		Object[] links = neuron.getLinks();
		for (Object lLink : links) {
			Entry next = (Entry) lLink;
			neuronArr.get(((Integer) next.getKey())).deleteLink(Integer.valueOf(number));
		}
		neuron.deleteAllLinksUnidirec();
	}

	public static void main(String[] args) {
		neuronArr = new ArrayList<>();

		String help = ("Usage: -i <name of input file> -o <name of output file>\nIf Gneu is started without arguments, default values are used:\nInput file: "
				+ inputNet + ", output file: " + outputNet);
		// use command line arguments for filenames
		boolean graph = false;
		int k = 0;
		while (k < args.length) {
			if (args[k].equals("-i")) {
				++k;
				inputNet = args[k];
				++k;
			} else if (args[k].equals("-o")) {
				++k;
				outputNet = args[k];
				++k;
			} else if (args[k].equals("-h")) {
				LOG.info(help);
				System.exit(1);
			} else if (args[k].equals("-g")) {
				graph = true;
				k = 10;
			}
		}
		if (graph) {
			gb = new GneuBuild();
			// gb.setBounds(100, 100, 650, 550);
			gb.pack();
			gb.setVisible(true);
		} else {
			String s = parseNet();
			if (s != null) {
				LOG.info(s);
			}
			/*
			 * for (int i = 0; i < neuronArr.size(); ++i) { LOG.info("neuron: " + i +
			 * ", name: " + ((Neuron) neuronArr.get(i)).getName() + ", activation: " +
			 * ((Neuron) neuronArr.get(i)).getActNew()); Object[] links = ((Neuron)
			 * neuronArr.get(i)).getLinks(); LOG.info("links: "); for (int
			 * j=0;j<links.length;++j) { Entry next = (Entry) links[j]; LOG.info(((Integer)
			 * next.getKey()).intValue()+" - "+ ((Double) next.getValue()).doubleValue()); }
			 * }
			 */
			simulate();
			LOG.info("\nactivations in state of maximum contraint satisfaction:");
			for (int i = 0; i < neuronArr.size(); ++i) {
				LOG.info("neuron: " + i + ", name: " + neuronArr.get(i).getName() + ", activation: "
						+ neuronArr.get(i).getActNew());
				if (!fileOutput()) {
					LOG.info("Output file not found or error while writing file.");
				}
			}
		}
	}
}