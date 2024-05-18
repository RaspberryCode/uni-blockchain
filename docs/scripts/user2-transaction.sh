#!/bin/bash

# Ensure that both parameters are provided
if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <receiverUsername> <value>"
  exit 1
fi

# Assign parameters to variables
RECEIVER_USERNAME=$1
VALUE=$2

# User credentials
USERNAME="user2"
PASSWORD="password2"

# Transaction API endpoint
API_URL="http://localhost:8080/user/transactions"

# Create the JSON payload
PAYLOAD=$(cat <<EOF
{
  "recipient": "$RECEIVER_USERNAME",
  "amount": $VALUE
}
EOF
)

# Send the HTTP POST request with Basic Auth
response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -u "$USERNAME:$PASSWORD" \
  -d "$PAYLOAD")

# Check the response status code
if [ "$response" -eq 200 ]; then
  echo "Transaction created and broadcasted successfully."
else
  echo "Failed to create transaction. HTTP status code: $response"
fi
