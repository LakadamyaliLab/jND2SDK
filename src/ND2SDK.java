import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.TreeMap;

/**
 * JNI implementation of the nd2ReadSDK_v9 library.
 * 
 * @author jborbely
 *
 */
public class ND2SDK {
	
	/** The file handle */
	public int hFile = 0;
	
	/** The image width, in pixels */
	public int width = 1;
	
	/** The image height, in pixels */
	public int height = 1;
	
	/** The number of channels */
	public int numChannels = 1;

	/** The number of slices */
	public int numSlices = 1;
	
	/** The number of frames */
	public int numFrames = 1;
	
	/** See {@link Attributes} */
	public final Attributes attribs = new Attributes();
	
	/** See {@link MetadataDesc} */
	public final MetadataDesc metaDesc = new MetadataDesc();
	
	/** See {@link TextInfo} */
	public final TextInfo textInfo = new TextInfo();
	
	/** See {@link Experiment} */
	public final Experiment experiment = new Experiment();
	
	/** See {@link Binaries} */
	public final Binaries binaries = new Binaries();
	
	/** See {@link Picture} */
	public final Picture picture = new Picture();
	
	/** See {@link LocalMetadata} */
	public final LocalMetadata imgInfo = new LocalMetadata();
	
	/** Whether the ND2SDK library was successfully loaded. */
	public static boolean libLoaded = true;
	
	/** The error message that was reported if the library 
	 * was not successfully loaded. */
	public static String libLoadedErrorMsg = "";

	/** Contains the raw image data for a particular SeqIndex */
	private ByteBuffer pictureBuffer;
	
	// #define constants that are in nd2ReadSDK.h
	public static final int LIMMAXBINARIES = 128;
	public static final int LIMMAXPICTUREPLANES = 256;
	public static final int LIMMAXEXPERIMENTLEVEL = 8;
	
	public static final int LIMLOOP_TIME = 0;
	public static final int LIMLOOP_MULTIPOINT = 1;
	public static final int LIMLOOP_Z = 2;
	public static final int LIMLOOP_OTHER = 3;
	
	public static final int LIMSTRETCH_QUICK = 1;
	public static final int LIMSTRETCH_SPLINES = 2;
	public static final int LIMSTRETCH_LINEAR = 3;

	static {
		try {
			System.loadLibrary("ND2SDK");
		} catch (Throwable t) {
			libLoadedErrorMsg = t.getMessage();			
			libLoaded = false;
		}
	}

