#!/usr/bin/env bash
set -euo pipefail
export NIXPKGS_ALLOW_UNFREE=1
nix-shell ./balkes-android-2-0.nix --run 'bash ./BUILD_AND_PUSH_2_0_FIXED.sh'
