package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.http.MethodNotSupportedException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Optional;


public class S3Store implements BlobStore {
    private final String photoStorageBucket;
    private final AmazonS3Client s3Client;

//    final Bucket bucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {

        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
//        if (!s3Client.doesBucketExist(photoStorageBucket)) {
//            bucket = s3Client.createBucket(photoStorageBucket);
//        }
//        else {
//            bucket = s3Client.listBuckets().stream().filter((Bucket b) -> b.getName().equals(photoStorageBucket)).findFirst().get();
//            s3Client.putObject(photoStorageBucket, )
//        }

    }

    @Override
    public void put(Blob blob) throws IOException, URISyntaxException {
        s3Client.putObject(photoStorageBucket, blob.name, blob.inputStream, null);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        S3Object object = s3Client.getObject(photoStorageBucket, name);
        InputStream is = object.getObjectContent();
        return Optional.of(new Blob(name, is, "BLOB"));
    }

    @Override
    public void deleteAll() {
//        throw new MethodNotSupportedException("Das haben wir noch nicht implementiert! (-:")
//        ObjectListing objectListing = s3Client.listObjects(photoStorageBucket);
//        objectListing.getBucketName()


        ObjectListing objectListing = s3Client.listObjects(photoStorageBucket);
        while (true) {
            Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
            while (objIter.hasNext()) {
                s3Client.deleteObject(photoStorageBucket, objIter.next().getKey());
            }

            // If the bucket contains many objects, the listObjects() call
            // might not return all of the objects in the first listing. Check to
            // see whether the listing was truncated. If so, retrieve the next page of objects
            // and delete them.
            if (objectListing.isTruncated()) {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
    }
}
