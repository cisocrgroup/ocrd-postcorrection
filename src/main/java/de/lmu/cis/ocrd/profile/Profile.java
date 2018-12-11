package de.lmu.cis.ocrd.profile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Profile {
	private final HashMap<String, Candidates> data;

	protected Profile(HashMap<String, Candidates> data) {
		this.data = data;
	}

	// for testing and mocking
	public static Profile empty() {
		return new Profile(new HashMap<>());
	}

	public static Profile read(Reader r) throws Exception {
		return fromJSON(IOUtils.toString(r));
	}

	public static Profile read(InputStream is) throws Exception {
		return fromJSON(IOUtils.toString(is, Charset.forName("UTF-8")));
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
		return new Profile(toLowerCase(data)).removeEmptyCandidates().sortCandidatesByVoteWeight();
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

	private Profile removeEmptyCandidates() {
		data.entrySet().removeIf((e) -> e.getValue().Candidates == null || e.getValue().Candidates.length == 0);
		return this;
	}

	private Profile sortCandidatesByVoteWeight() {
		for (Map.Entry<String, Candidates> e : data.entrySet()) {
			Arrays.sort(e.getValue().Candidates, (Candidate a, Candidate b) -> (int) (b.Weight - a.Weight));
		}
		return this;
	}

	public String toJSON() {
		return new Gson().toJson(this.data);
	}

	public boolean containsKey(String key) {
		if (key == null) {
			return false;
		}
		return this.data.containsKey(key.toLowerCase());
	}

	public Set<Map.Entry<String, Candidates>> entrySet() {
		return this.data.entrySet();
	}

	public Optional<Candidates> get(String key) {
		if (key == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(this.data.get(key.toLowerCase()));
	}

	public Set<String> keySet() {
		return this.data.keySet();
	}

	public int size() {
		return this.data.size();
	}
}
