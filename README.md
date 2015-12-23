# jND2SDK
Java Native Interface (JNI) to the nd2ReadSDK_v9 library for reading the Nikon ND2 image format.

All ND2 library dependencies that are required to compile ND2SDK.cpp and to load the library in your Java application can be downloaded from http://www.nd2sdk.com/ (requires a login). Nikon provides files for Windows x86/x64, Linux and Mac.

NOTE: the compiled lib file is loaded using **System.loadLibrary("ND2SDK");** so make sure that the filename of the compiled lib file is correct, or modify the string in the load library call.

Displaying a sample ND2 file in Test.java requires ImageJ to be in your Java build path (the ij-1.50e.jar is included in this repository). Alternatively, ImageJ can be downloaded from http://imagej.net/Downloads or you can clone the ImageJ repository from https://github.com/imagej/imagej1.git
