package jeopardy;

import jeopardy.core.Game;
import jeopardy.states.Action;
import jeopardy.states.State;

public class JeopardyEngine {

	public void userClicked(String userName) {
		state = Action.userClicked(game, state, userName);
	}

	volatile State state;
	public final Game game;

	public JeopardyEngine(Game game, State state) {
		this.game = game;
		this.state = state;
	}

	public void makeAction(String id) {
		state = state.makeAction(id);
	}

	public State state() {
		return state;
	}

}
