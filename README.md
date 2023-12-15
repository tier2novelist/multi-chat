[![Build Status](https://travis-ci.org/1988warren/multi-chat.svg?branch=master)](https://travis-ci.org/1988warren/multi-chat)

# Welcome to this Computer Networking Project (Fall 2018)

__Please kindly be reminded to follow [GW Academic Integrity Code](https://studentconduct.gwu.edu/code-academic-integrity) for any reference or use of this repo.__

- Deliverable
  * [Artifact Releases](https://github.com/1988warren/multi-chat/releases)
  * [Protocol RFC](https://github.com/1988warren/multi-chat/blob/spec/rfc.txt)
  * [Architecture Specs](#spec)
- Demo
  * [Usage](#usage)
  
## Spec

### Presentation
  * [PDF](https://github.com/1988warren/multi-chat/blob/spec/chatroom_presentation.pdf)
  * [Google Slides](https://docs.google.com/presentation/d/e/2PACX-1vTjtVsFY7YeCoiKiVj9k3T15V7TWpy_qFmHckHJSmGLAJyhZxDrWPs6eKQEQgVmvoxUP8KYXdM4xBNe/pub?start=false&loop=false&delayms=3000)

### Architecture

- Text Messaging

![text](https://github.com/1988warren/multi-chat/blob/spec/Designs_of_Text_Messaging.svg)

- File Sharing

![file](https://github.com/1988warren/multi-chat/blob/spec/Designs_of_File_Sharing.svg)


## Usage

Build Artifact
```shell
mvn compile package
```

Start Demo Chat Room Server on localhost
```shell
java -cp demo-1.0-SNAPSHOT-shaded.jar edu.gwu.cs6431.multichat.demo.server.DemoServer
```

Start Demo Chat Room Client (with JavaFx SDK 21 installed and exposed to PATH)
```shell
java -jar demo-1.0-SNAPSHOT-shaded.jar
```

Start With javafx maven plugin
```shell
mvn clean javafx:run
```
