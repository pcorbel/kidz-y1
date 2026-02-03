# Kidz-Y1 - Kids MP3 Player

A simple MP3 player designed for kids

## 📱 What is Kidz-Y1?

Kidz-Y1 is a kid-friendly mp3 player for Innioasis Y1 devices. You don't need to create playlists or add special information to your music files. Just put your music files in folders on your device, and the app will show them the same way you organized them.

Perfect for:

- 🎧 Kids who want a simple way to listen to music
- 👨‍👩‍👧‍👦 Parents organizing music by child or theme
- 📚 Educational content organized by subject or story
- 🎭 Audiobooks and stories organized by series

## 📸 Screenshots

<p align="center">
  <img src="docs/assets/images/coverflow.png" alt="Coverflow view" width="420"><br>
  <em>Browse your music with the coverflow view</em>
</p>

<p align="center">
  <img src="docs/assets/images/playing.png" alt="Playing view" width="420"><br>
  <em>Enjoy your music with an intuitive player interface</em>
</p>

## 📥 Installation

1. **Download the latest ROM** — Get the latest `rom.zip` from [GitHub Releases](https://github.com/pcorbel/kidz-y1/releases/latest).

2. **Install with the Innioasis Updater** — Use the official [Innioasis Updater](https://innioasis.app/) app and follow these steps:

   **1°)** Open the Innioasis Updater app.  
   <p align="center"><img src="docs/assets/images/app.png" alt="Innioasis Updater app" width="520"></p>

   **2°)** Select the `rom.zip` you previously downloaded.  
   <p align="center"><img src="docs/assets/images/select-rom.png" alt="Select rom.zip" width="520"></p>

   **3°)** Connect your Y1 device.  
   <p align="center"><img src="docs/assets/images/connect.png" alt="Connect Y1 device" width="520"></p>

   **4°)** Wait for the install to complete.  
   <p align="center"><img src="docs/assets/images/install.png" alt="Install in progress" width="520"></p>

    **5°)** Done — enjoy!  

## 🔄 Switching Between Apps

You can switch between the Stock launcher and Kidz by pressing and holding the back button for 5 seconds. This allows you to easily access the full Stock interface when needed, while keeping the simple Kidz interface as the one for kids.

## 📁 How to Organize Your Music

### Folder Structure

The app looks for files in a simple three-level folder structure:

```
/sdcard/Kidz/
  ├── Alice/              ← Profile folder
  │   ├── Stories/        ← Album folder
  │   │   ├── Story1.mp3  ← MP3 file
  │   │   ├── Story2.mp3
  │   │   └── Story3.mp3
  │   └── Songs/
  │       └── Song1.mp3
  └── Bob/
      └── Stories/
          └── Story1.mp3
```

### Adding Images

You can add pictures to show as covers for your music. The app looks for image files with the same name as your folders or music files. Here's how:

**Supported picture file types:** PNG or JPG files

#### Pictures for Profiles

Put a picture file next to the profile's folder with the same name. For example, if you have a folder called "Alice", put a picture called "Alice.png" or "Alice.jpg" next to it.

```
/sdcard/Kidz/
  ├── Alice.png          ← Picture for Alice
  ├── Alice/             ← Alice's music folder
  │   └── ...
  ├── Bob.jpg            ← Picture for Bob
  └── Bob/               ← Bob's music folder
      └── ...
```

#### Pictures for Albums

Put a picture file next to the album folder with the same name. For example, if you have an album folder called "Stories", put a picture called "Stories.png" or "Stories.jpg" next to it.

```
Alice/
  ├── Stories.png        ← Picture for Stories album
  ├── Stories/           ← Stories album folder
  │   ├── Story1.mp3
  │   └── Story2.mp3
  └── Songs/              ← Songs album folder
      └── Song1.mp3
```

#### Pictures for Individual Music Files

Put a picture file **next to** the music file with the **same name** (but without the .mp3 part). For example, if you have "Story1.mp3", put a picture called "Story1.png" or "Story1.jpg" next to it.

```
Stories/
  ├── Story1.mp3
  ├── Story1.png         ← Picture for Story1
  ├── Story2.mp3
  └── Story2.jpg         ← Picture for Story2
```

## 📄 License

See [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

---

**Made with ❤️ for kids who love music and stories**
