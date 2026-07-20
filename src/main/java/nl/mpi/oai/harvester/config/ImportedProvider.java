package nl.mpi.oai.harvester.config;

/**
 * Data transferred from a {@link ProviderImport} implementation back to the
 * harvester.
 *
 * <p>Only {@link #url} is required; every other field is an optional per-provider
 * override. Fields that are left {@code null} fall back to the configuration
 * defaults, and may be further overridden by an explicit
 * {@code <config url="...">} element inside the same {@code <import>}.
 */
public class ImportedProvider {

    /** Required endpoint URL. */
    public String url;

    /** Optional human-readable name. */
    public String name;

    /** Optional OAI-PMH set specs. */
    public String[] sets;

    /** Optional scenario override (e.g. {@code ListRecords}). */
    public String scenario;

    /** Optional connection/read timeout override, in seconds. */
    public Integer timeout;

    /** Optional maximum retry count override. */
    public Integer maxRetryCount;

    /** Optional retry delays override, in seconds. */
    public int[] retryDelays;

    /** Optional exclusive flag override. */
    public Boolean exclusive;

    /** Optional nice delay override, in seconds. */
    public Integer niceDelay;

    /**
     * If {@code true}, build a {@code StaticProvider} instead of a regular
     * {@code Provider}. Defaults to {@code false}.
     */
    public boolean staticProvider = false;
}
