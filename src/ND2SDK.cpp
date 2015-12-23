// ND2SDK.cpp
//
// Java Native Interface (JNI) to the nd2ReadSDK_v9 library, that is used in 
// combination with ND2SDK.java, for reading the Nikon ND2 image format.
// 
// All library dependencies (for Windows, Mac and Linux) can be downloaded from
// http://www.nd2sdk.com/
//
// Date: 16 Dec 2015
// Author: Joe Borbely
// Email: joe.borbely@icfo.es
//

#include <jni.h>
#include <iostream>
#include "ND2SDK.h"
#include "nd2ReadSDK.h"

LIMPICTURE cPicture;
LIMEXPERIMENT cExperiment;

// Implementation of Lim_FileOpenForRead()
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileOpenForRead(JNIEnv *env, jobject obj, jstring filename) {

    // convert jstring to char *
    const char *raw = env->GetStringUTFChars(filename, NULL);

    // convert char * to wchar_t *
    jsize len = env->GetStringUTFLength(filename) + 1;
    wchar_t* wc = new wchar_t[len];
    mbstowcs(wc, raw, len);	

    // open the file using the ND2SDK
    LIMFILEHANDLE lmf_handle = Lim_FileOpenForRead(wc);

    // release resources
    env->ReleaseStringUTFChars(filename, raw);
    delete[] wc;

    return lmf_handle;
}

// Implementation of Lim_FileClose()
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileClose(JNIEnv *env, jobject obj, jint hFile) {
    return Lim_FileClose( hFile );
}

// Implementation of Lim_FileGetAttributes()                       
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetAttributes(JNIEnv *env, jobject obj, jint hFile, jobject jAttributes) {

    // call the native method
    LIMATTRIBUTES cAttributes;
    jint ret = Lim_FileGetAttributes(hFile, &cAttributes);

    // if the call was successful then update the values of the fields in jAttributes
    if (ret == 0) {

        // get the class reference for the jAttributes object 
        jclass cls = env->GetObjectClass(jAttributes);

        // get the field ID's
        jfieldID uiWidthID          = env->GetFieldID(cls, "uiWidth"         , "I");
        jfieldID uiWidthBytesID     = env->GetFieldID(cls, "uiWidthBytes"    , "I");
        jfieldID uiHeightID         = env->GetFieldID(cls, "uiHeight"        , "I");
        jfieldID uiCompID           = env->GetFieldID(cls, "uiComp"          , "I");
        jfieldID uiBpcInMemoryID    = env->GetFieldID(cls, "uiBpcInMemory"   , "I");
        jfieldID uiBpcSignificantID = env->GetFieldID(cls, "uiBpcSignificant", "I");
        jfieldID uiSequenceCountID  = env->GetFieldID(cls, "uiSequenceCount" , "I");
        jfieldID uiTileWidthID      = env->GetFieldID(cls, "uiTileWidth"     , "I");
        jfieldID uiTileHeightID     = env->GetFieldID(cls, "uiTileHeight"    , "I");
        jfieldID uiCompressionID    = env->GetFieldID(cls, "uiCompression"   , "I");
        jfieldID uiQualityID        = env->GetFieldID(cls, "uiQuality"       , "I");

        // set the new values of the member fields in the jAttributes object
        env->SetIntField(jAttributes, uiWidthID         , cAttributes.uiWidth);
        env->SetIntField(jAttributes, uiWidthBytesID    , cAttributes.uiWidthBytes);
        env->SetIntField(jAttributes, uiHeightID        , cAttributes.uiHeight);
        env->SetIntField(jAttributes, uiCompID          , cAttributes.uiComp);
        env->SetIntField(jAttributes, uiBpcInMemoryID   , cAttributes.uiBpcInMemory);
        env->SetIntField(jAttributes, uiBpcSignificantID, cAttributes.uiBpcSignificant);
        env->SetIntField(jAttributes, uiSequenceCountID , cAttributes.uiSequenceCount);
        env->SetIntField(jAttributes, uiTileWidthID     , cAttributes.uiTileWidth);
        env->SetIntField(jAttributes, uiTileHeightID    , cAttributes.uiTileHeight);
        env->SetIntField(jAttributes, uiCompressionID   , cAttributes.uiCompression);
        env->SetIntField(jAttributes, uiQualityID       , cAttributes.uiQuality);
    }    

    return ret;
}

