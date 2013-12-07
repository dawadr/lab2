package message.response;

/**
 * A Response to a refused Request.
 * @author Alex
 *
 */
public class RefuseResponse extends MessageResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7620000457409546404L;

	public RefuseResponse() {
		super("Requst refused. You are not logged in.");
	}
	
	public RefuseResponse(String message) {
		super(message);
	}

}
