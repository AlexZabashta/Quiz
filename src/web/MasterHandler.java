package web;

import java.io.IOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;

import jeopardy.JeopardyEngine;
import jeopardy.core.Game;
import jeopardy.core.Part;
import jeopardy.core.Question;
import jeopardy.core.Round;
import jeopardy.core.Topic;
import jeopardy.states.Current;
import jeopardy.states.CurrentPart;
import jeopardy.states.CurrentRound;
import jeopardy.states.MainControl;
import jeopardy.states.State;

public class MasterHandler implements ContextHandler {
	final String[] masterHtml = Handler.readHtml("web/html/master.html").split("part");
	final JeopardyEngine engine;

	final Map<String, String> fileIds;
	final String origin;

	public MasterHandler(JeopardyEngine engine, Map<String, String> files, String origin) {
		this.engine = engine;
		this.fileIds = files;
		this.origin = origin;
	}

	void addScore(Writer writer, State state) throws IOException {
		writer.append("<table border = 1 style=\"width: 100%; height: 100%; border-collapse:collapse;\">");
		writer.append("<tbody>");

		List<Entry<String, Double>> list = new ArrayList<>(state.score.entrySet());
		Collections.sort(list, Comparator.comparingDouble(e -> -e.getValue().doubleValue()));

		for (Entry<String, Double> e : list) {
			writer.append("<tr>");
			writer.append("<td style=\"text-align: center;\">");

			String user = e.getKey();

			if (user.equals(state.lastSuccess)) {
				writer.append("<b>");
				writer.append(user);
				writer.append("</b>");
			} else {
				writer.append(user);
			}

			writer.append("</td>");
			writer.append("<td style=\"text-align: center;\">");
			writer.append(String.format(Locale.ENGLISH, "%13.3f", e.getValue().doubleValue()).replace(" ", "&nbsp;"));
			writer.append("</td>");
			writer.append("</tr>");
		}

		writer.append("</tbody>");
		writer.append("</table>");
	}

	private void handleCurrentRound(Writer output, Set<Question> used, CurrentRound currentRound, Round round) throws IOException {

		output.append("<table border = 1 style=\"width: 100%; height: 100%; border-collapse:collapse;\">");

		output.append("<tbody>");

		output.append("<tr>");

		output.append("<td style=\"text-align: right;\">");
		output.append(round.name);
		output.append("&nbsp;&nbsp;</td>");

		output.append("</tr>");

		for (int row = 0; row < round.topics.length; row++) {
			Topic topic = round.topics[row];

			output.append("<tr>");

			output.append("<td style=\"text-align: right; width: 40%;\">");
			output.append(topic.name);
			output.append("&nbsp;&nbsp;</td>");

			for (int column = 0; column < topic.questions.length; column++) {
				Question question = topic.questions[column];
				output.append("<td style=\"text-align: center;\">");
				if (used.contains(question)) {
					output.append("&nbsp;");
				} else {
					output.append("<a style=\"text-decoration:none\" href=\"");
					output.append("/master/");

					output.append(currentRound.selectQuestion[row][column]);

					output.append("\">");
					output.append(String.format(Locale.ENGLISH, "%.0f", question.score));
					output.append("</a>");
				}
				output.append("</td>");
			}

			output.append("</tr>");
		}

		output.append("</tbody>");
		output.append("</table>");
	}

	void iframe(Writer writer, String href) throws IOException {
		writer.append("<iframe width=\"100%\" height=\"100%\" src=\"");
		writer.append(href);
		writer.append("\" ></iframe>");
	}

	void youtube(Writer writer, String videoId) throws IOException {
		writer.append("<div width=\"100%\" height=\"100%\" id=\"ytplayer\"></div>");

		writer.append("<script>");
		writer.append("var tag = document.createElement('script');");
		writer.append("tag.src = \"https://www.youtube.com/player_api\";");
		writer.append("var firstScriptTag = document.getElementsByTagName('script')[0];");
		writer.append("firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);");
		writer.append("var player;");
		writer.append("function onYouTubePlayerAPIReady() {");
		writer.append("player = new YT.Player('ytplayer', {");
		writer.append("height: '100%',");
		writer.append("width: '100%',");
		writer.append("videoId: '");
		writer.append(videoId);
		writer.append("'");
		writer.append("});");
		writer.append("}");
		writer.append("</script>");
	}