// Implementation of Lim_FileGetMetadata
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetMetadata(JNIEnv *env, jobject obj, jint hFile, jobject jMetadata) {

    LIMMETADATA_DESC cMetadata;
    jint ret = Lim_FileGetMetadata(hFile, &cMetadata);

    // if the call was successful then update the values of the fields in jMetadata   
    if (ret == 0) {

        // get the class reference for the jMetadata object 
        jclass metaClazz = env->GetObjectClass(jMetadata);

        // get the field ID's
        jfieldID dTimeStartID       = env->GetFieldID(metaClazz, "dTimeStart"      , "D");
        jfieldID dAngleID           = env->GetFieldID(metaClazz, "dAngle"          , "D");
        jfieldID dCalibrationID     = env->GetFieldID(metaClazz, "dCalibration"    , "D");
        jfieldID dAspectID          = env->GetFieldID(metaClazz, "dAspect"         , "D");
        jfieldID dObjectiveMagID    = env->GetFieldID(metaClazz, "dObjectiveMag"   , "D");
        jfieldID dObjectiveNAID     = env->GetFieldID(metaClazz, "dObjectiveNA"    , "D");
        jfieldID dRefractIndex1ID   = env->GetFieldID(metaClazz, "dRefractIndex1"  , "D");
        jfieldID dRefractIndex2ID   = env->GetFieldID(metaClazz, "dRefractIndex2"  , "D");
        jfieldID dPinholeRadiusID   = env->GetFieldID(metaClazz, "dPinholeRadius"  , "D");
        jfieldID dZoomID            = env->GetFieldID(metaClazz, "dZoom"           , "D");
        jfieldID dProjectiveMagID   = env->GetFieldID(metaClazz, "dProjectiveMag"  , "D");
        jfieldID uiImageTypeID      = env->GetFieldID(metaClazz, "uiImageType"     , "I");
        jfieldID uiPlaneCountID     = env->GetFieldID(metaClazz, "uiPlaneCount"    , "I");
        jfieldID uiComponentCountID = env->GetFieldID(metaClazz, "uiComponentCount", "I");
        jfieldID wszObjectiveNameID = env->GetFieldID(metaClazz, "wszObjectiveName", "Ljava/lang/String;");
        jfieldID pPlanesID          = env->GetFieldID(metaClazz, "pPlanes"         , "[LPicturePlaneDesc;");

        //
        // set the new values of the member fields in the jMetadata object
        //
        env->SetDoubleField(jMetadata, dTimeStartID    , cMetadata.dTimeStart);
        env->SetDoubleField(jMetadata, dAngleID        , cMetadata.dAngle);
        env->SetDoubleField(jMetadata, dCalibrationID  , cMetadata.dCalibration);
        env->SetDoubleField(jMetadata, dAspectID       , cMetadata.dAspect);
        env->SetDoubleField(jMetadata, dObjectiveMagID , cMetadata.dObjectiveMag);
        env->SetDoubleField(jMetadata, dObjectiveNAID  , cMetadata.dObjectiveNA);
        env->SetDoubleField(jMetadata, dRefractIndex1ID, cMetadata.dRefractIndex1);
        env->SetDoubleField(jMetadata, dRefractIndex2ID, cMetadata.dRefractIndex2);
        env->SetDoubleField(jMetadata, dPinholeRadiusID, cMetadata.dPinholeRadius);
        env->SetDoubleField(jMetadata, dZoomID         , cMetadata.dZoom);
        env->SetDoubleField(jMetadata, dProjectiveMagID, cMetadata.dProjectiveMag);

        env->SetIntField(jMetadata, uiImageTypeID     , cMetadata.uiImageType);
        env->SetIntField(jMetadata, uiPlaneCountID    , cMetadata.uiPlaneCount);
        env->SetIntField(jMetadata, uiComponentCountID, cMetadata.uiComponentCount);

        // convert wchar_t * to char *
        char mbstr[256];
        wcstombs(mbstr, cMetadata.wszObjectiveName, 256);
        env->SetObjectField(jMetadata, wszObjectiveNameID, env->NewStringUTF(mbstr));

        //
        // build the PicturePlaneDesc array
        //

        jclass planeClazz = env->FindClass("PicturePlaneDesc");
        jmethodID constructortorID = env->GetMethodID(planeClazz, "<init>", "()V");
        jobjectArray jPlaneArray = env->NewObjectArray(cMetadata.uiPlaneCount, planeClazz, NULL);

        for (size_t i = 0; i < cMetadata.uiPlaneCount; i++) {
            jobject plane = env->NewObject(planeClazz, constructortorID);

            jfieldID uiCompCountID = env->GetFieldID(planeClazz, "uiCompCount", "I");
            jfieldID uiColorRGBID  = env->GetFieldID(planeClazz, "uiColorRGB" , "I");
            jfieldID dEmissionWLID = env->GetFieldID(planeClazz, "dEmissionWL", "D");
            jfieldID wszNameID     = env->GetFieldID(planeClazz, "wszName"    , "Ljava/lang/String;");
            jfieldID wszOCNameID   = env->GetFieldID(planeClazz, "wszOCName"  , "Ljava/lang/String;");

            env->SetIntField(plane, uiCompCountID, cMetadata.pPlanes[i].uiCompCount);
            env->SetIntField(plane, uiColorRGBID, cMetadata.pPlanes[i].uiColorRGB);
            env->SetDoubleField(plane, dEmissionWLID, cMetadata.pPlanes[i].dEmissionWL);
            wcstombs(mbstr, cMetadata.pPlanes[i].wszName, 256);
            env->SetObjectField(plane, wszNameID, env->NewStringUTF(mbstr));
            wcstombs(mbstr, cMetadata.pPlanes[i].wszOCName, 256);
            env->SetObjectField(plane, wszOCNameID, env->NewStringUTF(mbstr));

            env->SetObjectArrayElement(jPlaneArray, i, plane);
        }

        // set the PicturePlaneDesc array                
        env->SetObjectField(jMetadata, pPlanesID, jPlaneArray);
    }

    return ret;
}

