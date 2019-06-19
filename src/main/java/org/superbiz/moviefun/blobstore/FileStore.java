package org.superbiz.moviefun.blobstore;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.ClassLoader.getSystemResource;

@Component
public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException, URISyntaxException {


        Path path = Paths.get(getRootDir().toString(), blob.name);

        File targetFile = path.toFile();


        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        OutputStream outStream = new FileOutputStream(targetFile);

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = blob.inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        // ...
        Path path = Paths.get(getRootDir().toString(), name);
        File file = path.toFile();
        if (!file.exists()) {
            return Optional.empty();
        } else {
            Blob b = new Blob(file.getName(), new FileInputStream(file), "BLOB");
            return Optional.of(b);
        }
    }

    @Override
    public void deleteAll() {
        // ...
        Path path = getRootDir();
        try (Stream<Path> paths = Files.walk(path)) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach((Path p) -> p.toFile().delete());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path getRootDir() {
        try {
            return Paths.get(getSystemResource("default-cover.jpg").toURI()).getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Cannot happen :D");
        }
    }
}