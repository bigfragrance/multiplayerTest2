package nbt;

import nbt.scanner.NbtScanner;
import nbt.visitor.NbtElementVisitor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Represents an NBT 64-bit integer. Its type is {@value NbtElement#LONG_TYPE}.
 * Instances are immutable.
 */
public class NbtLong extends AbstractNbtNumber {
	private static final int SIZE = 16;
	public static final NbtType<NbtLong> TYPE = new NbtType.OfFixedSize<NbtLong>() {
		public NbtLong read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
			return NbtLong.of(readLong(dataInput, nbtSizeTracker));
		}

		public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
			return visitor.visitLong(readLong(input, tracker));
		}

		private static long readLong(DataInput input, NbtSizeTracker tracker) throws IOException {
			tracker.add(16L);
			return input.readLong();
		}

		public int getSizeInBytes() {
			return 8;
		}

		public String getCrashReportName() {
			return "LONG";
		}

		public String getCommandFeedbackName() {
			return "TAG_Long";
		}

		public boolean isImmutable() {
			return true;
		}
	};
	private final long value;

	NbtLong(long value) {
		this.value = value;
	}

	/**
	 * {@return the NBT long from {@code value}}
	 */
	public static NbtLong of(long value) {
		return value >= -128L && value <= 1024L ? Cache.VALUES[(int)value - -128] : new NbtLong(value);
	}

	public void write(DataOutput output) throws IOException {
		output.writeLong(this.value);
	}

	public int getSizeInBytes() {
		return 16;
	}

	public byte getType() {
		return NbtElement.LONG_TYPE;
	}

	public NbtType<NbtLong> getNbtType() {
		return TYPE;
	}

	public NbtLong copy() {
		return this;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else {
			return o instanceof NbtLong && this.value == ((NbtLong)o).value;
		}
	}

	public int hashCode() {
		return (int)(this.value ^ this.value >>> 32);
	}

	public void accept(NbtElementVisitor visitor) {
		visitor.visitLong(this);
	}

	public long longValue() {
		return this.value;
	}

	public int intValue() {
		return (int)(this.value & -1L);
	}

	public short shortValue() {
		return (short)((int)(this.value & 65535L));
	}

	public byte byteValue() {
		return (byte)((int)(this.value & 255L));
	}

	public double doubleValue() {
		return (double)this.value;
	}

	public float floatValue() {
		return (float)this.value;
	}

	public Number numberValue() {
		return this.value;
	}

	public NbtScanner.Result doAccept(NbtScanner visitor) {
		return visitor.visitLong(this.value);
	}

	static class Cache {
		private static final int MAX = 1024;
		private static final int MIN = -128;
		static final NbtLong[] VALUES = new NbtLong[1153];

		private Cache() {
		}

		static {
			for(int i = 0; i < VALUES.length; ++i) {
				VALUES[i] = new NbtLong((long)(-128 + i));
			}

		}
	}
}
