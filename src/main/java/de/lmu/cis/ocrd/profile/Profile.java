package de.lmu.cis.ocrd.profile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Profile {
    // for testing and mocking
    public static Profile empty() {
        return new Profile(new HashMap<>());
    }

	public static Profile read(InputStream is) throws IOException {
		StringWriter out = new StringWriter();
		IOUtils.copy(is, out, Charset.forName("UTF-8"));
		Gson gson = new Gson();
		Type map = new TypeToken<HashMap<String, Candidates>>() {
		}.getType();
        Logger.debug("profile: {}", out.toString());
		HashMap<String, Candidates> data = gson.fromJson(out.toString(), map);
		return new Profile(toLowerCase(data));
	}

	public static Profile read(Path path) throws IOException {
		try (InputStream is = new FileInputStream(path.toFile())) {
			return read(is);
		}
	}

	public static Profile read(String path) throws IOException {
		return read(Paths.get(path));
	}

	public String toJSON() {
        return new Gson().toJson(this.data);
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

	private final HashMap<String, Candidates> data;

	private Profile(HashMap<String, Candidates> data) {
		this.data = data;
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
