package jeopardy.states;

public class MainControl {
	public final String prevRound, prevPart, nextPart, nextRound;

	public MainControl(String prevRound, String prevPart, String nextPart, String nextRound) {
		this.prevRound = prevRound;
		this.prevPart = prevPart;
		this.nextPart = nextPart;
		this.nextRound = nextRound;
	}

}