	private void handleCurrentPart(Writer writer, Part part) throws IOException {

		// Round round = game.rounds[currentPart.rid];
		// Part[] parts = round.topics[currentPart.tid].questions[currentPart.qid].parts;

		// writer.append(control.nextRound)

		String youtube = part.map.get("youtube");
		if (youtube != null) {
			youtube(writer, youtube);
			// iframe(writer, "https://www.youtube.com/embed/" + youtube + "?enablejsapi=1&origin=" + origin);
			return;
		}

		String iframe = part.map.get("iframe");
		if (iframe != null) {
			iframe(writer, iframe);
			return;
		}

		String html = part.map.get("html");
		if (html != null) {
			iframe(writer, "/file/" + fileIds.get(html));
			return;
		}

		String text = part.map.get("text");
		if (text != null) {
			writer.append(text);
			return;
		}

		String image = part.map.get("image");
		String audio = part.map.get("audio");
		String video = part.map.get("video");

		writer.append("<video width=1 height=1 id=\"dst\"");

		if (image != null) {
			writer.append(" poster=\"");
			writer.append("/file/");
			writer.append(fileIds.get(image));
			writer.append("\"");
		}

		if (audio != null || video != null) {
			writer.append(" controls>");
		} else {
			writer.append(">");
		}

		if (audio != null) {
			writer.append("<source src=\"");
			writer.append("/file/");
			writer.append(fileIds.get(audio));
			writer.append("\">");
		}

		if (video != null) {
			writer.append("<source src=\"");
			writer.append("/file/");
			writer.append(fileIds.get(video));
			writer.append("\">");
		}

		writer.append("</video>");

	}

	void addControls(Writer writer, Current current, Round[] rounds, int rid, Part[] parts, int pid) throws IOException {
		writer.append("<table border = 1 style=\"width: 100%; height: 100%; border-collapse:collapse;\">");

		writer.append("<tbody>");

		writer.append("<tr>");

		MainControl control = current.control;

		if (control.prevRound != null) {
			writer.append("<td style=\"text-align: center;\">");

			writer.append("<a style=\"text-decoration:none\" href=\"");
			writer.append("/master/");

			writer.append(control.prevRound);

			writer.append("\">");

			if (parts == null) {
				writer.append(rounds[rid - 1].name);
				writer.append(" &#11013;"); // <<<
			} else {
				writer.append("&#x2196;");
				writer.append(rounds[rid].name);
			}

			writer.append("</a>");

			writer.append("</td>");
		}

		if (parts != null) {
			if (control.prevPart != null) {
				writer.append("<td style=\"text-align: center;\">");

				writer.append("<a style=\"text-decoration:none\" href=\"");
				writer.append("/master/");

				writer.append(control.prevPart);

				writer.append("\">");

				writer.append(parts[pid - 1].name);
				writer.append(" &#11013;"); // <<<

				writer.append("</a>");

				writer.append("</td>");
			}
			if (control.nextPart != null) {
				writer.append("<td style=\"text-align: center;\">");

				writer.append("<a style=\"text-decoration:none\" href=\"");
				writer.append("/master/");

				writer.append(control.nextPart);

				writer.append("\">");

				writer.append("&#10145; "); // >>>
				writer.append(parts[pid + 1].name);

				writer.append("</a>");

				writer.append("</td>");
			}
		}

		if (control.nextRound != null) {
			writer.append("<td style=\"text-align: center;\">");

			writer.append("<a style=\"text-decoration:none\" href=\"");
			writer.append("/master/");

			writer.append(control.nextRound);

			writer.append("\">");

			if (parts == null) {
				writer.append("&#10145; ");
				writer.append(rounds[rid + 1].name);
			} else {
				writer.append(rounds[rid].name);
				writer.append("&#x2197;");
			}

			writer.append("</a>");

			writer.append("</td>");
		}

		writer.append("</tr>");

		writer.append("</tbody>");
		writer.append("</table>");
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String[] request = exchange.getRequestURI().getPath().split("/");
		InetSocketAddress from = exchange.getRemoteAddress();

		if (request.length == 3) {
			engine.makeAction(request[2]);
		}

		State state = engine.state();
		Current current = state.current;
		Game game = engine.game;

		try (Writer writer = Handler.sendWriter(exchange, 200)) {

			writer.append(masterHtml[0]);
			addScore(writer, state);
			writer.append(masterHtml[1]);

			int pid = -1;
			Part[] parts = null;
			Round[] rounds = game.rounds;
			int rid = current.rid;

			if (current instanceof CurrentPart) {
				CurrentPart currentPart = (CurrentPart) current;

				pid = currentPart.pid;
				parts = rounds[rid].topics[currentPart.tid].questions[currentPart.qid].parts;

				handleCurrentPart(writer, parts[pid]);
			}

			if (current instanceof CurrentRound) {
				CurrentRound currentRound = (CurrentRound) current;
				handleCurrentRound(writer, state.used, currentRound, rounds[rid]);
			}

			writer.append(masterHtml[2]);

			addControls(writer, current, rounds, rid, parts, pid);

			writer.append(masterHtml[3]);
		}

		System.out.println(Arrays.toString(request));
		System.out.println(from.getHostName());
		Handler.sendString(exchange, 200, getClass().getName());
	}

	@Override
	public String getContext() {
		return "/master/";
	}

}
