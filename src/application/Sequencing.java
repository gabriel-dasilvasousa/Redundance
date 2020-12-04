package application;

public class Sequencing {
	protected String header;
	protected String sequencing;
	
	public Sequencing() {
		sequencing = "";
		header = "";
	}
	
	public Sequencing(String header, String sequencing) {
		this.header = header;
		this.sequencing = sequencing;
	}
	
	public String getSequencing() {
		return sequencing;
	}
	
	public void setSequencing(String sequencing) {
		this.sequencing = sequencing;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result + ((sequencing == null) ? 0 : sequencing.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sequencing other = (Sequencing) obj;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		if (sequencing == null) {
			if (other.sequencing != null)
				return false;
		} else if (!sequencing.equals(other.sequencing))
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
		return builder.toString();
	}
}
