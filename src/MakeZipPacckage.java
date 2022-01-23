
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.simple.JSONObject;

import format.Folder;
import format.Json;
import jeopardy.core.Game;
import jeopardy.core.Part;
import jeopardy.core.Question;
import jeopardy.core.Round;
import jeopardy.core.Topic;

public class MakeZipPacckage {

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

		System.out.println("-m [media]");
		System.out.println("\tPath to root folder with additional media files.");
		System.out.println("\tOptional. The default is the parent of the configuration file.");
		System.out.println();

		System.out.println("-e [encoding]");
		System.out.println("\tInput encoding. Optional. Default is UTF-8.");
		System.out.println();

		System.out.println("-o [output]");
		System.out.println("\tPath to output file in ZIP format. Optional. Default is game.zip");
		System.out.println();

		System.exit(1);
	}

	static void addFile(String name, InputStream stream, ZipOutputStream zipOutputStream) throws IOException {
		zipOutputStream.putNextEntry(new ZipEntry(name));

		byte[] bufer = new byte[4096];
		int len;

		while ((len = stream.read(bufer)) > 0) {
			zipOutputStream.write(bufer, 0, len);
		}

		zipOutputStream.closeEntry();
	}

	public static void main(String[] args) {
		if (args.length % 2 != 0 || args.length == 0) {
			printHelpAndExit();
		}

		try {
			File config = null;
			File media = null;
			File output = new File("game.zip");
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
					output = new File(value);
					continue;
				}

				if (key.startsWith("-m")) {
					media = getIfExists(value);
					continue;
				}
				System.err.println("Wrong argument" + key);
				printHelpAndExit();
			}

			if (config == null) {
				throw new IllegalArgumentException("The config is missing");
			}

			String prefix;
			if (media == null) {
				prefix = new File(config.getCanonicalPath()).getParent();
			} else {
				prefix = media.getCanonicalPath();

			}

			Game game = Folder.game(config, charset);

			try (ZipOutputStream zs = new ZipOutputStream(new FileOutputStream(output), StandardCharsets.UTF_8)) {
				JSONObject jsonConfig = Json.game(game);
				InputStream streamConfig = new ByteArrayInputStream(jsonConfig.toString().getBytes(StandardCharsets.UTF_8));
				addFile("config.json", streamConfig, zs);

				Set<String> names = new HashSet<>();
				names.add("config.json");

				for (Round round : game.rounds) {
					for (Topic topic : round.topics) {
						for (Question question : topic.questions) {
							for (Part part : question.parts) {
								for (Entry<String, String> entry : part.map.entrySet()) {
									String type = entry.getKey();
									if (type.equals("iframe") || type.equals("text") || type.equals("youtube")) {
										continue;
									}

									String name = entry.getValue();
									if (names.add(name)) {
										try (InputStream stream = new FileInputStream(prefix + File.separator + name.replace('/', File.separatorChar))) {
											addFile(name, stream, zs);
										}
									}
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
