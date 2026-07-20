/*
 * Test fixture for the class-based <import> path in Configuration.
 * Implements ProviderImport and returns a canned set of providers, one of which
 * is derived from the <import> node's own <registry url="..."> child to prove
 * the DOM node is handed to the implementation.
 */
package nl.mpi.oai.harvester.config;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class TestProviderImport implements ProviderImport {

    public static final String URL_A = "http://test.example.org/oai-a";
    public static final String URL_B = "http://test.example.org/oai-b";
    public static final String URL_STATIC = "http://test.example.org/oai-static";
    public static final String URL_EXCLUDED = "http://test.example.org/oai-excluded";

    @Override
    public List<ImportedProvider> getProviders(Node importNode) {
        final List<ImportedProvider> list = new ArrayList<>();

        // a provider whose URL is read from the <import> node's <registry url="...">
        // child - proves the DOM node is passed through to the implementation
        final String registryUrl = readRegistryUrl(importNode);
        if (registryUrl != null) {
            final ImportedProvider fromNode = new ImportedProvider();
            fromNode.url = registryUrl;
            fromNode.name = "From Node";
            list.add(fromNode);
        }

        // provider carrying DTO overrides that Configuration should honour
        final ImportedProvider a = new ImportedProvider();
        a.url = URL_A;
        a.name = "Provider A";
        a.timeout = 99;
        a.scenario = "ListRecords";
        list.add(a);

        // provider with no DTO overrides; targeted by a <config url="..."> in the
        // test config to verify post-processing overrides apply
        final ImportedProvider b = new ImportedProvider();
        b.url = URL_B;
        list.add(b);

        // static provider - Configuration should build a StaticProvider
        final ImportedProvider stat = new ImportedProvider();
        stat.url = URL_STATIC;
        stat.staticProvider = true;
        list.add(stat);

        // provider that the <exclude url="..."> in the test config must filter out
        final ImportedProvider excluded = new ImportedProvider();
        excluded.url = URL_EXCLUDED;
        list.add(excluded);

        return list;
    }

    private static String readRegistryUrl(Node importNode) {
        final NodeList children = importNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node c = children.item(i);
            if (c.getNodeType() == Node.ELEMENT_NODE && "registry".equals(c.getNodeName())) {
                final NamedNodeMap attrs = c.getAttributes();
                if (attrs != null) {
                    final Node urlAttr = attrs.getNamedItem("url");
                    if (urlAttr != null) {
                        return urlAttr.getNodeValue();
                    }
                }
            }
        }
        return null;
    }
}
