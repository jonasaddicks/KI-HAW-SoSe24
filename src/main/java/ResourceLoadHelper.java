import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class ResourceLoadHelper {

    /**
     * Resolves a classpath resource by its name and returns it as a {@link URI}.
     *
     * <p>
     * This method looks up the given resource using the class loader of {@code ResourceLoadHelper}.
     * If the resource cannot be found, a {@link NullPointerException} is thrown with a descriptive message.
     * If the resource URL cannot be converted into a valid {@link URI}, a {@link URISyntaxException} is thrown.
     * </p>
     *
     * <p>
     * This is typically used to access configuration files, datasets, or other bundled
     * application resources in a location-independent way.
     * </p>
     *
     * @param name the path of the resource to be resolved, relative to the classpath root
     * @return the {@link URI} representing the resolved resource location
     * @throws URISyntaxException   if the resolved resource URL is not a valid URI
     * @throws NullPointerException if no resource with the given name is found on the classpath
     */
    public static URI loadResource(String name) throws URISyntaxException, NullPointerException {
        return Objects.requireNonNull(ResourceLoadHelper.class.getResource(name), String.format("resource with name '%s' not found", name)).toURI();
    }
}