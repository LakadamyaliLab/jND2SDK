import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.HyperStackConverter;
import ij.process.ShortProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public class Test {
	
	public static void main(String[] args) {
				
		final String filename = "D:/samples/sample.nd2";
		
		final ND2SDK nd2 = new ND2SDK();

		try {
			
			// initialize the ND2 file
			nd2.initialize(filename);
			
			// view all of the metadata for this file
			final Map<String, Object> meta = nd2.metadata();
			System.out.println("------ Metadata -------");
			for (Map.Entry<String, Object> entry : meta.entrySet()) {
				System.out.println(entry.getKey() + ": " + entry.getValue());
			}

			// example for converting between Coords and SeqIndex
			System.out.println("------- SeqIndex -------");
			final int[] coordsIn = {1, 0, 0, 0};
			final int seqIndex = nd2.Lim_GetSeqIndexFromCoords(coordsIn);
			System.out.println("seqIndex: " + seqIndex);

			System.out.println("-------- Coords --------");
			final int[] coordsOut = nd2.Lim_GetCoordsFromSeqIndex(seqIndex);
			System.out.println("time....... " + coordsOut[0]);
			System.out.println("multipoint. " + coordsOut[1]);
			System.out.println("z.......... " + coordsOut[2]);
			System.out.println("other...... " + coordsOut[3]);

			// plot the ND2 file in ImageJ (assumes that the sample ND2 file is 16-bit and it is not RGB)
			new ImageJ();
			final ImageStack stack = new ImageStack(nd2.width, nd2.height);

			final int bytesPerComp = nd2.attribs.uiBpcInMemory / 8;
			final int shift = nd2.picture.uiComponents * bytesPerComp;
			
			System.out.println("----- Sequence Info ----");
			for (int seq = 0; seq < nd2.attribs.uiSequenceCount; seq++) {
				
				// get the bytes for this sequence index
				final ByteBuffer bb = nd2.getSeqBytes(seq);
				
				// print the time stamp and the XYZ position of the stage for this sequence index
				System.out.print("SeqIndex= " + seq + "; ");				
				System.out.print(String.format("T[sec]= %.3f; ", nd2.imgInfo.dTimeMSec * 1e-3));
				System.out.print(String.format("X= %.3f; ", nd2.imgInfo.dXPos));
				System.out.print(String.format("Y= %.3f; ", nd2.imgInfo.dYPos));
				System.out.println(String.format("Z= %.3f", nd2.imgInfo.dZPos));
				
				// create and fill in the ShortProcessor with the pixel data
				for (int c = 0; c < nd2.picture.uiComponents; c++) {
					final ShortProcessor sp = new ShortProcessor(nd2.width, nd2.height);
					int i = c * bytesPerComp;
					for (int y = 0; y < nd2.height; y++) {
						for (int x = 0; x < nd2.width; x++) {
							sp.set(x, y, bb.getShort(i));
							i += shift;
						}
					}
					stack.addSlice(sp);
				}
			}
						
			// show the stack or the hyperstack (if applicable)
			final ImagePlus imp = new ImagePlus(filename, stack);
			if (nd2.numChannels * nd2.numSlices * nd2.numFrames == stack.size() ) {
				HyperStackConverter.toHyperStack(imp, nd2.numChannels, nd2.numSlices, nd2.numFrames, "default", "Color").show();
			} else {
				imp.show();
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		} finally {
			// when finished using the ND2 file, deinitialize it to free the library resources
			nd2.deinitialize(); 
		}

	}
		   
}
