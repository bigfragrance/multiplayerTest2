package nbt;

import nbt.scanner.NbtScanner;
import nbt.visitor.NbtElementVisitor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Represents an NBT 16-bit integer. Its type is {@value NbtElement#SHORT_TYPE}.
 * Instances are immutable.
 */
public class NbtShort extends AbstractNbtNumber {
	private static final int SIZE = 10;
	public static final NbtType<NbtShort> TYPE = new NbtType.OfFixedSize<NbtShort>() {
		public NbtShort read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
			return NbtShort.of(readShort(dataInput, nbtSizeTracker));
		}

		public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
			return visitor.visitShort(readShort(input, tracker));
		}

		private static short readShort(DataInput input, NbtSizeTracker tracker) throws IOException {
			tracker.add(10L);
			return input.readShort();
		}

		public int getSizeInBytes() {
			return 2;
		}

		public String getCrashReportName() {
			return "SHORT";
		}

		public String getCommandFeedbackName() {
			return "TAG_Short";
		}

		public boolean isImmutable() {
			return true;
		}
	};
	private final short value;

	NbtShort(short value) {
		this.value = value;
	}

	/**
	 * {@return the NBT short from {@code value}}
	 */
	public static NbtShort of(short value) {
		return value >= -128 && value <= 1024 ? Cache.VALUES[value - -128] : new NbtShort(value);
	}

	public void write(DataOutput output) throws IOException {
		output.writeShort(this.value);
	}

	public int getSizeInBytes() {
		return 10;
	}

	public byte getType() {
		return NbtElement.SHORT_TYPE;
	}

	public NbtType<NbtShort> getNbtType() {
		return TYPE;
	}

	public NbtShort copy() {
		return this;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else {
			return o instanceof NbtShort && this.value == ((NbtShort)o).value;
		}
	}

	public int hashCode() {
		return this.value;
	}

	public void accept(NbtElementVisitor visitor) {
		visitor.visitShort(this);
	}

	public long longValue() {
		return (long)this.value;
	}

	public int intValue() {
		return this.value;
	}

	public short shortValue() {
		return this.value;
	}

	public byte byteValue() {
		return (byte)(this.value & 255);
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
		return visitor.visitShort(this.value);
	}

	static class Cache {
		private static final int MAX = 1024;
		private static final int MIN = -128;
		static final NbtShort[] VALUES = new NbtShort[1153];

		private Cache() {
		}

		static {
			for(int i = 0; i < VALUES.length; ++i) {
				VALUES[i] = new NbtShort((short)(-128 + i));
			}

		}
	}
}
