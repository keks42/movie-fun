package org.superbiz.moviefun.blobstore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

public interface BlobStore {

    void put(Blob blob) throws IOException, URISyntaxException;

    Optional<Blob> get(String name) throws IOException;

    void deleteAll();
}
