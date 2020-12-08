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
		if (similarity > 100)
			throw new IllegalArgumentException("The similarity cannot be longer than 100 percent");

		System.out.println("Comparing sequencings");
		Map<Sequencing, Double> sequencingsDown100 = new HashMap<>();
		Map<String, String> sequencingsUnits = new HashMap<>();
		compareSequencings(mountSequencings, similarity, sequencingsDown100, sequencingsUnits);

		System.out.println("Enter the path for your result file: (no necessary name file)");
		sc.nextLine();
		String pathForYourResult = sc.nextLine();

		writerResult(sequencingsDown100, sequencingsUnits, pathForYourResult + "/result.txt");
	}

	public static void compareSequencings(Map<String, String> mountSequencings, double similarity,
			Map<Sequencing, Double> sequencingsWithSimilarity, Map<String, String> sequencingsUnits) {

		// compare suffix with preffix
		for (String key : mountSequencings.keySet()) {
			int count = 0;
			String sequencing = mountSequencings.get(key);
			SequencingUnit sequencingAux = new SequencingUnit();

			for (String otherKey : mountSequencings.keySet()) {
				String otherSequencing = mountSequencings.get(otherKey);
				if (otherSequencing != sequencing) {
					int suffixSize = Math.round(sequencing.length() * 30 / 100);

					// verifing if the suffix is longer than other sequencing compared
					if (suffixSize >= otherSequencing.length()) {
						suffixSize = Math.round(otherSequencing.length() * 30 / 100);
					}

					String suffixOfSequencing = sequencing.substring(sequencing.length() - suffixSize,
							sequencing.length());
					String preffixOfOtherSequencing = otherSequencing.substring(0, suffixSize);

					double distanceBetweenSequencings = distanceOfLevenshtein(suffixOfSequencing,
							preffixOfOtherSequencing);
					double similarityBetweenSequencingsInPercent = 100.0
							- (double) (distanceBetweenSequencings / sequencing.length() * 100.0);

					if (similarityBetweenSequencingsInPercent >= similarity) {
						if (similarityBetweenSequencingsInPercent > sequencingAux.getSimilarity()) {
							String otherSequencingWithoutPreffix = otherSequencing.substring(suffixSize,
									otherSequencing.length());
							String sequencingWithoutSuffix = sequencing.substring(0, sequencing.length() - suffixSize);
							sequencingAux.setHeader(key);
							sequencingAux.setSequencing(sequencing);
							sequencingAux.setSimilarity(similarityBetweenSequencingsInPercent);
							

							if (sequencingAux.getSimilarity() == 100) {
								sequencingAux.setHeaderConcatenated(key + "Unit" + otherKey);
								sequencingAux.setSequencingConcatenated(
										sequencingWithoutSuffix + suffixOfSequencing + otherSequencingWithoutPreffix);
							}

							else if (sequencingAux.getSimilarity() < 100
									&& sequencingAux.getSimilarity() >= similarity) {
								sequencingAux.setHeaderConcatenated(key + "Concatenated" + otherKey);
								String conc = concatenateStringsWithoutRedundances(suffixOfSequencing,
										preffixOfOtherSequencing);
								sequencingAux.setSequencingConcatenated(
										sequencingWithoutSuffix + conc + otherSequencingWithoutPreffix);
							}
						}
					}
				}
				count++;
				if (count == mountSequencings.size() && sequencingAux.getSimilarity() >= similarity
						&& !sequencingAux.getHeader().equals("")) {
					sequencingsWithSimilarity.put(
							new Sequencing(sequencingAux.getHeader(), sequencingAux.getSequencing()),
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

					// Verifying if the prefix is longer than other sequencing compared
					if (preffixSize >= otherSequencing.length()) {
						preffixSize = Math.round(otherSequencing.length() * 30 / 100);
					}

					String preffixOfSequencing = sequencing.substring(0, preffixSize);
					String suffixOfOtherSequencing = otherSequencing.substring(otherSequencing.length() - preffixSize,
							otherSequencing.length());
					double distanceBetweenSequencings = distanceOfLevenshtein(preffixOfSequencing,
							suffixOfOtherSequencing);
					double similarityBetweenSequencingsInPercent = 100.0
							- (double) (distanceBetweenSequencings / sequencing.length() * 100.0);

					if (similarityBetweenSequencingsInPercent >= similarity) {
						if (similarityBetweenSequencingsInPercent > sequencingAux.getSimilarity()) {
							String sequencingWithoutPreffix = sequencing.substring(preffixSize, sequencing.length());
							String otherSequencingWithoutSuffix = otherSequencing.substring(0,
									sequencing.length() - preffixSize);
							sequencingAux.setHeader(key);
							sequencingAux.setSequencing(sequencing);
							sequencingAux.setSimilarity(similarityBetweenSequencingsInPercent);
							

							if (sequencingAux.getSimilarity() == 100) {
								sequencingAux.setHeaderConcatenated(key + "Unit" + otherKey);
								sequencingAux.setSequencingConcatenated(otherSequencingWithoutSuffix + sequencing);
							} else if (sequencingAux.getSimilarity() < 100
									&& sequencingAux.getSimilarity() >= similarity) {
								sequencingAux.setHeaderConcatenated(key + "Concatenated" + otherKey);
								String con = concatenateStringsWithoutRedundances(preffixOfSequencing,
										suffixOfOtherSequencing);
								sequencingAux.setSequencingConcatenated(
										otherSequencingWithoutSuffix + con + sequencingWithoutPreffix);
							}
						}
					}
				}
				count++;
				if (count == mountSequencings.size() && sequencingAux.getSimilarity() >= similarity
						&& !sequencingAux.getHeader().equals("")) {
					sequencingsWithSimilarity.put(
							new Sequencing(sequencingAux.getHeader(), sequencingAux.getSequencing()),
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

	public static void writerResult(Map<Sequencing, Double> sequencingsWithSimilarity, Map<String, String> sequencingsUnits,
			String path) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
			for (Sequencing key : sequencingsWithSimilarity.keySet()) {
				bw.write(key.getHeader());
				bw.newLine();
				bw.write(key.getSequencing());
				bw.newLine();
				bw.write(Double.toString(sequencingsWithSimilarity.get(key)));
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

	public static String concatenateStringsWithoutRedundances(String s1, String s2) {
		StringBuilder result = new StringBuilder();
		StringBuilder whenEquals = new StringBuilder();
		StringBuilder whenThereIsDifferentOne = new StringBuilder();
		StringBuilder whenThereIsDifferentTwo = new StringBuilder();
		for (int i = 0; i < s1.length(); i++) {
			if (s1.charAt(i) == s2.charAt(i)) {
				if (whenThereIsDifferentOne.length() != 0)
					result.append(whenThereIsDifferentOne.append(whenThereIsDifferentTwo));
				whenThereIsDifferentOne.delete(0, whenThereIsDifferentOne.length());
				whenThereIsDifferentTwo.delete(0, whenThereIsDifferentTwo.length());
				whenEquals.append(s1.charAt(i));
			} else {
				if (whenEquals.length() != 0) {
					result.append(whenEquals);
				}
				whenEquals.delete(0, whenEquals.length());
				whenThereIsDifferentOne.append(s1.charAt(i));
				whenThereIsDifferentTwo.append(s2.charAt(i));
			}
		}
		result.append(whenEquals);
		result.append(whenThereIsDifferentOne);
		result.append(whenThereIsDifferentTwo);
		return result.toString();
	}
}
