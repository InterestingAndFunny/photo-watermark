package person.concise.photowatermark.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import person.concise.photowatermark.service.PhotoService;

import javax.annotation.Resource;

@RestController
@Slf4j
public class PhotoWatermarkController {

    @Resource
    private PhotoService photoService;

    @PostMapping("/photoWatermark/upload")
    public ResponseEntity<byte[]> addPhotoWatermark(@RequestParam("file") MultipartFile file) {
        log.info("文件上传成功！");
        if (!file.getContentType().equals("image/jpeg")) {
            throw new IllegalArgumentException("只能上传jpeg格式的图片！");
        }
        byte[] processedFile = photoService.start(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", file.getOriginalFilename());
        headers.setContentLength(processedFile.length);
        return new ResponseEntity<>(processedFile, headers, HttpStatus.OK);
    }

}
