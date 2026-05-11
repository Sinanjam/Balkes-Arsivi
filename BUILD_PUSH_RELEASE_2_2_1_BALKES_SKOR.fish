#!/usr/bin/env fish
set -lx NIXPKGS_ALLOW_UNFREE 1
nix-shell ./balkes-android-2-0.nix --run 'bash ./BUILD_PUSH_RELEASE_2_2_1_BALKES_SKOR.sh'
