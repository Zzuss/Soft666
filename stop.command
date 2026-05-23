#!/usr/bin/env bash
cd "/Users/fanzj/Desktop/Soft666-1"
if ./scripts/ta-recruitment.sh stop; then
  echo
  echo "Tomcat stop command sent. You can close this window."
else
  echo
  echo "Stop failed. Please check the error message above."
fi
read -r -p "Press Enter to exit..."
