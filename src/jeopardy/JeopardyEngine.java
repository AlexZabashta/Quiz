package jeopardy;

import java.io.IOException;
import java.util.function.UnaryOperator;

import core.MasterHandler;
import core.UserHandler;
import jeopardy.states.CurrentQuestion;
import jeopardy.states.State;

public class JeopardyEngine implements MasterHandler, UserHandler {

	@Override
	public void userClicked(String userName) {
		synchronized (State.actions) {
			state = State.userClicked(state, userName);
		}
		System.out.println(userName + " clicked");
	}

	volatile State state;
	final Game game;

	public JeopardyEngine(Game game, State state) {
		this.game = game;
		this.state = state;
	}

	volatile long time = System.currentTimeMillis();

	@Override
	public void makeAction(String id) {
		synchronized (State.actions) {
			UnaryOperator<State> action = State.actions.get(id);
			if (action != null) {
				state = action.apply(state);
			}
			System.out.println(action);

		}

		long cur = System.currentTimeMillis();
		if (cur - time > 10_000 && !(state.currentRound instanceof CurrentQuestion)) {
			try {
				State.write(game, cur + ".txt", state);
			} catch (IOException e) {
				e.printStackTrace();
			}
			time = cur;
		}

	}

	@Override
	public void score(StringBuilder output) {
		state.printScore(output, game);
	}

	@Override
	public void question(StringBuilder output) {
		synchronized (State.actions) {
			State.actions.clear();
		}
		state.printBody(output, game);
	}

	@Override
	public void answer(StringBuilder output) {
		state.getAnswerBlock(output, game);

	}

	@Override
	public void actions(StringBuilder output) {
		state.printActions(output, game);
	}

}
