package jeopardy.core;

public class Topic {
	public final String name;
	public final Question[] questions;

	public Topic(String name, Question... questions) {
		this.name = name;
		this.questions = questions;
	}
}
