package com.jsrr.android_app93;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Tag} class represents a tag that is part of a Photo.
 * It provides functionality to manage the name and value of the tag.
 * It also provides methods to compare tags and generate a string representation of the tag.
 *
 * <p>This class implements {@link Serializable} to allow serialization of album objects.
 *
 * <p>Features of the {@code Tag} class include:
 * <ul>
 *   <li>Managing instance fields</li>
 *   <li>Retrieving a string representation of tag</li>
 *   <li>Comparing equality and getting a hashcode</li>
 * </ul>
 *
 * @author [Joseph Scarpulla and Roger Ramirez]
 * @version 1.0
 */
public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String value;

    public static List<String> tagTypes = new ArrayList<>();
    static {
        tagTypes.add("Location");
        tagTypes.add("Person");
    }

    /**
     * Constructs a {@code Tag} with the specified name and value.
     * @param name the name of the tag
     * @param value the value of the tag
     */
    public Tag(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Retrieves the name of the tag.
     * @return the name of the tag
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the value of the tag.
     * @return the value of the tag
     */
    public String getValue() {
        return value;
    }

    /**
     * Retrieves the Tag's String representation.
     * @return the string representation of the tag
     */
    @Override
    public String toString() {
        return name + ": " + value;
    }

    /**
     * Compares this tag to the specified object for equality.
     * @param obj the object to compare
     * @return true if the specified object is equal to this tag, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tag other = (Tag) obj;
        return name.equalsIgnoreCase(other.name) && value.equalsIgnoreCase(other.value);
    }

    /**
     * Returns the hash code value for this tag.
     * @return the hash code value for this tag
     */
    @Override
    public int hashCode() {
        return (name.toLowerCase() + ":" + value.toLowerCase()).hashCode();
    }
}