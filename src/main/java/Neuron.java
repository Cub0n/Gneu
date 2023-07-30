import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a neuron in the neural network.
 */
public class Neuron {

	// x, y: coordinates of the neuron for graphical output, activ: standard activation at the beginning of simulation, name: Name of the Neuron
	private int x, y;
	// activation before and after one step of the simulation
	private double actNew, actOld, actStart;
	/** Saves the edges to other neurons and the edge weights. */
	private Map links;
	private String name;
	private boolean active;

	/**
	 * Construct a new neuron with the given information.
	 * @param id The neuron's id that is saved in the link list of other neurons
	 * @param name The neuron's name; also used for graphical output
	 * @param activity The initial activity of the neuron
	 * @param coordX The x coordinate used for graphical output
	 * @param coordY The y coordinate used for graphical output
	 * @param active "active" if the neuron should not change its activation, "passive" else
	 */
	public Neuron(String name, double activity, int coordX, int coordY, String active) {
		x = coordX;
		y = coordY;
		actNew = activity;
		actOld = activity;
		actStart = activity;
		this.name = name;
		links = new HashMap();
		if (active.equals("passive"))
			this.active = false;
		else
			this.active = true;
	}

	/**
	 * Construct a new neuron with the given information.
	 * @param id The neuron's id that is saved in the link list of other neurons
	 * @param name The neuron's name; also used for graphical output
	 * @param activity The initial activity of the neuron
	 * @param coordX The x coordinate used for graphical output
	 * @param coordY The y coordinate used for graphical output
	 */
	public Neuron(String name, double activity, int coordX, int coordY) {
		x = coordX;
		y = coordY;
		actNew = activity;
		actOld = activity;
		actStart = activity;
		this.name = name;
		links = new HashMap();
		active = true;
	}

	/**
	 * Constructs a neuron with default values.
	 * @param activity The initial activity of the neuron
	 * @param active "active" if the neuron should not change its activation, "passive" else
	 */
	public Neuron(double activity, String active) {
		x = -1;
		y = -1;
		actNew = activity;
		actOld = activity;
		actStart = activity;
		name = "";
		links = new HashMap();
		if (active.equals("passive"))
			this.active = false;
		else
			this.active = true;
	}

	/**
	 * Constructs a neuron with default values.
	 * @param activity The initial activity of the neuron
	 */
	public Neuron(double activity) {
		x = -1;
		y = -1;
		actNew = activity;
		actOld = activity;
		actStart = activity;
		name = "";
		links = new HashMap();
		active = true;
	}

	/**
	 * Adds an edge to another neuron.
	 * @param target The target neuron
	 * @param weight The edge weight
	 */
	public boolean addLink(int target, double weight) {
		/** If there was already a link with the specified neuron, no link was inserted. */
		if (links.containsKey(new Integer(target)))
			return false;
		else {	
			links.put(new Integer(target), new Double(weight));
			return true;
		}
	}

	/**
	 * This method is only save if there are no threads involved. 
	 * Otherwise the entrySet has to be copied.
	 */
	public Object[] getLinks() {
		return links.entrySet().toArray();
	}

	public void setActNew(double actNew) {
		if (active) {
			//actOld = this.actNew;
			this.actNew = actNew;
		}
	}

        public void setActOld(double actOld) {
                this.actOld=actOld;
        }

	public double getActNew() {
		return actNew;
	}

	public double getActOld() {
		return actOld;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

        public void setX(int newX) {
		this.x=newX;
	}

	public void setY(int newY) {
		this.y=newY;
	}

	public String getName() {
		return name;
	}

        public void setName (String newName) {
		this.name=newName;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean b) {
		active = b;
	}

	public double getActStart() {
		return actStart;
	}

	public void setActStart(double actStart) {
		this.actStart = actStart;		
	}

        public void setWeight(int number, double newWeight) {
                Object[] links = this.getLinks();
                Entry next = (Entry) links[number];
                next.setValue(new Double(newWeight));
        }

        public double getWeight(int number) {
                Object[] links = this.getLinks();
                Entry next = (Entry) links[number];
                return ((Double)next.getValue()).doubleValue();
        }

        public void deleteLink(int number) {
                Object[] links = this.getLinks();
                Entry next = (Entry) links[number];
                Integer key=new Integer(((Integer)next.getKey()).intValue());
                this.links.remove(key);
        }

        public void deleteLink(Integer number) {
                this.links.remove(number);
        }

        public void deleteAllLinksUnidirec() {
                this.links=new HashMap();
        }
}