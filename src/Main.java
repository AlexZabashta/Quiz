import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import jeopardy.Game;
import jeopardy.JeopardyEngine;
import jeopardy.states.State;
import web.WebHandler;

public class Main {

	public static void main(String[] args) throws IOException {

		Game game = new Game("data", "game.tsv");

		// for (int round = 0; round < game.rounds; round++) {
		// for (int row = 0; row < game.rows; row++) {
		// System.out.println(game.name[round][row]);
		// for (int column = 0; column < game.columns; column++) {
		// }
		// }
		// }

		State state = State.read(game, "state.txt");

		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.setExecutor(Executors.newFixedThreadPool(4));
		JeopardyEngine engine = new JeopardyEngine(game, state);
		WebHandler webHandler = new WebHandler(server, engine, engine);
	}
}
