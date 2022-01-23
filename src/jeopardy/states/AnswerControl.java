package jeopardy.states;

public class AnswerControl {
	public final String yes, no, skip, user;

	public AnswerControl(String yes, String no, String skip, String user) {
		this.yes = yes;
		this.no = no;
		this.skip = skip;
		this.user = user;
	}
}
