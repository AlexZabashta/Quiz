package web;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import com.sun.net.httpserver.HttpExchange;

import jeopardy.JeopardyEngine;

public class UserHandler implements ContextHandler {
	final String userHtml = Handler.readHtml("web/html/user.html");
	final JeopardyEngine engine;

	public UserHandler(JeopardyEngine engine) {
		this.engine = engine;
	}

	void handleSelect(HttpExchange exchange) throws IOException {
		try (Writer writer = Handler.sendWriter(exchange, 200)) {
			writer.append("<!DOCTYPE html>");
			writer.append("<html>");
			writer.append("<head>");
			writer.append("<meta charset=\"utf-8\">");
			writer.append("<title>Quiz</title>");
			writer.append("</head>");
			writer.append("<body>");
			writer.append("<ul>");

			for (int team = 1; team <= 4; team++) {
				writer.append("<li>");
				writer.append("<a style=\"font-size: 48pt; text-decoration:none\" href=\"/team");
				writer.append(Integer.toString(team));
				writer.append("\">Team ");
				writer.append(Integer.toString(team));
				writer.append("</a>");
				writer.append("</li>");
			}
			writer.append("</ul>");
			writer.append("</body>");
			writer.append("</html>");
		}
	}

	void handleUser(HttpExchange exchange) throws IOException {
		Handler.sendString(exchange, 200, userHtml);
	}

	void handleClick(HttpExchange exchange, String user) throws IOException {
		engine.userClicked(user);
		Handler.sendString(exchange, 200, "Cliked");
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String[] request = exchange.getRequestURI().getPath().split("/");
		System.out.print(Arrays.toString(request));
		if (request.length < 2) {
			handleSelect(exchange);
			return;
		}
		if (request.length == 2) {
			handleUser(exchange);
			return;
		}
		handleClick(exchange, request[1]);
	}

	@Override
	public String getContext() {
		return "/";
	}

}
