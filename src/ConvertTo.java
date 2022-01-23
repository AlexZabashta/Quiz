import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import format.Folder;
import format.Json;
import format.Tsv;
import jeopardy.core.Game;

public class ConvertTo {
	static File getIfExists(String path) throws FileNotFoundException {
		File file = new File(path);
		if (file.exists()) {
			return file;
		} else {
			throw new FileNotFoundException(path + " does not exist");
		}
	}

	static void printHelpAndExit() {
		System.out.println("-c,-i [config]");
		System.out.println("\tPath to game config file/folder.");
		System.out.println();

		System.out.println("-e [encoding]");
		System.out.println("\tInput encoding. Optional. Default is UTF-8.");
		System.out.println();

		System.out.println("-o [output]");
		System.out.println("\tPath to output file in JSON or TSV format. Optional. Default is game.json");
		System.out.println();

		System.exit(1);
	}

	public static void main(String[] args) {
		if (args.length % 2 != 0 || args.length == 0) {
			printHelpAndExit();
		}

		try {
			File config = null;
			String output = "game.json";
			Charset charset = StandardCharsets.UTF_8;

			for (int i = 0; i < args.length; i += 2) {
				String key = args[i].toLowerCase();
				String value = args[i + 1];
				if (key.startsWith("-c") || key.startsWith("-i")) {
					config = getIfExists(value);
					continue;
				}

				if (key.startsWith("-e")) {
					charset = Charset.forName(value);
					continue;
				}

				if (key.startsWith("-o")) {
					output = (value);
					continue;
				}

				System.err.println("Wrong argument" + key);
				printHelpAndExit();
			}

			if (config == null) {
				throw new IllegalArgumentException("The config is missing");
			}

			Game game = Folder.game(config, charset);

			if (output.toLowerCase().endsWith(".tsv")) {
				try (PrintWriter writer = new PrintWriter(output, charset)) {
					Tsv.appendHeader(writer);
					Tsv.append(writer, game);
				}
				return;
			}

			if (output.toLowerCase().endsWith(".json")) {
				try (PrintWriter writer = new PrintWriter(output, charset)) {
					Json.game(game).writeJSONString(writer);
				}
				return;
			}

			throw new IllegalArgumentException("Wrong output format " + output);

		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
