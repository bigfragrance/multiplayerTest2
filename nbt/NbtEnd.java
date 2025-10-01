package nbt;

import nbt.scanner.NbtScanner;
import nbt.visitor.NbtElementVisitor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Represents the NBT end value.
 * Defines the end of an {@link NbtCompound} object during serialization,
 * and is the type of an empty {@link NbtList}.
 */
public class NbtEnd implements NbtElement {
	private static final int SIZE = 8;
	public static final NbtType<NbtEnd> TYPE = new NbtType<NbtEnd>() {
		public NbtEnd read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) {
			nbtSizeTracker.add(8L);
			return NbtEnd.INSTANCE;
		}

		public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) {
			tracker.add(8L);
			return visitor.visitEnd();
		}

		public void skip(DataInput input, int count, NbtSizeTracker tracker) {
		}

		public void skip(DataInput input, NbtSizeTracker tracker) {
		}

		public String getCrashReportName() {
			return "END";
		}

		public String getCommandFeedbackName() {
			return "TAG_End";
		}

		public boolean isImmutable() {
			return true;
		}
	};
	/**
	 * A dummy instance of the NBT end. It will never appear nested in any parsed NBT
	 * structure and should never be used as NBT compound values or list elements.
	 */
	public static final NbtEnd INSTANCE = new NbtEnd();

	private NbtEnd() {
	}

	public void write(DataOutput output) throws IOException {
	}

	public int getSizeInBytes() {
		return 8;
	}

	public byte getType() {
		return NbtElement.END_TYPE;
	}

	public NbtType<NbtEnd> getNbtType() {
		return TYPE;
	}

	public String toString() {
		return this.asString();
	}

	public NbtEnd copy() {
		return this;
	}

	public void accept(NbtElementVisitor visitor) {
		visitor.visitEnd(this);
	}

	public NbtScanner.Result doAccept(NbtScanner visitor) {
		return visitor.visitEnd();
	}
}