// Implementation of Lim_FileGetTextinfo
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetTextinfo(JNIEnv *env, jobject obj, jint handle, jobject jTextInfo) {

    LIMTEXTINFO cTextInfo;
    jint ret = Lim_FileGetTextinfo(handle, &cTextInfo);

    // if the call was successful then update the values of the fields in jTextInfo
    if (ret == 0) {

        // get the class reference for the jTextInfo object
        jclass clazz = env->GetObjectClass(jTextInfo);

        // get the field ID's
        jfieldID wszImageIDID     = env->GetFieldID(clazz, "wszImageID"    , "Ljava/lang/String;");
        jfieldID wszTypeID        = env->GetFieldID(clazz, "wszType"       , "Ljava/lang/String;");
        jfieldID wszGroupID       = env->GetFieldID(clazz, "wszGroup"      , "Ljava/lang/String;");
        jfieldID wszSampleIDID    = env->GetFieldID(clazz, "wszSampleID"   , "Ljava/lang/String;");
        jfieldID wszAuthorID      = env->GetFieldID(clazz, "wszAuthor"     , "Ljava/lang/String;");
        jfieldID wszDescriptionID = env->GetFieldID(clazz, "wszDescription", "Ljava/lang/String;");
        jfieldID wszCapturingID   = env->GetFieldID(clazz, "wszCapturing"  , "Ljava/lang/String;");
        jfieldID wszSamplingID    = env->GetFieldID(clazz, "wszSampling"   , "Ljava/lang/String;");
        jfieldID wszLocationID    = env->GetFieldID(clazz, "wszLocation"   , "Ljava/lang/String;");
        jfieldID wszDateID        = env->GetFieldID(clazz, "wszDate"       , "Ljava/lang/String;");
        jfieldID wszConclusionID  = env->GetFieldID(clazz, "wszConclusion" , "Ljava/lang/String;");
        jfieldID wszInfo1ID       = env->GetFieldID(clazz, "wszInfo1"      , "Ljava/lang/String;");
        jfieldID wszInfo2ID       = env->GetFieldID(clazz, "wszInfo2"      , "Ljava/lang/String;");
        jfieldID wszOpticsID      = env->GetFieldID(clazz, "wszOptics"     , "Ljava/lang/String;");

        // used to convert wchar_t * to char *
        char strShort[256];
        char strLong[4096];

        // set the new values of the member fields in the jTextInfo object
        wcstombs(strShort, cTextInfo.wszImageID, 256);
        env->SetObjectField(jTextInfo, wszImageIDID, env->NewStringUTF(strShort));
        wcstombs(strShort, cTextInfo.wszType, 256);
        env->SetObjectField(jTextInfo, wszTypeID, env->NewStringUTF(strShort));
        wcstombs(strShort, cTextInfo.wszGroup, 256);
        env->SetObjectField(jTextInfo, wszGroupID, env->NewStringUTF(strShort));
        wcstombs(strShort, cTextInfo.wszSampleID, 256);
        env->SetObjectField(jTextInfo, wszSampleIDID, env->NewStringUTF(strShort));
        wcstombs(strShort, cTextInfo.wszAuthor, 256);
        env->SetObjectField(jTextInfo, wszAuthorID, env->NewStringUTF(strShort));
        wcstombs(strShort, cTextInfo.wszSampling, 256);
        env->SetObjectField(jTextInfo, wszSamplingID, env->NewStringUTF(strShort));
        wcstombs(strShort, cTextInfo.wszLocation, 256);
        env->SetObjectField(jTextInfo, wszLocationID, env->NewStringUTF(strShort));
        wcstombs(strShort, cTextInfo.wszDate, 256);
        env->SetObjectField(jTextInfo, wszDateID, env->NewStringUTF(strShort));
        wcstombs(strShort, cTextInfo.wszConclusion, 256);
        env->SetObjectField(jTextInfo, wszConclusionID, env->NewStringUTF(strShort));
        wcstombs(strShort, cTextInfo.wszInfo1, 256);
        env->SetObjectField(jTextInfo, wszInfo1ID, env->NewStringUTF(strShort));
        wcstombs(strShort, cTextInfo.wszInfo2, 256);
        env->SetObjectField(jTextInfo, wszInfo2ID, env->NewStringUTF(strShort));
        wcstombs(strShort, cTextInfo.wszOptics, 256);
        env->SetObjectField(jTextInfo, wszOpticsID, env->NewStringUTF(strShort));

        wcstombs(strLong, cTextInfo.wszDescription, 4096);
        env->SetObjectField(jTextInfo, wszDescriptionID, env->NewStringUTF(strLong));
        wcstombs(strLong, cTextInfo.wszCapturing, 4096);
        env->SetObjectField(jTextInfo, wszCapturingID, env->NewStringUTF(strLong));
    }

    return ret;
}

