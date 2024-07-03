package datatypes;

@Deprecated
public class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K k, V v) {
        this.key = k;
        this.value = v;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }



    public int hashCode() {
        int keyHash = this.key == null ? 0 : this.key.hashCode();
        int valueHash = this.value == null ? 0 : this.value.hashCode();
        keyHash = 37 * keyHash + valueHash ^ valueHash >>> 16;
        return keyHash;
    }

    public String toString() {
        return "[" + this.getKey() + ", " + this.getValue() + "]";
    }
}