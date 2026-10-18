#include <jni.h>
#include <string>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <unistd.h>
#include <android/log.h>

#define LOG_TAG "UdpAudio"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {
JNIEXPORT jint JNICALL // Ubah jstring menjadi jint agar bisa return angka/socket
Java_com_hizukiarufi_mic_1cpp_MainActivity_initUdpSocket(
    JNIEnv *env,
    jobject thiz) {

        int sock = socket(AF_INET, SOCK_DGRAM, 0);
        if (sock < 0) {
            LOGI("Failed to create UDP socket");
            return -1; // -1 adalah int, sekarang cocok dengan jint
        }
        LOGI("UDP socket created successfully");
        return sock; // sock adalah int, sekarang cocok dengan jint
    }

JNIEXPORT jint JNICALL
Java_com_hizukiarufi_mic_1cpp_MainActivity_sendUdpAudio(
    JNIEnv *env,
    jobject thiz,
    jint sock,
    jstring ipAddress,
    jint port,
    jbyteArray audioData) {

        // Convert jstring to C string
        const char *ip = env->GetStringUTFChars(ipAddress, nullptr);

        // Menyiapkan struktur alamat tujuan secara dinamis
        struct sockaddr_in dest_addr;
        memset(&dest_addr, 0, sizeof(dest_addr));
        dest_addr.sin_family = AF_INET;
        dest_addr.sin_port = htons(port);
        inet_pton(AF_INET, ip, &dest_addr.sin_addr);

        // Mengambil data mentah (byte array) dari Kotlin
        jbyte *data = env->GetByteArrayElements(audioData, nullptr);
        jsize len = env->GetArrayLength(audioData);

        // Mengirim paket UDP ke IP dan port tujuan yang dinamis
        ssize_t sent_bytes = sendto(sock, (char*)data, len, 0, (struct sockaddr*)&dest_addr, sizeof(dest_addr));

        // Membersihkan memori
        env->ReleaseByteArrayElements(audioData, data, JNI_ABORT);
        env->ReleaseStringUTFChars(ipAddress, ip);

        return static_cast<jint>(sent_bytes); // Mengembalikan jumlah byte yang dikirim
    }
}