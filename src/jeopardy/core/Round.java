package jeopardy.core;

public class Round {
	public final String name;
	public final Topic[] topics;

	public Round(String name, Topic... topics) {
		this.name = name;
		this.topics = topics;
	}
}
