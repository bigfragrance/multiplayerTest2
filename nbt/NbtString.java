package nbt;

import nbt.scanner.NbtScanner;
import nbt.visitor.NbtElementVisitor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

/**
 * Represents an NBT string. Its type is {@value NbtElement#STRING_TYPE}.
 * Instances are immutable.
 */
public class NbtString implements NbtElement {
	private static final int SIZE = 36;
	public static final NbtType<NbtString> TYPE = new NbtType.OfVariableSize<NbtString>() {
		public NbtString read(DataInput dataInput, NbtSizeTracker nbtSizeTracker) throws IOException {
			return NbtString.of(readString(dataInput, nbtSizeTracker));
		}

		public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
			return visitor.visitString(readString(input, tracker));
		}

		private static String readString(DataInput input, NbtSizeTracker tracker) throws IOException {
			tracker.add(36L);
			String string = input.readUTF();
			tracker.add(2L, (long)string.length());
			return string;
		}

		public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
			NbtString.skip(input);
		}

		public String getCrashReportName() {
			return "STRING";
		}

		public String getCommandFeedbackName() {
			return "TAG_String";
		}

		public boolean isImmutable() {
			return true;
		}
	};
	private static final NbtString EMPTY = new NbtString("");
	private static final char DOUBLE_QUOTE = '"';
	private static final char SINGLE_QUOTE = '\'';
	private static final char BACKSLASH = '\\';
	private static final char NULL = '\u0000';
	private final String value;

	public static void skip(DataInput input) throws IOException {
		input.skipBytes(input.readUnsignedShort());
	}

	private NbtString(String value) {
		Objects.requireNonNull(value, "Null string not allowed");
		this.value = value;
	}

	/**
	 * {@return the NBT string from {@code value}}
	 */
	public static NbtString of(String value) {
		return value.isEmpty() ? EMPTY : new NbtString(value);
	}

	public void write(DataOutput output) throws IOException {
		output.writeUTF(this.value);
	}

	public int getSizeInBytes() {
		return 36 + 2 * this.value.length();
	}

	public byte getType() {
		return NbtElement.STRING_TYPE;
	}

	public NbtType<NbtString> getNbtType() {
		return TYPE;
	}

	public String toString() {
		return NbtElement.super.asString();
	}

	public NbtString copy() {
		return this;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else {
			return o instanceof NbtString && Objects.equals(this.value, ((NbtString)o).value);
		}
	}

	public int hashCode() {
		return this.value.hashCode();
	}

	public String asString() {
		return this.value;
	}

	public void accept(NbtElementVisitor visitor) {
		visitor.visitString(this);
	}

	/**
	 * {@return the string quoted with quotes and backslashes escaped}
	 *
	 * @implNote If {@code value} contains one of the singlequote or the double quote,
	 * it tries to use the other quotes to quote the string. If both appear, then the quote
	 * that appeared later will be used to quote the string. If neither of them appears, this
	 * uses a double quote. For example, the string {@code It's a "Tiny Potato"!} will be
	 * escaped as {@code "It's a \"Tiny Potato\"!"}, while the string
	 * {@code It is a "Tiny Potato"!} will be escaped as {@code 'It is a "Tiny Potato"!'}.
	 */
	public static String escape(String value) {
		StringBuilder stringBuilder = new StringBuilder(" ");
		char c = 0;

		for(int i = 0; i < value.length(); ++i) {
			char d = value.charAt(i);
			if (d == '\\') {
				stringBuilder.append('\\');
			} else if (d == '"' || d == '\'') {
				if (c == 0) {
					c = d == '"' ? 39 : 34;
				}

				if (c == d) {
					stringBuilder.append('\\');
				}
			}

			stringBuilder.append(d);
		}

		if (c == 0) {
			c = 34;
		}

		stringBuilder.setCharAt(0, (char)c);
		stringBuilder.append((char)c);
		return stringBuilder.toString();
	}

	public NbtScanner.Result doAccept(NbtScanner visitor) {
		return visitor.visitString(this.value);
	}
}
