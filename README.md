# Mic C++

Low-latency Android-to-PC raw PCM audio streaming pipeline utilizing native C++ (JNI) and UDP sockets.

## Overview

Mic C++ is a lightweight application and receiver system designed to stream real-time raw audio from an Android device microphone to a PC over a local network. By bypassing high-latency middleware and leveraging direct native socket operations via JNI, the system ensures minimal audio delay suitable for live broadcasting and audio routing.

## Highlights

- **Native C++ JNI Integration**: Core UDP socket creation and packet transmission implemented in C++ (`sys/socket.h`, `arpa/inet.h`) for optimal performance and minimal overhead.
- **Raw PCM Audio Pipeline**: Captures uncompressed audio (`44100Hz`, `Mono`, `16-bit`) using Android's `AudioRecord` API and transmits over standard UDP datagrams.
- **Python Receiver Server**: A robust multi-platform Python-based receiver utilizing `sounddevice` and `numpy` to handle incoming audio streams and route them locally.
- **Automation Support**: Pre-configured batch automation scripts for environment setup and server execution.

## Current Status & Roadmap
- **Core Pipeline (Completed)**: Succesful end-to-end raw PCM audio transmission via UDP, resolving JNI linkage and parameter passing.
- **Audio Routing (In Progress)**: Integration with Virtual Audio Cable (VB-Cable) for OBS Studio input mapping.
- **Standalone Distribution (Planned)**: Change usage from Python scripts into standalone binaries (```.exe```)
- **Interface Enhancement (Planned)**: Advanced GUI implementation for both Android control panel and PC interface.


