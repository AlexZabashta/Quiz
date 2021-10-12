package web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import core.MasterHandler;
import core.UserHandler;

public class WebHandler implements HttpHandler {
	public static int WIDTH = 1500, HEIGHT = 800;

	final HttpServer server;
	final MasterHandler masterHandler;
	final UserHandler userHandler;
	final Charset charset = Charset.forName("utf8");
	final String userHtml;
	final String[] masterHtml;

	public WebHandler(HttpServer server, MasterHandler masterHandler, UserHandler userHandler) {
		this.server = server;
		this.masterHandler = masterHandler;
		this.userHandler = userHandler;

		if (userHandler == null) {
			this.userHtml = null;
		} else {
			this.userHtml = readHtml("html" + File.separator + "user.html");
		}

		if (masterHandler == null) {
			this.masterHtml = null;
		} else {
			this.masterHtml = readHtml("html" + File.separator + "master.html").split("part");
		}

		server.createContext("/", this);
		server.start();

	}

	static String readHtml(String path) {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = reader.readLine()) != null) {
				for (int i = 0; i < line.length(); i++) {
					char c = line.charAt(i);
					if (c >= 20) {
						builder.append(c);
					}
				}
			}
			return builder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "error";
		}
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		String[] request = exchange.getRequestURI().getPath().split("/");
		InetSocketAddress from = exchange.getRemoteAddress();

		System.out.println(Arrays.toString(request));
		System.out.println(from.getHostName());
		System.out.println(handle(exchange, request, from));
	}

	int handle(HttpExchange exchange, String[] request, InetSocketAddress from) throws IOException {
		if (request.length <= 1) {
			return sendString(exchange, 200, "Hello!");
		} else {
			final String userName = request[1];

			if (userName.equals("master")) {
				if (from.getHostName().equals("localhost") && masterHandler != null) {
					if (request.length <= 2) {
						return sendMasterHTML(exchange);
					} else {
						final String action = request[2];
						if (action.equals("file")) {
							return sendFile(exchange, request);
						} else if (action.equals("answer")) {
							return sendAnswerBlock(exchange);
						} else {
							masterHandler.makeAction(action);
							return sendMasterHTML(exchange);
						}
					}
				} else {
					return sendString(exchange, 403, "Forbidden");
				}
			} else {
				if (request.length <= 2) {
					return sendUserHTML(exchange);
				} else {
					if (userHandler != null) {
						userHandler.userClicked(request[1]);
					}
					return sendString(exchange, 200, "Clicked");
				}
			}
		}
	}

	int sendUserHTML(HttpExchange exchange) throws IOException {
		return sendString(exchange, 200, userHtml);
	}

	int sendMasterHTML(HttpExchange exchange) throws IOException {
		
	
		
		StringBuilder builder = new StringBuilder();
		builder.append(masterHtml[0]);
		masterHandler.score(builder);
		builder.append(masterHtml[1]);
		masterHandler.question(builder);
		builder.append(masterHtml[2]);
		// masterHandler.answer(builder);
		// builder.append(masterHtml[3]);
		masterHandler.actions(builder);
		builder.append(masterHtml[3]);

		return sendString(exchange, 200, builder.toString());
	}

	int sendAnswerBlock(HttpExchange exchange) throws IOException {
		StringBuilder builder = new StringBuilder();
		masterHandler.answer(builder);
		return sendString(exchange, 200, builder.toString());
	}

	int sendString(HttpExchange exchange, int code, String string) throws IOException {
		byte[] buffer = string.getBytes(charset);
		exchange.sendResponseHeaders(code, buffer.length);
		try (OutputStream outputStream = exchange.getResponseBody()) {
			outputStream.write(buffer);
		}
		return code;
	}

	int sendFile(HttpExchange exchange, String[] path) throws IOException {

		if (path.length <= 3) {
			return sendString(exchange, 403, "Forbidden");
		}

		StringBuilder pathname = new StringBuilder();

		for (int i = 3; i < path.length; i++) {
			if (path[i].startsWith(".")) {
				return sendString(exchange, 403, "Forbidden");
			}

			if (i > 3) {
				pathname.append(File.separator);
			}
			pathname.append(path[i]);

		}

		File file = new File(pathname.toString());

		if (file.exists()) {
			exchange.sendResponseHeaders(200, file.length());

			try (InputStream inputStream = new FileInputStream(file)) {
				try (OutputStream outputStream = exchange.getResponseBody()) {
					byte[] buffer = new byte[1 << 20];
					int len;
					while ((len = inputStream.read(buffer)) > 0) {
						outputStream.write(buffer, 0, len);
					}
				}
			}
			return 200;
		} else {
			return sendString(exchange, 404, "Not Found");
		}
	}

}
