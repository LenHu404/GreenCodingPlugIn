
# Inspector GreenCode IntelliJ Plugin

## Overview
The Inspector GreenCode plugin for IntelliJ IDEA is designed to help developers write more efficient and environmentally friendly code. It leverages AI to analyze code and provide suggestions for improvements that can reduce energy consumption and enhance performance.

## Features
- **Code Analysis**: Analyze selected code or entire files for potential improvements.
- **AI-Powered Suggestions**: Utilize OpenAI or Ollama services to provide AI-generated suggestions for code optimization.
- **In-Editor Notifications**: Receive notifications about settings or connectivity issues directly in the IDE.
- **Configurable Settings**: Easily configure the plugin to use your preferred AI service and set necessary API keys and endpoints.

## Installation
1. Clone the repository:
    ```bash
    git clone https://github.com/LenHu404/GreenCodingPlugIn.git
    ```
2. Open the project in IntelliJ IDEA.
3. Build the project and install the plugin.

## Usage
1. Set the API key and endpoint in the plugin settings before using the plugin.
2. Select the code you want to analyze, or open the file you want to process.
3. Right-click and select "Inspector GreenCode" from the context menu, or use the shortcut `Ctrl + Alt + G` followed by `C`.
4. The plugin will analyze the selected code or the entire file and display a preview dialog with suggestions and improvements.
5. Review the suggested changes and apply them if desired.

## Settings
### Accessing Settings
- Go to `File > Settings > Tools > Inspector Green Code Settings` to configure the plugin.

### Configurable Options
- **Service**: Choose between OpenAI or Ollama for AI-powered code analysis.
- **Endpoint**: Set the API endpoint for the selected service.
- **API Key**: Enter the API key for authenticating with the selected service.
- **Mode**: Choose between "Few-Shot" or "One-Shot" modes for the AI analysis.

## Ollama Implementation
To use the local Ollama implementation, follow the steps in the following repository:
https://github.com/Jonnyre/Green_Coding_Backend.git
