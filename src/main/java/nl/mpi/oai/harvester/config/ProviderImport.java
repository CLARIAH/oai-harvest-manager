package nl.mpi.oai.harvester.config;

import org.w3c.dom.Node;

import java.util.List;

/**
 * Pluggable provider source, referenced from a {@code <import class="...">}
 * element in the harvest configuration.
 *
 * <p>An implementation is loaded reflectively by {@code Configuration}: either
 * from the classpath or from a jar file named by the {@code file} attribute on
 * the {@code <import>} element. It is handed the {@code <import>} DOM node so
 * that it can read whatever child elements it understands (for example
 * {@code <registry>} or {@code <sparql>}) and produce the resulting providers.
 *
 * <p>The returned {@link ImportedProvider} DTOs are turned into
 * {@code nl.mpi.oai.harvester.Provider} objects by {@code Configuration}, with
 * the shared {@code <exclude>} and {@code <config url="...">} overrides applied
 * exactly like providers imported from the Centre Registry.
 *
 * <p>Implementations must provide a public no-argument constructor.
 */
public interface ProviderImport {

    /**
     * Produce the providers defined by this import.
     *
     * @param importNode the {@code <import>} DOM node; its children carry the
     *                   implementation-specific configuration (registry URL,
     *                   SPARQL query, etc.)
     * @return the providers to harvest from
     * @throws Exception if the import cannot be performed
     */
    List<ImportedProvider> getProviders(Node importNode) throws Exception;
}
