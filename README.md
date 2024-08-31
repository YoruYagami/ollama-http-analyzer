
# BurpAI: AI-Powered Web Security Assistant

BurpAI is a powerful Burp Suite extension that leverages artificial intelligence to elevate your web security testing workflow. Featuring an interactive chat interface, BurpAI allows you to engage with an AI model for advanced analysis and tailored assistance directly within Burp Suite. Additionally, BurpAI supports a flexible plugin system, enabling users to choose and run specialized plugins, further enhancing its capabilities to meet your unique testing needs.

![Logo](https://github.com/alpernae/BurpAI/blob/main/assets/BURPAI_LOGO.png)


## Key Features

- **Interactive Chat**: Engage in a chat-like conversation with an AI model within Burp Suite to gain insights, generate payloads, and receive security advice.

- **Plugin Selection**: Choose from a variety of plugins or AI models to tailor your security testing to specific needs. Analyze different web application vulnerabilities, such as XSS, SQL injection, and more.

- **Request/Response Analysis**: Analyze specific web requests and responses to identify potential security issues. The AI model can pinpoint common vulnerabilities, suggest potential exploits, and provide insights into the application's security posture.

- **Payload Generation**: Ask the AI model to generate payloads for various vulnerabilities, including XSS, SQL injection, and command injection. Automate the testing process and increase efficiency.

- **Security Advice**: Receive security advice and recommendations based on the analysis of web requests, responses, and the application's overall security posture. Identify and mitigate potential risks.
## FAQ

<details>
  <summary>Can I use BurpAI with the Pro version of Burp Suite?</summary>
  
  Yes, **BurpAI** is fully compatible with both the Community and Pro versions of Burp Suite. You can take advantage of all the features of BurpAI regardless of which version of Burp Suite you are using.
</details>


## Installation

**Prerequisites:**

* **Burp Suite:** Ensure you have Burp Suite Professional or Community Edition installed.
* **Python 2:**  Download and install a compatible version of Python 2 if it's not already on your system.
* **Pip:**  A package installer for Python, usually installed along with Python. If you don't have it, you can get it from [https://pip.pypa.io/en/stable/installation/](https://pip.pypa.io/en/stable/installation/)
* **Jython:** Burp Suite uses Jython for its Python extensions. Download and install a compatible version from the [Jython website](https://www.jython.org/download.html). 



1. **Download BurpAI:**
   - Download the latest release of BurpAI from the [GitHub repository](https://github.com/alpernae/BurpAI).
   - Extract the contents of the downloaded ZIP file to a location on your computer. 

2. **Install Python Dependencies:**
   + Open a terminal or command prompt.
   + Navigate to the `setup` folder within the extracted BurpAI directory. 
   + Run the following command to install the required Python libraries:

      ```bash
      pip install -r requirements.txt 
      ```

3. **Configure BurpAI:**

   **Access Google AI Studio:** 
      - Open your web browser and go to [https://aistudio.google.com/](https://aistudio.google.com/). Make sure you are logged into your Google account.

   **Navigate to API Keys:**
     - In the left-hand menu, click on **GET API Key**. 

   **Create a New API Key:**
      - Click on the **CREATE API KEY** button.
      - Then click **Create API key in new project**.

   **Copy Your API Key:**
      - Your newly created API key will be displayed on the screen. **Copy this key** and save it for future use in BurpAI.

   - **Burp Suite Extension:**
     - Open Burp Suite.
     - Go to the "Extension" tab.
     - Click "Extension Settings" tab.
     - Set-up Python environment by givin Jyton path.
     - Navigate main tab and click on "Add" and select "Extension Type: Python".
     - In the "Extension Details" section:
       - Click "Select file..." and choose the `BurpAI.py` file from the extracted BurpAI directory. 
       - Enter your API key in the appropriate field within the extension's settings (You should see a configuration section in BurpAI's tab after loading it).

5. **Use BurpAI:**
   - The BurpAI tab in Burp Suite will now be active.
   - Click the "Activate" button to start interacting with the AI. To stop, simply click the "Deactivate" button.
   - Begin your interaction with the AI through the chat interface. Send prompts, request analysis, and generate payloads to enhance your web security testing.

## Feedback

If you have any feedback, please reach out to us at alperene@aof.anadolu.edu.tr

## License

This project is licensed under the CC BY-NC 4.0 License. For more information, please refer to the [LICENSE](https://choosealicense.com/licenses/mit/) file
