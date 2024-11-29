> [!NOTE]  
> EEAgent was made at hackaTUM24, the official hackathon of the Department of Computer Science at the Technical University of Munich, and won the 2nd place at the
> JetBrains Challange!

# EEAgent

An OpenAI based JetBrains Plugin that fixes all errors in a file by precompiling the fixed code. This way you can be sure not to run into even more errors while trying to fix your code with LLMs.

> [!IMPORTANT]  
> This project was made as a part of the JetBrains Challange at hackaTUM 2024. This plugin was created in less than 36 hours and therefore does not aim to be a perfect plugin.

## Inspiration
Drawing inspiration from various JetBrains plugins, EEAgent strives to simplify the lives of developers. One of the most frustrating aspects of development is error correction — finding and fixing issues can be unintuitive, especially when error messages are unclear or uninformative. EEAgent tackles this challenge by serving as an intelligent assistant that not only identifies errors but also provides guaranteed solutions, enabling developers to work more efficiently and with less stress.

EEAgent also saves significant time. Unlike traditional approaches using LLMs, where developers must repeatedly test their code after each correction, EEAgent streamlines the entire process. It excels in scenarios where generative AI models fall short, offering reliable and actionable fixes to ensure smoother development workflows.

## What it does
EEAgent resolves Kotlin and Java code errors through a structured, agent-driven process that combines automation with user interaction. Here's how it works:

1. Configuration:
The process begins with the user configuring the plugin settings. This includes setting the OpenAI API key, selecting the desired LLM model, and defining additional parameters like the maximum number of iterations. These settings ensure that the plugin aligns with the user’s preferences and requirements.

2. Error Detection:
After the configuration, EEAgent begins by running the user’s code in a controlled environment to determine whether it throws an error. If no errors are detected, the user is immediately notified with a message confirming that the code is functioning correctly, and no further action is required. However, if an error is identified, EEAgent captures the problematic code along with the associated error message. This information is then sent to the selected LLM in a structured prompt, designed to provide the model with all necessary context for generating a corrected version of the code.

3. Error Correction:
The LLM processes the input and generates a corrected version of the code. EEAgent evaluates this response by running the revised code. If the corrected code executes without errors, the process moves to the output stage. If the error persists, the new error message and updated code are sent back to the LLM for further refinement. This cycle continues iteratively until the error is resolved or the maximum iteration limit is reached, as defined in the plugin settings.

4. Handling Unresolved Errors:
If the LLM cannot fix the error within the predefined iteration limit, EEAgent notifies the user that the error could not be resolved automatically.

5. User Output:
Once the process is complete, whether the error has been resolved or the maximum number of iterations has been reached, EEAgent provides clear feedback to the user.

If the error is successfully fixed, the corrected code is displayed in a dedicated window. All changes made to the code are highlighted, making it easy for the user to review and understand the modifications. The user can then make additional adjustments, if necessary, and either accept or reject the changes.

## How we built it
The plugin was built with the plugin template provided by JetBrains. It is written in Java and is based on the following architecture: 
1. Action Layer: Handles user-triggered events in the IDE, like button clicks, to initiate tasks.
2. Service Layer: Core logic for code execution (Java/Kotlin), API interactions (OpenAI), and iterative code improvements.
3. Model Layer: Defines data structures, like ExecutionResult, for standardized handling of execution outcomes.
## Challenges we ran into
Developing EEAgent was not without its hurdles. One major challenge was our initial unfamiliarity with the IntelliJ Plugin SDK. Navigating its documentation, understanding its architecture, and integrating it effectively into our project required significant effort and time.

We also encountered several dependency errors during the development process. 

Additionally, we faced minor implementation difficulties when integrating certain features. Fine-tuning the communication with the LLM and ensuring a seamless user experience posed technical and design challenges that we had to address iteratively.

## Accomplishments that we're proud of
One of our proudest achievements is successfully building a fully functional plugin from scratch within a short timeframe. EEAgent is not only operational but also designed to assist developers effectively in their workflow, addressing common challenges in the development process.

## What we learned
Throughout the development of EEAgent, we gained valuable insights and skills in several areas:

**Building with the IntelliJ Plugin SDK**: We learned how to work with the IntelliJ Plugin SDK from the ground up, gaining a deeper understanding of its architecture and how to leverage it for creating robust plugins.
**Utilizing LLMs for AI Agents**: By integrating large language models, we explored the potential of AI-driven solutions, learning how to design prompt templates, handle iterative feedback loops, and deliver practical, user-friendly functionality.
**Teamwork and Collaboration**: This project reinforced the importance of teamwork, from dividing tasks effectively to supporting one another through challenges.
**Pushing Through Adversity**: Finally, we discovered how to manage tight deadlines and push forward despite sleep deprivation, finding motivation and creativity even in demanding circumstances.

## What's next for EEAgent
We plan to implement several key enhancements to further expand its capabilities. One major improvement is adding support for multiple files. This feature will allow EEAgent to handle and resolve errors across entire projects, making it an even more powerful tool for developers working with complex codebases.

Additionally, we aim to broaden language support by integrating more programming languages into EEAgent. This will enable developers from various domains to use the plugin, improving its versatility and usefulness for a wider audience.