	/**
	 * Opens the ND2 file, reads the {@link Attributes}, {@link MetadataDesc},
	 * {@link TextInfo}, {@link Experiment}, {@link Binaries} and 
	 * initializes the {@link Picture}.
	 *  
	 * @param filename the path to the ND2 file.
	 * @throws IOException if there was an error in a native method
	 * @see #deinitialize()
	 */
	public void initialize(final String filename) throws IOException {
		if (!libLoaded) throw new IOException(libLoadedErrorMsg);
		
		// open the file
		hFile = Lim_FileOpenForRead(filename);
		if (hFile == 0) throw new IOException("Cannot open " + filename);
		
		// read the information about this ND experiment
		check( Lim_FileGetAttributes(hFile, attribs) );
		check( Lim_FileGetTextinfo(hFile, textInfo) );
		check( Lim_FileGetMetadata(hFile, metaDesc) );
		check( Lim_FileGetExperiment(hFile, experiment) );
		check( Lim_FileGetBinaryDescriptors(hFile, binaries) );
		
		// update the public-accessible values
		width = attribs.uiWidth;
		height = attribs.uiHeight;
		numChannels = attribs.uiComp;
		for (int i = 0; i < experiment.uiLevelCount; i++) {
			final ExperimentLevel exp = experiment.pAllocatedLevels[i];
			switch (exp.uiExpType) {
				case LIMLOOP_TIME: 
					numFrames = exp.uiLoopSize; 
					break;
				case LIMLOOP_Z: 
					numSlices = exp.uiLoopSize; 
					break;
			}
		}
		
		// initialize the picture
		Lim_InitPicture(picture, attribs.uiWidth, attribs.uiHeight, attribs.uiBpcInMemory, attribs.uiComp);		
		pictureBuffer = ByteBuffer.allocateDirect(picture.uiSize);
		pictureBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	/**
	 * Call this method when you are finished with the ND2 file.
	 * 
	 * <p>Free the allocated memory for the {@link Picture} (in the SDK library) 
	 * and close the ND2 file reference.</p>
	 * 
	 * @see #initialize(String)
	 */
	public void deinitialize() {
		if (!libLoaded || hFile == 0) return;
		Lim_DestroyPicture();
		Lim_FileClose(hFile);
		hFile = 0;
	}
	
	/**
	 * Returns all the metadata that is found in the following structures:
	 * <ul>
	 * <li>{@link Attributes}</li>
	 * <li>{@link MetadataDesc}</li>
	 * <li>{@link TextInfo}</li>
	 * <li>{@link ExperimentLevel}</li>
	 * <li>{@link BinaryDescriptor}</li>
	 * </ul>
	 * @throws IOException if the is an error calling the native method
	 */
	public Map<String, Object> metadata() throws IOException {
		if (hFile == 0) throw new IOException("An ND2 file has not been initialized yet");
		
		final Map<String, Object> map = new TreeMap<String, Object>();
		
		// add the Attribute fields
		map.put("uiWidth", attribs.uiWidth);
		map.put("uiWidthBytes", attribs.uiWidthBytes);
		map.put("uiHeight", attribs.uiHeight);
		map.put("uiComp", attribs.uiComp);
		map.put("uiBpcInMemory", attribs.uiBpcInMemory);
		map.put("uiBpcSignificant", attribs.uiBpcSignificant);
		map.put("uiSequenceCount", attribs.uiSequenceCount);
		map.put("uiTileWidth", attribs.uiTileWidth);
		map.put("uiTileHeight", attribs.uiTileHeight);
		map.put("uiCompression", attribs.uiCompression);
		map.put("uiQuality", attribs.uiQuality);

		// add the MetadataDesc fields
		map.put("dTimeStart", metaDesc.dTimeStart);
		map.put("dAngle", metaDesc.dAngle);
		map.put("dCalibration", metaDesc.dCalibration);
		map.put("dAspect", metaDesc.dAspect);
		map.put("wszObjectiveName", metaDesc.wszObjectiveName);
		map.put("dObjectiveMag", metaDesc.dObjectiveMag);
		map.put("dObjectiveNA", metaDesc.dObjectiveNA);
		map.put("dRefractIndex1", metaDesc.dRefractIndex1);
		map.put("dRefractIndex2", metaDesc.dRefractIndex2);
		map.put("dPinholeRadius", metaDesc.dPinholeRadius);
		map.put("dZoom", metaDesc.dZoom);
		map.put("dProjectiveMag", metaDesc.dProjectiveMag);
		map.put("uiImageType", metaDesc.uiImageType);
		map.put("uiComponentCount", metaDesc.uiComponentCount);
		map.put("uiPlaneCount", metaDesc.uiPlaneCount);
		for (int i = 0; i < metaDesc.uiPlaneCount; i++) {
			final StringBuffer sb = new StringBuffer();
			sb.append("{uiCompCount}: " + metaDesc.pPlanes[i].uiCompCount + " ");
			sb.append("{uiColorRGB}: "  + metaDesc.pPlanes[i].uiColorRGB  + " ");
			sb.append("{dEmissionWL}: " + metaDesc.pPlanes[i].dEmissionWL + " ");
			sb.append("{wszName}: "     + metaDesc.pPlanes[i].wszName     + " ");
			sb.append("{wszOCName}: "   + metaDesc.pPlanes[i].wszOCName);
			map.put("uiPlaneCountIndex_" + i, sb);
		}

		// add the Experiment fields
		map.put("experimentDimension", experiment.uiLevelCount);
		for (int i = 0; i < experiment.uiLevelCount; i++) {
			final StringBuffer sb = new StringBuffer();
			sb.append("{uiExpType}: "  + experiment.pAllocatedLevels[i].uiExpType  + " ");
			sb.append("{uiLoopSize}: " + experiment.pAllocatedLevels[i].uiLoopSize + " ");
			sb.append("{dInterval}: "  + experiment.pAllocatedLevels[i].dInterval);
			map.put("experimentDimension_" + i, sb);
		}

		// add the Binary Descriptors fields	
		map.put("binaryLayers", binaries.uiCount);
		for (int i = 0; i < binaries.uiCount; i++) {
			final StringBuffer sb = new StringBuffer();
			sb.append("{wszName}: "      + binaries.pDescriptors[i].wszName     + " ");
			sb.append("{wszCompName}: "  + binaries.pDescriptors[i].wszCompName + " ");
			sb.append("{uiColorRGB}: " + binaries.pDescriptors[i].uiColorRGB);
			map.put("binaryLayer_" + i, sb);
		}

		// add the TextInfo fields
		map.put("wszImageID", textInfo.wszImageID);
		map.put("wszType", textInfo.wszType);
		map.put("wszGroup", textInfo.wszGroup);
		map.put("wszSampleID", textInfo.wszSampleID);
		map.put("wszAuthor", textInfo.wszAuthor);
		map.put("wszSampling", textInfo.wszSampling);
		map.put("wszLocation", textInfo.wszLocation);
		map.put("wszDate", textInfo.wszDate);
		map.put("wszConclusion", textInfo.wszConclusion);
		map.put("wszInfo1", textInfo.wszInfo1);
		map.put("wszInfo2", textInfo.wszInfo2);
		map.put("wszOptics", textInfo.wszOptics);
		final String[] txt = (textInfo.wszCapturing + "\n" + textInfo.wszDescription).split("\n");
		for (int i = 0; i < txt.length; i++) {
			final String[] kv = txt[i].split(":");
			if (kv.length > 1) {
				if (kv[0].contains("\t")) {					
					// then this is probably a multi-line key-value pair
					final String[] newKV = kv[0].trim().split("\t");
					if (newKV.length > 1) {
						final String key = newKV[0].trim();
						String value;
						if (i + 1 < txt.length) {
							if (kv.length == 3) {
								value = newKV[1].trim() + ": " + kv[1].trim() + ": " + kv[2].trim() + " " + txt[i+1].trim(); 
							} else {
								value = newKV[1].trim() + ": " + kv[1].trim() + " " + txt[i+1].trim();
							}
							map.put(key, value);
						}
					}
				} else {
					String key = kv[0].trim();
					if (key.startsWith("{")) {
						key = key.substring(1, key.length() -1 );
					}
					final String value = kv[1].trim();
					if (value.length() == 0) continue;
					try {
						map.put(key, Integer.parseInt(value));
					} catch (NumberFormatException e) {
						try {
							map.put(key, Double.parseDouble(value));
						} catch (Throwable t) {
							map.put(key, value);
						}
					}
				}
			}
		}

		return map;
	}
	
	/**
	 * Returns the {@link Picture} bytes for the specified sequence index. The size
	 * of the ByteBuffer is equal to {@code uiWidthBytes * uiHeight} from the 
	 * {@link #attribs} object.
	 * 
	 * <p>This method also updates the values of {@link #imgInfo} for the specified index
	 * so that once you call this method you have access to the timestamp (relative to 
	 * the first frame) and the X, Y, Z position of the microscope stage.</p>
	 * 
	 * @param uiSeqIndex the sequence index
	 * @throws IOException if there was an error in the native method
	 */
	public ByteBuffer getSeqBytes(int uiSeqIndex) throws IOException {
		if (hFile == 0) 
			throw new IOException("An ND2 file has not been initialized yet");
		
		if ( (uiSeqIndex < 0) || (uiSeqIndex >= attribs.uiSequenceCount) )
			throw new IOException(String.format("Invalid uiSeqIndex value of %d. "
					+ "Value must be >= 0 and < %d", uiSeqIndex, attribs.uiSequenceCount));
		
		pictureBuffer.position(0);
		check( Lim_FileGetImageData(hFile, uiSeqIndex, pictureBuffer, imgInfo) );
		return pictureBuffer;
	}
	
	/**
	 * Check the ND2SDK command for an error.
	 * 
	 * @param limResult the returned LIMRESULT value from a method in nd2ReadSDK.h
	 * @throws IOException if there was an error
	 */
	public void check(final int limResult) throws IOException {
		if (limResult < 0)
			throw new IOException(getClass() + ErrorStatus.getMessage(limResult));
	}
	
	/** Used for error reporting */
	private enum ErrorStatus {
		Lim_OK(0),
		Lim_ERR_UNEXPECTED(-1),
		Lim_ERR_NOTIMPL(-2),
		Lim_ERR_OUTOFMEMORY(-3),
		Lim_ERR_INVALIDARG(-4),
		Lim_ERR_NOINTERFACE(-5),
		Lim_ERR_POINTER(-6),
		Lim_ERR_HANDLE(-7),
		Lim_ERR_ABORT(-8),
		Lim_ERR_FAIL(-9),
		Lim_ERR_ACCESSDENIED(-10),
		Lim_ERR_OS_FAIL(-11),
		Lim_ERR_NOTINITIALIZED(-12),
		Lim_ERR_NOTFOUND(-13),
		Lim_ERR_IMPL_FAILED(-14),
		Lim_ERR_DLG_CANCELED(-15),
		Lim_ERR_DB_PROC_FAILED(-16),
		Lim_ERR_OUTOFRANGE(-17),
		Lim_ERR_PRIVILEGES(-18),
		Lim_ERR_VERSION(-19);

		private int code;

		private ErrorStatus(int code) {
			this.code = code;
		}
		
		private int getCode() {
			return code;
		}
		
		public static String getMessage(int errVal) {
			final ErrorStatus[] values = ErrorStatus.values();
			for (ErrorStatus value : values) {
				if (errVal == value.getCode()) {
					return value.name();
				}
			}
			return "UNKNOWN_ERROR";
		}	        
       
	}
	
	/*
	 * 
	 * Define the native methods found in nd2ReadSDK.h
	 * 
	 */
	
	/**
	 * This function opens the ND2 file for reading. It must be called before 
	 * using the other functions.
	 * 
	 * @param filename the path to the ND2 file.
	 * @return the file handle
	 */
	public native int Lim_FileOpenForRead(String filename);
	
	/**
	 * Closes the current ND2 file.
	 * 
	 * @param hFile the file handle
	 * @return LIMRESULT, an error-checking value for whether the native call 
	 * was successful
	 */
	public native int Lim_FileClose(int hFile);

	/**
	 * Reads the attributes of the ND2 file. See {@link Attributes}.
	 * 
	 * @param hFile the file handle
	 * @param pFileAttributes the attributes to populate the values of
	 * @return LIMRESULT, an error-checking value for whether the native call was successful
	 */
	public native int Lim_FileGetAttributes(int hFile, Attributes pFileAttributes);
	
	/**
	 * Reads the metadata of the ND2 file (see {@link MetadataDesc}). Additional 
	 * metadata can be read from {@link #Lim_FileGetTextinfo}.
	 * 
	 * @param hFile the file handle
	 * @param pFileMetadata the metadata to populate the values of
	 * @return LIMRESULT, an error-checking value for whether the native call was successful
	 */
	public native int Lim_FileGetMetadata(int hFile, MetadataDesc pFileMetadata);
	
	/**
	 * This function reads additional text metadata info from the ND2 file
	 * (see {@link TextInfo}). Additional metadata can be read from 
	 * {@link #Lim_FileGetMetadata}.
	 * 
	 * @param hFile the file handle
	 * @param pFileTextinfo the text metadata to populate the values of
	 * @return LIMRESULT, an error-checking value for whether the native call was successful
	 */
	public native int Lim_FileGetTextinfo(int hFile, TextInfo pFileTextinfo);
	
	/**
	 * This function reads information about the original ND experiment.
	 * 
	 * @param hFile the file handle
	 * @param pFileExperiment the {@link Experiment} struct to fill in
	 * @return LIMRESULT, an error-checking value for whether the native call was successful
	 */
	public native int Lim_FileGetExperiment(int hFile, Experiment pFileExperiment);
	
	/**
	 * This function reads information about binary layers contained within the ND2 file.
	 * 
	 * @param hFile the file handle
	 * @param pBinaries the {@link Binaries} struc to fill in
	 * @return LIMRESULT, an error-checking value for whether the native call was successful
	 */
	public native int Lim_FileGetBinaryDescriptors(int hFile, Binaries pBinaries);

	/**
	 * This function fills the {@link Picture} structure.
	 * 
	 * @param pPicture the structure to fill
	 * @param width the image width (use {@link Attributes#uiWidth})
	 * @param height the image height (use {@link Attributes#uiHeight})
	 * @param bpc the bits per component (use {@link Attributes#uiBpcSignificant})
	 * @param components the number of physical components (use {@link Attributes#uiComp})
	 * @return the size (number of bytes) of the picture
	 */
	public native int Lim_InitPicture(Picture pPicture, int width, int height, int bpc, int components);

	/**
	 * This function deallocates memory (from the DLL) that is used to store information 
	 * about the current image. Use this function when you are finished working with 
	 * the current ND2 file.
	 */
	public native void Lim_DestroyPicture();

	/**
	 * Returns the sequence index of a frame based on the given coordinates 
	 * within the ND experiment structure.
	 * 
	 * @param coords a 4-element vector: {time, multipoint, z, other}
	 * @see #Lim_GetCoordsFromSeqIndex(int)
	 */
	public native int Lim_GetSeqIndexFromCoords(int[] coords);

	/**
	 * Returns the coordinates of a frame within the ND experiment structure based 
	 * on the sequence index.
	 * 
	 * <p>The returned array contains 4 elements, where the indices correspond to</br>
	 *  0 &rarr; Position in time ({@link #LIMLOOP_TIME})</br>
	 *  1 &rarr; Position in multipoint ({@link #LIMLOOP_MULTIPOINT})</br>
	 *  2 &rarr; Position in z ({@link #LIMLOOP_Z})</br>
	 *  3 &rarr; Position in the custom loop ({@link #LIMLOOP_OTHER})</br>
	 * </p>
	 * 
	 * @param uiSeqIdx the sequence index
	 * @see #Lim_GetSeqIndexFromCoords(int[])
	 */
	public native int[] Lim_GetCoordsFromSeqIndex(int uiSeqIdx);
	
	/**
	 * Gets the raw image bytes for the specified {@code uiSeqIndex}
	 * 
	 * @param hFile the file handle
	 * @param uiSeqIndex the sequence index
	 * @param buffer a {@link ByteBuffer} that has memory which has been directly allocated 
	 * (i.e, {@code ByteBuffer.allocateDirect(}{@link Picture picture}{@code .uiSize)});
	 * @param pImgInfo updates the information about the relative timestamp and the XYZ 
	 * position of the microscope stage for the specified {@code uiSeqIndex} into this object 
	 * @return LIMRESULT, an error-checking value for whether the native call was successful
	 * @see #Lim_GetSeqIndexFromCoords(int[])
	 */
	public native int Lim_FileGetImageData(int hFile, int uiSeqIndex, ByteBuffer buffer, LocalMetadata pImgInfo);
	
	/*
	 * Methods that are not yet implemented
	 * 
	 */
	
	//LIMFILEAPI LIMRESULT Lim_FileGetImageRectData(LIMFILEHANDLE hFile, LIMUINT uiSeqIndex, LIMUINT uiDstTotalW, LIMUINT uiDstTotalH, LIMUINT uiDstX, LIMUINT uiDstY, LIMUINT uiDstW, LIMUINT uiDstH, void* pBuffer, LIMUINT uiDstLineSize, LIMINT iStretchMode, LIMLOCALMETADATA* pImgInfo);
	//LIMFILEAPI LIMRESULT Lim_FileGetBinary(LIMFILEHANDLE hFile, LIMUINT uiSequenceIndex, LIMUINT uiBinaryIndex, LIMPICTURE* pPicture);
	//LIMFILEAPI LIMRESULT Lim_GetMultipointName(LIMFILEHANDLE hFile, LIMUINT uiPointIdx, LIMWSTR wstrPointName);
	//LIMFILEAPI LIMRESULT Lim_GetLargeImageDimensions(LIMFILEHANDLE hFile, LIMUINT* puiXFields, LIMUINT* puiYFields, double* pdOverlap);
	//LIMFILEAPI LIMRESULT Lim_GetRecordedDataInt(LIMFILEHANDLE hFile, LIMCWSTR wszName, LIMINT uiSeqIndex, LIMINT *piData);
	//LIMFILEAPI LIMRESULT Lim_GetRecordedDataDouble(LIMFILEHANDLE hFile, LIMCWSTR wszName, LIMINT uiSeqIndex, double* pdData);
	//LIMFILEAPI LIMRESULT Lim_GetRecordedDataString(LIMFILEHANDLE hFile, LIMCWSTR wszName, LIMINT uiSeqIndex, LIMWSTR wszData);
	//LIMFILEAPI LIMRESULT Lim_GetNextUserEvent(LIMFILEHANDLE hFile, LIMUINT *puiNextID, LIMFILEUSEREVENT* pEventInfo);
}

/** 
 * The LIMATTRIBUTES struct found in nd2ReadSDK.h 
 * <pre>
 * int uiWidth;             // Width of images, in pixels
 * int uiWidthBytes;        // Line length 4-byte aligned
 * int uiHeight;            // Height of images, in pixels
 * int uiComp;              // Number of components
 * int uiBpcInMemory;       // Bits per component 8, 16 or 32 (for float image)
 * int uiBpcSignificant;    // Bits per component used 8 .. 16 or 32 (for float image)
 * int uiSequenceCount;     // Number of images in the sequence
 * int uiTileWidth;         // If an image is tiled then the width of the tile/strip, otherwise zero
 * int uiTileHeight;        // If an image is tiled then the height of the tile/strip, otherwise zero
 * int uiCompression;       // 0 (lossless), 1 (lossy), 2 (None)
 * int uiQuality;           // 0 (worst) - 100 (best)
 * </pre>
 */
class Attributes {
	/** Width of images, in pixels */
	int uiWidth;
	/** Line length 4-byte aligned */
	int uiWidthBytes;
	/** Height of images, in pixels */
	int uiHeight;
	/** Number of components */
	int uiComp;
	/** Bits per component 8, 16 or 32 (for float image) */
	int uiBpcInMemory;
	/** Bits per component used 8 .. 16 or 32 (for float image) */
	int uiBpcSignificant;
	/** Number of images in the sequence */
	int uiSequenceCount;
	/** If an image is tiled then the width of the tile/strip, otherwise zero */
	int uiTileWidth;
	/** If an image is tiled then the height of the tile/strip, otherwise zero */
	int uiTileHeight;
	/** 0 (lossless), 1 (lossy), 2 (None) */
	int uiCompression;
	/** 0 (worst) - 100 (best) */
	int uiQuality;
}

/** 
 * The LIMMETADATA_DESC struct found in nd2ReadSDK.h 
 * <pre>
 * double dTimeStart;          // Absolute Time in JDN
 * double dAngle;              // Camera Angle
 * double dCalibration;        // um/px (0.0 = uncalibrated)
 * double dAspect;             // pixel aspect (always 1.0)
 * String wszObjectiveName;    // The name of the objective
 * double dObjectiveMag;       // Optional additional information
 * double dObjectiveNA;        // dCalibration takes into accont all these
 * double dRefractIndex1;
 * double dRefractIndex2;
 * double dPinholeRadius;
 * double dZoom;
 * double dProjectiveMag;
 * int uiImageType;            // 0 (normal), 1 (spectral)
 * int uiPlaneCount;           // Number of logical planes (uiPlaneCount <= uiComponentCount)
 * int uiComponentCount;       // Number of physical components (same as uiComp in LIMFILEATTRIBUTES)
 * {@link PicturePlaneDesc}[] pPlanes;
 * </pre>
 */
class MetadataDesc {
	/** Absolute Time in JDN */
	double dTimeStart;
	/** Camera Angle */
	double dAngle;
	/** um/px (0.0 = uncalibrated) */
	double dCalibration; 
	/** pixel aspect (always 1.0) */
	double dAspect;
	/** The name of the objective */
	String wszObjectiveName;
	double dObjectiveMag;       // Optional additional information
	double dObjectiveNA;        // dCalibration takes into account all these
	double dRefractIndex1;
	double dRefractIndex2;
	double dPinholeRadius;
	double dZoom;
	double dProjectiveMag;
	/** 0 (normal), 1 (spectral) */
	int uiImageType; 
	/** Number of logical planes (uiPlaneCount <= uiComponentCount) */
	int uiPlaneCount; 
	/** Number of physical components (same as uiComp in {@link Attributes}) */
	int uiComponentCount;       //
	PicturePlaneDesc[] pPlanes;
}

/** 
 * The LIMPICTUREPLANE_DESC struct found in nd2ReadSDK.h
 * <pre>
 * int uiCompCount;    // Number of physical components
 * int uiColorRGB;     // RGB color for display 0xBBGGRR
 * String wszName;     // Name for display
 * String wszOCName;   // Name of the Optical Configuration
 * double dEmissionWL; // The emission wavelength
 * </pre>
 */
class PicturePlaneDesc {
	/** Number of physical components */
	int uiCompCount;
	/** RGB color for display 0xBBGGRR */
	int uiColorRGB;
	/** Name for display */
	String wszName;  
	/** Name of the Optical Configuration */
	String wszOCName;
	/** The emission wavelength */
	double dEmissionWL;
}

/** 
 * The LIMTEXTINFO struct found in nd2ReadSDK.h 
 * <pre>
 * String wszImageID;
 * String wszType;
 * String wszGroup;
 * String wszSampleID;
 * String wszAuthor;
 * String wszDescription;
 * String wszCapturing;
 * String wszSampling;
 * String wszLocation;
 * String wszDate;
 * String wszConclusion;
 * String wszInfo1;
 * String wszInfo2;
 * String wszOptics;
 * </pre>
 */
class TextInfo {
	String wszImageID;
	String wszType;
	String wszGroup;
	String wszSampleID;
	String wszAuthor;
	String wszDescription;
	String wszCapturing;
	String wszSampling;
	String wszLocation;
	String wszDate;
	String wszConclusion;
	String wszInfo1;
	String wszInfo2;
	String wszOptics;
}

/** 
 * The LIMEXPERIMENT struct found in nd2ReadSDK.h
 * <pre>
 * int uiLevelCount;                    // Number of dimensions excluding Lambda
 * {@link ExperimentLevel}[] pAllocatedLevels;  // An array containing information about number of frames within each dimension.
 * </pre>
 */
class Experiment {
	/** Number of dimensions excluding Lambda. */
	int uiLevelCount;
	/** An array containing information about number of frames within each dimension. */
	ExperimentLevel[] pAllocatedLevels; 
}

/** 
 * The LIMEXPERIMENTLEVEL struct found in nd2ReadSDK.h 
 * <pre>
 * int uiExpType;    // Dimension type, see the LIMLOOP_* constant values
 * int uiLoopSize;   // Number of images in the loop
 * double dInterval; // ms (for Time), um (for ZStack), -1.0 (for Multipoint)
 * </pre>
 */
class ExperimentLevel {
	/** Dimension type, see the LIMLOOP_* constant values. */
	int uiExpType;
	/** Number of images in the loop. */
	int uiLoopSize;
	/** ms (for Time), um (for ZStack), -1.0 (for Multipoint) */
	double dInterval; 
}

/** 
 * The LIMBINARIES struct found in nd2ReadSDK.h 
 * <pre>
 * int uiCount;                              // Number of binary layers
 * {@link BinaryDescriptor}[] pDescriptors;  // Describes a binary layer: name, associated component, color 
 * </pre>
 */
class Binaries {
	/** Number of binary layers */
	int uiCount;
	/** Describes a binary layer: name, associated component, color. */
	BinaryDescriptor[] pDescriptors;
}

/** 
 * The LIMBINARIES struct found in nd2ReadSDK.h
 * <pre>
 * String wszName;     // Name of binary layer
 * String wszCompName; // Name of component, or empty string if this binary layer is unbound
 * int uiColorRGB;     // Color of the binary layer
 * </pre> 
 */
class BinaryDescriptor {
	/** Name of binary layer. */
	String wszName;
	/** Name of component, or empty string if this binary layer is unbound. */
	String wszCompName;
	/** Color of the binary layer. */
	int uiColorRGB;
}

/** 
 * The LIMPICTURE struct found in nd2ReadSDK.h
 * <pre>
 * int uiWidth;        // Width of the image, in pixels
 * int uiHeight;       // Height of the image, in pixels
 * int uiBitsPerComp;  // Number of bits per component (8, 10, 12, 14, 16). For binary images, use 32 bits 
 * int uiComponents;   // Number of components in every pixel (any number up to 160 1:mono, 3:RGB)
 * int uiWidthBytes;   // Aligned to 4-byte (like windows BITMAP)
 * int uiSize;         // Size of the image in memory (= uiWidthBytes * uiHeight)
 * long pImageData;    // Java version of a void pointer to the image data
 * </pre>  
 */
class Picture {
	/** Width of the image, in pixels. */
	int uiWidth;
	/** Height of the image, in pixels. */
	int uiHeight;
	/** Number of bits per component (8, 10, 12, 14, 16). For binary images, use 32 bits. */
	int uiBitsPerComp;
	/** Number of components in every pixel (any number up to 160 1:mono, 3:RGB). */
	int uiComponents;
	/** Aligned to 4-byte (like windows BITMAP). */
	int uiWidthBytes;
	/** Size of the image in memory (= uiWidthBytes * uiHeight). */
	int uiSize;
	/** Java version of a void pointer to the image data. */
	long pImageData;
}

/** 
 * The LIMLOCALMETADATA struct found in nd2ReadSDK.h
 * <pre>
 * double dTimeMSec;   // Relative time msec from the first
 * double dXPos;       // Stage XPos
 * double dYPos;       // Stage YPos
 * double dZPos;       // Stage ZPos
 * </pre>  
 */
class LocalMetadata {
	/** Relative time msec from the first */
	double dTimeMSec;
	/** Stage XPos */
	double dXPos;
	/** Stage YPos */
	double dYPos;
	/** Stage ZPos */
	double dZPos;
}
