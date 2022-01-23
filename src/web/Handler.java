package web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.zip.ZipFile;

import org.json.simple.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import jeopardy.JeopardyEngine;
import jeopardy.core.Part;
import jeopardy.core.Question;
import jeopardy.core.Round;
import jeopardy.core.Topic;

public class Handler {

	public static final Charset charset = StandardCharsets.UTF_8;

	public static void addContext(HttpServer server, ContextHandler handler) {
		server.createContext(handler.getContext(), handler);
	}

	public static void init(JeopardyEngine engine, ZipFile zipFile, JSONObject params) {
		Map<String, String> fileToId = new HashMap<>();
		Map<String, String> idToFile = new HashMap<>();

		long fileId = System.currentTimeMillis();

		for (Round round : engine.game.rounds) {
			for (Topic topic : round.topics) {
				for (Question question : topic.questions) {
					for (Part part : question.parts) {
						for (Entry<String, String> entry : part.map.entrySet()) {
							String type = entry.getKey();
							if (type.equals("iframe") || type.equals("text") || type.equals("youtube")) {
								continue;
							}

							String file = entry.getValue();
							String id = Long.toString(++fileId);

							int dot = file.lastIndexOf('.');
							if (dot >= 0) {
								id += file.substring(dot);
							}

							fileToId.put(file, id);
							idToFile.put(id, file);
						}
					}
				}
			}
		}

		try {
			String host = (String) params.get("host");
			int port = (int) (long) params.get("port");
			boolean handleMaster = (boolean) params.get("master");
			boolean handleUser = (boolean) params.get("user");

			HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);
			server.setExecutor(Executors.newFixedThreadPool(4));

			// server.createContext("/favicon.svg", new IconHandler(((char) 10068) + ""));

			if (handleMaster) {

				addContext(server, new MasterHandler(engine, fileToId, "http://" + host + ":" + port));
				addContext(server, new AnswerHandler(engine));
				addContext(server, new FileHandler(zipFile, idToFile));

				// server.createContext("", );
				// server.createContext("/file/", );
			}

			if (handleUser) {
				addContext(server, new UserHandler(engine));
				// server.createContext("/", );
			}
			server.start();

			System.out.println(port);

		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static String readHtml(String path) {
		StringBuilder builder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Handler.class.getClassLoader().getResourceAsStream(path), charset))) {
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

	public static Writer sendWriter(HttpExchange exchange, int code) throws IOException {
		Headers headers = exchange.getResponseHeaders();
		headers.set("Content-Type", "text/html; charset=" + charset.name());
		headers.set("Access-Control-Allow-Origin", "*");
		exchange.sendResponseHeaders(code, 0);
		return new OutputStreamWriter(exchange.getResponseBody(), charset);
	}

	public static void sendString(HttpExchange exchange, int code, String string) throws IOException {
		byte[] buffer = string.getBytes(charset);
		exchange.sendResponseHeaders(code, buffer.length);
		try (OutputStream outputStream = exchange.getResponseBody()) {
			outputStream.write(buffer);
		}
	}

}
