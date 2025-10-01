package nbt.scanner;

import nbt.NbtType;

import java.util.HashMap;
import java.util.Map;

/**
 * The tree node for representing NBT.
 */
public record NbtTreeNode(int depth, Map<String, NbtType<?>> selectedFields, Map<String, NbtTreeNode> fieldsToRecurse) {
	private NbtTreeNode(int depth) {
		this(depth, new HashMap(), new HashMap());
	}

	public NbtTreeNode(int depth, Map<String, NbtType<?>> selectedFields, Map<String, NbtTreeNode> fieldsToRecurse) {
		this.depth = depth;
		this.selectedFields = selectedFields;
		this.fieldsToRecurse = fieldsToRecurse;
	}

	/**
	 * {@return the root node}
	 *
	 * @implNote The root node has the depth of {@code 1}.
	 */
	public static NbtTreeNode createRoot() {
		return new NbtTreeNode(1);
	}

	public void add(NbtScanQuery query) {
		if (this.depth <= query.path().size()) {
			((NbtTreeNode)this.fieldsToRecurse.computeIfAbsent((String)query.path().get(this.depth - 1), (path) -> {
				return new NbtTreeNode(this.depth + 1);
			})).add(query);
		} else {
			this.selectedFields.put(query.key(), query.type());
		}

	}

	/**
	 * {@return whether the queried type for the key {@code key} matches {@code type}}
	 */
	public boolean isTypeEqual(NbtType<?> type, String key) {
		return type.equals(this.selectedFields().get(key));
	}

	public int depth() {
		return this.depth;
	}

	public Map<String, NbtType<?>> selectedFields() {
		return this.selectedFields;
	}

	public Map<String, NbtTreeNode> fieldsToRecurse() {
		return this.fieldsToRecurse;
	}
}
