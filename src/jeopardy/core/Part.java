package jeopardy.core;

import java.util.Map;
import java.util.Objects;

public class Part {

	public final String name;
	public final Map<String, String> map;

	public Part(String name, Map<String, String> map) {
		this.name = Objects.requireNonNull(name);
		this.map = Objects.requireNonNull(map);
		for (String value : map.values()) {
			Objects.requireNonNull(value);
		}
	}

}
