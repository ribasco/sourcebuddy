package com.ibasco.sourcebuddy.service;

import java.io.File;
import java.io.InputStream;

public interface CryptService {

    String computeHash(byte[] data);

    String computeHash(File file);

    String computeHash(InputStream stream);
}