// Implementation of Lim_FileGetExperiment
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetExperiment(JNIEnv *env, jobject obj, jint handle, jobject jExperiment) {

    jint ret = Lim_FileGetExperiment(handle, &cExperiment);

    // if the call was successful then update the values of the fields in jExperiment
    if (ret == 0) {

        // get the class reference for the jExperiment object
        jclass clazz = env->GetObjectClass(jExperiment);

        // get the field ID's
        jfieldID uiLevelCountID     = env->GetFieldID(clazz, "uiLevelCount"    , "I");
        jfieldID pAllocatedLevelsID = env->GetFieldID(clazz, "pAllocatedLevels", "[LExperimentLevel;");

        // set the uiLevelCount
        env->SetIntField(jExperiment, uiLevelCountID     , cExperiment.uiLevelCount);

        //
        // build the ExperimentLevel array
        //

        jclass expClazz = env->FindClass("ExperimentLevel");
        jmethodID constructortorID = env->GetMethodID(expClazz, "<init>", "()V");
        jobjectArray jExpArray = env->NewObjectArray(cExperiment.uiLevelCount, expClazz, NULL);

        for (size_t i = 0; i < cExperiment.uiLevelCount; i++) {
            jobject level = env->NewObject(expClazz, constructortorID);

            jfieldID uiExpTypeID  = env->GetFieldID(expClazz, "uiExpType" , "I");
            jfieldID uiLoopSizeID = env->GetFieldID(expClazz, "uiLoopSize", "I");
            jfieldID dIntervalID  = env->GetFieldID(expClazz, "dInterval" , "D");

            env->SetIntField(level, uiExpTypeID , cExperiment.pAllocatedLevels[i].uiExpType);
            env->SetIntField(level, uiLoopSizeID, cExperiment.pAllocatedLevels[i].uiLoopSize);
            env->SetDoubleField(level, dIntervalID, cExperiment.pAllocatedLevels[i].dInterval);

            env->SetObjectArrayElement(jExpArray, i, level);
        }

        // set the ExperimentLevel array
        env->SetObjectField(jExperiment, pAllocatedLevelsID, jExpArray);

    }

    return ret;
}

