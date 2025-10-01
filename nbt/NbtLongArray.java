package nbt;


import nbt.scanner.NbtScanner;
import nbt.visitor.NbtElementVisitor;
import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Represents an NBT 64-bit integer array. This object is mutable and backed by
 * {@code long[]}. Its type is {@value NbtElement#LONG_ARRAY_TYPE}. Like Java arrays,
 * accessing indices that are out of bounds will throw {@link ArrayIndexOutOfBoundsException}.
 * The backing array can be obtained via {@link #getLongArray()}.
 */
public class NbtLongArray extends AbstractNbtList<NbtLong> {
	private static final int SIZE = 24;
	public static final NbtType<NbtLongArray> TYPE = new NbtType.OfVariableSize<NbtLongArray>() {
		public NbtLongArray read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
			return new NbtLongArray(readLongArray(dataInput, nbtSizeTracker));
		}

		public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
			return visitor.visitLongArray(readLongArray(input, tracker));
		}

		private static long[] readLongArray(DataInput input, NbtSizeTracker tracker) throws IOException {
			tracker.add(24L);
			int i = input.readInt();
			tracker.add(8L, (long)i);
			long[] ls = new long[i];

			for(int j = 0; j < i; ++j) {
				ls[j] = input.readLong();
			}

			return ls;
		}

		public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
			input.skipBytes(input.readInt() * 8);
		}

		public String getCrashReportName() {
			return "LONG[]";
		}

		public String getCommandFeedbackName() {
			return "TAG_Long_Array";
		}
	};
	private long[] value;

	public NbtLongArray(long[] value) {
		this.value = value;
	}

	public NbtLongArray(LongSet value) {
		this.value = value.toLongArray();
	}

	public NbtLongArray(List<Long> value) {
		this(toArray(value));
	}

	private static long[] toArray(List<Long> list) {
		long[] ls = new long[list.size()];

		for(int i = 0; i < list.size(); ++i) {
			Long long_ = (Long)list.get(i);
			ls[i] = long_ == null ? 0L : long_;
		}

		return ls;
	}

	public void write(DataOutput output) throws IOException {
		output.writeInt(this.value.length);
		long[] var2 = this.value;
		int var3 = var2.length;

		for(int var4 = 0; var4 < var3; ++var4) {
			long l = var2[var4];
			output.writeLong(l);
		}

	}

	public int getSizeInBytes() {
		return 24 + 8 * this.value.length;
	}

	public byte getType() {
		return NbtElement.LONG_ARRAY_TYPE;
	}

	public NbtType<NbtLongArray> getNbtType() {
		return TYPE;
	}

	public String toString() {
		return this.asString();
	}

	public NbtLongArray copy() {
		long[] ls = new long[this.value.length];
		System.arraycopy(this.value, 0, ls, 0, this.value.length);
		return new NbtLongArray(ls);
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else {
			return o instanceof NbtLongArray && Arrays.equals(this.value, ((NbtLongArray)o).value);
		}
	}

	public int hashCode() {
		return Arrays.hashCode(this.value);
	}

	public void accept(NbtElementVisitor visitor) {
		visitor.visitLongArray(this);
	}

	/**
	 * {@return the underlying long array}
	 *
	 * @apiNote This does not copy the array, so modifications to the returned array
	 * also apply to this NBT long array.
	 */
	public long[] getLongArray() {
		return this.value;
	}

	public int size() {
		return this.value.length;
	}

	public NbtLong get(int i) {
		return NbtLong.of(this.value[i]);
	}

	public NbtLong method_10606(int i, NbtLong nbtLong) {
		long l = this.value[i];
		this.value[i] = nbtLong.longValue();
		return NbtLong.of(l);
	}

	public void add(int i, NbtLong nbtLong) {
		this.value = ArrayUtils.add(this.value, i, nbtLong.longValue());
	}

	public boolean setElement(int index, NbtElement element) {
		if (element instanceof AbstractNbtNumber) {
			this.value[index] = ((AbstractNbtNumber)element).longValue();
			return true;
		} else {
			return false;
		}
	}

	public boolean addElement(int index, NbtElement element) {
		if (element instanceof AbstractNbtNumber) {
			this.value = ArrayUtils.add(this.value, index, ((AbstractNbtNumber)element).longValue());
			return true;
		} else {
			return false;
		}
	}

	public NbtLong remove(int i) {
		long l = this.value[i];
		this.value = ArrayUtils.remove(this.value, i);
		return NbtLong.of(l);
	}

	public byte getHeldType() {
		return NbtElement.LONG_TYPE;
	}

	public void clear() {
		this.value = new long[0];
	}

	public NbtScanner.Result doAccept(NbtScanner visitor) {
		return visitor.visitLongArray(this.value);
	}
}
