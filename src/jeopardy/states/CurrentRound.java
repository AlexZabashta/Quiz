package jeopardy.states;

import java.util.Locale;

import jeopardy.Game;

public class CurrentRound {
	public final int round;

	public CurrentRound(int round) {
		this.round = round;
	}

	public void printBody(StringBuilder output, Game game, State state) {

		synchronized (State.actions) {

			output.append("<table border = 1, style=\"width: 100%; height: 100%; border-collapse:collapse;\">");

			output.append("<tbody>");

			output.append("<tr>");

			output.append("<td style=\"text-align: right;\">");
			output.append("Round " + (round + 1) + " / " + game.rounds);
			output.append("&nbsp;&nbsp;</td>");

			output.append("</tr>");

			for (int row = 0; row < game.rows; row++) {
				output.append("<tr>");

				output.append("<td style=\"text-align: right; width: 40%;\">");
				output.append(game.name[round][row]);
				output.append("&nbsp;&nbsp;</td>");

				for (int column = 0; column < game.columns; column++) {
					output.append("<td style=\"text-align: center;\">");
					if (state.used[round][row][column] == false && game.questions[round][row][column].stages.length > 0) {
						output.append("<a style=\"text-decoration:none\" href=\"");
						output.append("/master/");

						output.append(State.goToQuestionStage(round, row, column, 0, true));

						output.append("\">");
						output.append(String.format(Locale.ENGLISH, "%.0f", game.questions[round][row][column].score));
						output.append("</a>");
					}
					output.append("</td>");
				}

				output.append("</tr>");
			}

			output.append("</tbody>");
			output.append("</table>");
		}
	}

	public void printActions(StringBuilder output, Game game) {
		synchronized (State.actions) {

			output.append("<table border = 1, style=\"width: 100%; height: 100%; border-collapse:collapse;\">");

			output.append("<tbody>");

			output.append("<tr>");

			for (int delta = -1; delta <= +1; delta += 2) {
				int deltaRound = round + delta;
				if (0 <= deltaRound && deltaRound < game.rounds) {
					output.append("<td style=\"text-align: center;\">");

					output.append("<a style=\"text-decoration:none\" href=\"");
					output.append("/master/");

					output.append(State.goToRound(game, deltaRound, false));

					output.append("\">");
					output.append("Go to Round ");
					output.append(deltaRound + 1);
					output.append("</a>");

					output.append("</td>");

				}
			}

			output.append("</tr>");

			output.append("</tbody>");
			output.append("</table>");

		}
	}

	public double progress(boolean[][][] used) {
		double cnt = 0, all = 0;

		for (boolean[] array : used[round]) {
			for (boolean value : array) {
				if (value) {
					++cnt;
				}
				++all;
			}
		}

		return cnt / all;
	}

	public void getAnswerBlock(StringBuilder output, Game game, State state) {

		output.append("<table id = \"stop\", border = 0, style=\"width: 100%; height: 100%; border-collapse:collapse;\">");

		output.append("<tbody>");

		output.append("<tr>");

		output.append("<td style=\"text-align: center;\">");
		output.append(String.format(Locale.ENGLISH, "%.0f", progress(state.used) * 100));
		output.append(" &percnt;");
		output.append("</td>");
		output.append("</tr>");

		output.append("</tbody>");
		output.append("</table>");

	}
}
