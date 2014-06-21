package kr.pe.kwonnam.hibernate4memcached.timestamper;

import kr.pe.kwonnam.hibernate4memcached.Hibernate4MemcachedRegionFactory;
import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates increasing identifier for {@link org.hibernate.cache.spi.RegionFactory#nextTimestamp()}.
 * This implementation generates strictly increasing timestamp based on memcached incr feature.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class HibernateCacheTimestamperMemcachedImpl implements HibernateCacheTimestamper {
    public static final String TIMESTAMP_KEY = "timestamp";
    public static final long INCREASE_BY = 1L;
    public static final int EXPIRY_SECONDS = Hibernate4MemcachedRegionFactory.MEMCACHED_MAX_EPIRY_SECONDS;

    private Logger log = LoggerFactory.getLogger(HibernateCacheTimestamperMemcachedImpl.class);

    private Settings settings;

    private OverridableReadOnlyProperties properties;

    private MemcachedAdapter memcachedAdapter;

    private CacheNamespace cacheNamespace;

    public HibernateCacheTimestamperMemcachedImpl(Settings settings, OverridableReadOnlyProperties properties,
                                                  MemcachedAdapter memcachedAdapter) {
        this.settings = settings;
        this.properties = properties;
        this.memcachedAdapter = memcachedAdapter;


        String cacheRegionPrefix = settings.getCacheRegionPrefix() == null ? "" : settings.getCacheRegionPrefix() + ".";
        cacheNamespace = new CacheNamespace(cacheRegionPrefix + HibernateCacheTimestamperMemcachedImpl.class
                .getSimpleName(), false);

        log.debug("memcached timestamper initialized. CacheNamespace : {}", cacheNamespace);
    }

    public CacheNamespace getCacheNamespace() {
        return cacheNamespace;
    }

    @Override
    public long next() {
        long next = memcachedAdapter.increaseCounter(cacheNamespace, TIMESTAMP_KEY, INCREASE_BY,
                                                     System.currentTimeMillis(), EXPIRY_SECONDS);
        log.debug("memcached timestamper next : {}", next);
        return next;
    }
}