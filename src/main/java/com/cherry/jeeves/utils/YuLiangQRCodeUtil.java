package com.cherry.jeeves.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * Created by yuliang-ds1 on 2017/6/22.
 */
public class YuLiangQRCodeUtil {

    private  static Logger logger = LoggerFactory.getLogger(YuLiangQRCodeUtil.class);

    private static final String CHARSET = "UTF-8";

    private static final String FORMAT_NAME = "png";

    // 二维码尺寸
    private static final int QRCODE_SIZE = 400;
    // LOGO宽度
    private static final int WIDTH = 100;
    // LOGO高度
    private static final int HEIGHT = 100;


    /**
     * 创建简单二维码图片
     * @param content
     * @param imgPath
     * @param needCompress
     * @param fileName
     * @return
     */
    private static BufferedImage createImage(String content, String imgPath, boolean needCompress, String fileName)  {

        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(content,
                    BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, hints);
        } catch (WriterException e) {
            if(logger.isDebugEnabled()){
                logger.debug("YuLiangQRCodeUtil-createImage-WriterException:"+e);
            }
        }
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        if (imgPath == null || "".equals(imgPath)) {
            return image;
        }
        // 插入图片
        try {
            YuLiangQRCodeUtil.insertImage(image, imgPath, needCompress,fileName);
        } catch (Exception e) {
            if(logger.isDebugEnabled()){
                logger.debug("YuLiangQRCodeUtil-createImage-Exception:"+e);
            }
        }
        return image;
    }

    /**
     *  在简单二维码图片中插入LOGO
     * @param source 二维码图片
     * @param imgPath LOGO图片地址
     * @param needCompress 是否压缩
     * @throws Exception
     */
    private static void insertImage(BufferedImage source, String imgPath, boolean needCompress,String fileName) throws Exception {
        File file = new File(imgPath);
        if (!file.exists()) {
            if(logger.isDebugEnabled()){
                logger.debug("YuLiangQRCodeUtil-insertImage-file.exists():"+imgPath+"   该文件不存在！");
            }
            return;
        }
        Image src = ImageIO.read(new File(imgPath));
        int width = src.getWidth(null);
        int height = src.getHeight(null);
        if (needCompress) { // 压缩LOGO
            if (width > WIDTH) {
                width = WIDTH;
            }
            if (height > HEIGHT) {
                height = HEIGHT;
            }
            Image image = src.getScaledInstance(width, height,
                    Image.SCALE_SMOOTH);
            BufferedImage tag = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose();
            src = image;
        }
        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = (QRCODE_SIZE - width) / 2;
        int y = (QRCODE_SIZE - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

    /**
     * 生成二维码(内嵌LOGO)
     * @param content      内容
     * @param imgPath      LOGO地址
     * @param destPath     存放目录
     * @param needCompress 是否压缩LOGO
     * @throws Exception
     */
    public static void encode(String content, String imgPath, String destPath,boolean needCompress,String fileName) throws Exception {
        BufferedImage image = YuLiangQRCodeUtil.createImage(content, imgPath, needCompress,fileName);
        mkdirs(destPath);
        ImageIO.write(image, FORMAT_NAME, new File(destPath+fileName));
    }

    /**
     * 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
     * @author lanyuan
     * Email: mmm333zzz520@163.com
     * @date 2013-12-11 上午10:16:36
     * @param destPath 存放目录
     */
    public static void mkdirs(String destPath) {
        File file =new File(destPath);
        //当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
    }

    /**
     * 生成二维码(内嵌LOGO)
     * @param content    内容
     * @param imgPath    LOGO地址
     * @param destPath   存储地址
     * @throws Exception
     */
    public static void encode(String content, String imgPath, String destPath,String fileName) throws Exception {
        YuLiangQRCodeUtil.encode(content, imgPath, destPath, false,fileName);
    }

    /**
     * 生成二维码
     * @param content 内容
     * @param destPath 存储地址
     * @param needCompress 是否压缩LOGO
     * @throws Exception
     */
    public static void encode(String content, String destPath, boolean needCompress,String fileName) throws Exception {
        YuLiangQRCodeUtil.encode(content, null, destPath, needCompress,fileName);
    }

    /**
     * 生成简单二维码
     * @param content  内容
     * @param destPath 存储地址
     * @throws Exception
     */
    public static void encode(String content, String destPath,String fileName) throws Exception {
        YuLiangQRCodeUtil.encode(content, null, destPath, false,fileName);
    }

    /**
     * 生成内嵌图片二维码(内嵌LOGO)
     * @param content 内容
     * @param imgPath LOGO地址
     * @param output  输出流
     * @param needCompress 是否压缩LOGO
     * @throws Exception
     */
    public static void encode(String content, String imgPath, OutputStream output, boolean needCompress,String fileName) throws Exception {
        BufferedImage image = YuLiangQRCodeUtil.createImage(content,imgPath,needCompress,fileName);
        ImageIO.write(image, FORMAT_NAME, output);
    }

    /**
     * 生成简单二维码
     * @param content 内容
     * @param output  输出流
     * @throws Exception
     */
    public static void encode(String content, OutputStream output, String fileName)
            throws Exception {
        YuLiangQRCodeUtil.encode(content, null, output, false,fileName);
    }

    /**
     * 解析二维码
     * @param file 二维码图片
     * @return
     * @throws Exception
     */
    public static String decodeByFile(File file) throws Exception {
        BufferedImage image;
        image = ImageIO.read(file);
        if (image == null) {
            return null;
        }
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(
                image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result;
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
        result = new MultiFormatReader().decode(bitmap, hints);
        String resultStr = result.getText();
        return resultStr;
    }

    /**
     * 解析二维码
     * @param path  二维码图片地址
     * @return
     * @throws Exception
     */
    public static String decodeByPath(String path) throws Exception {
        return YuLiangQRCodeUtil.decodeByFile(new File(path));
    }

    public static void main(String[] args) throws Exception {

        String text = "http://fish_08.oschina.io/";
        //YuLiangQRCodeUtil.encode(text,"D:/QRCode/meeting/logo.jpg","D:/QRCode/meeting/", true,"houzi.png");
        String  result= YuLiangQRCodeUtil.decodeByPath("D:/QRCode/meeting/wechat.png");
        System.out.println("result:"+result);

    }


    public static void encodeByWeChat(byte[] b, String destPath, String fileName) throws Exception {
        YuLiangFileUtils.delAllFile(destPath);
        mkdirs(destPath);
        ByteArrayInputStream in = new ByteArrayInputStream(b);    //将b作为输入流；
        BufferedImage image = ImageIO.read(in);
        ImageIO.write(image, FORMAT_NAME, new File(destPath+fileName));
    }

}
