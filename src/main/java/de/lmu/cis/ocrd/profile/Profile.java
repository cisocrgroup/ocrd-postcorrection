package de.lmu.cis.ocrd.profile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Profile extends HashMap<String, Candidates> {
	// private final HashMap<String, Candidates> data;

	protected Profile(HashMap<String, Candidates> data) {
		super(data);
		// this.data = data;
	}

	// for testing and mocking
	public static Profile empty() {
		return new Profile(new HashMap<>());
	}

	public static Profile read(Reader r) throws Exception {
		return fromJSON(IOUtils.toString(r));
	}

	public static Profile read(InputStream is) throws Exception {
		return fromJSON(IOUtils.toString(is, StandardCharsets.UTF_8));
	}

	public static Profile read(Path path) throws Exception {
		try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile())))) {
			return read(r);
		}
	}

	public static Profile read(String path) throws Exception {
		return read(Paths.get(path));
	}

	public static Profile fromJSON(String json) throws Exception {
		Gson gson = new Gson();
		Type map = new TypeToken<HashMap<String, Candidates>>() {
		}.getType();
		HashMap<String, Candidates> data = gson.fromJson(json, map);
		if (data == null) {
			throw new Exception("cannot parse profile from json");
		}
		return new Profile(toLowerCase(data))
				.removeEmptyCandidates()
				.sortCandidatesByVoteWeight()
				.adjustCandidatePostPatterns();
	}

	private static HashMap<String, Candidates> toLowerCase(HashMap<String, Candidates> map) {
		HashMap<String, Candidates> newMap = new HashMap<>();
		for (Map.Entry<String, Candidates> entry : map.entrySet()) {
			String lower = entry.getKey().toLowerCase();
			Candidates cLower = entry.getValue().toLowerCase();
			newMap.put(lower, cLower);
		}
		return newMap;
	}

	private Profile adjustCandidatePostPatterns() {
		for (Map.Entry<String, Candidates> e: this.entrySet()) {
			for (Candidate candidate: e.getValue().Candidates) {
				candidate.adjustOCRPosPatterns();
			}
		}
		return this;
	}

	private Profile removeEmptyCandidates() {
		this.entrySet().removeIf((e) -> e.getValue().Candidates == null || e.getValue().Candidates.size() == 0);
		return this;
	}

	private Profile sortCandidatesByVoteWeight() {
		for (Map.Entry<String, Candidates> e : this.entrySet()) {
			e.getValue().Candidates.sort((a, b)-> (int) (b.Weight - a.Weight));
		}
		return this;
	}

	public String toJSON() {
		return new Gson().toJson(this);
	}

	public boolean containsKey(String key) {
		if (key == null) {
			return false;
		}
		return super.containsKey(key.toLowerCase());
	}

	public Optional<Candidates> get(String key) {
		if (key == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(super.get(key.toLowerCase()));
	}
}
