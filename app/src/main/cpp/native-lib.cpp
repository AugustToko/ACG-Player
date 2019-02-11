#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_top_geek_1studio_chenlongcould_musicplayer_activity_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C" ;