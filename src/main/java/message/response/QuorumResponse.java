package message.response;

import message.Response;

public class QuorumResponse implements Response {
	private static final long serialVersionUID = 3134831925072300109L;

	public enum QuorumType {
		READ("Read-Quorum"),
		WRITE("Write-Quorum");

		String type;

		QuorumType(String type) {
			this.type = type;
		}
	}

	private final QuorumType type;
	private final int quorum;

	public QuorumResponse(QuorumType type, int quorum) {
		this.type = type;
		this.quorum = quorum;
	}

	public QuorumType getType() {
		return type;
	}

	public int getQuorum() {
		return quorum;
	}

	@Override
	public String toString() {
		return getType().type + " is set to " + this.quorum + ".";
	}
}
