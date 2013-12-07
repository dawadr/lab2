package message.response;

/**
 * A Response to a failed Request.
 * @author Alex
 *
 */
public class FailedResponse extends MessageResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7620000457409546404L;
	private boolean ex;

	public FailedResponse(Exception e) {
		super(e.getMessage());
		ex = true;
	}

	public FailedResponse(String message) {
		super(message);
	}

	@Override
	public String toString() {
		if (ex) return "Request failed: " + getMessage();
		return getMessage();
	}

}
