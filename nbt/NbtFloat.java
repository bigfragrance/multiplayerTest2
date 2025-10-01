package nbt;

import big.engine.math.MathHelper;
import nbt.scanner.NbtScanner;
import nbt.visitor.NbtElementVisitor;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Represents an NBT 32-bit floating-point number. Its type is {@value NbtElement#FLOAT_TYPE}.
 * Instances are immutable.
 */
public class NbtFloat extends AbstractNbtNumber {
	private static final int SIZE = 12;
	/**
	 * The NBT float representing {@code 0.0f}.
	 */
	public static final NbtFloat ZERO = new NbtFloat(0.0F);
	public static final NbtType<NbtFloat> TYPE = new NbtType.OfFixedSize<NbtFloat>() {
		public NbtFloat read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
			return NbtFloat.of(readFloat(dataInput, nbtSizeTracker));
		}

		public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
			return visitor.visitFloat(readFloat(input, tracker));
		}

		private static float readFloat(DataInput input, NbtSizeTracker tracker) throws IOException {
			tracker.add(12L);
			return input.readFloat();
		}

		public int getSizeInBytes() {
			return 4;
		}

		public String getCrashReportName() {
			return "FLOAT";
		}

		public String getCommandFeedbackName() {
			return "TAG_Float";
		}

		public boolean isImmutable() {
			return true;
		}
	};
	private final float value;

	private NbtFloat(float value) {
		this.value = value;
	}

	/**
	 * {@return the NBT float from {@code value}}
	 */
	public static NbtFloat of(float value) {
		return value == 0.0F ? ZERO : new NbtFloat(value);
	}

	public void write(DataOutput output) throws IOException {
		output.writeFloat(this.value);
	}

	public int getSizeInBytes() {
		return 12;
	}

	public byte getType() {
		return NbtElement.FLOAT_TYPE;
	}

	public NbtType<NbtFloat> getNbtType() {
		return TYPE;
	}

	public NbtFloat copy() {
		return this;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else {
			return o instanceof NbtFloat && this.value == ((NbtFloat)o).value;
		}
	}

	public int hashCode() {
		return Float.floatToIntBits(this.value);
	}

	public void accept(NbtElementVisitor visitor) {
		visitor.visitFloat(this);
	}

	public long longValue() {
		return (long)this.value;
	}

	public int intValue() {
		return (int) Math.floor(this.value);
	}

	public short shortValue() {
		return (short)(MathHelper.floor(this.value) & '\uffff');
	}

	public byte byteValue() {
		return (byte)(MathHelper.floor(this.value) & 255);
	}

	public double doubleValue() {
		return (double)this.value;
	}

	public float floatValue() {
		return this.value;
	}

	public Number numberValue() {
		return this.value;
	}

	public NbtScanner.Result doAccept(NbtScanner visitor) {
		return visitor.visitFloat(this.value);
	}
}
