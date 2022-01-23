package jeopardy.states;

public class CurrentRound extends Current {
	public final String[][] selectQuestion;

	public CurrentRound(int rid, String[][] selectQuestion, MainControl control) {
		super(rid, control);
		this.selectQuestion = selectQuestion;
	}

}
