package jeopardy;

import java.util.Arrays;

public class Question {
	public final String[] stages;
	public final double score;

	public Question(String[] stages, double score) {
		this.stages = stages;
		this.score = score;
	}

	@Override
	public String toString() {
		return "Question [stages=" + Arrays.toString(stages) + ", score=" + score + "]";
	}
}
