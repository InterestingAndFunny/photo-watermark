package person.concise.photowatermark.service;

import cn.hutool.core.img.ImgUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.GenericImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.springframework.stereotype.Service;
import person.concise.photowatermark.domain.PhotoMetaInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PhotoService {

    private static final int WHITE_HEIGHT = 600;    // 白色的图片的高度

    private static final int EDGE_DISTANCE = (int) (WHITE_HEIGHT * 0.15);   // 边距设置为短边的 0.01
    private int newWidth = 0;           // 生成水印图片宽度
    private int newHeight = 0;          // 生成水印图片高度

    private static final int LOGO_HEIGHT = (int) Math.round(WHITE_HEIGHT * 0.3);   // logo的高度

    private int newLogoWidth = 0;       // logo实际打印的宽度
    private int logo_y = 0;             // logo打印的y轴



    public void start(String url) {
        try {
            // 加载图片
            File originalImageFile = new File(url);
            PhotoMetaInfo photoMetaInfo = getPhotoMetaInfo(originalImageFile);
            BufferedImage originalImage = ImageIO.read(originalImageFile);
            BufferedImage outPutImg = createOutPutImg(photoMetaInfo, originalImage);
            Graphics2D g2d = outPutImg.createGraphics();
            fillOutImg(g2d, originalImage);
            fillCameraLogo(outPutImg, g2d);
            // 设置字体和颜色
            Font font = new Font("Arial", Font.PLAIN, 150);
            g2d.setFont(font);
            g2d.setColor(Color.BLACK);
            fillCameraModel(g2d, photoMetaInfo);
            fillShotDetailInfo(g2d, photoMetaInfo);
            g2d.setColor(Color.gray);
            fillLens(g2d, photoMetaInfo);
            fillShotDateTime(g2d, photoMetaInfo);
            g2d.dispose();
            outPutFile(outPutImg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 获取相片的元数据 */
    public PhotoMetaInfo getPhotoMetaInfo(File originalImageFile) {
        PhotoMetaInfo photoMetaInfo = new PhotoMetaInfo();
        try {
            // 读取图片元数据
            ImageMetadata metadata = Imaging.getMetadata(originalImageFile);

            if (metadata instanceof JpegImageMetadata) {
                // 如果是JPEG格式，获取EXIF信息
                JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                TiffImageMetadata exif = jpegMetadata.getExif();
                List<? extends ImageMetadata.ImageMetadataItem> items = exif.getItems();
                for (ImageMetadata.ImageMetadataItem item : items) {
                    GenericImageMetadata.GenericImageMetadataItem metaData = (GenericImageMetadata.GenericImageMetadataItem) item;
                    switch (metaData.getKeyword()) {
                        case "Make" :
                            // 相机厂商
                            photoMetaInfo.setMake(metaData.getText());
                            break;
                        case "Model" :
                            // 相机型号
                            photoMetaInfo.setModel(metaData.getText());
                            break;
                        case "LensModel":
                            // 镜头型号
                            photoMetaInfo.setLensModel(metaData.getText());
                            break;
                        case "DateTime":
                            // 拍摄时间
                            photoMetaInfo.setDateTime(metaData.getText());
                            break;
                        case "ExposureTime":
                            // 快门时间
                            photoMetaInfo.setExposureTime(metaData.getText());
                            break;
                        case "FNumber":
                            // 光圈
                            photoMetaInfo.setFNumber(metaData.getText());
                            break;
                        case "PhotographicSensitivity":
                            // iso
                            photoMetaInfo.setPhotographicSensitivity(metaData.getText());
                            break;
                        case "Orientation":
                            // 旋转信息
                            photoMetaInfo.setOrientation(metaData.getText());
                            break;
                        case "FocalLength":
                            photoMetaInfo.setFocalLength(metaData.getText());
                            break;
                    }
                }


            } else {
//                System.out.println("不支持该图片格式");
                log.info("不支持该图片格式！");
            }
        } catch (ImageReadException | IOException e) {
            e.printStackTrace();
        }
        return photoMetaInfo;
    }

    /**
     * 生成输出的图片
     *
     * @return
     */
    public BufferedImage createOutPutImg( PhotoMetaInfo photoMetaInfo, BufferedImage originalImage) throws IOException {
        // 纵向
        if (photoMetaInfo.getOrientation().equals("6") || photoMetaInfo.getOrientation().equals("8")) {
            originalImage = (BufferedImage) ImgUtil.rotate(originalImage, 270);
            int height = originalImage.getHeight();
            newWidth = originalImage.getWidth();
            newHeight = height + WHITE_HEIGHT;
        } else if (photoMetaInfo.getOrientation().equals("1")) {
            int height = originalImage.getHeight();
            newWidth = originalImage.getWidth();
            newHeight = height + WHITE_HEIGHT;
        }

        return new BufferedImage(newWidth, newHeight, originalImage.getType());
    }

    /** 填充原始照片内容与白色区域 */
    public void fillOutImg (Graphics2D g2d, BufferedImage originalImage) {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, newHeight - WHITE_HEIGHT, newWidth, WHITE_HEIGHT);

        // Draw the original image onto the new image
        g2d.drawImage(originalImage, 0, 0, null);
    }

    /** 填充索尼Logo */
    public void fillCameraLogo(BufferedImage outPutImg, Graphics2D g2d) throws IOException {
        InputStream inputStream = PhotoService.class.getResourceAsStream("/logo-image/logo-sony.png");
        BufferedImage logo = ImageIO.read(inputStream);

//        BufferedImage sonyLogo = ImageIO.read(new File("CLASSPATH:logo-image/logo-sony.png"));

        // 绘制 获取原始图片宽高
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();
        // logo的x,y坐标
        int logo_x = EDGE_DISTANCE;
        logo_y = outPutImg.getHeight() - WHITE_HEIGHT + EDGE_DISTANCE;
        newLogoWidth = LOGO_HEIGHT * (logoWidth / logoHeight);
        g2d.drawImage(logo, logo_x, logo_y, newLogoWidth, LOGO_HEIGHT, null);
    }

    /** 填充相机型号 */
    public void fillCameraModel(Graphics2D g2d, PhotoMetaInfo photoMetaInfo) {
        // 写入相机型号
        String modelText = photoMetaInfo.getModel().replaceAll("^'+|'+$", "");
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int model_x = EDGE_DISTANCE + newLogoWidth + EDGE_DISTANCE;
        int model_y = logo_y + fontMetrics.getHeight();
        g2d.drawString(modelText, model_x, model_y);
    }

    /** 写入 焦距、光圈、快门时间、iso */
    public void fillShotDetailInfo(Graphics2D g2d, PhotoMetaInfo photoMetaInfo) {
        Matcher matcher = Pattern.compile("\\(([^)]+)\\).*").matcher(photoMetaInfo.getFNumber());
        // 光圈的处理比较麻烦
        String fNumber = photoMetaInfo.getFNumber();
        if (matcher.find()) {
            fNumber = matcher.group(1);
        }
        FontMetrics fontMetrics = g2d.getFontMetrics();
        String shotData = photoMetaInfo.getFocalLength() + "mm "
                + "f/" + fNumber + " "
                + photoMetaInfo.getExposureTime().replaceAll("\\(.*?\\)", "") + " "
                + "ISO" + photoMetaInfo.getPhotographicSensitivity();
        int detailData_x = newWidth - fontMetrics.stringWidth(shotData) - EDGE_DISTANCE;
        int detailData_y = logo_y + fontMetrics.getHeight();
        g2d.drawString(shotData, detailData_x, detailData_y);
    }

    /** 镜头信息 */
    public void fillLens(Graphics2D g2d, PhotoMetaInfo photoMetaInfo) {
        String lensData = photoMetaInfo.getLensModel().replaceAll("^'+|'+$", "");
        int lensData_x = EDGE_DISTANCE;
        int lensData_y = newHeight - EDGE_DISTANCE;
        g2d.drawString(lensData, lensData_x, lensData_y);
    }

    /** 拍摄日期 */
    public void fillShotDateTime(Graphics2D g2d, PhotoMetaInfo photoMetaInfo) {
        // 拍摄日期
        String dateTime = photoMetaInfo.getDateTime().replaceAll("^'+|'+$", "");
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int dateTime_x = newWidth - fontMetrics.stringWidth(dateTime) - EDGE_DISTANCE;
        int dateTime_y = newHeight - EDGE_DISTANCE;
        g2d.drawString(dateTime, dateTime_x, dateTime_y);
    }

    /** 保存文件 */
    public void outPutFile(BufferedImage outPutImg) throws IOException {
        File outputImageFile = new File("C:\\Users\\yang\\Desktop\\testImage\\test\\test.jpg");
        ImageIO.write(outPutImg, "jpg", outputImageFile);
    }

}
