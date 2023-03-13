package person.concise.photowatermark.controller;

import lombok.extern.slf4j.Slf4j;
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
    public void addPhotoWatermark(@RequestParam("file") MultipartFile file) {

        log.info("文件上传成功！");
    }

}
