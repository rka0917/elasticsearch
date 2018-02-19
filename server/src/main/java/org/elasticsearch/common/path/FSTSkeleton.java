package org.elasticsearch.common.path;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.ByteSequenceOutputs;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.FST.INPUT_TYPE;
import org.apache.lucene.util.fst.Util;
import org.elasticsearch.common.path.FSTRepresentation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FSTSkeleton<T> {


	/**
	 * @param keys The keys to add to the FST
	 * @param values The values to add to the FST
	 * @return Returns a FST containing the key-value pairs
	 */
	public FST<BytesRef> build(String[] keys, T[] values) {
		FSTRepresentation<T> representation = new FSTRepresentation<T>();

		HashMap<String, T> map = new HashMap<String, T>();
		for (int i = 0; i < keys.length; i++) {
			map.put(keys[i], values[i]);
		}
		Arrays.sort(keys);

		FST<BytesRef> ret = null;
		try {
			ByteSequenceOutputs outputs = ByteSequenceOutputs.getSingleton();
			Builder<BytesRef> builder = new Builder<BytesRef>(INPUT_TYPE.BYTE1, outputs);
			IntsRefBuilder scratchInts = new IntsRefBuilder();
			for (String key : keys) {
				BytesRef scratchBytes = new BytesRef(key);
				byte[] value = representation.toBytes(map.get(key));
				builder.add(Util.toIntsRef(scratchBytes, scratchInts), new BytesRef(value));
			}
			ret = builder.finish();
		} catch (IOException e) { }

		return ret;
	}

	public static void main(String[] args) throws IOException {

		// Create a FST<String>
		FSTSkeleton<String> stringSkeleton = new FSTSkeleton<String>();
		String inputValues[] = { "dog", "cat", "dogs" };
		String[] outputStringValues = { "1", "2", "3" };
		FST<BytesRef> fst = stringSkeleton.build(inputValues, outputStringValues);

		// Read values from the FST
		FSTRepresentation<String> stringRepresentation = new FSTRepresentation<String>();

		byte[] bytes = Util.get(fst, new BytesRef("dog")).bytes;
		String stringValue = stringRepresentation.fromBytes(bytes);
		System.out.println(stringValue.equals("1"));

		bytes = Util.get(fst, new BytesRef("cat")).bytes;
		stringValue = stringRepresentation.fromBytes(bytes);
		System.out.println(stringValue.equals("2"));

		bytes = Util.get(fst, new BytesRef("dogs")).bytes;
		stringValue = stringRepresentation.fromBytes(bytes);
		System.out.println(stringValue.equals("3"));

		// Create a FST<int[]>
		FSTSkeleton<int[]> intArraySkeleton = new FSTSkeleton<int[]>();
		inputValues = new String[] { "dog", "cat", "dogs" };
		int[][] outputIntArrayValues = {
				new int[] { 1, 2, 3 },
				new int[] { 2, 3, 1 },
				new int[] { 3, 1, 2 }
		};
		fst = intArraySkeleton.build(inputValues, outputIntArrayValues);

		// Read values from the FST
		FSTRepresentation<int[]> intArrayRepresentation = new FSTRepresentation<int[]>();

		bytes = Util.get(fst, new BytesRef("dog")).bytes;
		int[] intArrayValue = intArrayRepresentation.fromBytes(bytes);
		System.out.println(intArrayValue[0] == 1 && intArrayValue[1] == 2 && intArrayValue[2] == 3);

		bytes = Util.get(fst, new BytesRef("cat")).bytes;
		intArrayValue = intArrayRepresentation.fromBytes(bytes);
		System.out.println(intArrayValue[0] == 2 && intArrayValue[1] == 3 && intArrayValue[2] == 1);

		bytes = Util.get(fst, new BytesRef("dogs")).bytes;
		intArrayValue = intArrayRepresentation.fromBytes(bytes);
		System.out.println(intArrayValue[0] == 3 && intArrayValue[1] == 1 && intArrayValue[2] == 2);
	}

}
