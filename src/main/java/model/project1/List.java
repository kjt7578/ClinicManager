package model.project1;
import java.util.Iterator;

/**
 * Represents a dynamically resizable list that stores elements of type E.
 * This class provides methods for adding, removing, and accessing elements,
 * as well as checking the list's size and whether it is empty.
 * The class implements the Iterable interface to allow for iteration over the elements.
 *
 * @author Stephen Kwok and Jeongtae Kim
 */
public class List<E> implements Iterable<E> {
    private E[] objects;
    private int size;

    private static final int INITIAL_SIZE = 4;
    private static final int GROWTH_INCREMENT = 4;
    private static final int NOT_FOUND = -1;

    /**
     * Constructs an empty List with an initial capacity of 4.
     */
    public List() {
        objects = (E[]) new Object[INITIAL_SIZE];
        size = 0;
    }

    /**
     * Finds the index of the specified object.
     * @param e The object to find.
     * @return The index of the object, or NOT_FOUND if not found.
     */
    private int find(E e) {
        for (int i = 0; i < size; i++) {
            if (objects[i].equals(e)) {
                return i;
            }
        }
        return NOT_FOUND;
    }

    /**
     * Increases the capacity of the objects array by a fixed increment.
     */
    private void grow() {
        int newSize = objects.length + GROWTH_INCREMENT;
        @SuppressWarnings("unchecked")
        E[] newObjects = (E[]) new Object[newSize];
        for (int i = 0; i < size; i++) {
            newObjects[i] = objects[i];
        }
        objects = newObjects;
    }

    /**
     * Checks if the specified object is present in the list.
     * @param e The object to check for.
     * @return true if the object is found, false otherwise.
     */
    public boolean contains(E e) {
        return find(e) != NOT_FOUND;
    }

    /**
     * Adds a new object to the list.
     * @param e The object to add.
     */
    public void add(E e) {
        if (size == objects.length) {
            grow();
        }
        objects[size++] = e;
    }

    /**
     * Removes the specified object from the list.
     * @param e The object to remove.
     * @return true if the object was removed, false if it was not found.
     */
    public boolean remove(E e) {
        int index = find(e);
        if (index != NOT_FOUND) {
            for (int i = index; i < size - 1; i++) {
                objects[i] = objects[i + 1];
            }
            objects[--size] = null; // Avoid memory leak
            return true;
        }
        return false;
    }

    /**
     * Checks if the list is empty.
     * @return true if the list is empty, false otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the current number of objects in the list.
     * @return The current size of the list.
     */
    public int size() {
        return size;
    }

    /**
     * Returns the object at the specified index.
     * @param index The index of the object to retrieve.
     * @return The object at the specified index, or null if out of bounds.
     */
    public E get(int index) {
        if (index < 0 || index >= size) {
            return null; // Index out of bounds
        }
        return objects[index];
    }

    /**
     * Sets the object at the specified index.
     * @param index The index to set the object at.
     * @param e The object to set.
     */
    public void set(int index, E e) {
        if (index >= 0 && index < size) {
            objects[index] = e;
        }
    }

    /**
     * Returns the index of the specified object.
     * @param e The object to find.
     * @return The index of the object, or -1 if not found.
     */
    public int indexOf(E e) {
        return find(e);
    }

    /**
     * Returns an iterator over the elements in the list.
     * @return An iterator for the list.
     */
    @Override
    public Iterator<E> iterator() {
        return new ListIterator();
    }

    /**
     * Internal class to implement the iterator for List<E>.
     */
    private class ListIterator implements Iterator<E> {
        private int currentIndex = 0;

        /**
         * Checks if there is a next object in the list.
         * @return true if there is a next object, false otherwise.
         */
        @Override
        public boolean hasNext() {
            return currentIndex < size;
        }

        /**
         * Returns the next object in the list.
         * @return The next object in the list.
         */
        @Override
        public E next() {
            if (!hasNext()) {
                return null;
            }
            return objects[currentIndex++];
        }
    }
}