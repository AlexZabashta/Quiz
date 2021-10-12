package jeopardy.states;

import java.util.Locale;

import jeopardy.Game;
import web.WebHandler;

public class CurrentQuestion extends CurrentRound {
	public final int row;
	public final int column;
	public final int stage;

	public CurrentQuestion(int round, int row, int column, int stage) {
		super(round);
		this.row = row;
		this.column = column;
		this.stage = stage;
	}

	@Override
	public void printActions(StringBuilder output, Game game) {

		synchronized (State.actions) {

			output.append("<table border = 1, style=\"width: 100%; height: 100%; border-collapse:collapse;\">");

			output.append("<tbody>");

			output.append("<tr>");

			for (int delta = -1; delta <= +1; delta += 2) {
				int deltaStage = stage + delta;
				output.append("<td style=\"text-align: center;\">");
				output.append("<a style=\"text-decoration:none\" href=\"");
				output.append("/master/");

				String[] stages = game.questions[round][row][column].stages;
				if (0 <= deltaStage && deltaStage < stages.length) {
					output.append(State.goToQuestionStage(round, row, column, deltaStage, false));
					output.append("\">");
					output.append("Go to ");
					output.append(game.questions[round][row][column].stages[deltaStage]);
				} else {
					output.append(State.goToRound(game, round, deltaStage >= 0));
					output.append("\">");
					output.append("Return to Round ");
					output.append(round + 1);
				}

				output.append("</a>");
				output.append("</td>");
			}

			output.append("</tr>");

			output.append("</tbody>");
			output.append("</table>");
		}

	}

	@Override
	public void printBody(StringBuilder output, Game game, State state) {
		String path = game.questions[round][row][column].stages[stage];

		if (path.toLowerCase().endsWith("jpg") || path.toLowerCase().endsWith("png")) {
			output.append("<img ");
			output.append(" width=\"" + WebHandler.WIDTH + "\" height=\"" + WebHandler.HEIGHT + "\" ");
			output.append("src=\"/master/file/" + game.prefix + "/" + (round + 1) + "/" + (row + 1) + "/" + (column + 1) + "/" + path + "\" ");
			output.append("/>");
		}

		if (path.toLowerCase().endsWith("webm") || path.toLowerCase().endsWith("mp4")) {
			output.append("<video controls autoplay ");
			output.append(" width=\"" + WebHandler.WIDTH + "\" height=\"" + WebHandler.HEIGHT + "\" ");
			output.append("src=\"/master/file/" + game.prefix + "/" + (round + 1) + "/" + (row + 1) + "/" + (column + 1) + "/" + path + "\" ");
			output.append("/>");
		}

	}

	@Override
	public void getAnswerBlock(StringBuilder output, Game game, State state) {
		double score = game.questions[round][row][column].score;
		double fine = (1 - progress(state.used)) * score;

		if (state.userClicked == null) {
			output.append("<table border = 0, style=\"width: 100%; height: 100%; border-collapse:collapse;\">");

			output.append("<tbody>");

			output.append("<tr>");

			output.append("<td style=\"text-align: center;\">+");
			output.append(String.format(Locale.ENGLISH, "%.0f", score));
			output.append("</td>");

			output.append("<td style=\"text-align: center;\">-");
			output.append(String.format(Locale.ENGLISH, "%.0f", fine));
			output.append("</td>");

			output.append("</tr>");

			output.append("</tbody>");
		} else {

			synchronized (State.actions) {

				output.append("<table id = \"stop\", border = 1, style=\"width: 100%; height: 100%; border-collapse:collapse;\">");

				output.append("<tbody>");
				{
					output.append("<tr>");
					output.append("<td colspan=\"3\" style=\"text-align: center;\">");
					output.append(state.userClicked);
					output.append("</td>");
					output.append("</tr>");
				}
				output.append("<tr>");

				{
					output.append("<td style=\"text-align: center;\">");
					output.append("<a style=\"text-decoration:none\" href=\"");
					output.append("/master/");
					output.append(State.goToQuestionStage(round, row, column, stage, state.userClicked, -fine));
					output.append("\">");
					output.append("NO");
					output.append("</a>");
					output.append("</td>");
				}
				{
					output.append("<td style=\"text-align: center;\">");
					output.append("<a style=\"text-decoration:none\" href=\"");
					output.append("/master/");
					output.append(State.goToQuestionStage(round, row, column, stage, true));
					output.append("\">");
					output.append("SKIP");
					output.append("</a>");
					output.append("</td>");
				}
				{
					output.append("<td style=\"text-align: center;\">");
					output.append("<a style=\"text-decoration:none\" href=\"");
					output.append("/master/");
					output.append(State.goToQuestionStage(round, row, column, stage, state.userClicked, score));
					output.append("\">");
					output.append("YES");
					output.append("</a>");
					output.append("</td>");
				}
			}

			output.append("</tr>");

			output.append("</tbody>");
			output.append("</table>");

		}
	}

}
