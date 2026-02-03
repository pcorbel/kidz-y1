package com.kidz.y1.models;

import java.io.File;

/**
 * Represents a music album (collection of tracks).
 */
public class Album {
    private final File directory;
    private final String name;
    private final Profile profile;

    public Album(File directory, Profile profile) {
        this.directory = directory;
        this.name = directory.getName();
        this.profile = profile;
    }

    public File getDirectory() {
        return directory;
    }

    public String getName() {
        return name;
    }

    public Profile getProfile() {
        return profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return directory.equals(album.directory);
    }

    @Override
    public int hashCode() {
        return directory.hashCode();
    }

    @Override
    public String toString() {
        return "Album{" +
                "name='" + name + '\'' +
                ", directory=" + directory +
                '}';
    }
}
