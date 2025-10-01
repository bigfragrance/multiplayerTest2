package nbt.scanner;

import nbt.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An NBT collector scans an NBT structure and builds an object
 * representation out of it.
 */
public class NbtCollector implements NbtScanner {
	private String currentKey = "";
	@Nullable
	private NbtElement root;
	private final Deque<Consumer<NbtElement>> stack = new ArrayDeque();

	@Nullable
	public NbtElement getRoot() {
		return this.root;
	}

	protected int getDepth() {
		return this.stack.size();
	}

	private void append(NbtElement nbt) {
		((Consumer)this.stack.getLast()).accept(nbt);
	}

	public Result visitEnd() {
		this.append(NbtEnd.INSTANCE);
		return Result.CONTINUE;
	}

	public Result visitString(String value) {
		this.append(NbtString.of(value));
		return Result.CONTINUE;
	}

	public Result visitByte(byte value) {
		this.append(NbtByte.of(value));
		return Result.CONTINUE;
	}

	public Result visitShort(short value) {
		this.append(NbtShort.of(value));
		return Result.CONTINUE;
	}

	public Result visitInt(int value) {
		this.append(NbtInt.of(value));
		return Result.CONTINUE;
	}

	public Result visitLong(long value) {
		this.append(NbtLong.of(value));
		return Result.CONTINUE;
	}

	public Result visitFloat(float value) {
		this.append(NbtFloat.of(value));
		return Result.CONTINUE;
	}

	public Result visitDouble(double value) {
		this.append(NbtDouble.of(value));
		return Result.CONTINUE;
	}

	public Result visitByteArray(byte[] value) {
		this.append(new NbtByteArray(value));
		return Result.CONTINUE;
	}

	public Result visitIntArray(int[] value) {
		this.append(new NbtIntArray(value));
		return Result.CONTINUE;
	}

	public Result visitLongArray(long[] value) {
		this.append(new NbtLongArray(value));
		return Result.CONTINUE;
	}

	public Result visitListMeta(NbtType<?> entryType, int length) {
		return Result.CONTINUE;
	}

	public NestedResult startListItem(NbtType<?> type, int index) {
		this.pushStack(type);
		return NestedResult.ENTER;
	}

	public NestedResult visitSubNbtType(NbtType<?> type) {
		return NestedResult.ENTER;
	}

	public NestedResult startSubNbt(NbtType<?> type, String key) {
		this.currentKey = key;
		this.pushStack(type);
		return NestedResult.ENTER;
	}

	private void pushStack(NbtType<?> type) {
		if (type == NbtList.TYPE) {
			NbtList nbtList = new NbtList();
			this.append(nbtList);
			Deque<Consumer<NbtElement>> var10000 = this.stack;
			Objects.requireNonNull(nbtList);
			var10000.addLast(nbtList::add);
		} else if (type == NbtCompound.TYPE) {
			NbtCompound nbtCompound = new NbtCompound();
			this.append(nbtCompound);
			this.stack.addLast((nbt) -> {
				nbtCompound.put(this.currentKey, nbt);
			});
		}

	}

	public Result endNested() {
		this.stack.removeLast();
		return Result.CONTINUE;
	}

	public Result start(NbtType<?> rootType) {
		if (rootType == NbtList.TYPE) {
			NbtList nbtList = new NbtList();
			this.root = nbtList;
            Objects.requireNonNull(nbtList);
			this.stack.addLast(nbtList::add);
		} else if (rootType == NbtCompound.TYPE) {
			NbtCompound nbtCompound = new NbtCompound();
			this.root = nbtCompound;
			this.stack.addLast((nbt) -> {
				nbtCompound.put(this.currentKey, nbt);
			});
		} else {
			this.stack.addLast((nbt) -> {
				this.root = nbt;
			});
		}

		return Result.CONTINUE;
	}
}
