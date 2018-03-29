import java.util.List;

import de.lmu.cis.ocrd.Line;
import de.lmu.cis.pocoweb.Token;

public class TestLine implements Line {
	private final int id;
	private final String norm;
	
	TestLine(int id, String norm) {
		this.id = id;
		this.norm = norm;
	}
	@Override
	public int getLineId() {
		return id;
	}

	@Override
	public String getNormalized() {
		return norm;
	}

	@Override
	public Token getTokenAt(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Token> getTokens() {
		// TODO Auto-generated method stub
		return null;
	}

}
