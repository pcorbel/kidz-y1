package com.kidz.y1.models;

import java.io.File;

/**
 * Represents a music track (MP3 file).
 */
public class Track {
    private final File file;
    private final String name;
    private final Album album;

    public Track(File file, Album album) {
        this.file = file;
        String fileName = file.getName();
        if (fileName.toLowerCase().endsWith(".mp3")) {
            this.name = fileName.substring(0, fileName.length() - 4);
        } else {
            this.name = fileName;
        }
        this.album = album;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public Album getAlbum() {
        return album;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return file.equals(track.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public String toString() {
        return "Track{" +
                "name='" + name + '\'' +
                ", file=" + file +
                '}';
    }
}
