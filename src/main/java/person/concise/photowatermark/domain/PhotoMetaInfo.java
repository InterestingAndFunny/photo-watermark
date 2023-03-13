package person.concise.photowatermark.domain;

import lombok.Data;

@Data
public class PhotoMetaInfo {
    /** 相机厂商 */
    private String make;
    /** 相机型号 */
    private String model;
    /** 镜头型号 */
    private String lensModel;
    /** 拍摄日期 */
    private String dateTime;
    /** 快门时间 */
    private String exposureTime;
    /** 光圈 */
    private String fNumber;
    /** ISO */
    private String photographicSensitivity;
    /** 旋转信息 6：旋转90度 8：旋转270度 1：不旋转 */
    private String orientation;
    /** 焦距 */
    private String focalLength;
}
