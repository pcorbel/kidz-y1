package com.kidz.y1.models;

import java.io.File;

/**
 * Represents a user profile (music collection directory).
 */
public class Profile {
    private final File directory;
    private final String name;

    public Profile(File directory) {
        this.directory = directory;
        this.name = directory.getName();
    }

    public File getDirectory() {
        return directory;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return directory.equals(profile.directory);
    }

    @Override
    public int hashCode() {
        return directory.hashCode();
    }

    @Override
    public String toString() {
        return "Profile{" +
                "name='" + name + '\'' +
                ", directory=" + directory +
                '}';
    }
}
