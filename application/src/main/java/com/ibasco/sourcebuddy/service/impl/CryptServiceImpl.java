package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.sourcebuddy.service.CryptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class CryptServiceImpl implements CryptService {

    private static final Logger log = LoggerFactory.getLogger(CryptServiceImpl.class);

    @Override
    public String computeHash(byte[] data) {
        return computeHash(new ByteArrayInputStream(data));
    }

    @Override
    public String computeHash(File file) {
        try {
            return computeHash(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String computeHash(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
                int count;
                byte[] buffer = new byte[8192];
                while ((count = bis.read(buffer)) > 0) {
                    digest.update(buffer, 0, count);
                }
                return bytesToHex(digest.digest());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

        } catch (NoSuchAlgorithmException e) {
            log.error("Algorithm not found", e);
        }
        return null;
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
