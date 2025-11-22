package datatypes;

/**
 * A simple generic container class that stores an immutable key–value pair.
 * <p>
 * This class is marked as deprecated and should not be used in new code.
 * Consider using {@link java.util.Map.Entry} or a specialized record/class instead.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
@Deprecated
public class Pair<K, V> {
    private final K key;
    private final V value;

    /**
     * Constructs a new {@code Pair} containing the given key and value.
     *
     * @param k the key; may be {@code null}
     * @param v the value; may be {@code null}
     */
    public Pair(K k, V v) {
        this.key = k;
        this.value = v;
    }


    /**
     * Returns the key of this pair.
     *
     * @return the key (may be {@code null})
     */
    public K getKey() {
        return this.key;
    }

    /**
     * Returns the value of this pair.
     *
     * @return the value (may be {@code null})
     */
    public V getValue() {
        return this.value;
    }



    /**
     * Computes a hash code for this pair based on its key and value.
     * <p>
     * This implementation is null-safe and attempts to distribute values
     * by combining the hash codes of the components.
     *
     * @return the computed hash code
     */
    @Override
    public int hashCode() {
        int keyHash = this.key == null ? 0 : this.key.hashCode();
        int valueHash = this.value == null ? 0 : this.value.hashCode();
        keyHash = 37 * keyHash + valueHash ^ valueHash >>> 16;
        return keyHash;
    }

    /**
     * Returns a human-readable representation of this pair.
     *
     * @return a string of the form "[key, value]"
     */
    @Override
    public String toString() {
        return "[" + this.getKey() + ", " + this.getValue() + "]";
    }
}