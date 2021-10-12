
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import jeopardy.Game;

public class GenerateFolders {

	public static void main(String[] args) throws Exception {
		Game game = new Game("data2", "game.tsv");
		System.out.println(game.rounds);
		System.out.println(game.rows);
		System.out.println(game.columns);

		// System.out.println(Arrays.deepToString(game.score));
		// System.out.println(Arrays.deepToString(game.name));

		Random random = new Random(42);

		for (int round = 0; round < game.rounds; round++) {
			for (int row = 0; row < game.rows; row++) {
				for (int column = 0; column < game.columns; column++) {
					String prefix = game.folder(round, row, column);

					int id = 0;
					for (String s : new String[] { "question", "answer" }) {
						int cnt = random.nextInt(3) + 1;
						for (int i = 0; i < cnt; i++) {

							String name = prefix + (++id) + s + ".png";
							BufferedImage img = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
							Graphics2D g2d = img.createGraphics();
							g2d.setPaint(Color.BLACK);
							g2d.setFont(new Font("Serif", Font.BOLD, 20));
							g2d.drawString(name, 0, 120);
							g2d.dispose();

							ImageIO.write(img, "png", new File(name));

						}
					}

				}
			}
		}

		// HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		// server.createContext("/", new MyHandler());
		// server.setExecutor(Executors.newFixedThreadPool(4));
		// server.start();
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

			// System.out.println(t.getRequestMethod());
			// System.out.println(t.getHttpContext());
			// System.out.println(t.getPrincipal());
			// System.out.println(t.getRequestURI());
			// System.out.println(t.getRemoteAddress());

		}
	}

}