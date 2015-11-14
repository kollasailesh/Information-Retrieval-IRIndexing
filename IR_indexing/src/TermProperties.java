import java.util.HashMap;

public class TermProperties {
	int docFrequency;
	HashMap<String,Integer> postingFileInfo = new HashMap<String,Integer>();
	public int getDocFrequency() {
		return docFrequency;
	}
	public void setDocFrequency(int docFrequency) {
		this.docFrequency = docFrequency;
	}
	public HashMap<String, Integer> getPostingFileInfo() {
		return postingFileInfo;
	}
	public void setPostingFileInfo(HashMap<String, Integer> postingFileInfo) {
		this.postingFileInfo = postingFileInfo;
	}
}
