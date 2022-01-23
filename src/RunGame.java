import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipFile;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import format.Json;
import jeopardy.JeopardyEngine;
import jeopardy.core.Game;
import jeopardy.states.State;

public class RunGame {
	static void printHelpAndExit() {
		System.out.println("Method call example:");
		System.out.println(RunGame.class.getName() + " handlers game [state]");
		System.out.println("\thandlers - handlers config JSON file");
		System.out.println("\tgame - game config ZIP file");
		System.out.println("\tstate - initial state (optional)");
		System.exit(1);
	}

	public static void main(String[] args) {
		if (args.length < 2 || args.length > 3) {
			printHelpAndExit();
		}

		try {

			Charset charset = StandardCharsets.UTF_8;

			JSONObject config = Json.read(new File(args[0]), charset);
			ZipFile zipFile = new ZipFile(args[1], charset);
			Game game;
			try (Reader reader = new InputStreamReader(zipFile.getInputStream(zipFile.getEntry("config.json")), charset)) {
				game = Json.game(Json.read(reader));
			}

			State state;
			if (args.length == 3) {
				state = State.read(game, new File(args[2]));
			} else {
				state = game.init();
			}

			JeopardyEngine engine = new JeopardyEngine(game, state);

			JSONArray handlers = (JSONArray) config.get("handlers");
			int n = handlers.size();
			for (int i = 0; i < n; i++) {
				JSONObject handler = (JSONObject) handlers.get(i);
				String className = (String) handler.get("class");
				Class<?> clazz = Class.forName(className);

				JSONObject parameters = (JSONObject) handler.get("parameters");
				Method method = clazz.getMethod("init", JeopardyEngine.class, ZipFile.class, JSONObject.class);
				method.invoke(null, engine, zipFile, parameters);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(1);
		}

	}
}
