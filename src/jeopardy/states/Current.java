package jeopardy.states;

public abstract class Current {
	public final int rid;
	public final MainControl control;

	public Current(int rid, MainControl control) {
		this.rid = rid;
		this.control = control;
	}

}
