package format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import jeopardy.core.Game;
import jeopardy.core.Part;
import jeopardy.core.Question;
import jeopardy.core.Round;
import jeopardy.core.Topic;

public class Tsv {

	static final CSVFormat format = CSVFormat.newFormat('\t').builder().setHeader().setSkipHeaderRecord(true).build();

	public static void appendHeader(Appendable appendable) throws IOException {
		appendable.append("round\ttopic\tquestion\tscore\tpart\ttype\tmedia");
		appendable.append('\n');
	}

	public static void append(Appendable appendable, Game game) throws IOException {
		for (Round round : game.rounds) {
			append(appendable, game, round);
		}
	}

	public static Game game(String name, List<CSVRecord> records) {

		String key = "round";

	//	records.sort(Comparator.comparing(record -> record.get(key)));
		List<Round> rounds = new ArrayList<>();

		int l = 0;
		int n = records.size();
		while (l < n) {
			String roundName = records.get(l).get(key);

			int r = l;
			while (r < n && records.get(r).get(key).equals(roundName)) {
				++r;
			}
			rounds.add(round(roundName, records, l, r));
			l = r;
		}
		return new Game(name, rounds.toArray(new Round[0]));
	}

	static Round round(String name, List<CSVRecord> records) {
		return round(name, records, 0, records.size());
	}

	static Round round(String name, List<CSVRecord> records, int from, int to) {
		String key = "topic";

	//	records.subList(from, to).sort(Comparator.comparing(record -> record.get(key)));
		List<Topic> topics = new ArrayList<>();

		int l = from;
		while (l < to) {
			String topicName = records.get(l).get(key);

			int r = l;
			while (r < to && records.get(r).get(key).equals(topicName)) {
				++r;
			}
			topics.add(topic(topicName, records, l, r));
			l = r;
		}

		return new Round(name, topics.toArray(new Topic[0]));
	}

	static Topic topic(String name, List<CSVRecord> records, int from, int to) {
		String key = "question";

	//	records.subList(from, to).sort(Comparator.comparing(record -> record.get(key)));
		List<Question> questions = new ArrayList<>();

		int l = from;
		while (l < to) {
			String questionName = records.get(l).get(key);

			int r = l;
			while (r < to && records.get(r).get(key).equals(questionName)) {
				++r;
			}
			questions.add(question(questionName, records, l, r));
			l = r;
		}

		return new Topic(name, questions.toArray(new Question[0]));
	}

	static Question question(String name, List<CSVRecord> records, int from, int to) {
		String key = "part";

	//	records.subList(from, to).sort(Comparator.comparing(record -> record.get(key)));

		List<Part> parts = new ArrayList<>();

		double score = 0;

		int l = from;
		while (l < to) {
			String partName = records.get(l).get(key);

			int r = l;
			while (r < to && records.get(r).get(key).equals(partName)) {
				++r;
			}

			for (int i = l; i < r; i++) {
				score = Math.max(score, Double.parseDouble(records.get(i).get("score")));
			}

			parts.add(part(partName, records, l, r));
			l = r;
		}

		return new Question(name, score, parts.toArray(new Part[0]));
	}

	private static Part part(String name, List<CSVRecord> records, int from, int to) {
		Map<String, String> media = new HashMap<>();
		for (int i = from; i < to; i++) {
			CSVRecord record = records.get(i);
			media.put(record.get("type"), record.get("media"));
		}
		return new Part(name, media);
	}

	static List<CSVRecord> read(File file, Charset charset) throws IOException {
		try (CSVParser parser = new CSVParser(new InputStreamReader(new FileInputStream(file), charset), format)) {
			return parser.getRecords();
		}
	}

	public static void append(Appendable appendable, Game game, Round round) throws IOException {
		for (Topic topic : round.topics) {
			append(appendable, game, round, topic);
		}
	}

	public static void append(Appendable appendable, Game game, Round round, Topic topic) throws IOException {
		for (Question question : topic.questions) {
			append(appendable, game, round, topic, question);
		}

	}

	public static void append(Appendable appendable, Game game, Round round, Topic topic, Question question) throws IOException {
		for (Part part : question.parts) {
			append(appendable, game, round, topic, question, part);
		}
	}

	public static void append(Appendable appendable, Game game, Round round, Topic topic, Question question, Part part) throws IOException {
		for (Entry<String, String> e : part.map.entrySet()) {
			appendable.append(round.name);
			appendable.append('\t');
			appendable.append(topic.name);
			appendable.append('\t');
			appendable.append(question.name);
			appendable.append('\t');
			appendable.append(Double.toString(question.score));
			appendable.append('\t');
			appendable.append(part.name);
			appendable.append('\t');
			appendable.append(e.getKey());
			appendable.append('\t');
			appendable.append(e.getValue());
			appendable.append('\n');
		}
	}

	public static Topic topic(String name, List<CSVRecord> records) {
		return topic(name, records, 0, records.size());
	}

	public static Question question(String name, List<CSVRecord> records) {
		return question(name, records, 0, records.size());
	}

	public static Part part(String name, List<CSVRecord> records) {
		return part(name, records, 0, records.size());
	}

}
