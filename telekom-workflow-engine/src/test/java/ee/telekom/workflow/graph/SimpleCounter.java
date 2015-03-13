package ee.telekom.workflow.graph;

import java.util.Map;

public class SimpleCounter {

	private int count = 0;

	public void inc() {
		++count;
	}

	public int incAndGet() {
		return ++count;
	}

	public void incByDelta(int delta) {
		count += delta;
	}

	public int incByDeltaAndGet(int delta) {
		count += delta;
		return count;
	}

	public int get() {
		return count;
	}

	public void incByDeltas(int[] deltas) {
		for (int delta : deltas) {
			count += delta;
		}
	}

	public void add(Map<String, Integer> deltas) {
		for (Map.Entry<String, Integer> entry : deltas.entrySet()) {
			count += entry.getValue();
		}
	}
}