package format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jeopardy.core.Game;
import jeopardy.core.Part;
import jeopardy.core.Question;
import jeopardy.core.Round;
import jeopardy.core.Topic;

public class Json {

	static final JSONParser parser = new JSONParser();

	public static JSONObject read(File file, Charset charset) throws IOException, ParseException {
		try (Reader reader = new InputStreamReader(new FileInputStream(file), charset)) {
			return (JSONObject) parser.parse(reader);
		}
	}

	public static JSONObject read(Reader reader) throws IOException, ParseException {
		return (JSONObject) parser.parse(reader);
	}

	@SuppressWarnings("unchecked")
	public static JSONObject game(Game game) {
		JSONArray jsonArray = new JSONArray();
		for (Round round : game.rounds) {
			jsonArray.add(round(round));
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", game.name);
		jsonObject.put("rounds", jsonArray);
		return jsonObject;
	}

	public static Game game(JSONObject jsonObject) {
		String name = (String) jsonObject.get("name");
		Round[] rounds = rounds((JSONArray) jsonObject.get("rounds"));
		return new Game(name, rounds);
	}

	@SuppressWarnings("unchecked")
	public static Part part(JSONObject jsonObject) {
		Map<String, String> map = new HashMap<>(jsonObject);

		for (Entry<String, String> e : map.entrySet()) {
			if ((e.getKey() instanceof String) && (e.getValue() instanceof String)) {
				continue;
			}
			throw new ClassCastException("not String");
		}

		String name = map.remove("name");
		return new Part(name, map);
	}

	@SuppressWarnings("unchecked")
	public static JSONObject part(Part part) {
		JSONObject jsonObject = new JSONObject(part.map);
		jsonObject.put("name", part.name);
		return jsonObject;
	}

	public static Part[] parts(JSONArray jsonArray) {
		int n = jsonArray.size();
		Part[] parts = new Part[n];
		for (int i = 0; i < n; i++) {
			parts[i] = part((JSONObject) jsonArray.get(i));
		}
		return parts;
	}

	public static Question question(JSONObject jsonObject) {
		String name = (String) jsonObject.get("name");
		double score = (double) jsonObject.get("score");
		Part[] parts = parts((JSONArray) jsonObject.get("parts"));
		return new Question(name, score, parts);
	}

	@SuppressWarnings("unchecked")
	public static JSONObject question(Question question) {
		JSONArray jsonArray = new JSONArray();
		for (Part part : question.parts) {
			jsonArray.add(part(part));
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", question.name);
		jsonObject.put("score", question.score);
		jsonObject.put("parts", jsonArray);
		return jsonObject;
	}

	public static Question[] questions(JSONArray jsonArray) {
		int n = jsonArray.size();
		Question[] questions = new Question[n];
		for (int i = 0; i < n; i++) {
			questions[i] = question((JSONObject) jsonArray.get(i));
		}
		return questions;
	}

	public static Round round(JSONObject jsonObject) {
		String name = (String) jsonObject.get("name");
		Topic[] topics = topics((JSONArray) jsonObject.get("topics"));
		return new Round(name, topics);
	}

	@SuppressWarnings("unchecked")
	public static JSONObject round(Round round) {
		JSONArray jsonArray = new JSONArray();
		for (Topic topic : round.topics) {
			jsonArray.add(topic(topic));
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", round.name);
		jsonObject.put("topics", jsonArray);
		return jsonObject;
	}

	public static Round[] rounds(JSONArray jsonArray) {
		int n = jsonArray.size();
		Round[] rounds = new Round[n];
		for (int i = 0; i < n; i++) {
			rounds[i] = round((JSONObject) jsonArray.get(i));
		}
		return rounds;
	}

	public static Topic topic(JSONObject jsonObject) {
		String name = (String) jsonObject.get("name");
		Question[] questions = questions((JSONArray) jsonObject.get("questions"));
		return new Topic(name, questions);
	}

	@SuppressWarnings("unchecked")
	public static JSONObject topic(Topic topic) {
		JSONArray jsonArray = new JSONArray();
		for (Question question : topic.questions) {
			jsonArray.add(question(question));
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", topic.name);
		jsonObject.put("questions", jsonArray);
		return jsonObject;
	}

	public static Topic[] topics(JSONArray jsonArray) {
		int n = jsonArray.size();
		Topic[] topics = new Topic[n];
		for (int i = 0; i < n; i++) {
			topics[i] = topic((JSONObject) jsonArray.get(i));
		}
		return topics;
	}

}
