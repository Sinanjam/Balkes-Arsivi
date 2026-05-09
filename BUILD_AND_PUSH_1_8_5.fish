#!/usr/bin/env fish
echo "NixOS Android ortamında build + push + release başlıyor..."
env NIXPKGS_ALLOW_UNFREE=1 nix-shell -E '
with import <nixpkgs> {
  config = {
    allowUnfree = true;
    android_sdk.accept_license = true;
  };
};
let
  androidComposition = androidenv.composeAndroidPackages {
    platformVersions = [ "35" "34" ];
    buildToolsVersions = [ "35.0.0" "34.0.0" ];
    includeNDK = false;
  };
in mkShell {
  buildInputs = [
    androidComposition.androidsdk
    jdk17
    gradle_8
    git
    curl
    python3
    unzip
    zip
    which
    findutils
    gnugrep
    gnused
  ];
  shellHook = "
    export ANDROID_HOME=${androidComposition.androidsdk}/libexec/android-sdk
    export ANDROID_SDK_ROOT=$ANDROID_HOME
    export AAPT2_PATH=/nix/store/wgvkh0mrmba3dpign2hqv3xf64cs9psb-android-sdk-build-tools-34.0.0/libexec/android-sdk/build-tools/34.0.0/aapt2
    echo SDK=$ANDROID_HOME
    echo AAPT2=$AAPT2_PATH
  ";
}
' --run "bash ./BUILD_AND_PUSH_1_8_5_FIXED.sh"
