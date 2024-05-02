package com.fileStorage.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class FileSegment extends ByteObject{
    private String filePath;
    private int read;
    private long position;
    private byte[] buffer;
}
