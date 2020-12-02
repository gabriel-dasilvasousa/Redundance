package application;

public class SequencingBuilder {
	
	private String header;
	private String sequencing;
	private Double similarity;
	
	public SequencingBuilder() {
		sequencing = "";
		similarity = 0.0;
		header = "";
	}
	
	public String getSequencing() {
		return sequencing;
	}
	
	public void setSequencing(String sequencing) {
		this.sequencing = sequencing;
	}
	
	public Double getSimilarity() {
		return similarity;
	}
	
	public void setSimilarity(Double similarity) {
		this.similarity = similarity;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}
}	
