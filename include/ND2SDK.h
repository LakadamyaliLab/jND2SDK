/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class ND2SDK */

#ifndef _Included_ND2SDK
#define _Included_ND2SDK
#ifdef __cplusplus
extern "C" {
#endif
#undef ND2SDK_LIMMAXBINARIES
#define ND2SDK_LIMMAXBINARIES 128L
#undef ND2SDK_LIMMAXPICTUREPLANES
#define ND2SDK_LIMMAXPICTUREPLANES 256L
#undef ND2SDK_LIMMAXEXPERIMENTLEVEL
#define ND2SDK_LIMMAXEXPERIMENTLEVEL 8L
#undef ND2SDK_LIMLOOP_TIME
#define ND2SDK_LIMLOOP_TIME 0L
#undef ND2SDK_LIMLOOP_MULTIPOINT
#define ND2SDK_LIMLOOP_MULTIPOINT 1L
#undef ND2SDK_LIMLOOP_Z
#define ND2SDK_LIMLOOP_Z 2L
#undef ND2SDK_LIMLOOP_OTHER
#define ND2SDK_LIMLOOP_OTHER 3L
#undef ND2SDK_LIMSTRETCH_QUICK
#define ND2SDK_LIMSTRETCH_QUICK 1L
#undef ND2SDK_LIMSTRETCH_SPLINES
#define ND2SDK_LIMSTRETCH_SPLINES 2L
#undef ND2SDK_LIMSTRETCH_LINEAR
#define ND2SDK_LIMSTRETCH_LINEAR 3L
/*
 * Class:     ND2SDK
 * Method:    Lim_FileOpenForRead
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileOpenForRead
  (JNIEnv *, jobject, jstring);

/*
 * Class:     ND2SDK
 * Method:    Lim_FileClose
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileClose
  (JNIEnv *, jobject, jint);

/*
 * Class:     ND2SDK
 * Method:    Lim_FileGetAttributes
 * Signature: (ILAttributes;)I
 */
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetAttributes
  (JNIEnv *, jobject, jint, jobject);

/*
 * Class:     ND2SDK
 * Method:    Lim_FileGetMetadata
 * Signature: (ILMetadataDesc;)I
 */
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetMetadata
  (JNIEnv *, jobject, jint, jobject);

/*
 * Class:     ND2SDK
 * Method:    Lim_FileGetTextinfo
 * Signature: (ILTextInfo;)I
 */
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetTextinfo
  (JNIEnv *, jobject, jint, jobject);

/*
 * Class:     ND2SDK
 * Method:    Lim_FileGetExperiment
 * Signature: (ILExperiment;)I
 */
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetExperiment
  (JNIEnv *, jobject, jint, jobject);

/*
 * Class:     ND2SDK
 * Method:    Lim_FileGetBinaryDescriptors
 * Signature: (ILBinaries;)I
 */
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetBinaryDescriptors
  (JNIEnv *, jobject, jint, jobject);

/*
 * Class:     ND2SDK
 * Method:    Lim_InitPicture
 * Signature: (LPicture;IIII)I
 */
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1InitPicture
  (JNIEnv *, jobject, jobject, jint, jint, jint, jint);

/*
 * Class:     ND2SDK
 * Method:    Lim_DestroyPicture
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_ND2SDK_Lim_1DestroyPicture
  (JNIEnv *, jobject);

/*
 * Class:     ND2SDK
 * Method:    Lim_GetSeqIndexFromCoords
 * Signature: ([I)I
 */
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1GetSeqIndexFromCoords
  (JNIEnv *, jobject, jintArray);

/*
 * Class:     ND2SDK
 * Method:    Lim_GetCoordsFromSeqIndex
 * Signature: (I)[I
 */
JNIEXPORT jintArray JNICALL Java_ND2SDK_Lim_1GetCoordsFromSeqIndex
  (JNIEnv *, jobject, jint);

/*
 * Class:     ND2SDK
 * Method:    Lim_FileGetImageData
 * Signature: (IILjava/nio/ByteBuffer;LLocalMetadata;)I
 */
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetImageData
  (JNIEnv *, jobject, jint, jint, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif
