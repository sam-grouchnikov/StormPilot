#!/bin/bash
# Exit on failure
set -e

# Change directory to the script's directory (StormPilot root)
cd "$(dirname "$0")"

echo "=========================================================="
echo "🚀 Running all StormPilot Unit Tests..."
echo "=========================================================="

./gradlew allTests

echo ""
echo "=========================================================="
echo "✅ All tests compiled and completed successfully!"
echo "=========================================================="
