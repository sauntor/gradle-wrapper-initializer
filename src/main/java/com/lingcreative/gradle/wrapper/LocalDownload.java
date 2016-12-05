/*
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lingcreative.gradle.wrapper;

import org.gradle.wrapper.IDownload;
import org.gradle.wrapper.Logger;

import java.io.*;
import java.net.URI;

public class LocalDownload implements IDownload {
    private static final int PROGRESS_CHUNK = 20000;
    private static final int BUFFER_SIZE = 10000;
    private final Logger logger;
    private final File distributionsDir;

    public LocalDownload(Logger logger, File distributionsDir) {
        this.logger = logger;
        this.distributionsDir = distributionsDir;
    }

    public void download(URI address, File destination) throws Exception {
        destination.getParentFile().mkdirs();
        String path = address.getPath();
        String file = path.substring(path.lastIndexOf(File.separatorChar) + 1);
        File distributionFile = new File(distributionsDir, file);
        if (!distributionFile.exists()) {
            throw new IllegalArgumentException(String.format("Can't download %s(replaced with local path: %s) because it does not exist", address, distributionFile));
        }
        downloadInternal(distributionFile, destination);
    }

    private void downloadInternal(File address, File destination)
            throws Exception {
        OutputStream out = null;
        InputStream in = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(destination));
            in = new FileInputStream(address);
            byte[] buffer = new byte[BUFFER_SIZE];
            int numRead;
            long progressCounter = 0;
            while ((numRead = in.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.print("interrupted");
                    throw new IOException("File copying was interrupted.");
                }
                progressCounter += numRead;
                if (progressCounter / PROGRESS_CHUNK > 0) {
                    logger.append(".");
                    progressCounter = progressCounter - PROGRESS_CHUNK;
                }
                out.write(buffer, 0, numRead);
            }
        } finally {
            logger.log("");
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
