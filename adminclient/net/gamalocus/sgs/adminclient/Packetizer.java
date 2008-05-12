package net.gamalocus.sgs.adminclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Container for packetizing a serialized object.
 *  
 * @author j0rg3n
 */
public class Packetizer<T extends Serializable> implements Iterable<ByteBuffer>
{
	/**
	 * A single-part packet contains only the tail, while receiving a PART 
	 * indicates that there's more to come.
	 */
	public enum Type {
		PART, TAIL
	}

	private byte[] data;
	private int packet_size;
	
	public Packetizer(T content, int packet_size) throws IOException {
		this.packet_size = packet_size;
		this.data = serialize(content);
	}
	
	private byte[] serialize(T content) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream object_out = new ObjectOutputStream(out);
		object_out.writeObject(content);
		object_out.close();
		return out.toByteArray();
	}

	public Iterator<ByteBuffer> iterator()
	{
		return new Iterator<ByteBuffer>() {

			protected int packet_number = 0;
			
			public boolean hasNext()
			{
				return getPacketStart() < data.length;
			}

			private int getPacketStart()
			{
				return packet_number * packet_size;
			}

			public ByteBuffer next()
			{
				int pos = getPacketStart();

				byte tag = (byte)Type.TAIL.ordinal();
				int copy_count = data.length - pos;
				
				// Prune if longer than allowed packet size
				if (copy_count > packet_size) {
					tag = (byte)Type.PART.ordinal();
					copy_count = packet_size;
				}
				
				ByteBuffer part = ByteBuffer.wrap(new byte[copy_count + 1]);
				
				/// TODO Do optimized array copy?
				part.put(tag);
				part.put(data, pos, copy_count);
				/*
				for (int j = 1; j < copy_count + 1; ++j) {
					part[j] = data[pos];
					++pos;
				}
				*/
				
				++packet_number;
				part.rewind();
				return part;
			}

			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}
