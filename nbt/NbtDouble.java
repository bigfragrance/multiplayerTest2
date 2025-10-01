package nbt;

import nbt.scanner.NbtScanner;
import nbt.visitor.NbtElementVisitor;
import net.minecraft.util.math.MathHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Represents an NBT 64-bit floating-point number. Its type is {@value NbtElement#DOUBLE_TYPE}.
 * Instances are immutable.
 */
public class NbtDouble extends AbstractNbtNumber {
	private static final int SIZE = 16;
	/**
	 * The NBT double representing {@code 0.0}.
	 */
	public static final NbtDouble ZERO = new NbtDouble(0.0);
	public static final NbtType<NbtDouble> TYPE = new NbtType.OfFixedSize<NbtDouble>() {
		public NbtDouble read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
			return NbtDouble.of(readDouble(dataInput, nbtSizeTracker));
		}

		public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
			return visitor.visitDouble(readDouble(input, tracker));
		}

		private static double readDouble(DataInput input, NbtSizeTracker tracker) throws IOException {
			tracker.add(16L);
			return input.readDouble();
		}

		public int getSizeInBytes() {
			return 8;
		}

		public String getCrashReportName() {
			return "DOUBLE";
		}

		public String getCommandFeedbackName() {
			return "TAG_Double";
		}

		public boolean isImmutable() {
			return true;
		}
	};
	private final double value;

	private NbtDouble(double value) {
		this.value = value;
	}

	/**
	 * {@return the NBT double from {@code value}}
	 */
	public static NbtDouble of(double value) {
		return value == 0.0 ? ZERO : new NbtDouble(value);
	}

	public void write(DataOutput output) throws IOException {
		output.writeDouble(this.value);
	}

	public int getSizeInBytes() {
		return 16;
	}

	public byte getType() {
		return NbtElement.DOUBLE_TYPE;
	}

	public NbtType<NbtDouble> getNbtType() {
		return TYPE;
	}

	public NbtDouble copy() {
		return this;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else {
			return o instanceof NbtDouble && this.value == ((NbtDouble)o).value;
		}
	}

	public int hashCode() {
		long l = Double.doubleToLongBits(this.value);
		return (int)(l ^ l >>> 32);
	}

	public void accept(NbtElementVisitor visitor) {
		visitor.visitDouble(this);
	}

	public long longValue() {
		return (long)Math.floor(this.value);
	}

	public int intValue() {
		return (int) Math.floor(this.value);
	}

	public short shortValue() {
		return (short)((short)Math.floor(this.value) & '\uffff');
	}

	public byte byteValue() {
		return (byte)((byte)Math.floor(this.value) & 255);
	}

	public double doubleValue() {
		return this.value;
	}

	public float floatValue() {
		return (float)this.value;
	}

	public Number numberValue() {
		return this.value;
	}

	public NbtScanner.Result doAccept(NbtScanner visitor) {
		return visitor.visitDouble(this.value);
	}
}
