{ pkgs ? import <nixpkgs> {
    config = {
      allowUnfree = true;
      android_sdk.accept_license = true;
    };
  }
}:

let
  androidComposition = pkgs.androidenv.composeAndroidPackages {
    platformVersions = [ "34" "35" ];
    buildToolsVersions = [ "34.0.0" "35.0.0" ];
    includeSystemImages = false;
    includeNDK = false;
  };
  androidSdk = androidComposition.androidsdk;
in
pkgs.mkShell {
  packages = [
    pkgs.jdk17
    pkgs.git
    pkgs.curl
    pkgs.unzip
    pkgs.zip
    androidSdk
  ];

  ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
  ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";
  JAVA_HOME = "${pkgs.jdk17}";
  AAPT2_PATH = "${androidSdk}/libexec/android-sdk/build-tools/35.0.0/aapt2";
  GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidSdk}/libexec/android-sdk/build-tools/35.0.0/aapt2";

  shellHook = ''
    export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/35.0.0:$ANDROID_HOME/build-tools/34.0.0:$PATH"
    export AAPT2_PATH="$ANDROID_HOME/build-tools/35.0.0/aapt2"
    echo "ANDROID_HOME=$ANDROID_HOME"
    echo "AAPT2_PATH=$AAPT2_PATH"
  '';
}
