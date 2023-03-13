package person.concise.photowatermark;

import org.junit.jupiter.api.Test;
import person.concise.photowatermark.service.PhotoService;

public class PhotoServiceTest {

    @Test
    public void testStart() {
        PhotoService photoService = new PhotoService();
        String url = "C:\\Users\\yang\\Desktop\\testImage\\test\\IMG_5695.JPG";
        photoService.start(url);
    }

}
