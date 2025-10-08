# How to Update the App's Website Content

This document provides a detailed, step-by-step guide on how to replace the app's offline website content with your own.

## Overview

The Poet Music app does not load a live website from the internet. Instead, it displays a website that is stored directly inside the app package. This allows the app to work completely offline.

This website content is stored in a single compressed file named `index.zip`. To update the website displayed in the app, you must replace this `index.zip` file with a new one containing your website's files.

---

## Prerequisites

Before you begin, you will need:

1.  **Your complete website files:** This includes your main `index.html` file, all CSS files (`.css`), JavaScript files (`.js`), images (`.png`, `.jpg`, etc.), and any other assets your site needs to function.
2.  **A file compression tool:** Most operating systems have a built-in tool to create `.zip` files. (e.g., "Compress" on macOS, "Send to > Compressed (zipped) folder" on Windows).

---

## Step 1: Prepare Your Website Files

This is the most critical step. The structure of your files *before* you zip them is very important.

1.  Gather all your website files and place them into a single folder. Let's call this folder `my_website` for this example.

2.  **Crucially, your main HTML file must be named `index.html` and it must be at the top level (the "root") of this folder.**

    #### ✅ Correct Folder Structure:

    When you open the `my_website` folder, you should immediately see `index.html`.

    ```
    my_website/
    ├── index.html  <-- Correct! At the top level.
    ├── style.css
    ├── scripts.js
    └── images/
        └── logo.png
    ```

    #### ❌ Incorrect Folder Structure:

    Do not have your website nested inside another sub-folder. The app will not be able to find it.

    ```
    my_website/
    └── my_actual_site/  <-- This extra folder will break the app!
        ├── index.html
        └── style.css
    ```

---

## Step 2: Create the `index.zip` File

Now you will compress your prepared files into the required `index.zip` file.

1.  Open your `my_website` folder.
2.  Select **all** the files and folders inside it (e.g., `index.html`, `style.css`, `images/`). **Do not select the `my_website` folder itself.**
3.  Right-click on the selected files.
4.  From the context menu, choose the option to compress the files.
    *   **On Windows:** `Send to > Compressed (zipped) folder`
    *   **On macOS:** `Compress X Items`
5.  This will create a new `.zip` file. Your system might name it something like `archive.zip` or `images.zip` (if the `images` folder was the first thing you clicked).
6.  **You must rename this file to `index.zip`**. This exact name is required.

---

## Step 3: Locate the Project's `assets` Folder

You now need to find where to put your new `index.zip` file in this project.

1.  In the project's file explorer, navigate through the following directories:
    `app` -> `src` -> `main` -> `assets`

2.  The full path is: `app/src/main/assets/`

3.  Inside this `assets` folder, you will see the existing `index.zip` file. This is the file you will replace.

---

## Step 4: Replace the Old `index.zip`

1.  **Delete** the old `index.zip` file from the `app/src/main/assets/` folder.
2.  **Copy or move** your new `index.zip` (that you created in Step 2) into this exact location: `app/src/main/assets/`.

---

## Final Check

After following these steps, your project structure should look like this:

```
poet-music-app/
└── app/
    └── src/
        └── main/
            └── assets/
                └── index.zip  <-- This should be YOUR zip file.
```

That's it! No code changes are necessary. The next time the app is built and run, it will automatically find your new `index.zip`, unzip it, and display your `index.html` page.