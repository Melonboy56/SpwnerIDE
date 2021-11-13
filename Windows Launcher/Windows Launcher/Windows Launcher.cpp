// Windows Launcher.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <iostream>
#include <windows.h>
#include <jni.h>
#include <thread>
#include <filesystem>

typedef UINT(CALLBACK* JVMDLLFunction)(JavaVM**, void**, JavaVMInitArgs*);
typedef UINT(CALLBACK* JVM_DEFAULT_ARGS)(void*);

std::wstring java_bin_path = L"";

std::wstring get_java_bin_install(JNIEnv* env, jclass) {
    return java_bin_path;
}

std::wstring get_exe_path_wstring() {
    WCHAR path[MAX_PATH];
    GetModuleFileNameW(NULL, path, MAX_PATH);
    return std::wstring(path);
}

std::wstring join_wstrings(std::wstring string1, std::wstring string2) {
    std::wstring output = L"";
    output += string1;
    output += string2;
    return output;
}

void set_java_bin_path() {
    java_bin_path = join_wstrings(get_exe_path_wstring(), L"\\..\\runtime\\bin");
}

std::string get_exe_path() {
    WCHAR path[MAX_PATH];
    GetModuleFileNameW(NULL, path, MAX_PATH);

    char path_chars[MAX_PATH];
    sprintf_s(path_chars, "%ws", path);

    return std::string(path_chars);
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

char* convert_string_to_char(std::string thestring) {
    char* cstr = new char[thestring.length() + 1];
    strcpy_s(cstr,thestring.length()+1, thestring.c_str());
    return cstr;
}


int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance,LPSTR lpCmdLine, int nCmdShow)
//int main()
{
    set_java_bin_path();
    HINSTANCE jvmDLL = LoadLibrary((LPCWSTR)join_wstrings(java_bin_path,L"\\server\\jvm.dll").c_str());
    
    if (!jvmDLL) {
        //printf("failed to find jvm.dll at specified location, exiting.\n");
        MessageBox(NULL, L"Could not find jvm.dll, error code 1", L"Error", MB_OK | MB_ICONERROR);
        return 1;
    }
    JVMDLLFunction createJavaVMFunction = (JVMDLLFunction)GetProcAddress(jvmDLL, "JNI_CreateJavaVM");
    JVM_DEFAULT_ARGS jvm_default_args_function = (JVM_DEFAULT_ARGS)GetProcAddress(jvmDLL, "JNI_GetDefaultJavaVMInitArgs");

    if (!createJavaVMFunction) {
        //printf("Failed to get pointer to JNI_CreateJavaVM function from jvm.dll, exiting\n");
        MessageBox(NULL, L"Could not load jvm.dll, error code 2", L"Error", MB_OK | MB_ICONERROR);
        return 2;
    }
    JavaVM* jvm;
    JNIEnv* env;
    JavaVMInitArgs vm_args;
    vm_args.version = JNI_VERSION_10;
    jvm_default_args_function((void*)&vm_args);
    JavaVMOption* options = new JavaVMOption[1];

    //int index = 0;
    options[0].optionString = (char*)convert_string_to_char("-Djava.class.path=" + get_exe_path() + "\\..\\app.jar");
    //options[1].optionString = (char*)"--add-exports=java.desktop/sun.font=ALL-UNNAMED";
    //options[2].optionString = (char*)"--add-exports=java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED";

    vm_args.nOptions = 1;
    vm_args.options = options;
    //vm_args.ignoreUnrecognized = false;

    createJavaVMFunction(&jvm, (void**)&env, &vm_args);
    //delete options;


    jmethodID main = NULL;
    jclass cls = NULL;

    cls = env->FindClass("me/melonsboy/spwn/ide/Main");
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
    }

    // adds the native functions to java
    JNINativeMethod methods[]{ { (char*)"ide_get_exe_path",(char*)"()Ljava/lang/String;",(void*)get_exe_path_java } };
    if (env->RegisterNatives(cls, methods, 1) < 0) {
        if (env->ExceptionOccurred()) {
            MessageBox(NULL, L"Could not launch program: native register error, error code 4", L"Error", MB_OK | MB_ICONERROR);
            return 4;
            //std::cout << " OOOOOPS: exception when registreing naives" << std::endl;
        } else {
            MessageBox(NULL, L"Could not launch program: native register error, error code 5", L"Error", MB_OK | MB_ICONERROR);
            return 5;
            //std::cout << " ERROR: problem when registreing naives" << std::endl;
        }
    }


    if (cls != NULL) {
        if (strlen(lpCmdLine) != 0) {
            main = env->GetStaticMethodID(cls, "start_and_open_project", "(Ljava/lang/String;)V");
        } else {
            main = env->GetStaticMethodID(cls, "start_program", "()V");
        }
    }
    else {
        printf("Unable to find the requested class\n");
        MessageBox(NULL, L"Could not find main class or app.jar is missing, error code 3", L"Error", MB_OK | MB_ICONERROR);
        return 3;
    }

    if (main != NULL) {
        if (strlen(lpCmdLine) != 0) {
            env->CallStaticVoidMethod(cls, main, env->NewStringUTF(lpCmdLine));
        } else {
            env->CallStaticVoidMethod(cls, main, " ");
        }
    }
    else {
        printf("Could not find start_program method");
        MessageBox(NULL, L"Could not find main class or app.jar is missing, error code 6", L"Error", MB_OK | MB_ICONERROR);
        return 6;
    }
    jvm->DestroyJavaVM();
    
    return 0;
}

// Run program: Ctrl + F5 or Debug > Start Without Debugging menu
// Debug program: F5 or Debug > Start Debugging menu

// Tips for Getting Started: 
//   1. Use the Solution Explorer window to add/manage files
//   2. Use the Team Explorer window to connect to source control
//   3. Use the Output window to see build output and other messages
//   4. Use the Error List window to view errors
//   5. Go to Project > Add New Item to create new code files, or Project > Add Existing Item to add existing code files to the project
//   6. In the future, to open this project again, go to File > Open > Project and select the .sln file
