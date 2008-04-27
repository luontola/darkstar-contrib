package net.gamalocus.sgs.adminclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.logging.Logger;

import net.gamalocus.sgs.adminclient.Packetizer.Type;

public class Assembler<T extends Serializable> implements Serializable
{
	private static final long serialVersionUID = -583149632820183298L;
	private final static Logger logger = Logger.getLogger(Assembler.class.getName());
	
	protected LinkedList<byte[]> parts = new LinkedList<byte[]>();
	int data_length = 0;
	
	public Assembler()
	{
	}

	public void append(byte[] part) {
		parts.add(part);
		data_length += part.length - 1;
	}
	
	public boolean isComplete() {
		byte[] last_part = parts.getLast();
		return last_part != null && last_part[0] == (byte)Type.TAIL.ordinal();
	}
	
	/**
	 * Extract complete object.
	 * @return Extracted object, or <code>null</code> if not complete.
	 */
	public T poll() throws IOException, ClassNotFoundException {
		if (!isComplete()) {
			return null;
		}
		
		// Assemble data parts
		// TODO Optimize by making custom ByteArrayInputStream that can read from 
		// a segmented buffer.
		byte[] data = new byte[data_length];
		int pos = 0;
		for (byte[] part : parts) {
			
			// TODO Perform optimized array copy?
			for (int i = 1; i < part.length; ++i) {
				data[pos] = part[i];
				++pos;
			}
		}
		
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			
			// Note this difficult way of doing it is to provide some control
			// over which class loader is used.
			ObjectInputStream object_in = getObjectInputStream(in);
			return (T)object_in.readObject();
		} finally {
			data_length = 0;
			parts.clear();
		}
	}
	
	
	protected ObjectInputStream getObjectInputStream(InputStream in) throws IOException
	{
		return new ObjectInputStream(in);
	}
}