package message.response;

import message.Response;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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

	private final Map<String, Integer> downloads;

	public TopThreeDownloadsResponse(Map<String, Integer> downloads) {
		this.downloads = Collections.unmodifiableMap(new LinkedHashMap<String, Integer>(downloads));
	}

	@Override
	public String toString() {
		if (downloads.isEmpty()) {
			return "No files downloaded yet.";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Top Three Downloads:").append("\n");
		Iterator<Entry<String, Integer>> it = downloads.entrySet().iterator();
		int i = 1;
		
	    while (it.hasNext()) {
	        Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>)it.next();
	        sb.append(i + ". ").append(pairs.getKey()).append(pairs.getValue()).append("\n");
	        it.remove(); // avoids a ConcurrentModificationException
	        i++;
	    }
		return sb.toString();
	}
}
