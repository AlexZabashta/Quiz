package web;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import com.sun.net.httpserver.HttpExchange;

import jeopardy.JeopardyEngine;
import jeopardy.core.Game;
import jeopardy.core.Question;
import jeopardy.states.Action;
import jeopardy.states.AnswerControl;
import jeopardy.states.Current;
import jeopardy.states.CurrentPart;
import jeopardy.states.CurrentPartUser;
import jeopardy.states.State;

public class AnswerHandler implements ContextHandler {
	final JeopardyEngine engine;

	public AnswerHandler(JeopardyEngine engine) {
		this.engine = engine;
	}

	void handleCurrentPartUser(Writer writer, AnswerControl answerControl) throws IOException {
		writer.append("<table id=\"stop\" border = 1 style=\"width: 100%; height: 100%; border-collapse:collapse;\">");

		writer.append("<tbody>");
		{
			writer.append("<tr>");
			writer.append("<td colspan=\"3\" style=\"text-align: center;\">");
			writer.append(answerControl.user);
			writer.append("</td>");
			writer.append("</tr>");
		}
		writer.append("<tr>");

		{
			writer.append("<td style=\"text-align: center;\">");
			writer.append("<a style=\"text-decoration:none\" href=\"");
			writer.append("/master/");
			writer.append(answerControl.no);
			writer.append("\">");
			writer.append("&#10060; NO");
			writer.append("</a>");
			writer.append("</td>");
		}
		{
			writer.append("<td style=\"text-align: center;\">");
			writer.append("<a style=\"text-decoration:none\" href=\"");
			writer.append("/master/");
			writer.append(answerControl.skip);
			writer.append("\">");
			writer.append("&#128260; SKIP");
			writer.append("</a>");
			writer.append("</td>");
		}
		{
			writer.append("<td style=\"text-align: center;\">");
			writer.append("<a style=\"text-decoration:none\" href=\"");
			writer.append("/master/");
			writer.append(answerControl.yes);
			writer.append("\">");
			writer.append("&#9989; YES");
			writer.append("</a>");
			writer.append("</td>");
		}

		writer.append("</tr>");

		writer.append("</tbody>");
		writer.append("</table>");
	}

	void handleCurrentPart(Writer writer, double score, double fine) throws IOException {

		writer.append("<table border = 0 style=\"width: 100%; height: 100%; border-collapse:collapse;\">");

		writer.append("<tbody>");

		writer.append("<tr>");

		writer.append("<td style=\"text-align: center;\">+");
		writer.append(String.format(Locale.ENGLISH, "%.0f", score));
		writer.append("</td>");

		writer.append("<td style=\"text-align: center;\">-");
		writer.append(String.format(Locale.ENGLISH, "%.0f", fine));
		writer.append("</td>");

		writer.append("</tr>");

		writer.append("</tbody>");
	}

	void handleCurrentRound(Writer writer, double progress) throws IOException {
		writer.append("<table id=\"stop\" border = 0 style=\"width: 100%; height: 100%; border-collapse:collapse;\">");

		writer.append("<tbody>");

		writer.append("<tr>");

		writer.append("<td style=\"text-align: center;\">");
		writer.append(String.format(Locale.ENGLISH, "%.0f", progress * 100));
		writer.append(" &percnt;");
		writer.append("</td>");
		writer.append("</tr>");

		writer.append("</tbody>");
		writer.append("</table>");
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		try (Writer writer = Handler.sendWriter(exchange, 200)) {
			State state = engine.state();
			Current current = state.current;
			Game game = engine.game;

			if (current instanceof CurrentPartUser) {
				CurrentPartUser user = (CurrentPartUser) current;
				handleCurrentPartUser(writer, user.answer);
				return;
			}

			double progress = Action.progress(game, current.rid, state.used);

			if (current instanceof CurrentPart) {
				CurrentPart currentPart = (CurrentPart) current;

				Question question = game.rounds[currentPart.rid].topics[currentPart.tid].questions[currentPart.qid];

				double score = question.score;
				double fine = question.score * (1 - progress);
				handleCurrentPart(writer, score, fine);
				return;
			}

			handleCurrentRound(writer, progress);
		}

	}

	@Override
	public String getContext() {
		return "/answer/";
	}

}
