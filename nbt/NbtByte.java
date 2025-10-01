package nbt;

import nbt.scanner.NbtScanner;
import nbt.visitor.NbtElementVisitor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Represents an NBT byte. Its type is {@value NbtElement#BYTE_TYPE}.
 * Instances are immutable.
 */
public class NbtByte extends AbstractNbtNumber {
	private static final int SIZE = 9;
	public static final NbtType<NbtByte> TYPE = new NbtType.OfFixedSize<NbtByte>() {
		public NbtByte read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
			return NbtByte.of(readByte(dataInput, nbtSizeTracker));
		}

		public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
			return visitor.visitByte(readByte(input, tracker));
		}

		private static byte readByte(DataInput input, NbtSizeTracker tracker) throws IOException {
			tracker.add(9L);
			return input.readByte();
		}

		public int getSizeInBytes() {
			return 1;
		}

		public String getCrashReportName() {
			return "BYTE";
		}

		public String getCommandFeedbackName() {
			return "TAG_Byte";
		}

		public boolean isImmutable() {
			return true;
		}
	};
	/**
	 * The NBT byte representing {@code 0}.
	 *
	 * @apiNote This is often used to indicate a false boolean value.
	 */
	public static final NbtByte ZERO = of((byte)0);
	/**
	 * The NBT byte representing {@code 1}.
	 *
	 * @apiNote This is often used to indicate a true boolean value.
	 */
	public static final NbtByte ONE = of((byte)1);
	private final byte value;

	NbtByte(byte value) {
		this.value = value;
	}

	/**
	 * {@return the NBT byte from {@code value}}
	 *
	 * @implNote This returns the value from the cache.
	 */
	public static NbtByte of(byte value) {
		return Cache.VALUES[128 + value];
	}

	/**
	 * {@return the NBT byte representing the boolean {@code value}}
	 */
	public static NbtByte of(boolean value) {
		return value ? ONE : ZERO;
	}

	public void write(DataOutput output) throws IOException {
		output.writeByte(this.value);
	}

	public int getSizeInBytes() {
		return 9;
	}

	public byte getType() {
		return NbtElement.BYTE_TYPE;
	}

	public NbtType<NbtByte> getNbtType() {
		return TYPE;
	}

	public NbtByte copy() {
		return this;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else {
			return o instanceof NbtByte && this.value == ((NbtByte)o).value;
		}
	}

	public int hashCode() {
		return this.value;
	}

	public void accept(NbtElementVisitor visitor) {
		visitor.visitByte(this);
	}

	public long longValue() {
		return (long)this.value;
	}

	public int intValue() {
		return this.value;
	}

	public short shortValue() {
		return (short)this.value;
	}

	public byte byteValue() {
		return this.value;
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
		return visitor.visitByte(this.value);
	}

	private static class Cache {
		static final NbtByte[] VALUES = new NbtByte[256];

		static {
			for(int i = 0; i < VALUES.length; ++i) {
				VALUES[i] = new NbtByte((byte)(i - 128));
			}

		}
	}
}
