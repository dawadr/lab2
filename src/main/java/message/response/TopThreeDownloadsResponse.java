package message.response;

import message.Response;

import java.util.List;

import model.File;

/**
 * Lists all files available on all file servers.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !list}<br/>
 * <b>Response:</b><br/>
 * {@code No files found.}<br/>
 * or<br/>
 * {@code &lt;filename1&gt;}<br/>
 * {@code &lt;filename2&gt;}<br/>
 * {@code ...}<br/>
 *
 * @see message.request.ListRequest
 */
public class TopThreeDownloadsResponse implements Response {
	private static final long serialVersionUID = -7319020129445822795L;

	private final List<File> downloads;

	public TopThreeDownloadsResponse(List<File> downloads) {
		this.downloads = downloads;
	}

	@Override
	public String toString() {
		if (downloads.isEmpty()) {
			return "No files downloaded yet.";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Top Three Downloads:").append("\n");
		
	    for(int i = 0; i < downloads.size(); i++) {
	        sb.append(i+1).append(". ");
	        sb.append(this.downloads.get(i).toString());
	        sb.append("\n");
	    }
		return sb.toString();
	}
}
