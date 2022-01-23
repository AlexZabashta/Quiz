package jeopardy.states;

public class CurrentPartUser extends CurrentPart {

	public final AnswerControl answer;

	public CurrentPartUser(int rid, int tid, int qid, int pid, MainControl control, AnswerControl answer) {
		super(rid, tid, qid, pid, control);
		this.answer = answer;
	}

}
