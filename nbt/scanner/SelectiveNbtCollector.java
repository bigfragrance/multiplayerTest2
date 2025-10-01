package nbt.scanner;

import com.google.common.collect.ImmutableSet;
import nbt.NbtCompound;
import nbt.NbtType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * A selective NBT collector builds an NBT object including only the
 * prescribed queries.
 *
 * @see ExclusiveNbtCollector
 */
public class SelectiveNbtCollector extends NbtCollector {
	private int queriesLeft;
	private final Set<NbtType<?>> allPossibleTypes;
	private final Deque<NbtTreeNode> selectionStack = new ArrayDeque();

	public SelectiveNbtCollector(NbtScanQuery... queries) {
		this.queriesLeft = queries.length;
		ImmutableSet.Builder<NbtType<?>> builder = ImmutableSet.builder();
		NbtTreeNode nbtTreeNode = NbtTreeNode.createRoot();
        int var5 = queries.length;

		for(int var6 = 0; var6 < var5; ++var6) {
			NbtScanQuery nbtScanQuery = queries[var6];
			nbtTreeNode.add(nbtScanQuery);
			builder.add(nbtScanQuery.type());
		}

		this.selectionStack.push(nbtTreeNode);
		builder.add(NbtCompound.TYPE);
		this.allPossibleTypes = builder.build();
	}

	public Result start(NbtType<?> rootType) {
		return rootType != NbtCompound.TYPE ? Result.HALT : super.start(rootType);
	}

	public NestedResult visitSubNbtType(NbtType<?> type) {
		NbtTreeNode nbtTreeNode = (NbtTreeNode)this.selectionStack.element();
		if (this.getDepth() > nbtTreeNode.depth()) {
			return super.visitSubNbtType(type);
		} else if (this.queriesLeft <= 0) {
			return NestedResult.HALT;
		} else {
			return !this.allPossibleTypes.contains(type) ? NestedResult.SKIP : super.visitSubNbtType(type);
		}
	}

	public NestedResult startSubNbt(NbtType<?> type, String key) {
		NbtTreeNode nbtTreeNode = (NbtTreeNode)this.selectionStack.element();
		if (this.getDepth() > nbtTreeNode.depth()) {
			return super.startSubNbt(type, key);
		} else if (nbtTreeNode.selectedFields().remove(key, type)) {
			--this.queriesLeft;
			return super.startSubNbt(type, key);
		} else {
			if (type == NbtCompound.TYPE) {
				NbtTreeNode nbtTreeNode2 = (NbtTreeNode)nbtTreeNode.fieldsToRecurse().get(key);
				if (nbtTreeNode2 != null) {
					this.selectionStack.push(nbtTreeNode2);
					return super.startSubNbt(type, key);
				}
			}

			return NestedResult.SKIP;
		}
	}

	public Result endNested() {
		if (this.getDepth() == ((NbtTreeNode)this.selectionStack.element()).depth()) {
			this.selectionStack.pop();
		}

		return super.endNested();
	}

	public int getQueriesLeft() {
		return this.queriesLeft;
	}
}
