.PHONY: all apk install rom

PACKAGE_NAME := com.kidz.y1
GRADLE_APK_PATH := app/build/outputs/apk/release/app-release.apk
DOCKER_IMAGE := storiies-y1
DOCKER_CONTAINER := storiies-y1-build

all: apk install

apk: 
	./gradlew assemble

install: apk
	adb install -r $(GRADLE_APK_PATH)
	adb shell pm clear $(PACKAGE_NAME)
	adb shell am force-stop $(PACKAGE_NAME)
	adb shell am start -n $(PACKAGE_NAME)/.activities.MainActivity

rom:
	rm -rf build/
	docker build -t $(DOCKER_IMAGE) .
	docker run --rm --privileged --name $(DOCKER_CONTAINER) -v $(PWD):/workspace $(DOCKER_IMAGE)
