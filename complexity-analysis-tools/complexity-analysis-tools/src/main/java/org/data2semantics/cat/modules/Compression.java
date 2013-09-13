package org.data2semantics.cat.modules;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.lilian.util.GZIPCompressor;

/**
 * 
 * @author Peter
 *
 */
@Module(name="GZIP Compression test")
public class Compression
{
	@In(name="file")
	public String file;

	@Main()
	public void run()
	{
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(new File(file)));
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ByteArrayOutputStream boutRaw = new ByteArrayOutputStream();
			OutputStream out = new GZIPOutputStream(bout);
			
			int bt = 0;
			while(bt != -1) {
				bt = in.read();
				if(bt != -1)
				{
					out.write(bt);
					boutRaw.write(bt);
				}
			} 
			
			out.flush();
			out.close();
			
			bits = bout.toByteArray().length * 8.0;
			rawBits = boutRaw.toByteArray().length * 8.0;
			
		} catch (IOException e){
			throw new RuntimeException(e);
		}
	}
	
	@Out(name="compressed size", description="Size of the compressed dataset in bits")
	public double bits;
	
	@Out(name="compressed size", description="Size of the compressed dataset in bits")
	public double rawBits;

}
