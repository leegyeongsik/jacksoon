package io.jacksoon.router.filter;

import java.net.URI;

public class FilterRequestSetting {
    private final URI versionUri;
    private final URI bundleUri;
    private volatile long version;

    public FilterRequestSetting(URI versionUri, URI bundleUri) {
        this.versionUri = versionUri;
        this.bundleUri = bundleUri;
    }

    public URI versionUri() {
        return versionUri;
    }

    public URI bundleUri() {
        return bundleUri;
    }

    public long version() {
        return version;
    }

    public void updateVersion(long version) {
        this.version = version;
    }
}
