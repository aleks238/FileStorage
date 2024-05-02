package com.fileStorage.common;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class CreateEmptyFile extends ByteObject {
    private String filePath;
}
