package nbt.scanner;

import nbt.NbtType;

/**
 * An NBT scanner may reduce unnecessary data parsing to allow finding
 * desired information in an NBT structure as fast as possible.
 *
 * <p>Call format: start -> VisitBody
 * <br>
 * VisitBody:<br>
 * { visitEnd | visitString | visitByte | visitShort | visitInt<br>
 * | visitLong | visitFloat | visitDouble | visitByteArray<br>
 * | visitIntArray | visitLongArray<br>
 * | visitListMeta -> [startListItem -> VisitBody]* -> endNested<br>
 * | [visitSubNbtType -> startSubNbt -> VisitBody]* -> endNested<br>
 * }
 *
 * <p>The visit order is depth-first.
 */
public interface NbtScanner {
	Result visitEnd();

	Result visitString(String value);

	Result visitByte(byte value);

	Result visitShort(short value);

	Result visitInt(int value);

	Result visitLong(long value);

	Result visitFloat(float value);

	Result visitDouble(double value);

	Result visitByteArray(byte[] value);

	Result visitIntArray(int[] value);

	Result visitLongArray(long[] value);

	Result visitListMeta(NbtType<?> entryType, int length);

	NestedResult visitSubNbtType(NbtType<?> type);

	/**
	 * Starts a visit to an NBT structure in the sub NBT of a compound NBT.
	 *
	 * @see #start
	 * @see #startListItem
	 */
	NestedResult startSubNbt(NbtType<?> type, String key);

	/**
	 * Starts a visit to an NBT structure in an element of a list NBT.
	 *
	 * @see #startSubNbt
	 * @see #start
	 */
	NestedResult startListItem(NbtType<?> type, int index);

	/**
	 * Ends a nested visit.
	 *
	 * <p>This is guaranteed to be called once for each call to {@link #start},
	 * {@link #visitSubNbtType}, and {@link #startListItem} where the list or
	 * the compound NBT type is passed, unless the visit is halted.
	 */
	Result endNested();

	/**
	 * Starts a visit to an NBT structure.
	 *
	 * @see #startSubNbt
	 * @see #startListItem
	 */
	Result start(NbtType<?> rootType);

	public static enum NestedResult {
		/**
		 * Proceeds to visit more data of this element, or to enter this element.
		 * (this element is a list element or a sub NBT)
		 */
		ENTER,
		/**
		 * Skips this element and visit the next list element or sub NBT.
		 */
		SKIP,
		/**
		 * Skips the whole list or compound NBT currently under scan. Will make a
		 * call to {@link NbtScanner#endNested()}.
		 */
		BREAK,
		/**
		 * Halts the whole scanning completely.
		 */
		HALT;
	}

	public static enum Result {
		/**
		 * Proceed to visit more data of this element.
		 */
		CONTINUE,
		/**
		 * Skips the current element under scan.
		 */
		BREAK,
		/**
		 * Halts the whole scanning completely.
		 */
		HALT;
	}
}
