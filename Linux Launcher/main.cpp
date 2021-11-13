// Windows Launcher.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <iostream>
#include <jni.h>
#include <libgen.h>         // dirname
#include <unistd.h>         // readlink
#include <linux/limits.h>   // PATH_MAX
#include <string.h>

#include <dlfcn.h>
#include <stdio.h>

std::string get_exe_path() {
    char result[PATH_MAX];
    ssize_t count = readlink("/proc/self/exe", result, PATH_MAX);
    char *path;
    if (count != -1) {
        path = dirname(result);
    }
    //std::cout << path;
    return std::string(path);
}

char* to_c_string(std::string thestring) {
    return strdup(thestring.c_str());
}

std::string return_string(std::string thestring) {
   return thestring;
}

JNIEXPORT jstring JNICALL get_exe_path_java(JNIEnv* env, jclass) {
    std::string message = get_exe_path();

    int byteCount = message.length();
    jbyte* pNativeMessage = (jbyte*)reinterpret_cast<const jbyte*>(message.c_str());
    jbyteArray bytes = env->NewByteArray(byteCount);
    env->SetByteArrayRegion(bytes, 0, byteCount, pNativeMessage);

    // find the Charset.forName method:
    //   javap -s java.nio.charset.Charset | egrep -A2 "forName"
    jclass charsetClass = env->FindClass("java/nio/charset/Charset");
    jmethodID forName = env->GetStaticMethodID(
        charsetClass, "forName", "(Ljava/lang/String;)Ljava/nio/charset/Charset;");
    jstring utf8 = env->NewStringUTF("UTF-8");
    jobject charset = env->CallStaticObjectMethod(charsetClass, forName, utf8);

    // find a String constructor that takes a Charset:
    //   javap -s java.lang.String | egrep -A2 "String\(.*charset"
    jclass stringClass = env->FindClass("java/lang/String");
    jmethodID ctor = env->GetMethodID(
        stringClass, "<init>", "([BLjava/nio/charset/Charset;)V");

    jstring jMessage = reinterpret_cast<jstring>(
        env->NewObject(stringClass, ctor, bytes, charset));

    return jMessage;
}

typedef int(*JVMDLLFunction)(JavaVM**, void**, JavaVMInitArgs*);
typedef int(*JVM_DEFAULT_ARGS)(void*);

int main(int argc, char *argv[]) {
    void* jvmhandle = dlopen(to_c_string(get_exe_path()+"/runtime/lib/server/libjvm.so"),RTLD_NOW);
    if (!jvmhandle) {
        printf("Could not load libjvm.so: %s\n",dlerror());
        return 1;
    }
    JVMDLLFunction createjvmfunction = (JVMDLLFunction)dlsym(jvmhandle,"JNI_CreateJavaVM");
    JVM_DEFAULT_ARGS jvm_default_args_function = (JVM_DEFAULT_ARGS)dlsym(jvmhandle, "JNI_GetDefaultJavaVMInitArgs");
    const char* error = dlerror();
    if (error) {
        printf("Could not load java runtime: %s\n",error);
        return 2;
    }

    JavaVM* jvm;
    JNIEnv* env;
    JavaVMInitArgs vm_args;
    vm_args.version = JNI_VERSION_10;
    jvm_default_args_function((void*)&vm_args);
    JavaVMOption* options = new JavaVMOption[1];

    //int index = 0;
    //std::cout << to_c_string(get_exe_path() + "/app.jar");
    //options[0].optionString = (char*)to_c_string("-Djava.class.path=" + get_exe_path() + "/app.jar");
    options[0].optionString = to_c_string("-Djava.class.path="+get_exe_path()+"/app.jar");
    //std::cout << options[0].optionString << "test2";
    vm_args.nOptions = 1;
    vm_args.options = options;
    //vm_args.ignoreUnrecognized = false;

    createjvmfunction(&jvm, (void**)&env, &vm_args);
    //delete options;


    jmethodID main = NULL;
    jclass cls = NULL;

    cls = env->FindClass("me/melonsboy/spwn/ide/Main");
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        return 3;
    }

    // adds the native functions to java
    JNINativeMethod methods[]{ { (char*)"ide_get_exe_path",(char*)"()Ljava/lang/String;",(void*)get_exe_path_java } };
    if (env->RegisterNatives(cls, methods, 1) < 0) {
        if (env->ExceptionOccurred()) {
            std::cout << "Could not start java program\n";
            return 4;
            //std::cout << " OOOOOPS: exception when registreing naives" << std::endl;
        } else {
            std::cout << "Could not start java program\n";
            return 5;
            //std::cout << " ERROR: problem when registreing naives" << std::endl;
        }
    }

    if (cls != NULL) {
        if (argc != 1) {
            main = env->GetStaticMethodID(cls, "start_and_open_project", "(Ljava/lang/String;)V");
        } else {
            main = env->GetStaticMethodID(cls, "start_program", "()V");
        }
    }
    else {
        std::cout << "Could not start java program\n";
        return 6;
    }

    if (main != NULL) {
        if (argc != 1) {
            env->CallStaticVoidMethod(cls, main, env->NewStringUTF(argv[1]));
        } else {
            env->CallStaticVoidMethod(cls, main, " ");
        }
    }
    else {
        std::cout << "Could not start java program\n";
        return 7;
    }
    jvm->DestroyJavaVM();
    
    return 0;
}
