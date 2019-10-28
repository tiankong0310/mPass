package com.ibyte.common.core.util;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;

/**
 * 依据文件扩展名取mime类型
 *
 * @author li.shangzhi
 */
public class MimeTypeUtil {
    
    /**
     * 依据文件扩展名取mime类型
     * @param filename 文件全名或.号加扩展名，.号不可省略
     */
    public static String getMimeType(String filename){
        return new MimetypesFileTypeMap().getContentType(new File(filename));
    }
}
