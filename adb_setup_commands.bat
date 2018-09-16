pushd C:\Users\sudee\AppData\Local\Android\Sdk\platform-tools

adb uninstall com.kiloo.subwaysurf
adb uninstall com.digivive.offdeck
adb uninstall com.halfbrick.fruitninja
adb uninstall com.aldiko.android
adb uninstall com.dataviz.docstogo
adb install c:\WorkArea\contentgen\play.area\googlepdfviewer.apk
rem TODO: Still need to uninstall black tile ad monster

adb install -r C:\Users\sudee\StudioProjects\GridActivities\app\release\app-release.apk

popd