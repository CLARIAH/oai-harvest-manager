/*
 * Sample {@link nl.mpi.oai.harvester.config.ProviderImport} implementation.
 *
 * Mirrors the NDE datasetregister use case from nde_config.xml: a <registry url>
 * points at a SPARQL endpoint and a <sparql> child carries the query. The
 * implementation runs the query, reads publisher URIs out of the SPARQL results
 * JSON, and returns one {@link nl.mpi.oai.harvester.config.ImportedProvider}
 * per publisher.
 *
 *   <import class="...NdeImportSample">
 *     <registry url="https://datasetregister.netwerkdigitaalerfgoed.nl/sparql"/>
 *     <sparql>
 *       PREFIX dct: &lt;http://purl.org/dc/terms/&gt;
 *       SELECT DISTINCT ?publisher WHERE {
 *         ?dataset dct:publisher ?publisher .
 *         FILTER (isURI(?publisher) &amp;&amp; !isBLANK(?publisher))
 *       }
 *     </sparql>
 *   </import>
 *
 * Kept under test sources so it is compile-checked against the published API but
 * is NOT shipped in the main jar and is NOT executed as a test (surefire only
 * runs *Test / Test* classes). Copy this file into your extension project,
 * rename the package/class (e.g. to
 * nl.clariah.oai.harvester.config.control.NdeImport) and adjust as needed.
 *
 * The implementation only relies on things already on the harvester runtime
 * classpath:
 *   - json.path (com.jayway.jsonpath)  -- parsing SPARQL results JSON
 *   - JDK java.net (HttpURLConnection) -- the HTTP request
 * so no extra dependencies are required in the extension project.
 */
package nl.mpi.oai.harvester.config.examples;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import nl.mpi.oai.harvester.config.ImportedProvider;
import nl.mpi.oai.harvester.config.ProviderImport;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NdeImportSample implements ProviderImport {

    /**
     * Fallback query used when the {@code <import>} element has no
     * {@code <sparql>} child. Selects all distinct dataset publishers.
     */
    private static final String DEFAULT_QUERY =
            "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "SELECT DISTINCT ?publisher WHERE {\n"
            + "  ?dataset dct:publisher ?publisher .\n"
            + "  FILTER (isURI(?publisher) && !isBLANK(?publisher))\n"
            + "}";

    private static final Configuration JSON_CONF;

    static {
        JSON_CONF = Configuration.defaultConfiguration();
        // ALWAYS_RETURN_LIST: always return a List even for a single match.
        // SUPPRESS_EXCEPTIONS: return null/empty when the path does not match
        //   (e.g. empty result set) instead of throwing.
        JSON_CONF.addOptions(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS);
    }

    @Override
    public List<ImportedProvider> getProviders(Node importNode) throws Exception {
        // 1) read configuration from the <import> element's children
        final String endpoint = readChildAttribute(importNode, "registry", "url");
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException(
                    "NdeImportSample requires a <registry url=\"...\"/> child in <import>");
        }
        final String queryText = readChildText(importNode, "sparql");
        final String query = (queryText != null && !queryText.isBlank())
                ? queryText : DEFAULT_QUERY;

        // 2) run the SPARQL query against the registry endpoint
        final String resultsJson = executeSparql(endpoint, query);

        // 3) extract the publisher URIs from the SPARQL results JSON. The W3C
        //    SPARQL results JSON shape is:
        //      { "results": { "bindings": [ { "publisher": { "value": "..." } } ] } }
        final List<String> publisherUrls = JsonPath.using(JSON_CONF)
                .parse(resultsJson)
                .read("$.results.bindings[*].publisher.value");
        if (publisherUrls == null) {
            return Collections.emptyList();
        }

        // 4) turn each publisher URI into a provider. The harvester applies the
        //    global <settings> defaults (scenario, timeout, retry, ...) and any
        //    <exclude>/<config> overrides afterwards, so we only need to set
        //    what the import itself contributes here.
        final List<ImportedProvider> providers = new ArrayList<>(publisherUrls.size());
        for (String url : publisherUrls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            final ImportedProvider p = new ImportedProvider();
            p.url = url;
            // Optional per-provider fields. Leave null to fall back to the
            // global defaults, or set them here to customise per provider:
            //   p.name           = deriveName(url);
            //   p.scenario       = "ListRecords";
            //   p.timeout        = 60;          // seconds
            //   p.sets           = new String[]{ "some-set" };
            //   p.staticProvider = false;
            providers.add(p);
        }
        return providers;
    }

    /**
     * Run a SPARQL SELECT via HTTP GET and return the
     * {@code application/sparql-results+json} response body.
     */
    private static String executeSparql(String endpoint, String query) throws Exception {
        final String url = endpoint + (endpoint.contains("?") ? "&" : "?")
                + "query=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/sparql-results+json");
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(60_000);
            final int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new RuntimeException("SPARQL endpoint " + endpoint
                        + " returned HTTP " + status);
            }
            try (InputStream in = conn.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            conn.disconnect();
        }
    }

    // ---- small DOM helpers (no external XML library needed) ----

    private static String readChildAttribute(Node parent, String childName, String attr) {
        final Node child = firstChildElement(parent, childName);
        if (child == null) {
            return null;
        }
        final NamedNodeMap attrs = child.getAttributes();
        if (attrs == null) {
            return null;
        }
        final Node attrNode = attrs.getNamedItem(attr);
        return attrNode == null ? null : attrNode.getNodeValue();
    }

    private static String readChildText(Node parent, String childName) {
        final Node child = firstChildElement(parent, childName);
        return child == null ? null : child.getTextContent();
    }

    private static Node firstChildElement(Node parent, String name) {
        final NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node c = children.item(i);
            if (c.getNodeType() == Node.ELEMENT_NODE && name.equals(c.getNodeName())) {
                return c;
            }
        }
        return null;
    }
}
