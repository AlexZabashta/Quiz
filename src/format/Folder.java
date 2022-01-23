package format;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.ParseException;

import jeopardy.core.Game;
import jeopardy.core.Part;
import jeopardy.core.Question;
import jeopardy.core.Round;
import jeopardy.core.Topic;

public class Folder {

	static final Map<String, String> extensions = new HashMap<>();
	static {
		extensions.put("html", "html");
		extensions.put("iframe", "iframe");
		extensions.put("txt", "text");
		extensions.put("text", "text");
		extensions.put("utf8", "text");

		extensions.put("apng", "image");
		extensions.put("gif", "image");
		extensions.put("jpg", "image");
		extensions.put("jpeg", "image");
		extensions.put("png", "image");
		extensions.put("svg", "image");
		extensions.put("webp", "image");

		extensions.put("aac", "audio");
		extensions.put("flac", "audio");
		extensions.put("m4a", "audio");
		extensions.put("mp3", "audio");
		extensions.put("ogg", "audio");
		extensions.put("ogga", "audio");
		extensions.put("opus", "audio");
		extensions.put("wav", "audio");

		extensions.put("webm", "video");
		extensions.put("mkv", "video");
		extensions.put("flv", "video");
		extensions.put("avi", "video");
		extensions.put("mp4", "video");
		extensions.put("mov", "video");
		extensions.put("mpeg", "video");

		extensions.put("youtube", "youtube");
	}

	public static String[] nameAndExt(String fullName) {
		int dot = fullName.lastIndexOf('.');
		if (dot < 0) {
			return new String[] { fullName, "" };
		} else {
			return new String[] { fullName.substring(0, dot), fullName.substring(dot + 1).toLowerCase() };
		}
	}

	public static Game game(File file, Charset charset) throws IOException, ParseException {
		if (file.isFile()) {
			String[] nameAndExt = nameAndExt(file.getName());
			if (nameAndExt[1].equals("tsv")) {
				return Tsv.game(nameAndExt[0], Tsv.read(file, charset));
			}
			if (nameAndExt[1].equals("json")) {
				return Json.game(Json.read(file, charset));
			}
			throw new IllegalArgumentException("Wrong file format " + file);
		}

		String prefix = file.getName() + "/";
		File[] files = file.listFiles();
		Arrays.sort(files);
		int n = files.length;
		Round[] rounds = new Round[n];

		for (int i = 0; i < n; i++) {
			rounds[i] = round(prefix, files[i], charset);
		}

		return new Game(file.getName(), rounds);
	}

	static Round round(String prefix, File file, Charset charset) throws IOException, ParseException {
		if (file.isFile()) {
			String[] nameAndExt = nameAndExt(file.getName());
			if (nameAndExt[1].equals("tsv")) {
				return Tsv.round(nameAndExt[0], Tsv.read(file, charset));
			}
			if (nameAndExt[1].equals("json")) {
				return Json.round(Json.read(file, charset));
			}
			throw new IllegalArgumentException("Wrong file format " + file);
		}

		prefix += file.getName() + "/";
		File[] files = file.listFiles();
		Arrays.sort(files);
		int n = files.length;
		Topic[] topics = new Topic[n];

		for (int i = 0; i < n; i++) {
			topics[i] = topic(prefix, files[i], charset);
		}

		return new Round(file.getName(), topics);
	}

	static Topic topic(String prefix, File file, Charset charset) throws IOException, ParseException {
		if (file.isFile()) {
			String[] nameAndExt = nameAndExt(file.getName());
			if (nameAndExt[1].equals("tsv")) {
				return Tsv.topic(nameAndExt[0], Tsv.read(file, charset));
			}
			if (nameAndExt[1].equals("json")) {
				return Json.topic(Json.read(file, charset));
			}
			throw new IllegalArgumentException("Wrong file format " + file);
		}
		prefix += file.getName() + "/";
		File[] files = file.listFiles();
		Arrays.sort(files);
		int n = files.length;
		Question[] questions = new Question[n];

		for (int i = 0; i < n; i++) {
			questions[i] = question(prefix, files[i], charset);
		}

		return new Topic(file.getName(), questions);
	}

	static Question question(String prefix, File file, Charset charset) throws IOException, ParseException {
		if (file.isFile()) {
			String[] nameAndExt = nameAndExt(file.getName());
			if (nameAndExt[1].equals("tsv")) {
				return Tsv.question(nameAndExt[0], Tsv.read(file, charset));
			}
			if (nameAndExt[1].equals("json")) {
				return Json.question(Json.read(file, charset));
			}
			throw new IllegalArgumentException("Wrong file format " + file);
		}
		prefix += file.getName() + "/";
		File[] files = file.listFiles();
		Arrays.sort(files);
		int n = files.length;
		Part[] parts = new Part[n];

		for (int i = 0; i < n; i++) {
			parts[i] = part(prefix, files[i], charset);
		}

		double score = Double.parseDouble(file.getName());
		return new Question(file.getName(), score, parts);
	}

	static Part part(String prefix, File file, Charset charset) throws IOException, ParseException {
		if (file.isFile()) {
			String[] nameAndExt = nameAndExt(file.getName());
			if (nameAndExt[1].equals("tsv")) {
				return Tsv.part(nameAndExt[0], Tsv.read(file, charset));
			}
			if (nameAndExt[1].equals("json")) {
				return Json.part(Json.read(file, charset));
			}

			String type = extensions.get(nameAndExt[1]);

			if (type == null) {
				throw new IllegalArgumentException("Wrong file format " + file);
			}

			String value = prefix + file.getName();

			if (type.equals("text")) {
				value = readTextFromFile(file, charset);
			}

			if (type.equals("youtube")) {
				value = readTextFromFile(file, charset).trim();
			}

			return new Part(nameAndExt[0], Map.of(type, value));

		} else {
			prefix += file.getName() + "/";
			Map<String, String> media = new HashMap<>();
			for (File mediaFile : file.listFiles()) {
				if (mediaFile.isFile()) {
					String[] nameAndExt = nameAndExt(mediaFile.getName());
					String type = nameAndExt[0];
					if (extensions.containsKey(nameAndExt[1]) == false) {
						throw new IllegalArgumentException("Wrong file format " + mediaFile);
					}
					String value = prefix + mediaFile.getName();
					if (type.equals("text")) {
						value = readTextFromFile(file, charset);
					}
					if (type.equals("youtube")) {
						value = readTextFromFile(file, charset).trim();
					}
					media.put(type, value);
				}
			}
			return new Part(file.getName(), media);
		}
	}

	static String readTextFromFile(File file, Charset charset) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), charset);
	}
}
