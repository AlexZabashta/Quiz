package jeopardy.states;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.UnaryOperator;

import jeopardy.core.Game;
import jeopardy.core.Question;
import jeopardy.core.Round;
import jeopardy.core.Topic;

public class State {

	public final Map<String, UnaryOperator<State>> actions;

	public final Current current;

	public final Map<String, Double> score;
	public final Set<Question> used;

	public final String lastSuccess;

	public State(Map<String, UnaryOperator<State>> actions, Map<String, Double> score, Current current, Set<Question> used, String lastSuccess) {
		this.actions = actions;
		this.score = score;
		this.current = current;
		this.used = used;
		this.lastSuccess = lastSuccess;
	}

	public State makeAction(String id) {
		UnaryOperator<State> action = actions.get(id);

		if (action == null) {
			return this;
		}
		return action.apply(this);
	}

	public void write(Game game, File file) throws IOException {
		try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
			writer.print(score.size());
			for (Entry<String, Double> entry : score.entrySet()) {
				writer.print(entry.getKey());
				writer.print('\t');
				writer.print(entry.getValue());
				writer.println();
			}

			for (Round round : game.rounds) {
				writer.println(round.name);
				for (Topic topic : round.topics) {
					writer.print(topic.name);
					writer.print('\t');
					for (Question question : topic.questions) {
						if (used.contains(question)) {
							writer.print('.');
						} else {
							writer.print('#');
						}
					}
					writer.println();
				}
			}
		}
	}

	public static State read(Game game, File file) throws IOException {
		Map<String, Double> score = new HashMap<>();
		Set<Question> used = new HashSet<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
			int n = Integer.parseInt(reader.readLine());
			for (int i = 0; i < n; i++) {
				String[] row = reader.readLine().split("\t");
				score.put(row[0], Double.parseDouble(row[1]));
			}

			for (Round round : game.rounds) {
				String roundName = reader.readLine();
				if (round.name.equals(roundName) == false) {
					throw new IllegalStateException("Unexpected round " + roundName);
				}
				for (Topic topic : round.topics) {
					String[] row = reader.readLine().split("\t");
					if (topic.name.equals(row[0]) == false) {
						throw new IllegalStateException("Unexpected topic " + row[0]);
					}
					for (int i = 0; i < topic.questions.length; i++) {
						if (row[1].charAt(i) == '.') {
							used.add(topic.questions[i]);
						}
					}
				}
			}
		}

		Map<String, UnaryOperator<State>> actions = new HashMap<>();
		Current current = Action.currentRound(actions, game, 0, used);
		return new State(actions, score, current, used, null);
	}

}
