package nbt.scanner;

import nbt.NbtCompound;
import nbt.NbtType;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * An exclusive NBT collector builds an NBT object including everything
 * except the prescribed queries.
 *
 * @see SelectiveNbtCollector
 */
public class ExclusiveNbtCollector extends NbtCollector {
	private final Deque<NbtTreeNode> treeStack = new ArrayDeque();

	public ExclusiveNbtCollector(NbtScanQuery... excludedQueries) {
		NbtTreeNode nbtTreeNode = NbtTreeNode.createRoot();
		NbtScanQuery[] var3 = excludedQueries;
		int var4 = excludedQueries.length;

		for(int var5 = 0; var5 < var4; ++var5) {
			NbtScanQuery nbtScanQuery = var3[var5];
			nbtTreeNode.add(nbtScanQuery);
		}

		this.treeStack.push(nbtTreeNode);
	}

	public NestedResult startSubNbt(NbtType<?> type, String key) {
		NbtTreeNode nbtTreeNode = (NbtTreeNode)this.treeStack.element();
		if (nbtTreeNode.isTypeEqual(type, key)) {
			return NestedResult.SKIP;
		} else {
			if (type == NbtCompound.TYPE) {
				NbtTreeNode nbtTreeNode2 = (NbtTreeNode)nbtTreeNode.fieldsToRecurse().get(key);
				if (nbtTreeNode2 != null) {
					this.treeStack.push(nbtTreeNode2);
				}
			}

			return super.startSubNbt(type, key);
		}
	}

	public Result endNested() {
		if (this.getDepth() == ((NbtTreeNode)this.treeStack.element()).depth()) {
			this.treeStack.pop();
		}

		return super.endNested();
	}
}
