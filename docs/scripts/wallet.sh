#!/bin/bash

# Ensure that the correct number of parameters are provided
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <username>"
  exit 1
fi

# Assign parameter to variable
TARGET_USERNAME=$1

# User credentials
USERNAME="user1"
PASSWORD="password1"

# Wallet API endpoint
API_URL="http://localhost:8080/user/wallet/$TARGET_USERNAME"

# Send the HTTP GET request with Basic Auth
response=$(curl -s -w "\n%{http_code}" -X GET "$API_URL" \
  -H "Content-Type: application/json" \
  -u "$USERNAME:$PASSWORD")

# Extract the response body and status code
body=$(echo "$response" | sed '$d')
status_code=$(echo "$response" | tail -n1)

# Check the response status code
if [ "$status_code" -eq 200 ]; then
  echo "Wallet state for $TARGET_USERNAME: $body"
else
  echo "Failed to retrieve wallet state. HTTP status code: $status_code"
fi
