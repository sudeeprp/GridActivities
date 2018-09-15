pushd

cd C:\Users\sudee\AppData\Local\Android\Sdk\platform-tools
rem cd 

adb uninstall com.kiloo.subwaysurf
adb uninstall com.digivive.offdeck
adb uninstall com.halfbrick.fruitninja
adb uninstall com.aldiko.android
adb uninstall com.dataviz.docstogo
adb install c:\WorkArea\contentgen\play.area\googlepdfviewer.apk
rem TODO: Still need to uninstall black tile ad monster

adb install -r C:\Users\sudee\StudioProjects\GridActivities\app\release\app-release.apk

adb shell mkdir /sdcard/LearningGrid

adb shell rm -r /sdcard/LearningGrid/1_French

adb push "C:\WorkArea\contentgen\French-grid\1_French" /sdcard/LearningGrid/1_French

popd