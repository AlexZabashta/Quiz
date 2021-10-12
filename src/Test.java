
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import jeopardy.Game;

public class Test {

	public static void main(String[] args) throws Exception {
		Game game = new Game("data", "game.tsv");
		System.out.println(game.rounds);
		System.out.println(game.rows);
		System.out.println(game.columns);

		System.out.println(Arrays.deepToString(game.questions));
		System.out.println(Arrays.deepToString(game.name));

		Random random = new Random(42);

		//		for (int round = 0; round < game.rounds; round++) {
		//			for (int row = 0; row < game.rows; row++) {
		//				for (int column = 0; column < game.columns; column++) {
		//					for (int stage = 0; stage < game.STAGE.length; stage++) {
		//
		//					}
		//					for (String stage : Game.STAGE) {
		//						int parts = random.nextInt(3) + 1;
		//						for (int p = 0; p < parts; p++) {
		//							File folder = new File(String.format(stage, args));
		//						}
		//					}
		//				}
		//			}
		//		}

		//		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		//		server.createContext("/", new MyHandler());
		//		server.setExecutor(Executors.newFixedThreadPool(4));
		//		server.start();
	}

	static class MyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			String response = "This is the response";

			t.getResponseHeaders().add("Set-Cookie", "abc=123");
			t.sendResponseHeaders(200, response.length());

			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();

			Headers reqHeaders = t.getRequestHeaders();
			List<String> cookies = reqHeaders.get("Cookie");
			for (String cookie : cookies) {
				System.out.println(cookie);
			}

			//			System.out.println(t.getRequestMethod());
			//			System.out.println(t.getHttpContext());
			//			System.out.println(t.getPrincipal());
			//			System.out.println(t.getRequestURI());
			//			System.out.println(t.getRemoteAddress());

		}
	}

}