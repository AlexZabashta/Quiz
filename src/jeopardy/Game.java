package jeopardy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Game {
	public final int rounds;
	public final int rows;
	public final int columns;
	public final String prefix;
	public final String[][] name;

	public final Question[][][] questions;

	public String folder(int round, int row, int column) {
		String s = File.separator;
		return String.format("%s%s%d%s%d%s%d%s", prefix, s, round + 1, s, row + 1, s, column + 1, s);
	}

	public Game(String prefix, String file) throws IOException {
		this.prefix = prefix;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
			String[] dim = reader.readLine().split("\t");
			rounds = Integer.parseInt(dim[0].trim());
			rows = Integer.parseInt(dim[1].trim());
			columns = Integer.parseInt(dim[2].trim());
			name = new String[rounds][rows];
			questions = new Question[rounds][rows][columns];

			for (int round = 0; round < rounds; round++) {

				for (int row = 0; row < rows; row++) {
					String[] rowInfo = reader.readLine().split("\t");
					name[round][row] = rowInfo[0];
					for (int column = 0; column < columns; column++) {
						double score = Double.parseDouble(rowInfo[column + 1].trim());

						File folder = new File(folder(round, row, column));
						if ((folder.exists() || folder.mkdirs()) == false) {
							throw new IOException("failed to create " + folder);
						}

						File[] files = folder.listFiles();
						String[] stages = new String[files.length];
						for (int i = 0; i < stages.length; i++) {
							stages[i] = files[i].getName();
						}

						Arrays.sort(stages);

						questions[round][row][column] = new Question(stages, score);
					}
				}
			}
		}
	}

}