// Implementation of Lim_FileGetBinaryDescriptors
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetBinaryDescriptors(JNIEnv *env, jobject obj, jint handle, jobject jBinaries) {

    LIMBINARIES cBinaries;
    jint ret = Lim_FileGetBinaryDescriptors(handle, &cBinaries);

    // if the call was successful then update the values of the fields in jBinaries
    if (ret == 0) {

        // used to convert wchar_t * to char *
        char mbstr[256];

        // get the class reference for the jBinaries object
        jclass clazz = env->GetObjectClass(jBinaries);

        // get the field ID's
        jfieldID uiCountID      = env->GetFieldID(clazz, "uiCount"     , "I");
        jfieldID pDescriptorsID = env->GetFieldID(clazz, "pDescriptors", "[LBinaryDescriptor;");

        // set the uiLevelCount
        env->SetIntField(jBinaries, uiCountID     , cBinaries.uiCount);

        //
        // build the BinaryDescriptor array
        //

        jclass binClazz = env->FindClass("BinaryDescriptor");
        jmethodID constructortorID = env->GetMethodID(binClazz, "<init>", "()V");
        jobjectArray jBinArray = env->NewObjectArray(cBinaries.uiCount, binClazz, NULL);

        for (size_t i = 0; i < cBinaries.uiCount; i++) {
            jobject bin = env->NewObject(binClazz, constructortorID);

            jfieldID wszNameID     = env->GetFieldID(binClazz, "wszName"    , "Ljava/lang/String;");
            jfieldID wszCompNameID = env->GetFieldID(binClazz, "wszCompName", "Ljava/lang/String;");
            jfieldID uiColorRGBID  = env->GetFieldID(binClazz, "uiColorRGB" , "I");

            wcstombs(mbstr, cBinaries.pDescriptors[i].wszName, 256);
            env->SetObjectField(jBinaries, wszNameID, env->NewStringUTF(mbstr));

            wcstombs(mbstr, cBinaries.pDescriptors[i].wszCompName, 256);
            env->SetObjectField(jBinaries, wszCompNameID, env->NewStringUTF(mbstr));

            env->SetIntField(bin, uiColorRGBID, cBinaries.pDescriptors[i].uiColorRGB);

            env->SetObjectArrayElement(jBinArray, i, bin);
        }

        // set the BinaryDescriptor array
        env->SetObjectField(jBinaries, pDescriptorsID, jBinArray);

    }

    return ret;
}

// Implementation of Lim_InitPicture
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1InitPicture(JNIEnv *env, jobject obj, jobject jPicture, jint width, jint height, jint bpc, jint components) {

    // returns the number of bytes per picture
    jint pictureSize = Lim_InitPicture(&cPicture, width, height, bpc, components);

    // get the class reference for the jPicture object
    jclass clazz = env->GetObjectClass(jPicture);

    // get the field ID's
    jfieldID uiWidthID       = env->GetFieldID(clazz, "uiWidth"      , "I");
    jfieldID uiHeightID      = env->GetFieldID(clazz, "uiHeight"     , "I");
    jfieldID uiBitsPerCompID = env->GetFieldID(clazz, "uiBitsPerComp", "I");
    jfieldID uiComponentsID  = env->GetFieldID(clazz, "uiComponents" , "I");
    jfieldID uiWidthBytesID  = env->GetFieldID(clazz, "uiWidthBytes" , "I");
    jfieldID uiSizeID        = env->GetFieldID(clazz, "uiSize"       , "I");
    jfieldID pImageDataID    = env->GetFieldID(clazz, "pImageData"   , "J");

    // set the new values of the member fields in the jPicture object
    env->SetIntField(jPicture, uiWidthID      , cPicture.uiWidth);
    env->SetIntField(jPicture, uiHeightID     , cPicture.uiHeight);
    env->SetIntField(jPicture, uiBitsPerCompID, cPicture.uiBitsPerComp);
    env->SetIntField(jPicture, uiComponentsID , cPicture.uiComponents);
    env->SetIntField(jPicture, uiWidthBytesID , cPicture.uiWidthBytes);
    env->SetIntField(jPicture, uiSizeID       , cPicture.uiSize);
    env->SetLongField(jPicture, pImageDataID  , (jlong)cPicture.pImageData);

    return pictureSize;
}

