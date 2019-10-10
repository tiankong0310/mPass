package com.ibyte.common.util;

import com.ibyte.common.exception.FileZipException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @Description: <文件解压>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
@Slf4j
public class FileUtil {

    public static final String  ENCODING="UTF-8";
    /**
     * 压缩文件
     *
     * @param zipFilePath 压缩文件存放地址
     * @param filePaths   待压缩的文件夹
     * @return 压缩后的文件
     */
    public static File zip(String zipFilePath, String... filePaths) throws FileZipException {
        if (filePaths == null || filePaths.length == 0) {
            // 待压缩文件路径为空
            throw new FileZipException("filePaths is null ");
        }
        File target = new File(zipFilePath, IDGenerator.generateID() + ".zip");
        if (!target.getParentFile().exists()) {
            target.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(target);
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {
            for (String filePath : filePaths) {
                File source = new File(filePath);
                if (source.exists()) {
                    // 添加对应的文件Entry
                    addEntry(null, source, zos);
                }
            }
        } catch (Exception e) {
            throw new FileZipException(e);
        }
        return target;
    }

    /**
     * 扫描添加文件Entry
     *
     * @param base   基路径
     * @param source 源文件
     * @param zos    Zip文件输出流
     * @throws IOException
     */
    private static void addEntry(String base, File source, ZipOutputStream zos)
            throws FileZipException {
        // 按目录分级，形如：/aaa/bbb.txt
        String entry = (base==null?"":base)+source.getName();
        if (source.isDirectory()) {
            for (File file : source.listFiles()) {
                // 递归列出目录下的所有文件，添加文件Entry
                addEntry(entry + File.separator, file, zos);
            }
        } else {
            byte[] buffer = new byte[1024 * 10];
            try (FileInputStream fis = new FileInputStream(source);
                 BufferedInputStream bis = new BufferedInputStream(fis, buffer.length)) {
                int read;
                zos.putNextEntry(new ZipEntry(entry));
                while ((read = bis.read(buffer, 0, buffer.length)) != -1) {
                    zos.write(buffer, 0, read);
                }
                zos.closeEntry();
            } catch (Exception e) {
                throw new FileZipException(e);
            }
        }
    }

    /**
     * zip解压
     *
     * @param srcFile     zip源文件
     * @param destDirPath 解压后的目标文件夹
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    public static void unZip(File srcFile, String destDirPath) throws FileZipException {
        // 判断源文件是否存在
        // 开始解压
        try (ZipFile zipFile = new ZipFile(srcFile, Charset.forName(ENCODING))) {
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {

                ZipEntry entry = (ZipEntry) entries.nextElement();
                System.out.println(entry.getName());
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = destDirPath + "/" + entry.getName();
                    File dir = new File(dirPath);
                    if (!dir.mkdirs()) {
                        throw new FileZipException();
                    }
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    generateFile(destDirPath, entry.getName(), zipFile.getInputStream(entry));
                }
            }
        } catch (Exception e) {
            log.error("解压文件异常！fileName={}", srcFile.getName(), e);
            throw new FileZipException(e);
        }

    }

    /**
     * 生成文件
     *
     * @param destDirPath
     * @param fileName
     * @param inputStream
     * @throws IOException
     */
    public static void generateFile(String destDirPath, String fileName, InputStream inputStream) throws FileZipException {

        File targetFile = new File(destDirPath + File.separator + fileName);
        // 保证这个文件的父文件夹必须要存在
        if (!targetFile.getParentFile().exists()) {
            if (!targetFile.getParentFile().mkdirs()) {
                throw new FileZipException(" path is not found ");
            }
        }
        // 将压缩文件内容写入到这个文件中
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));
             PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), ENCODING)))
        ) {
            String strV;
            while ((strV = reader.readLine()) != null) {
                writer.write(strV);
            }
            return;
        } catch (Exception e) {
            log.error("生成静态资源错误！path={},fileName={}", destDirPath, fileName, e);
            throw new FileZipException();
        }

    }

    /**
     * 直接根据内容生成 文件
     *
     * @param destDirPath
     * @param fileName
     * @param content
     */
    public static void generateFile(String destDirPath, String fileName, String content) throws FileZipException {
        File targetFile = new File(destDirPath + File.separator + fileName);
        //确保父级目录存在
        if (!targetFile.getParentFile().exists()) {
            if (!targetFile.getParentFile().mkdirs()) {
                throw new FileZipException(" path is not found ");
            }
        }

        //设置文件编码格式
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), ENCODING)))
        ) {
            writer.write(content);
            return;
        } catch (Exception e) {
            log.error("生成静态资源错误！path={},fileName={}", destDirPath, fileName, e);
            throw new FileZipException("create file error",e);
        }

    }

    /**
     * 时间格式
     */
    public final static String DATE_FORMAT = "yyyyMMddHHmmSS";

    /**
     * 获取 随机数字、字符串
     *
     * @return
     */
    public static String getRandomStr() {
        return IDGenerator.generateID();
    }

    /**
     * 读取文件
     * @param path
     * @param fileName
     * @return
     * @throws Exception
     */
    public static String readFile(String path, String fileName) throws FileZipException {
        File file=new File(path+File.separator+fileName);
        return readFile(file);
    }

    /**
     * 读取文件
     * @param file
     * @return
     * @throws Exception
     */
    public static String readFile(File file) throws FileZipException{
        if(file==null){
            throw new FileZipException("file is null");
        }
        StringBuilder strBuilder = new StringBuilder();
        //设置读取文编码格式
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), ENCODING))) {
            String readV;
            while ((readV = reader.readLine()) != null) {
                strBuilder.append(readV);
            }
            return strBuilder.toString();
        } catch (Exception e) {
            log.error("读取文件内容失败:path={}",file.getPath(), e);
            throw new FileZipException("read file error");
        }
    }

    /**
     * 获得临时目录
     *
     * @return
     */
    public static File getTempFolder() {
        String folder = System.getProperty("java.io.tmpdir");
        if (!folder.endsWith("/") && !folder.endsWith("\\")) {
            folder += "/";
        }
        folder += IDGenerator.generateID();
        File f = new File(folder);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    /**
     * 删除文件或文件夹
     *
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        Arrays.stream(file.listFiles()).forEach(f -> deleteFile(f));
        file.delete();
    }

    public static void main(String[] args) {
        String p = "E:\\temp";
        String zip = "e:\\";
        try {
            zip(zip, p);
        } catch (FileZipException e) {
            e.printStackTrace();
        }
    }
}
