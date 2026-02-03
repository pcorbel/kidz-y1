---
layout: default
title: Kidz-Y1
---

<div class="hero-section">
  <div class="hero-content">
    <h1 class="hero-title">Kidz-Y1</h1>
    <p class="hero-subtitle">A simple MP3 player designed for kids</p>
  </div>
</div>

<div class="container">
  <section class="intro-section">
    <h2>What is Kidz-Y1?</h2>
    <p>Kidz-Y1 is a kid-friendly mp3 player for Innioasis Y1 devices. You don't need to create playlists or add special information to your music files. Just put your music files in folders on your device, and the app will show them the same way you organized them.</p>
    
    <div class="features-grid">
      <div class="feature-card">
        <div class="feature-icon">🎧</div>
        <h3>Simple for Kids</h3>
        <p>Kids who want a simple way to listen to music</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">👨‍👩‍👧‍👦</div>
        <h3>Parent-Friendly</h3>
        <p>Parents organizing music by child or theme</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">📚</div>
        <h3>Educational</h3>
        <p>Educational content organized by subject or story</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">🔈</div>
        <h3>Audiobooks</h3>
        <p>Audiobooks and stories organized by series</p>
      </div>
    </div>
  </section>

  <section class="images-section">
    <h2>📸 Screenshots</h2>
    <div class="screenshots-grid">
      <div class="screenshot-card">
        <img src="assets/images/coverflow.png" alt="Coverflow view" class="screenshot-image">
        <p class="screenshot-caption">Browse your music with the coverflow view</p>
      </div>
      <div class="screenshot-card">
        <img src="assets/images/playing.png" alt="Playing view" class="screenshot-image">
        <p class="screenshot-caption">Enjoy your music with an intuitive player interface</p>
      </div>
    </div>
  </section>

  <section class="installation-section">
    <h2>📥 Installation</h2>
    <ol>
      <li><strong>Download the latest ROM</strong> — Get the latest <code>rom.zip</code> from <a href="https://github.com/pcorbel/kidz-y1/releases/latest">GitHub Releases</a>.</li>
      <li><strong>Install with the Innioasis Updater</strong> — Use the official <a href="https://innioasis.app/">Innioasis Updater</a> app and follow these steps:</li>
    </ol>
    <div class="install-steps">
      <div class="install-step">
        <p><strong>1°)</strong> Open the Innioasis Updater app.</p>
        <img src="assets/images/app.png" alt="Innioasis Updater app" class="install-image">
      </div>
      <div class="install-step">
        <p><strong>2°)</strong> Select the <code>rom.zip</code> you previously downloaded.</p>
        <img src="assets/images/select-rom.png" alt="Select rom.zip" class="install-image">
      </div>
      <div class="install-step">
        <p><strong>3°)</strong> Connect your Y1 device.</p>
        <img src="assets/images/connect.png" alt="Connect Y1 device" class="install-image">
      </div>
      <div class="install-step">
        <p><strong>4°)</strong> Wait for the install to complete.</p>
        <img src="assets/images/install.png" alt="Install in progress" class="install-image">
      </div>
      <div class="install-step">
        <p><strong>5°)</strong> Done — enjoy!</p>
      </div>
    </div>
  </section>

  <section class="switching-section">
    <h2>🔄 Switching Between Apps</h2>
    <p>You can switch between the Stock launcher and Kidz by pressing and holding the back button for 5 seconds. This allows you to easily access the full Stock interface when needed, while keeping the simple Kidz interface as the one for kids.</p>
  </section>

  <section class="organization-section">
    <h2>📁 How to Organize Your Music</h2>
    
    <h3>Folder Structure</h3>
    <p>The app looks for files in a simple three-level folder structure:</p>
    
    <div class="code-block">
      <pre><code>/sdcard/Kidz/
  ├── Alice/              ← Profile folder
  │   ├── Stories/        ← Album folder
  │   │   ├── Story1.mp3  ← MP3 file
  │   │   ├── Story2.mp3
  │   │   └── Story3.mp3
  │   └── Songs/
  │       └── Song1.mp3
  └── Bob/
      └── Stories/
          └── Story1.mp3</code></pre>
    </div>

    <h3>Adding Images</h3>
    <p>You can add pictures to show as covers for your music. The app looks for image files with the same name as your folders or music files. Here's how:</p>

    <p><strong>Supported picture file types:</strong> PNG or JPG files</p>

    <div class="info-cards">
      <div class="info-card">
        <h4>📷 Pictures for Profiles</h4>
        <p>Put a picture file next to the profile's folder with the same name.<br> For example, if you have a folder called "Alice", put a picture called "Alice.png" or "Alice.jpg" next to it.</p>
        <div class="code-block small">
          <pre><code>/sdcard/Kidz/

├── Alice.png ← Picture for Alice
├── Alice/ ← Alice's music folder
│ └── ...
├── Bob.jpg ← Picture for Bob
└── Bob/ ← Bob's music folder
└── ...</code></pre>
</div>
</div>

      <div class="info-card">
        <h4>📷 Pictures for Albums</h4>
        <p>Put a picture file next to the album folder with the same name.<br> For example, if you have an album folder called "Stories", put a picture called "Stories.png" or "Stories.jpg" next to it.</p>
        <div class="code-block small">
          <pre><code>Alice/

├── Stories.png ← Picture for Stories album
├── Stories/ ← Stories album folder
│ ├── Story1.mp3
│ └── Story2.mp3
└── Songs/ ← Songs album folder
└── Song1.mp3</code></pre>
</div>
</div>

      <div class="info-card">
        <h4>📷 Pictures for Individual Music Files</h4>
        <p>Put a picture file <strong>next to</strong> the music file with the <strong>same name</strong> (but without the .mp3 part).<br> For example, if you have "Story1.mp3", put a picture called "Story1.png" or "Story1.jpg" next to it.</p>
        <div class="code-block small">
          <pre><code>Stories/

├── Story1.mp3
├── Story1.png ← Picture for Story1
├── Story2.mp3
└── Story2.jpg ← Picture for Story2</code></pre>
</div>
</div>
</div>

  </section>

  <section class="footer-section">
    <h2>📄 License</h2>
    <p>See <a href="https://github.com/pcorbel/kidz-y1/blob/main/LICENSE">LICENSE</a> file for details.</p>
    
    <h2>🤝 Contributing</h2>
    <p>Contributions are welcome! Please feel free to submit issues or pull requests.</p>
    
    <div class="heart-footer">
      <p><strong>Made with ❤️ for kids who love music and stories</strong></p>
    </div>
  </section>
</div>