// Implementation of Lim_DestroyPicture
JNIEXPORT void JNICALL Java_ND2SDK_Lim_1DestroyPicture(JNIEnv *env, jobject obj) {
    Lim_DestroyPicture(&cPicture);
}

// Implementation of Lim_GetSeqIndexFromCoords
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1GetSeqIndexFromCoords(JNIEnv *env, jobject obj, jintArray jCoords) {

    // ensure that the length of the jCoords array is 4
    jsize len = env->GetArrayLength(jCoords);
    if (len != 4) {
        char msg[90];
        sprintf(msg, "Lim_GetSeqIndexFromCoords must pass in a 4-element array. Got array of length %d", len);
        env->ThrowNew(env->FindClass("java/lang/ArrayIndexOutOfBoundsException"), msg);
    }

    LIMUINT pExpCoords[4];

    jint *el = env->GetIntArrayElements(jCoords, NULL);
    for (int i = 0; i < len; i++) {
        pExpCoords[i] = (LIMUINT)el[i];
    }
    env->ReleaseIntArrayElements(jCoords, el, NULL);

    return Lim_GetSeqIndexFromCoords(&cExperiment, pExpCoords);
}

// Implementation of Lim_GetCoordsFromSeqIndex
JNIEXPORT jintArray JNICALL Java_ND2SDK_Lim_1GetCoordsFromSeqIndex(JNIEnv *env, jobject obj, jint uiSeqIdx) {

    LIMUINT *cCoords = new LIMUINT[4];

    Lim_GetCoordsFromSeqIndex(&cExperiment, uiSeqIdx, cCoords);

    jintArray jCoords = env->NewIntArray(4);
    jint *el = env->GetIntArrayElements(jCoords, NULL);

    for (int i = 0; i < 4; i++) {
        el[i] = cCoords[i];
    }

    env->ReleaseIntArrayElements(jCoords, el, NULL);

    delete[] cCoords;

    return jCoords;
}

// Implementation of Lim_FileGetImageData
JNIEXPORT jint JNICALL Java_ND2SDK_Lim_1FileGetImageData(JNIEnv *env, jobject obj, jint handle, jint uiSeqIndex, jobject jByteBuffer, jobject jImgInfo) {

    LIMLOCALMETADATA cImgInfo;
    jint ret = Lim_FileGetImageData(handle, uiSeqIndex, &cPicture, &cImgInfo);

    // if the call was successful then fill in the jByteBuffer and the values of the fields in jImgInfo
    if (ret == 0) {

        // get the class reference for the jImgInfo object
        jclass clazz = env->GetObjectClass(jImgInfo);

        // get the field ID's
        jfieldID dTimeMSecID = env->GetFieldID(clazz, "dTimeMSec", "D");
        jfieldID dXPosID     = env->GetFieldID(clazz, "dXPos"    , "D");
        jfieldID dYPosID     = env->GetFieldID(clazz, "dYPos"    , "D");
        jfieldID dZPosID     = env->GetFieldID(clazz, "dZPos"    , "D");

        // set the new values of the member fields in the jImgInfo object
        env->SetDoubleField(jImgInfo, dTimeMSecID, cImgInfo.dTimeMSec);
        env->SetDoubleField(jImgInfo, dXPosID    , cImgInfo.dXPos);
        env->SetDoubleField(jImgInfo, dYPosID    , cImgInfo.dYPos);
        env->SetDoubleField(jImgInfo, dZPosID    , cImgInfo.dZPos);

        // fill in the jByteBuffer
        jbyte *buf = (jbyte *)env->GetDirectBufferAddress(jByteBuffer);
        jbyte *p = (jbyte *)cPicture.pImageData;
        for (int i = 0; i < cPicture.uiSize; i++) {
            buf[i] = *p;
            p++;
        }

    }

    return ret;
}
