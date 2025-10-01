package nbt.scanner;

import nbt.NbtType;

/**
 * A simple NBT scanner visits all elements shallowly, allowing
 * implementations to override it and perform more actions.
 */
public interface SimpleNbtScanner extends NbtScanner {
	/**
	 * The simple NBT scanner that performs no action.
	 */
	SimpleNbtScanner NOOP = new SimpleNbtScanner() {
	};

	default Result visitEnd() {
		return Result.CONTINUE;
	}

	default Result visitString(String value) {
		return Result.CONTINUE;
	}

	default Result visitByte(byte value) {
		return Result.CONTINUE;
	}

	default Result visitShort(short value) {
		return Result.CONTINUE;
	}

	default Result visitInt(int value) {
		return Result.CONTINUE;
	}

	default Result visitLong(long value) {
		return Result.CONTINUE;
	}

	default Result visitFloat(float value) {
		return Result.CONTINUE;
	}

	default Result visitDouble(double value) {
		return Result.CONTINUE;
	}

	default Result visitByteArray(byte[] value) {
		return Result.CONTINUE;
	}

	default Result visitIntArray(int[] value) {
		return Result.CONTINUE;
	}

	default Result visitLongArray(long[] value) {
		return Result.CONTINUE;
	}

	default Result visitListMeta(NbtType<?> entryType, int length) {
		return Result.CONTINUE;
	}

	default NestedResult startListItem(NbtType<?> type, int index) {
		return NestedResult.SKIP;
	}

	default NestedResult visitSubNbtType(NbtType<?> type) {
		return NestedResult.SKIP;
	}

	default NestedResult startSubNbt(NbtType<?> type, String key) {
		return NestedResult.SKIP;
	}

	default Result endNested() {
		return Result.CONTINUE;
	}

	default Result start(NbtType<?> rootType) {
		return Result.CONTINUE;
	}
}
