@echo off
notepad app\build.gradle
mkdir stage\release
mkdir stage\ICproduction
mkdir stage\GAproduction
set JAVA_HOME=C:\Program Files\Android\Android Studio\jre
color e4
call gradlew %1 assembleRelease -Pandroid.injected.signing.store.file=..\keystore-tide.jks -Pandroid.injected.signing.store.password=tide@!23 -Pandroid.injected.signing.key.alias=tidelearning -Pandroid.injected.signing.key.password=tide@!23
copy app\build\outputs\apk\release\app-release.apk stage\release
color 2b
call gradlew %1 assembleICproduction -Pandroid.injected.signing.store.file=..\keystore-tide.jks -Pandroid.injected.signing.store.password=tide@!23 -Pandroid.injected.signing.key.alias=tidelearning -Pandroid.injected.signing.key.password=tide@!23
copy app\build\outputs\apk\ICproduction\app-ICproduction.apk stage\ICproduction
color 1e
call gradlew %1 assembleGAproduction -Pandroid.injected.signing.store.file=..\keystore-tide.jks -Pandroid.injected.signing.store.password=tide@!23 -Pandroid.injected.signing.key.alias=tidelearning -Pandroid.injected.signing.key.password=tide@!23
copy app\build\outputs\apk\GAproduction\app-GAproduction.apk stage\GAproduction
color 0f
pause
