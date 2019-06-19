package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;

    @Autowired
    private BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean) {
        this.albumsBean = albumsBean;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
//        blobStore.get()
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        saveUploadToFile(uploadedFile, getCoverFile(albumId));

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
//        Path coverFilePath = getExistingCoverPath(albumId);
        File coverFile = getCoverFile(albumId);

        File file = Paths.get(getSystemResource("default-cover.jpg").toURI()).toFile();


        Blob b = blobStore.get(coverFile.getName())
                .orElseGet(() -> {
                    try {
                        return new Blob("default-cover.jpg", new FileInputStream(file), "BLOB");
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });

//        byte[] imageBytes = new byte[(int) file.length()];
//        DataInputStream dis = new DataInputStream(b.inputStream);
//        dis.readFully(imageBytes);


        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((bytesRead = b.inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        byte[] imageBytes = bos.toByteArray();


        HttpHeaders headers = createImageHttpHeaders(imageBytes);

        return new HttpEntity<>(imageBytes, headers);
    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {

        Blob b = new Blob(targetFile.getName(), uploadedFile.getInputStream(), "BLOB");
        try {
            blobStore.put(b);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private HttpHeaders createImageHttpHeaders(byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(imageBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }
}
