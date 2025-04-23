#include <jni.h>
#include <string>
#include <fstream>
#include <sstream>
#include <vector>
#include <android/log.h>
#include <dirent.h>
#include <iostream>
#include <cctype>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_helperapplication_services_ProcessHelperService_getProcessesStats(JNIEnv *env,
                                                                                   jobject thiz) {
        std::stringstream result;

        FILE* fp = popen("ps -A -o PID,USER,STAT,%CPU,%MEM,CMD", "r"); //"top -b -n 1 | head -20"
        if (!fp) {
            std::cerr << "Error: Unable to execute ps command!" << std::endl;
            return env->NewStringUTF("");
        }

        char buffer[512];

        while (fgets(buffer, sizeof(buffer), fp) != NULL) {
            std::string line(buffer);

            result << line << "\n";
            std::cerr << "Added Process Line: " << line << std::endl;
        }

        pclose(fp);

        std::string resStr = result.str();
        return env->NewStringUTF(resStr.c_str());
}