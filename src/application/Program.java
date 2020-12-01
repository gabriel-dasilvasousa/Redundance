package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Program {

	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the complete path for your fasta file: ");
		String path = sc.nextLine();

		Map<String, String> mountSequencings = new HashMap<String, String>();

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			System.out.println("Reading the fasta file");
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			String header = "";
			while (line != null) {
				if (line.startsWith(">")) {
					sb.setLength(0);
					header = line;
					line = br.readLine();
				} else {
					while (line != null && !line.startsWith(">")) {
						sb.append(line);
						line = br.readLine();
					}
				}
				if(sb.length() != 0) {
					mountSequencings.put(header, sb.toString());
				}
			}
			System.out.println("Read complete");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("Comparing sequencings");
		Map<String, String> scoresOfSequencings = compareSequencings(mountSequencings);
		
		System.out.println("Enter the path for your result file: (no necessary name file)");
		String pathForYourResult = sc.nextLine();
		
		writerResult(scoresOfSequencings, pathForYourResult+"/result.txt");
	}

	public static Map<String, String> compareSequencings(Map<String, String> mountSequencings) {

		Map<String, String> scores = new HashMap<>();
		
		for (String key : mountSequencings.keySet()) {
			String sequencing = mountSequencings.get(key);
			int preffixSize = sequencing.length() * 30 / 100;
			String preffixOfSequencing = sequencing.substring(0, preffixSize);

			for (String otherKey : mountSequencings.keySet()) {
				String otherSequencing = mountSequencings.get(otherKey);
				if (otherSequencing != sequencing) {
					int suffixSize = otherSequencing.length() * 30 / 100;
					String suffixOfOtherSequencing = otherSequencing
							.substring(otherSequencing.length() - suffixSize, otherSequencing.length());
					double distanceBetweenSequencings = distanceOfLevenshtein(preffixOfSequencing, suffixOfOtherSequencing);

					if (100.0 - (double) (distanceBetweenSequencings / otherSequencing.length() * 100.0) == 100) {
						String sequencingWithoutPreffix = sequencing.substring(preffixSize, sequencing.length());
						scores.put(key + otherKey,
								otherSequencing + sequencingWithoutPreffix);
					}
				}
			}
		}
		return scores;
	}

	public static int distanceOfLevenshtein(String a, String b) {
		a = a.toLowerCase();
		b = b.toLowerCase();
		// i == 0
		int[] costs = new int[b.length() + 1];
		for (int j = 0; j < costs.length; j++)
			costs[j] = j;
		for (int i = 1; i <= a.length(); i++) {
			// j == 0; nw = lev(i - 1, j)
			costs[0] = i;
			int nw = i - 1;
			for (int j = 1; j <= b.length(); j++) {
				int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
						a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
				nw = costs[j];
				costs[j] = cj;
			}
		}
		return costs[b.length()];
	}
	
	public static void writerResult(Map<String, String> mountSequencings, String path) {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(path))){
			for(String key : mountSequencings.keySet()) {
				bw.write(key);
				bw.newLine();
				bw.write(mountSequencings.get(key));
				bw.newLine();
			}
			System.out.println("Write complete, file available at: " + path);
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
