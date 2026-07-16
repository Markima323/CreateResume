@echo off
docker run --rm -v "%cd%:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn %*
