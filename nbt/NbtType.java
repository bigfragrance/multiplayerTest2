package nbt;

import nbt.scanner.NbtScanner;

import java.io.DataInput;
import java.io.IOException;

/**
 * Represents an NBT type.
 */
public interface NbtType<T extends NbtElement> {
	T read(DataInput input, NbtSizeTracker tracker) throws IOException;

	NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException;

	default void accept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
		switch (visitor.start(this)) {
			case CONTINUE:
				this.doAccept(input, visitor, tracker);
			case HALT:
			default:
				break;
			case BREAK:
				this.skip(input, tracker);
		}

	}

	void skip(DataInput input, int count, NbtSizeTracker tracker) throws IOException;

	void skip(DataInput input, NbtSizeTracker tracker) throws IOException;

	/**
	 * Determines the immutability of this type.
	 * <p>
	 * The mutability of an NBT type means the held value can be modified
	 * after the NBT element is instantiated.
	 *
	 * @return {@code true} if this NBT type is immutable, else {@code false}
	 */
	default boolean isImmutable() {
		return false;
	}

	String getCrashReportName();

	String getCommandFeedbackName();

	/**
	 * {@return an invalid NBT type}
	 *
	 * <p>Operations with an invalid NBT type always throws {@link IOException}.
	 *
	 * @see NbtTypes#byId(int)
	 */
	static NbtType<NbtEnd> createInvalid(final int type) {
		return new NbtType<NbtEnd>() {
			private IOException createException() {
				return new IOException("Invalid tag id: " + type);
			}

			public NbtEnd read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
				throw this.createException();
			}

			public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
				throw this.createException();
			}

			public void skip(DataInput input, int count, NbtSizeTracker tracker) throws IOException {
				throw this.createException();
			}

			public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
				throw this.createException();
			}

			public String getCrashReportName() {
				return "INVALID[" + type + "]";
			}

			public String getCommandFeedbackName() {
				return "UNKNOWN_" + type;
			}
		};
	}

	/**
	 * Represents an NBT type whose elements can have a variable size, such as lists.
	 */
	public interface OfVariableSize<T extends NbtElement> extends NbtType<T> {
		default void skip(DataInput input, int count, NbtSizeTracker tracker) throws IOException {
			for(int i = 0; i < count; ++i) {
				this.skip(input, tracker);
			}

		}
	}

	/**
	 * Represents an NBT type whose elements have a fixed size, such as primitives.
	 */
	public interface OfFixedSize<T extends NbtElement> extends NbtType<T> {
		default void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
			input.skipBytes(this.getSizeInBytes());
		}

		default void skip(DataInput input, int count, NbtSizeTracker tracker) throws IOException {
			input.skipBytes(this.getSizeInBytes() * count);
		}

		/**
		 * {@return the size of the elements in bytes}
		 */
		int getSizeInBytes();
	}
}
