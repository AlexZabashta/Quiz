package jeopardy.states;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

import jeopardy.Game;

public class State {
	public final static AtomicLong actionID = new AtomicLong(System.currentTimeMillis());
	public final static Map<String, UnaryOperator<State>> actions = new HashMap<>();

	static boolean[][] clone(boolean[][] array) {
		boolean[][] clone = new boolean[array.length][];
		for (int i = 0; i < array.length; i++) {
			clone[i] = array[i].clone();
		}
		return clone;
	}

	static boolean[][][] clone(boolean[][][] array) {
		boolean[][][] clone = new boolean[array.length][][];
		for (int i = 0; i < array.length; i++) {
			clone[i] = State.clone(array[i]);
		}
		return clone;
	}

	static String goToQuestionStage(int round, int row, int column, int stage, boolean removeClick) {
		String key = Long.toString(actionID.incrementAndGet());
		actions.put(key, new UnaryOperator<State>() {
			@Override
			public State apply(State state) {
				if (removeClick) {
					return new State(state.score, state.used, null, new CurrentQuestion(round, row, column, stage));
				} else {
					return new State(state.score, state.used, state.userClicked, new CurrentQuestion(round, row, column, stage));
				}
			}
		});
		return key;
	}

	public static Object goToQuestionStage(int round, int row, int column, int stage, String userClicked, double value) {
		String key = Long.toString(actionID.incrementAndGet());
		actions.put(key, new UnaryOperator<State>() {
			@Override
			public State apply(State state) {
				Map<String, Double> score = new HashMap<>(state.score);
				double cur = score.getOrDefault(userClicked, 0.0);
				score.put(userClicked, cur + value);
				return new State(score, state.used, null, new CurrentQuestion(round, row, column, stage));
			}
		});
		return key;
	}

	static String goToRound(Game game, int round, boolean removeQuestion) {
		String key = Long.toString(actionID.incrementAndGet());
		actions.put(key, new UnaryOperator<State>() {
			@Override
			public State apply(State state) {
				boolean[][][] used = state.used;

				if (removeQuestion && state.currentRound instanceof CurrentQuestion) {
					CurrentQuestion questionState = (CurrentQuestion) state.currentRound;
					used = State.clone(state.used);
					used[questionState.round][questionState.row][questionState.column] = true;
				}
				return new State(state.score, used, null, new CurrentRound(round));
			}
		});

		return key;
	}

	public static State read(Game game, String file) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			int offsetRound = Integer.parseInt(reader.readLine().strip());
			if (offsetRound <= 0 || game.rounds < offsetRound) {
				throw new IllegalArgumentException(offsetRound + " " + game.rounds);
			}

			int players = Integer.parseInt(reader.readLine().strip());
			Map<String, Double> score = new HashMap<String, Double>(players);
			for (int player = 0; player < players; player++) {
				String[] playerScore = reader.readLine().split("\t");
				score.put(playerScore[0], Double.parseDouble(playerScore[1]));
			}
			boolean[][][] used = new boolean[game.rounds][game.rows][game.columns];

			for (int round = 0; round < game.rounds; round++) {
				for (int row = 0; row < game.rows; row++) {
					String string = reader.readLine();
					for (int column = 0; column < game.columns; column++) {
						used[round][row][column] = string.charAt(column) != '.';
					}
				}
			}

			return new State(score, used, null, new CurrentRound(offsetRound - 1));

		}
	}

	public static State userClicked(State state, String userName) {
		Map<String, Double> score = state.score;
		if (state.score.containsKey(userName) == false) {
			score = new HashMap<>(state.score);
			score.put(userName, 0.0);
		}
		String userClicked = state.userClicked;
		if (userClicked == null) {
			userClicked = userName;
		}

		if (userClicked == state.userClicked && score == state.score) {
			return state;
		}
		return new State(score, state.used, userClicked, state.currentRound);
	}

	public static void write(Game game, String file, State progress) throws IOException {
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.println(progress.currentRound.round + 1);
			writer.println(progress.score.size());

			for (Entry<String, Double> e : progress.score.entrySet()) {
				writer.printf(Locale.ENGLISH, "%s\t%.3f%n", e.getKey(), e.getValue().doubleValue());
			}

			for (int round = 0; round < game.rounds; round++) {
				for (int row = 0; row < game.rows; row++) {
					for (int column = 0; column < game.columns; column++) {
						writer.print(progress.used[round][row][column] ? '#' : '.');
					}
					writer.println();
				}
			}

		}
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			int currentRound = Integer.parseInt(reader.readLine().strip());
			if (currentRound <= 0 || game.rounds < currentRound) {
				throw new IllegalArgumentException(currentRound + " " + game.rounds);
			}

			int players = Integer.parseInt(reader.readLine().strip());
			Map<String, Double> score = new HashMap<String, Double>(players);
			for (int player = 0; player < players; player++) {
				String[] playerScore = reader.readLine().split("\t");
				score.put(playerScore[0], Double.parseDouble(playerScore[1]));
			}
			boolean[][][] used = new boolean[game.rounds][game.rows][game.columns];
			for (int round = 0; round < game.rounds; round++) {
				for (int row = 0; row < game.rows; row++) {
					String string = reader.readLine();
					for (int column = 0; column < game.columns; column++) {
						used[round][row][column] = string.charAt(column) != '.';
					}
				}
			}
			// return new SelectQuestion(score, used, currentRound);

		}
	}

	public final CurrentRound currentRound;

	public final Map<String, Double> score;

	public final boolean[][][] used;

	public final String userClicked;

	public State(Map<String, Double> score, boolean[][][] used, String userClicked, CurrentRound currentRound) {
		this.score = score;
		this.used = used;
		this.userClicked = userClicked;
		this.currentRound = currentRound;
	}

	public void getAnswerBlock(StringBuilder output, Game game) {
		currentRound.getAnswerBlock(output, game, this);
	}

	public void printActions(StringBuilder output, Game game) {

		currentRound.printActions(output, game);

	}

	public void printBody(StringBuilder output, Game game) {
		currentRound.printBody(output, game, this);
	}

	public void printScore(StringBuilder output, Game game) {
		output.append("<table border = 1, style=\"width: 100%; height: 100%; border-collapse:collapse;\">");
		output.append("<tbody>");

		List<Entry<String, Double>> list = new ArrayList<>(score.entrySet());
		Collections.sort(list, Comparator.comparingDouble(e -> -e.getValue().doubleValue()));

		for (Entry<String, Double> e : list) {
			output.append("<tr>");
			output.append("<td style=\"text-align: center;\">");
			output.append(e.getKey());
			output.append("</td>");
			output.append("<td style=\"text-align: center;\">");
			output.append(String.format(Locale.ENGLISH, "%13.3f", e.getValue().doubleValue()).replace(" ", "&nbsp;"));
			output.append("</td>");
			output.append("</tr>");
		}

		output.append("</tbody>");
		output.append("</table>");
	}

}
