package nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Used to handle Minecraft NBTs within {@link com.mojang.serialization.Dynamic
 * dynamics} for DataFixerUpper, allowing generalized serialization logic
 * shared across different type of data structures. Use {@link NbtOps#INSTANCE}
 * for the ops singleton.
 *
 * <p>For instance, dimension data may be stored as JSON in data packs, but
 * they will be transported in packets as NBT. DataFixerUpper allows
 * generalizing the dimension serialization logic to prevent duplicate code,
 * where the NBT ops allow the DataFixerUpper dimension serialization logic
 * to interact with Minecraft NBTs.
 *
 * @see NbtOps#INSTANCE
 */
public class NbtOps implements DynamicOps<NbtElement> {
	/**
	 * An singleton of the NBT dynamic ops.
	 *
	 * <p>This ops does not compress maps (replace field name to value pairs
	 * with an ordered list of values in serialization). In fact, since
	 * Minecraft NBT lists can only contain elements of the same type, this op
	 * cannot compress maps.
	 */
	public static final NbtOps INSTANCE = new NbtOps();
	private static final String MARKER_KEY = "";

	protected NbtOps() {
	}

	public NbtElement empty() {
		return NbtEnd.INSTANCE;
	}

	public <U> U convertTo(DynamicOps<U> dynamicOps, NbtElement nbtElement) {
		Object var10000;
		switch (nbtElement.getType()) {
			case 0:
				var10000 = (Object)dynamicOps.empty();
				break;
			case 1:
				var10000 = (Object)dynamicOps.createByte(((AbstractNbtNumber)nbtElement).byteValue());
				break;
			case 2:
				var10000 = (Object)dynamicOps.createShort(((AbstractNbtNumber)nbtElement).shortValue());
				break;
			case 3:
				var10000 = (Object)dynamicOps.createInt(((AbstractNbtNumber)nbtElement).intValue());
				break;
			case 4:
				var10000 = (Object)dynamicOps.createLong(((AbstractNbtNumber)nbtElement).longValue());
				break;
			case 5:
				var10000 = (Object)dynamicOps.createFloat(((AbstractNbtNumber)nbtElement).floatValue());
				break;
			case 6:
				var10000 = (Object)dynamicOps.createDouble(((AbstractNbtNumber)nbtElement).doubleValue());
				break;
			case 7:
				var10000 = (Object)dynamicOps.createByteList(ByteBuffer.wrap(((NbtByteArray)nbtElement).getByteArray()));
				break;
			case 8:
				var10000 = (Object)dynamicOps.createString(nbtElement.asString());
				break;
			case 9:
				var10000 = (Object)this.convertList(dynamicOps, nbtElement);
				break;
			case 10:
				var10000 = (Object)this.convertMap(dynamicOps, nbtElement);
				break;
			case 11:
				var10000 = (Object)dynamicOps.createIntList(Arrays.stream(((NbtIntArray)nbtElement).getIntArray()));
				break;
			case 12:
				var10000 = (Object)dynamicOps.createLongList(Arrays.stream(((NbtLongArray)nbtElement).getLongArray()));
				break;
			default:
				throw new IllegalStateException("Unknown tag type: " + String.valueOf(nbtElement));
		}

		return var10000;
	}

	public DataResult<Number> getNumberValue(NbtElement nbtElement) {
		if (nbtElement instanceof AbstractNbtNumber abstractNbtNumber) {
			return DataResult.success(abstractNbtNumber.numberValue());
		} else {
			return DataResult.error(() -> {
				return "Not a number";
			});
		}
	}

	public NbtElement createNumeric(Number number) {
		return NbtDouble.of(number.doubleValue());
	}

	public NbtElement createByte(byte b) {
		return NbtByte.of(b);
	}

	public NbtElement createShort(short s) {
		return NbtShort.of(s);
	}

	public NbtElement createInt(int i) {
		return NbtInt.of(i);
	}

	public NbtElement createLong(long l) {
		return NbtLong.of(l);
	}

	public NbtElement createFloat(float f) {
		return NbtFloat.of(f);
	}

	public NbtElement createDouble(double d) {
		return NbtDouble.of(d);
	}

	public NbtElement createBoolean(boolean bl) {
		return NbtByte.of(bl);
	}

	public DataResult<String> getStringValue(NbtElement nbtElement) {
		if (nbtElement instanceof NbtString nbtString) {
			return DataResult.success(nbtString.asString());
		} else {
			return DataResult.error(() -> {
				return "Not a string";
			});
		}
	}

	public NbtElement createString(String string) {
		return NbtString.of(string);
	}

	public DataResult<NbtElement> mergeToList(NbtElement nbtElement, NbtElement nbtElement2) {
		return (DataResult)createMerger(nbtElement).map((merger) -> {
			return DataResult.success(merger.merge(nbtElement2).getResult());
		}).orElseGet(() -> {
			return DataResult.error(() -> {
				return "mergeToList called with not a list: " + String.valueOf(nbtElement);
			}, (Object)nbtElement);
		});
	}

	public DataResult<NbtElement> mergeToList(NbtElement nbtElement, List<NbtElement> list) {
		return (DataResult)createMerger(nbtElement).map((merger) -> {
			return DataResult.success(merger.merge((Iterable)list).getResult());
		}).orElseGet(() -> {
			return DataResult.error(() -> {
				return "mergeToList called with not a list: " + String.valueOf(nbtElement);
			}, (Object)nbtElement);
		});
	}

	public DataResult<NbtElement> mergeToMap(NbtElement nbtElement, NbtElement nbtElement2, NbtElement nbtElement3) {
		if (!(nbtElement instanceof NbtCompound) && !(nbtElement instanceof NbtEnd)) {
			return DataResult.error(() -> {
				return "mergeToMap called with not a map: " + String.valueOf(nbtElement);
			}, (Object)nbtElement);
		} else if (!(nbtElement2 instanceof NbtString)) {
			return DataResult.error(() -> {
				return "key is not a string: " + String.valueOf(nbtElement2);
			}, (Object)nbtElement);
		} else {
			NbtCompound var10000;
			if (nbtElement instanceof NbtCompound) {
				NbtCompound nbtCompound = (NbtCompound)nbtElement;
				var10000 = nbtCompound.shallowCopy();
			} else {
				var10000 = new NbtCompound();
			}

			NbtCompound nbtCompound2 = var10000;
			nbtCompound2.put(nbtElement2.asString(), nbtElement3);
			return DataResult.success(nbtCompound2);
		}
	}

	public DataResult<NbtElement> mergeToMap(NbtElement nbtElement, MapLike<NbtElement> mapLike) {
		if (!(nbtElement instanceof NbtCompound) && !(nbtElement instanceof NbtEnd)) {
			return DataResult.error(() -> {
				return "mergeToMap called with not a map: " + String.valueOf(nbtElement);
			}, (Object)nbtElement);
		} else {
			NbtCompound var10000;
			if (nbtElement instanceof NbtCompound) {
				NbtCompound nbtCompound = (NbtCompound)nbtElement;
				var10000 = nbtCompound.shallowCopy();
			} else {
				var10000 = new NbtCompound();
			}

			NbtCompound nbtCompound2 = var10000;
			List<NbtElement> list = new ArrayList();
			mapLike.entries().forEach((pair) -> {
				NbtElement nbtElement = (NbtElement)pair.getFirst();
				if (!(nbtElement instanceof NbtString)) {
					list.add(nbtElement);
				} else {
					nbtCompound2.put(nbtElement.asString(), (NbtElement)pair.getSecond());
				}
			});
			return !list.isEmpty() ? DataResult.error(() -> {
				return "some keys are not strings: " + String.valueOf(list);
			}, (Object)nbtCompound2) : DataResult.success(nbtCompound2);
		}
	}

	public DataResult<NbtElement> mergeToMap(NbtElement nbtElement, Map<NbtElement, NbtElement> map) {
		if (!(nbtElement instanceof NbtCompound) && !(nbtElement instanceof NbtEnd)) {
			return DataResult.error(() -> {
				return "mergeToMap called with not a map: " + String.valueOf(nbtElement);
			}, (Object)nbtElement);
		} else {
			NbtCompound var10000;
			if (nbtElement instanceof NbtCompound) {
				NbtCompound nbtCompound = (NbtCompound)nbtElement;
				var10000 = nbtCompound.shallowCopy();
			} else {
				var10000 = new NbtCompound();
			}

			NbtCompound nbtCompound2 = var10000;
			List<NbtElement> list = new ArrayList();
			Iterator var5 = map.entrySet().iterator();

			while(var5.hasNext()) {
				Map.Entry<NbtElement, NbtElement> entry = (Map.Entry)var5.next();
				NbtElement nbtElement2 = (NbtElement)entry.getKey();
				if (nbtElement2 instanceof NbtString) {
					nbtCompound2.put(nbtElement2.asString(), (NbtElement)entry.getValue());
				} else {
					list.add(nbtElement2);
				}
			}

			if (!list.isEmpty()) {
				return DataResult.error(() -> {
					return "some keys are not strings: " + String.valueOf(list);
				}, (Object)nbtCompound2);
			} else {
				return DataResult.success(nbtCompound2);
			}
		}
	}

	public DataResult<Stream<Pair<NbtElement, NbtElement>>> getMapValues(NbtElement nbtElement) {
		if (nbtElement instanceof NbtCompound nbtCompound) {
			return DataResult.success(nbtCompound.entrySet().stream().map((entry) -> {
				return Pair.of(this.createString((String)entry.getKey()), (NbtElement)entry.getValue());
			}));
		} else {
			return DataResult.error(() -> {
				return "Not a map: " + String.valueOf(nbtElement);
			});
		}
	}

	public DataResult<Consumer<BiConsumer<NbtElement, NbtElement>>> getMapEntries(NbtElement nbtElement) {
		if (nbtElement instanceof NbtCompound nbtCompound) {
			return DataResult.success((biConsumer) -> {
				Iterator var3 = nbtCompound.entrySet().iterator();

				while(var3.hasNext()) {
					Map.Entry<String, NbtElement> entry = (Map.Entry)var3.next();
					biConsumer.accept(this.createString((String)entry.getKey()), (NbtElement)entry.getValue());
				}

			});
		} else {
			return DataResult.error(() -> {
				return "Not a map: " + String.valueOf(nbtElement);
			});
		}
	}

	public DataResult<MapLike<NbtElement>> getMap(NbtElement nbtElement) {
		if (nbtElement instanceof final NbtCompound nbtCompound) {
			return DataResult.success(new MapLike<NbtElement>() {
				@Nullable
				public NbtElement get(NbtElement nbtElement) {
					return nbtCompound.get(nbtElement.asString());
				}

				@Nullable
				public NbtElement get(String string) {
					return nbtCompound.get(string);
				}

				public Stream<Pair<NbtElement, NbtElement>> entries() {
					return nbtCompound.entrySet().stream().map((entry) -> {
						return Pair.of(NbtOps.this.createString((String)entry.getKey()), (NbtElement)entry.getValue());
					});
				}

				public String toString() {
					return "MapLike[" + String.valueOf(nbtCompound) + "]";
				}
			});
		} else {
			return DataResult.error(() -> {
				return "Not a map: " + String.valueOf(nbtElement);
			});
		}
	}

	public NbtElement createMap(Stream<Pair<NbtElement, NbtElement>> stream) {
		NbtCompound nbtCompound = new NbtCompound();
		stream.forEach((entry) -> {
			nbtCompound.put(((NbtElement)entry.getFirst()).asString(), (NbtElement)entry.getSecond());
		});
		return nbtCompound;
	}

	private static NbtElement unpackMarker(NbtCompound nbt) {
		if (nbt.getSize() == 1) {
			NbtElement nbtElement = nbt.get("");
			if (nbtElement != null) {
				return nbtElement;
			}
		}

		return nbt;
	}

	public DataResult<Stream<NbtElement>> getStream(NbtElement nbtElement) {
		if (nbtElement instanceof NbtList nbtList) {
			return nbtList.getHeldType() == NbtElement.COMPOUND_TYPE ? DataResult.success(nbtList.stream().map((nbt) -> {
				return unpackMarker((NbtCompound)nbt);
			})) : DataResult.success(nbtList.stream());
		} else if (nbtElement instanceof AbstractNbtList<?> abstractNbtList) {
			return DataResult.success(abstractNbtList.stream().map((nbt) -> {
				return nbt;
			}));
		} else {
			return DataResult.error(() -> {
				return "Not a list";
			});
		}
	}

	public DataResult<Consumer<Consumer<NbtElement>>> getList(NbtElement nbtElement) {
		if (nbtElement instanceof NbtList nbtList) {
			if (nbtList.getHeldType() == NbtElement.COMPOUND_TYPE) {
				return DataResult.success((consumer) -> {
					Iterator var2 = nbtList.iterator();

					while(var2.hasNext()) {
						NbtElement nbtElement = (NbtElement)var2.next();
						consumer.accept(unpackMarker((NbtCompound)nbtElement));
					}

				});
			} else {
				Objects.requireNonNull(nbtList);
				return DataResult.success(nbtList::forEach);
			}
		} else if (nbtElement instanceof AbstractNbtList<?> abstractNbtList) {
			Objects.requireNonNull(abstractNbtList);
			return DataResult.success(abstractNbtList::forEach);
		} else {
			return DataResult.error(() -> {
				return "Not a list: " + String.valueOf(nbtElement);
			});
		}
	}

	public DataResult<ByteBuffer> getByteBuffer(NbtElement nbtElement) {
		if (nbtElement instanceof NbtByteArray nbtByteArray) {
			return DataResult.success(ByteBuffer.wrap(nbtByteArray.getByteArray()));
		} else {
			return DynamicOps.super.getByteBuffer(nbtElement);
		}
	}

	public NbtElement createByteList(ByteBuffer byteBuffer) {
		ByteBuffer byteBuffer2 = byteBuffer.duplicate().clear();
		byte[] bs = new byte[byteBuffer.capacity()];
		byteBuffer2.get(0, bs, 0, bs.length);
		return new NbtByteArray(bs);
	}

	public DataResult<IntStream> getIntStream(NbtElement nbtElement) {
		if (nbtElement instanceof NbtIntArray nbtIntArray) {
			return DataResult.success(Arrays.stream(nbtIntArray.getIntArray()));
		} else {
			return DynamicOps.super.getIntStream(nbtElement);
		}
	}

	public NbtElement createIntList(IntStream intStream) {
		return new NbtIntArray(intStream.toArray());
	}

	public DataResult<LongStream> getLongStream(NbtElement nbtElement) {
		if (nbtElement instanceof NbtLongArray nbtLongArray) {
			return DataResult.success(Arrays.stream(nbtLongArray.getLongArray()));
		} else {
			return DynamicOps.super.getLongStream(nbtElement);
		}
	}

	public NbtElement createLongList(LongStream longStream) {
		return new NbtLongArray(longStream.toArray());
	}

	public NbtElement createList(Stream<NbtElement> stream) {
		return BasicMerger.EMPTY.merge(stream).getResult();
	}

	public NbtElement remove(NbtElement nbtElement, String string) {
		if (nbtElement instanceof NbtCompound nbtCompound) {
			NbtCompound nbtCompound2 = nbtCompound.shallowCopy();
			nbtCompound2.remove(string);
			return nbtCompound2;
		} else {
			return nbtElement;
		}
	}

	public String toString() {
		return "NBT";
	}

	public RecordBuilder<NbtElement> mapBuilder() {
		return new MapBuilder(this);
	}

	private static Optional<Merger> createMerger(NbtElement nbt) {
		if (nbt instanceof NbtEnd) {
			return Optional.of(BasicMerger.EMPTY);
		} else {
			if (nbt instanceof AbstractNbtList) {
				AbstractNbtList<?> abstractNbtList = (AbstractNbtList)nbt;
				if (abstractNbtList.isEmpty()) {
					return Optional.of(BasicMerger.EMPTY);
				}

				if (abstractNbtList instanceof NbtList) {
					NbtList nbtList = (NbtList)abstractNbtList;
					Optional var10000;
					switch (nbtList.getHeldType()) {
						case 0:
							var10000 = Optional.of(BasicMerger.EMPTY);
							break;
						case 10:
							var10000 = Optional.of(new CompoundListMerger(nbtList));
							break;
						default:
							var10000 = Optional.of(new ListMerger(nbtList));
					}

					return var10000;
				}

				if (abstractNbtList instanceof NbtByteArray) {
					NbtByteArray nbtByteArray = (NbtByteArray)abstractNbtList;
					return Optional.of(new ByteArrayMerger(nbtByteArray.getByteArray()));
				}

				if (abstractNbtList instanceof NbtIntArray) {
					NbtIntArray nbtIntArray = (NbtIntArray)abstractNbtList;
					return Optional.of(new IntArrayMerger(nbtIntArray.getIntArray()));
				}

				if (abstractNbtList instanceof NbtLongArray) {
					NbtLongArray nbtLongArray = (NbtLongArray)abstractNbtList;
					return Optional.of(new LongArrayMerger(nbtLongArray.getLongArray()));
				}
			}

			return Optional.empty();
		}
	}

	static class BasicMerger implements Merger {
		public static final BasicMerger EMPTY = new BasicMerger();

		private BasicMerger() {
		}

		public Merger merge(NbtElement nbt) {
			if (nbt instanceof NbtCompound nbtCompound) {
				return (new CompoundListMerger()).merge(nbtCompound);
			} else if (nbt instanceof NbtByte nbtByte) {
				return new ByteArrayMerger(nbtByte.byteValue());
			} else if (nbt instanceof NbtInt nbtInt) {
				return new IntArrayMerger(nbtInt.intValue());
			} else if (nbt instanceof NbtLong nbtLong) {
				return new LongArrayMerger(nbtLong.longValue());
			} else {
				return new ListMerger(nbt);
			}
		}

		public NbtElement getResult() {
			return new NbtList();
		}
	}

	private interface Merger {
		Merger merge(NbtElement nbt);

		default Merger merge(Iterable<NbtElement> nbts) {
			Merger merger = this;

			NbtElement nbtElement;
			for(Iterator var3 = nbts.iterator(); var3.hasNext(); merger = merger.merge(nbtElement)) {
				nbtElement = (NbtElement)var3.next();
			}

			return merger;
		}

		default Merger merge(Stream<NbtElement> nbts) {
			Objects.requireNonNull(nbts);
			return this.merge(nbts::iterator);
		}

		NbtElement getResult();
	}

	private class MapBuilder extends RecordBuilder.AbstractStringBuilder<NbtElement, NbtCompound> {
		protected MapBuilder(final NbtOps ops) {
			super(ops);
		}

		protected NbtCompound initBuilder() {
			return new NbtCompound();
		}

		protected NbtCompound append(String string, NbtElement nbtElement, NbtCompound nbtCompound) {
			nbtCompound.put(string, nbtElement);
			return nbtCompound;
		}

		protected DataResult<NbtElement> build(NbtCompound nbtCompound, NbtElement nbtElement) {
			if (nbtElement != null && nbtElement != NbtEnd.INSTANCE) {
				if (!(nbtElement instanceof NbtCompound)) {
					return DataResult.error(() -> {
						return "mergeToMap called with not a map: " + String.valueOf(nbtElement);
					}, (Object)nbtElement);
				} else {
					NbtCompound nbtCompound2 = (NbtCompound)nbtElement;
					NbtCompound nbtCompound3 = nbtCompound2.shallowCopy();
					Iterator var5 = nbtCompound.entrySet().iterator();

					while(var5.hasNext()) {
						Map.Entry<String, NbtElement> entry = (Map.Entry)var5.next();
						nbtCompound3.put((String)entry.getKey(), (NbtElement)entry.getValue());
					}

					return DataResult.success(nbtCompound3);
				}
			} else {
				return DataResult.success(nbtCompound);
			}
		}
	}

	private static class CompoundListMerger implements Merger {
		private final NbtList list = new NbtList();

		public CompoundListMerger() {
		}

		public CompoundListMerger(Collection<NbtElement> nbts) {
			this.list.addAll(nbts);
		}

		public CompoundListMerger(IntArrayList list) {
			list.forEach((value) -> {
				this.list.add(createMarkerNbt(NbtInt.of(value)));
			});
		}

		public CompoundListMerger(ByteArrayList list) {
			list.forEach((value) -> {
				this.list.add(createMarkerNbt(NbtByte.of(value)));
			});
		}

		public CompoundListMerger(LongArrayList list) {
			list.forEach((value) -> {
				this.list.add(createMarkerNbt(NbtLong.of(value)));
			});
		}

		private static boolean isMarker(NbtCompound nbt) {
			return nbt.getSize() == 1 && nbt.contains("");
		}

		private static NbtElement makeMarker(NbtElement value) {
			if (value instanceof NbtCompound nbtCompound) {
				if (!isMarker(nbtCompound)) {
					return nbtCompound;
				}
			}

			return createMarkerNbt(value);
		}

		private static NbtCompound createMarkerNbt(NbtElement value) {
			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.put("", value);
			return nbtCompound;
		}

		public Merger merge(NbtElement nbt) {
			this.list.add(makeMarker(nbt));
			return this;
		}

		public NbtElement getResult() {
			return this.list;
		}
	}

	private static class ListMerger implements Merger {
		private final NbtList list = new NbtList();

		ListMerger(NbtElement nbt) {
			this.list.add(nbt);
		}

		ListMerger(NbtList nbt) {
			this.list.addAll(nbt);
		}

		public Merger merge(NbtElement nbt) {
			if (nbt.getType() != this.list.getHeldType()) {
				return (new CompoundListMerger()).merge(this.list).merge(nbt);
			} else {
				this.list.add(nbt);
				return this;
			}
		}

		public NbtElement getResult() {
			return this.list;
		}
	}

	static class ByteArrayMerger implements Merger {
		private final ByteArrayList list = new ByteArrayList();

		public ByteArrayMerger(byte value) {
			this.list.add(value);
		}

		public ByteArrayMerger(byte[] values) {
			this.list.addElements(0, values);
		}

		public Merger merge(NbtElement nbt) {
			if (nbt instanceof NbtByte nbtByte) {
				this.list.add(nbtByte.byteValue());
				return this;
			} else {
				return (new CompoundListMerger(this.list)).merge(nbt);
			}
		}

		public NbtElement getResult() {
			return new NbtByteArray(this.list.toByteArray());
		}
	}

	private static class IntArrayMerger implements Merger {
		private final IntArrayList list = new IntArrayList();

		public IntArrayMerger(int value) {
			this.list.add(value);
		}

		public IntArrayMerger(int[] values) {
			this.list.addElements(0, values);
		}

		public Merger merge(NbtElement nbt) {
			if (nbt instanceof NbtInt nbtInt) {
				this.list.add(nbtInt.intValue());
				return this;
			} else {
				return (new CompoundListMerger(this.list)).merge(nbt);
			}
		}

		public NbtElement getResult() {
			return new NbtIntArray(this.list.toIntArray());
		}
	}

	private static class LongArrayMerger implements Merger {
		private final LongArrayList list = new LongArrayList();

		public LongArrayMerger(long value) {
			this.list.add(value);
		}

		public LongArrayMerger(long[] values) {
			this.list.addElements(0, values);
		}

		public Merger merge(NbtElement nbt) {
			if (nbt instanceof NbtLong nbtLong) {
				this.list.add(nbtLong.longValue());
				return this;
			} else {
				return (new CompoundListMerger(this.list)).merge(nbt);
			}
		}

		public NbtElement getResult() {
			return new NbtLongArray(this.list.toLongArray());
		}
	}
}
