#!/usr/bin/env bash
cd "/Users/fanzj/Desktop/Soft666-1"
if ./scripts/ta-recruitment.sh build; then
  ./scripts/ta-recruitment.sh foreground
else
  echo
  echo "Run failed. Please check the error message above."
  read -r -p "Press Enter to exit..."
fi
