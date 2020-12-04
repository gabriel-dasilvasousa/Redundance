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

		Map<String, String> mountSequencings = new HashMap<>();

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
				if (sb.length() != 0) {
					mountSequencings.put(header, sb.toString());
				}
			}
			System.out.println("Read complete");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		System.out.println("Enter the minimum similarity between sequencings");
		double similarity = sc.nextDouble();

		System.out.println("Comparing sequencings");
		Map<Sequencing, Double> scoresWith100 = new HashMap<>();
		Map<Sequencing, Double> sequencingsDown100 = new HashMap<>();
		Map<String, String> sequencingsUnits = new HashMap<>();
		compareSequencings(mountSequencings, similarity, scoresWith100, sequencingsDown100, sequencingsUnits);

		System.out.println("Enter the path for your result file: (no necessary name file)");
		sc.nextLine();
		String pathForYourResult = sc.nextLine();

		writerResult(scoresWith100, sequencingsDown100, sequencingsUnits, pathForYourResult + "/result.txt");
	}

	public static void compareSequencings(Map<String, String> mountSequencings, double similarity,
			Map<Sequencing, Double> scoresWith100, Map<Sequencing, Double> sequencingsDown100,
			Map<String, String> sequencingsUnits) {

		// compare suffix with preffix
		for (String key : mountSequencings.keySet()) {
			int count = 0;
			String sequencing = mountSequencings.get(key);
			SequencingUnit sequencingAux = new SequencingUnit();

			for (String otherKey : mountSequencings.keySet()) {
				String otherSequencing = mountSequencings.get(otherKey);
				if (otherSequencing != sequencing) {
					int suffixSize = Math.round(sequencing.length() * 30 / 100);
					int preffixSize = Math.round(otherSequencing.length() * 30 / 100);

					// verifing if the preffix or suffix are longer than other sequencing compared
					if (preffixSize >= sequencing.length()) {
						preffixSize = suffixSize;
					}
					if (suffixSize >= otherSequencing.length()) {
						suffixSize = preffixSize;
					}

					String suffixOfSequencing = sequencing.substring(sequencing.length() - suffixSize,
							sequencing.length());
					String preffixOfOtherSequencing = otherSequencing.substring(0, preffixSize);

					double distanceBetweenSequencings = distanceOfLevenshtein(suffixOfSequencing,
							preffixOfOtherSequencing);
					double similarityBetweenSequencingsInPercent = 100.0
							- (double) (distanceBetweenSequencings / sequencing.length() * 100.0);

					if (similarityBetweenSequencingsInPercent == 100) {
						String otherSequencingWithoutPreffix = otherSequencing.substring(preffixSize,
								otherSequencing.length());
						SequencingUnit newSequencingUnit = new SequencingUnit();
						newSequencingUnit.setHeader(key + "Unit" + otherKey);
						newSequencingUnit.setSequencing(sequencing + otherSequencingWithoutPreffix);
						newSequencingUnit.setSimilarity(100.0);
						scoresWith100.put(newSequencingUnit, newSequencingUnit.getSimilarity());
						sequencingAux.setHeader("");
						sequencingAux.setSimilarity(100.0);

					} else if (similarityBetweenSequencingsInPercent >= similarity) {
						if (similarityBetweenSequencingsInPercent > sequencingAux.getSimilarity()) {
							String otherSequencingWithoutPreffix = otherSequencing.substring(preffixSize,
									otherSequencing.length());
							String sequencingWithoutSuffix = sequencing.substring(0, sequencing.length() - suffixSize);
							sequencingAux.setHeader(key);
							sequencingAux.setSequencing(sequencing);
							sequencingAux.setSimilarity(similarityBetweenSequencingsInPercent);
							sequencingAux.setHeaderConcatenated(key + "Unit" + otherKey);
							// How to concatenate when the similarity < 100?
							String conc = concatenateStringsWithoutRedundances(suffixOfSequencing,
									preffixOfOtherSequencing);
							sequencingAux.setSequencingConcatenated(
									sequencingWithoutSuffix + conc + otherSequencingWithoutPreffix);
						}
					}
				}
				count++;
				if (count == mountSequencings.size() && sequencingAux.getSimilarity() > similarity && !sequencingAux.getHeader().equals("")) {
					sequencingsDown100.put(new Sequencing(sequencingAux.getHeader(), sequencingAux.getSequencing()),
							sequencingAux.getSimilarity());
					sequencingsUnits.put(sequencingAux.getHeaderConcatenated(),
							sequencingAux.getSequencingConcatenated());
				}
			}
		}

		// compare prefix with suffix
		for (String key : mountSequencings.keySet()) {
			int count = 0;
			String sequencing = mountSequencings.get(key);
			SequencingUnit sequencingAux = new SequencingUnit();

			for (String otherKey : mountSequencings.keySet()) {
				String otherSequencing = mountSequencings.get(otherKey);
				if (otherSequencing != sequencing) {
					int preffixSize = Math.round(sequencing.length() * 30 / 100);
					int suffixSize = Math.round(otherSequencing.length() * 30 / 100);

					// Verifying if the prefix or suffix are longer than other sequencing compared
					if (preffixSize >= otherSequencing.length()) {
						preffixSize = suffixSize;
					}
					if (suffixSize >= sequencing.length()) {
						suffixSize = preffixSize;
					}

					String preffixOfSequencing = sequencing.substring(0, preffixSize);
					String suffixOfOtherSequencing = otherSequencing.substring(otherSequencing.length() - suffixSize,
							otherSequencing.length());
					double distanceBetweenSequencings = distanceOfLevenshtein(preffixOfSequencing,
							suffixOfOtherSequencing);
					double similarityBetweenSequencingsInPercent = 100.0
							- (double) (distanceBetweenSequencings / sequencing.length() * 100.0);

					if (similarityBetweenSequencingsInPercent == 100) {
						String sequencingWithoutPreffix = sequencing.substring(preffixSize, sequencing.length());
						SequencingUnit newSequencingUnit = new SequencingUnit();
						newSequencingUnit.setHeader(key + "Unit" + otherKey);
						newSequencingUnit.setSequencing(otherSequencing + sequencingWithoutPreffix);
						newSequencingUnit.setSimilarity(100.0);
						scoresWith100.put(newSequencingUnit, newSequencingUnit.getSimilarity());
						sequencingAux.setHeader("");
						sequencingAux.setSimilarity(100.0);
					} else if (similarityBetweenSequencingsInPercent >= similarity) {
						if (similarityBetweenSequencingsInPercent > sequencingAux.getSimilarity()) {
							String sequencingWithoutPreffix = sequencing.substring(preffixSize, sequencing.length());
							String otherSequencingWithoutSuffix = otherSequencing.substring(0,
									sequencing.length() - suffixSize);
							sequencingAux.setHeader(key);
							sequencingAux.setSequencing(sequencing);
							sequencingAux.setSimilarity(similarityBetweenSequencingsInPercent);
							// How to concatenate when the similarity < 100?
							sequencingAux.setHeaderConcatenated(key + "Unit" + otherKey);
							String con = concatenateStringsWithoutRedundances(preffixOfSequencing, suffixOfOtherSequencing);
							sequencingAux.setSequencingConcatenated(otherSequencingWithoutSuffix + con + sequencingWithoutPreffix);
						}
					}
				}
				count++;
				if (count == mountSequencings.size() && sequencingAux.getSimilarity() > similarity && !sequencingAux.getHeader().equals("")) {
					sequencingsDown100.put(new Sequencing(sequencingAux.getHeader(), sequencingAux.getSequencing()),
							sequencingAux.getSimilarity());
					sequencingsUnits.put(sequencingAux.getHeaderConcatenated(),
							sequencingAux.getSequencingConcatenated());
				}
			}
		}
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

	public static void writerResult(Map<Sequencing, Double> scoresWith100, Map<Sequencing,Double> sequencingsDown100, Map<String, String> sequencingsUnits, String path) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
			for (Sequencing key : scoresWith100.keySet()) {
				bw.write(key.getHeader());
				bw.newLine();
				bw.write(key.getSequencing());
				bw.newLine();
				bw.write(Double.toString(scoresWith100.get(key)));
				bw.newLine();
			}
			for (Sequencing key : sequencingsDown100.keySet()) {
				bw.write(key.getHeader());
				bw.newLine();
				bw.write(key.getSequencing());
				bw.newLine();
				bw.write(Double.toString(sequencingsDown100.get(key)));
				bw.newLine();
			}
			for (String key : sequencingsUnits.keySet()) {
				bw.write(key);
				bw.newLine();
				bw.write(sequencingsUnits.get(key));
				bw.newLine();
			}
			System.out.println("Write complete, file available at: " + path);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static String concatenateStringsWithoutRedundances(String a, String b) {
		StringBuilder sb = new StringBuilder();
		System.out.println(a);
		System.out.println(b);
		for (int i = 0; i < a.length(); i++) {
			if (a.charAt(i) == b.charAt(i)) {
				sb.append(a.charAt(i));
			} else {
				int j = i;
				while (a.charAt(j) != b.charAt(j)) {
					sb.append(a.charAt(j));
					j++;
				}
				while (j != i) {
					sb.append(b.charAt(i));
					i++;
				}
				i--;
			}
		}
		return sb.toString();
	}
}
