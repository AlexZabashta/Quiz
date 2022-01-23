package jeopardy.core;

public class Question {
	public final String name;
	public final double score;
	public final Part[] parts;

	public Question(String name, double score, Part... parts) {
		this.name = name;
		this.score = score;
		this.parts = parts;
	}

}
