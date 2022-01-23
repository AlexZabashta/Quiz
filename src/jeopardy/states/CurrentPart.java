package jeopardy.states;

public class CurrentPart extends Current {
	public final int tid, qid, pid;

	public CurrentPart(int rid, int tid, int qid, int pid, MainControl control) {
		super(rid, control);
		this.tid = tid;
		this.qid = qid;
		this.pid = pid;
	}

}
