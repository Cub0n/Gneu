import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NeuronTest {

	@Test
	public void initNeuron() {
		Neuron neuron = new Neuron("Neuron", 2.0, 2, 3, "passive");
		assertEquals("Neuron", neuron.getName());
		assertEquals(2.0, neuron.getActNew());
		assertEquals(2.0, neuron.getActOld());
		assertEquals(2.0, neuron.getActStart());
		assertEquals(2, neuron.getX());
		assertEquals(3, neuron.getY());
		assertEquals(0, neuron.getLinks().length);
	}

	@Test
	public void initNeuron2() {
		Neuron neuron = new Neuron("Neuron", 2.0, 2, 3);
		assertEquals("Neuron", neuron.getName());
		assertEquals(2.0, neuron.getActNew());
		assertEquals(2.0, neuron.getActOld());
		assertEquals(2.0, neuron.getActStart());
		assertEquals(2, neuron.getX());
		assertEquals(3, neuron.getY());
		assertEquals(0, neuron.getLinks().length);
	}

	@Test
	public void initNeuron3() {
		Neuron neuron = new Neuron(2.0, "passive");
		assertEquals("", neuron.getName());
		assertEquals(2.0, neuron.getActNew());
		assertEquals(2.0, neuron.getActOld());
		assertEquals(2.0, neuron.getActStart());
		assertEquals(-1, neuron.getX());
		assertEquals(-1, neuron.getY());
		assertEquals(0, neuron.getLinks().length);
	}

	@Test
	public void initNeuron4() {
		Neuron neuron = new Neuron(2.0);
		assertEquals("", neuron.getName());
		assertEquals(2.0, neuron.getActNew());
		assertEquals(2.0, neuron.getActOld());
		assertEquals(2.0, neuron.getActStart());
		assertEquals(-1, neuron.getX());
		assertEquals(-1, neuron.getY());
		assertEquals(0, neuron.getLinks().length);
	}

	@Test
	public void links() {
		Neuron neuron = new Neuron(2.0);
		neuron.addLink(1, 3.0);
		neuron.addLink(2, 4.0);
		neuron.addLink(0, 5.0);
		neuron.addLink(5, 10.0);

		Object[] links = neuron.getLinks();
		assertEquals(4, links.length);

		assertEquals(4.0, neuron.getWeight(2));
		neuron.setWeight(2, 8.0);
		assertEquals(8.0, neuron.getWeight(2));

		neuron.deleteLink(3);
		assertEquals(3, neuron.getLinks().length);

		neuron.deleteLink(Integer.valueOf(2));
		assertEquals(2, neuron.getLinks().length);

		neuron.deleteAllLinksUnidirec();
		assertEquals(0, neuron.getLinks().length);
	}
}
