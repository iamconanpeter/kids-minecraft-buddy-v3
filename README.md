# Kids Minecraft Buddy v3 (Android)

Native Android game prototype for kids 6–7+.

Core loop: tap-to-place blocks, complete buddy missions, earn stars, unlock blocks, save progress locally.

## Run quality gate

```bash
export JAVA_HOME=/home/openclaw/.openclaw/workspace/.local/jdk-17
export ANDROID_SDK_ROOT=/home/openclaw/.openclaw/workspace/.local/android-sdk
export PATH=$JAVA_HOME/bin:$PATH
./gradlew test assembleDebug
```

## Install debug build

```bash
./gradlew installDebug
```
