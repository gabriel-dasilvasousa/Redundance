package application;

public class SequencingUnit extends Sequencing{
	
	private Double similarity;
	private String sequencingConcatenated;
	private String headerConcatenated;
	
	public SequencingUnit() {
		super();
		similarity = 0.0;
	}

	public Double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(Double similarity) {
		this.similarity = similarity;
	}

	public String getSequencingConcatenated() {
		return sequencingConcatenated;
	}

	public void setSequencingConcatenated(String sequencinConcatened) {
		this.sequencingConcatenated = sequencinConcatened;
	}

	public String getHeaderConcatenated() {
		return headerConcatenated;
	}

	public void setHeaderConcatenated(String headerConcatened) {
		this.headerConcatenated = headerConcatened;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((similarity == null) ? 0 : similarity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SequencingUnit other = (SequencingUnit) obj;
		if (similarity == null) {
			if (other.similarity != null)
				return false;
		} else if (!similarity.equals(other.similarity))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(header);
		builder.append("\n");
		builder.append(sequencing);
		builder.append("\n");
		builder.append(similarity);
		builder.append("\n");
		return builder.toString();
	}
	
	
}	
