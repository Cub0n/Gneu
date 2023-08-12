import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GneuTest {

	@Test
	public void simpleNet() {

		Gneu.neuronArr = new ArrayList<>();
		Gneu.addNeuron(1, 2, 1, "Neuron1");
		Gneu.addNeuron(2, 3, 5, "Neuron2");
		Gneu.addNeuron(4, 4, 2, "Neuron3");
		Gneu.addLink(0, 1, 0.5);
		Gneu.addLink(0, 2, 0.5);
		Gneu.simulate();

		List<Neuron> neurons = Gneu.neuronArr;

		assertEquals("Neuron1", neurons.get(0).getName());
		assertEquals("Neuron2", neurons.get(1).getName());
		assertEquals("Neuron3", neurons.get(2).getName());

		assertEquals(2, neurons.get(0).getLinks().length);
		assertEquals(0, neurons.get(1).getLinks().length);
		assertEquals(0, neurons.get(2).getLinks().length);

		assertEquals(0.023327665816473, neurons.get(0).getActNew());
		assertEquals(3.4141625429278495E-4, neurons.get(1).getActNew());
		assertEquals(1.3656650171711395E-4, neurons.get(2).getActNew());

		assertEquals(0.024297061424748783, neurons.get(0).getActOld());
		assertEquals(3.593855308345105E-4, neurons.get(1).getActOld());
		assertEquals(1.4375421233380416E-4, neurons.get(2).getActOld());

		assertEquals(1.0, neurons.get(0).getActStart());
		assertEquals(5.0, neurons.get(1).getActStart());
		assertEquals(2.0, neurons.get(2).getActStart());

	}
}
