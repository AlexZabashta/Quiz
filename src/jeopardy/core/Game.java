package jeopardy.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import jeopardy.states.Action;
import jeopardy.states.Current;
import jeopardy.states.State;

public class Game {

	public final String name;
	public final Round[] rounds;

	public Game(String name, Round[] rounds) {
		this.name = name;
		this.rounds = rounds;
	}

	public State init() {
		Map<String, UnaryOperator<State>> actions = new HashMap<>();
		Set<Question> used = new HashSet<>();
		Current current = Action.currentRound(actions, this, 0, used);
		return new State(actions, new HashMap<>(), current, used, null);
	}

}
