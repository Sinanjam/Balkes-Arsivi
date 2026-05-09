{ pkgs ? import <nixpkgs> {
    config = {
      allowUnfree = true;
      android_sdk.accept_license = true;
    };
  }
}:

let
  androidComposition = pkgs.androidenv.composeAndroidPackages {
    platformVersions = [ "35" "34" ];
    buildToolsVersions = [ "35.0.0" "34.0.0" ];
    includeNDK = false;
  };
in pkgs.mkShell {
  buildInputs =
    [ androidComposition.androidsdk ]
    ++ androidComposition."build-tools"
    ++ (with pkgs; [
      jdk17
      gradle_8
      git
      curl
      python3
      unzip
      zip
      findutils
      gnugrep
      gnused
      coreutils
    ]);
  ANDROID_HOME = "${androidComposition.androidsdk}/libexec/android-sdk";
  ANDROID_SDK_ROOT = "${androidComposition.androidsdk}/libexec/android-sdk";
}
