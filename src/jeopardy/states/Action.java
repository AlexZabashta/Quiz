package jeopardy.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

import jeopardy.core.Game;
import jeopardy.core.Question;
import jeopardy.core.Round;
import jeopardy.core.Topic;

public class Action {
	public final static AtomicLong id = new AtomicLong(System.currentTimeMillis());

	public static String add(Map<String, UnaryOperator<State>> actions, UnaryOperator<State> action) {
		String key = Long.toString(id.incrementAndGet());
		actions.put(key, action);
		return key;
	}

	static UnaryOperator<State> goToQuestionPart(Game game, int rid, int tid, int qid, int pid, boolean removeClick) {

		return new UnaryOperator<State>() {
			@Override
			public State apply(State state) {
				Question question = game.rounds[rid].topics[tid].questions[qid];
				Map<String, UnaryOperator<State>> actions = new HashMap<>();
				MainControl mainControl = partControl(actions, game, rid, tid, qid, pid, question.parts.length);
				CurrentPart current = new CurrentPart(rid, tid, qid, pid, mainControl);

				if (removeClick == false && state.current instanceof CurrentPartUser) {
					CurrentPartUser currentPartUser = (CurrentPartUser) state.current;
					String user = currentPartUser.answer.user;
					AnswerControl answer = answerControl(actions, state.used, game, rid, tid, qid, pid, user);
					current = new CurrentPartUser(pid, pid, pid, pid, mainControl, answer);
				}

				return new State(actions, state.score, current, state.used, state.lastSuccess);
			}

		};
	}

	static UnaryOperator<State> goToQuestionPart(Game game, int rid, int tid, int qid, int pid, String user, double value, boolean success) {
		return new UnaryOperator<State>() {
			@Override
			public State apply(State state) {
				Question question = game.rounds[rid].topics[tid].questions[qid];
				Map<String, UnaryOperator<State>> actions = new HashMap<>();
				MainControl mainControl = partControl(actions, game, rid, tid, qid, pid, question.parts.length);
				CurrentPart current = new CurrentPart(rid, tid, qid, pid, mainControl);

				Map<String, Double> score = new HashMap<>(state.score);
				double cur = score.getOrDefault(user, 0.0);
				score.put(user, cur + value);

				if (success) {
					return new State(actions, score, current, state.used, user);
				} else {
					return new State(actions, score, current, state.used, state.lastSuccess);
				}

			}
		};
	}

	public static Current currentRound(Map<String, UnaryOperator<State>> actions, Game game, int rid, Set<Question> used) {
		Round[] rounds = game.rounds;
		Round round = rounds[rid];
		Topic[] topics = round.topics;
		String[][] selectQuestion = new String[topics.length][];

		for (int tid = 0; tid < topics.length; tid++) {
			Question[] questions = topics[tid].questions;
			selectQuestion[tid] = new String[questions.length];
			for (int qid = 0; qid < questions.length; qid++) {
				Question question = questions[qid];
				if (used.contains(question)) {
					continue;
				}
				selectQuestion[tid][qid] = add(actions, goToQuestionPart(game, rid, tid, qid, 0, true));
			}
		}

		MainControl mainControl = roundControl(actions, game, rid, rounds.length);
		return new CurrentRound(rid, selectQuestion, mainControl);
	}

	static UnaryOperator<State> goToRound(Game game, int rid, boolean removeQuestion) {
		return new UnaryOperator<State>() {
			@Override
			public State apply(State state) {
				Round[] rounds = game.rounds;
				Round round = rounds[rid];

				Set<Question> used = state.used;
				if (removeQuestion && state.current instanceof CurrentPart) {
					CurrentPart currentPart = (CurrentPart) state.current;
					Question question = round.topics[currentPart.tid].questions[currentPart.qid];
					used = new HashSet<>(state.used);
					used.add(question);
				}

				Map<String, UnaryOperator<State>> actions = new HashMap<>();
				Current current = currentRound(actions, game, rid, used);
				return new State(actions, state.score, current, used, state.lastSuccess);
			}
		};
	}

	static MainControl partControl(Map<String, UnaryOperator<State>> actions, Game game, int rid, int tid, int qid, int pid, int parts) {
		String prevRound = null;
		String prevPart = null;
		String nextPart = null;
		String nextRound = null;

		if (pid <= 0) {
			prevRound = add(actions, goToRound(game, rid, false));
		} else {
			prevPart = add(actions, goToQuestionPart(game, rid, tid, qid, pid - 1, false));
		}

		if (pid >= parts - 1) {
			nextRound = add(actions, goToRound(game, rid, true));
		} else {
			nextPart = add(actions, goToQuestionPart(game, rid, tid, qid, pid + 1, false));
		}

		return new MainControl(prevRound, prevPart, nextPart, nextRound);
	}

	public static double progress(Game game, int rid, Set<Question> used) {
		double cnt = 0, sum = 0;

		for (Topic topic : game.rounds[rid].topics) {
			for (Question question : topic.questions) {
				if (used.contains(question)) {
					++cnt;
				}
				++sum;
			}
		}

		return cnt / sum;
	}

	static MainControl roundControl(Map<String, UnaryOperator<State>> actions, Game game, int rid, int rounds) {
		String prevRound = null;
		String nextRound = null;
		if (rid > 0) {
			prevRound = add(actions, goToRound(game, rid - 1, false));
		}
		if (rid < rounds - 1) {
			nextRound = add(actions, goToRound(game, rid + 1, false));
		}
		return new MainControl(prevRound, null, null, nextRound);
	}

	static AnswerControl answerControl(Map<String, UnaryOperator<State>> actions, Set<Question> used, Game game, int rid, int tid, int qid, int pid, String user) {
		Question question = game.rounds[rid].topics[tid].questions[qid];
		double fine = progress(game, rid, used) - 1;
		String yes = add(actions, goToQuestionPart(game, rid, tid, qid, pid, user, question.score, true));
		String no = add(actions, goToQuestionPart(game, rid, tid, qid, pid, user, fine * question.score, false));
		String skip = add(actions, goToQuestionPart(game, rid, tid, qid, pid, true));
		return new AnswerControl(yes, no, skip, user);
	}

	static State addUser(State state, String user) {
		Map<String, Double> score = state.score;
		if (score.containsKey(user)) {
			return state;
		}
		score = new HashMap<>(score);
		score.put(user, 0.0);
		return new State(state.actions, score, state.current, state.used, state.lastSuccess);
	}

	public static State userClicked(Game game, State state, String user) {
		state = addUser(state, user);
		if ((state.current instanceof CurrentRound) || (state.current instanceof CurrentPartUser)) {
			return state;
		}

		CurrentPart currentPart = (CurrentPart) state.current;

		int rid = currentPart.rid, tid = currentPart.tid, qid = currentPart.qid, pid = currentPart.pid;

		Map<String, UnaryOperator<State>> actions = new HashMap<>(state.actions);

		MainControl mainControl = currentPart.control;

		AnswerControl answer = answerControl(actions, state.used, game, rid, tid, qid, pid, user);

		Current current = new CurrentPartUser(rid, tid, qid, pid, mainControl, answer);

		return new State(actions, state.score, current, state.used, state.lastSuccess);
	}
}
