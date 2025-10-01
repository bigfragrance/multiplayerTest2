package nbt.scanner;

import nbt.NbtType;

import java.util.List;

/**
 * A query for scanning the NBT using {@link ExclusiveNbtCollector} or
 * {@link SelectiveNbtCollector}.
 */
public record NbtScanQuery(List<String> path, NbtType<?> type, String key) {
	public NbtScanQuery(NbtType<?> type, String key) {
		this(List.of(), type, key);
	}

	public NbtScanQuery(String path, NbtType<?> type, String key) {
		this(List.of(path), type, key);
	}

	public NbtScanQuery(String path1, String path2, NbtType<?> type, String key) {
		this(List.of(path1, path2), type, key);
	}

	public NbtScanQuery(List<String> path, NbtType<?> type, String key) {
		this.path = path;
		this.type = type;
		this.key = key;
	}

	public List<String> path() {
		return this.path;
	}

	public NbtType<?> type() {
		return this.type;
	}

	public String key() {
		return this.key;
	}
}
